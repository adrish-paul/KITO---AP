package com.kito.kaya

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import com.kito.core.database.AppDB
import com.kito.core.database.entity.ActiveSessionEntity
import com.kito.core.database.entity.SectionEntity
import com.kito.core.database.entity.StudentEntity
import com.kito.core.database.repository.StudentSectionRepository
import com.kito.core.platform.AppSyncTrigger
import com.kito.core.platform.SecureStorage
import com.kito.core.sync.domain.SupabaseTimetableSync
import com.kito.core.sync.data.SyncRemoteDataSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.DefaultRequest
import io.ktor.http.HttpStatusCode
import com.kito.kaya.sensitive.KayaPortalClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toOkioPath
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class KayaTimetableTest {
    private suspend fun fixture(block: suspend (KayaTimetableStore, StudentSectionRepository, AppDB, androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences>) -> Unit) {
        val dir = Files.createTempDirectory("kito-kaya-test").toFile()
        val job = SupervisorJob()
        val scope = CoroutineScope(job + Dispatchers.IO)
        val data = PreferenceDataStoreFactory.createWithPath(scope = scope) { dir.resolve("prefs.preferences_pb").toOkioPath() }
        val db = Room.databaseBuilder<AppDB>(dir.resolve("test.db").absolutePath).setDriver(BundledSQLiteDriver()).build()
        try {
            data.edit { it[stringPreferencesKey("User_Password")] = "student" }
            db.studentDao().insertStudent(listOf(StudentEntity("student", "CS1", "B1"), StudentEntity("friend", "CS1", "B1")))
            db.activeSessionDao().insertActiveSession(ActiveSessionEntity(academic_year = "2026", term_code = "010", version = 1))
            db.sectionDao().insertSection(listOf(SectionEntity(id = 1, academic_year = "2026", term_code = "010", version = 1,
                section = "CS1", batch = "B1", day = "MON", start_time = "08:00", end_time = "09:00", subject = "Supabase")))
            val store = KayaTimetableStore(data)
            block(store, StudentSectionRepository(db.studentSectionDao(), store), db, data)
        } finally {
            db.close()
            scope.cancel()
            job.join()
            dir.deleteRecursively()
        }
    }

    @Test
    fun connectionSwitchesExistingSubscriptionAndLogoutRestoresSupabase() = runBlocking {
        fixture { store, reader, db, data ->
            reader.getAllScheduleForStudent("student").distinctUntilChanged().test {
                assertEquals("Supabase", awaitItem().single().subject)
                store.save("student", listOf(SectionEntity(id = 1, day = "Monday", start_time = "10:00", end_time = "11:00", subject = "KAYA")))
                assertEquals("KAYA", awaitItem().single().subject)
                assertEquals("KAYA", reader.getScheduleForStudent("student", "MON").first().single().subject)
                assertTrue(reader.getScheduleForStudent("student", "TUE").first().isEmpty())
                assertEquals("Supabase", reader.getAllScheduleForStudent("friend").first().single().subject)
                // Reconstruct the reader: saved KAYA data remains authoritative after restart.
                assertEquals("KAYA", StudentSectionRepository(db.studentSectionDao(), KayaTimetableStore(data))
                    .getAllScheduleForStudent("student").first().single().subject)
                store.save("student", emptyList())
                assertTrue(awaitItem().isEmpty(), "An empty KAYA schedule must never fall back to Supabase")
                store.clear()
                assertEquals("Supabase", awaitItem().single().subject)
            }
        }
    }

    @Test
    fun legacyConnectionAndAccountChangeNeverExposeAnotherTimetable() = runBlocking {
        fixture { store, reader, _, data ->
            data.edit { it[booleanPreferencesKey("kaya_connected")] = true }
            assertFalse(store.isConnected.first(), "Old verify-only login requires reconnecting")
            assertEquals("Supabase", reader.getAllScheduleForStudent("student").first().single().subject)
            store.save("student", listOf(SectionEntity(subject = "Private KAYA")))
            data.edit { it[stringPreferencesKey("User_Password")] = "friend" }
            assertFalse(store.isConnected.first())
            assertEquals("Supabase", reader.getAllScheduleForStudent("friend").first().single().subject)
        }
    }

    @Test
    fun missingCredentialKeepsCachedKayaAndDoesNotTouchSap() = runBlocking {
        fixture { store, reader, db, data ->
            store.save("student", listOf(SectionEntity(subject = "Cached KAYA")))
            val secure = SecureStorage()
            secure.saveSapPassword("test-sap-value")
            val client = HttpClient(MockEngine { respond("offline", HttpStatusCode.ServiceUnavailable) }) {
                install(DefaultRequest) { url("https://test.invalid/") }
            }
            try {
                val repo = KayaRepository(KayaPortalClient(), store, secure, AppSyncTrigger(), db.studentSectionDao(),
                    SupabaseTimetableSync(db, SyncRemoteDataSource(client), data))
                assertIs<KayaResult.Error>(repo.refresh("student"))
                assertEquals("Cached KAYA", reader.getAllScheduleForStudent("student").first().single().subject)
                assertTrue(repo.disconnect("student").isFailure, "Offline logout still clears KAYA and exposes saved Supabase rows")
            } finally { client.close() }
            assertEquals("test-sap-value", secure.getSapPassword())
            assertEquals("Supabase", reader.getAllScheduleForStudent("student").first().single().subject)
        }
    }

}
