package com.kito.core.sync

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.kito.core.database.AppDB
import com.kito.core.database.entity.SectionEntity
import com.kito.core.database.entity.StudentEntity
import com.kito.core.database.repository.StudentSectionRepository
import com.kito.core.datastore.data.PrefsRepositoryImpl
import com.kito.core.platform.AppSyncTrigger
import com.kito.core.platform.SecureStorage
import com.kito.core.sync.domain.AppSyncUseCase
import com.kito.feature.attendance.data.AttendanceRepositoryImpl
import com.kito.kaya.KayaRepository
import com.kito.kaya.KayaTimetableStore
import com.kito.kaya.sensitive.KayaPortalClient
import com.kito.sap.SapPortalClient
import com.kito.sap.SapRepository
import com.kito.core.sync.data.ActiveSessionConfig
import com.kito.core.sync.data.StudentElectiveConfig
import com.kito.core.sync.data.SyncRemoteDataSource
import com.kito.core.sync.domain.SupabaseTimetableSync
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.Path.Companion.toOkioPath
import java.nio.file.Files
import java.util.Collections
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SupabaseTimetableSyncTest {
    private class Fixture {
        val dir = Files.createTempDirectory("kito-sync-test").toFile()
        val job = SupervisorJob()
        val scope = CoroutineScope(job + Dispatchers.IO)
        val data = PreferenceDataStoreFactory.createWithPath(scope = scope) { dir.resolve("cache.preferences_pb").toOkioPath() }
        val db = Room.databaseBuilder<AppDB>(dir.resolve("db").absolutePath).setDriver(BundledSQLiteDriver()).build()
        val requests = Collections.synchronizedList(mutableListOf<String>())
        var version = 1
        var subjectSuffix = ""
        var electives = false
        var missingStudent = false
        var failTable: String? = null
        var gate: CompletableDeferred<Unit>? = null
        val entered = CompletableDeferred<Unit>()
        val client = HttpClient(MockEngine { request ->
            assertEquals(HttpMethod.Get, request.method, "Supabase sync must be read-only")
            val table = request.url.encodedPath.substringAfterLast('/')
            requests.add(table)
            entered.complete(Unit)
            gate?.await()
            if (table == failTable) return@MockEngine respond("[]", HttpStatusCode.ServiceUnavailable,
                headersOf(HttpHeaders.ContentType, "application/json"))
            val payload = when (table) {
                "students" -> Json.encodeToString(if (missingStudent) emptyList() else listOf(StudentEntity(
                    request.url.parameters["roll_no"]!!.removePrefix("eq."), "CS1", if (electives) "batch_3" else "batch_2")))
                "active_session" -> Json.encodeToString(listOf(ActiveSessionConfig(academic_year = "2026", term_code = "010", version = version)))
                "student_elective" -> Json.encodeToString(listOf(StudentElectiveConfig("student", "PE1", "PE2", "batch_3")))
                "timetable" -> {
                    val section = request.url.parameters["section"]!!.removePrefix("eq.")
                    Json.encodeToString(listOf(SectionEntity(id = when(section) { "PE1" -> 2; "PE2" -> 3; else -> 1 },
                        academic_year = "2026", term_code = "010", version = version,
                        section = section, batch = if (electives) "batch_3" else "batch_2", day = "MON",
                        start_time = "08:00", end_time = "09:00", subject = "$section-v$version$subjectSuffix")))
                }
                else -> error("Unexpected remote request: $table")
            }
            respond(payload, headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }) {
            install(DefaultRequest) { url("https://test.invalid/") }
            install(ContentNegotiation) { json() }
        }
        fun sync() = SupabaseTimetableSync(db, SyncRemoteDataSource(client), data)
        fun appSync(): AppSyncUseCase {
            val store = KayaTimetableStore(data)
            return AppSyncUseCase(db, AppSyncTrigger(), StudentSectionRepository(db.studentSectionDao(), store),
                AttendanceRepositoryImpl(db.attendanceDao()), SapRepository(SapPortalClient()), PrefsRepositoryImpl(data),
                KayaRepository(KayaPortalClient(), store, SecureStorage(), AppSyncTrigger(), db.studentSectionDao(), sync()), sync())
        }
        suspend fun close() {
            client.close()
            db.close()
            scope.cancel()
            job.join()
            dir.deleteRecursively()
        }
    }

    private suspend fun withFixture(block: suspend (Fixture) -> Unit) {
        val f = Fixture()
        try { block(f) } finally { f.close() }
    }

    @Test
    fun ordinaryStudentUsesPersistentCacheThenPicksUpUnversionedEditsAfterExpiry() = runBlocking {
        withFixture { f ->
            assertTrue(f.sync().syncAt("student", 1_000))
            assertEquals(listOf("students", "active_session", "timetable"), f.requests.toList())
            assertFalse(f.sync().syncAt("student", 2_000), "A new sync instance must reuse the persisted cache")
            assertEquals(3, f.requests.size)
            f.subjectSuffix = "-corrected"
            assertTrue(f.sync().syncAt("student", 1_000 + SupabaseTimetableSync.CACHE_AGE_MS))
            assertEquals(listOf("students", "active_session", "timetable"), f.requests.drop(3))
            assertEquals("CS1-v1-corrected", f.db.studentSectionDao().getAllScheduleForStudent("student").first().single().subject)
        }
    }

    @Test
    fun electiveCacheRefreshesAllRowsAfterExpiryWithOrWithoutVersionChange() = runBlocking {
        withFixture { f ->
            f.electives = true
            val sync = f.sync()
            sync.syncAt("student", 0)
            assertEquals(6, f.requests.size)
            sync.syncAt("student", 10)
            assertEquals(6, f.requests.size)
            sync.syncAt("student", SupabaseTimetableSync.CACHE_AGE_MS)
            assertEquals(12, f.requests.size)
            f.version = 2
            sync.syncAt("student", SupabaseTimetableSync.CACHE_AGE_MS * 2)
            assertEquals(18, f.requests.size)
            assertTrue(f.db.studentSectionDao().getAllScheduleForStudent("student").first().all { it.subject.endsWith("v2") })
        }
    }

    @Test
    fun failuresPreserveSnapshotAndDoNotMarkCacheFresh() = runBlocking {
        withFixture { f ->
            val sync = f.sync()
            sync.syncAt("student", 0)
            f.version = 2
            f.failTable = "timetable"
            assertTrue(runCatching { sync.syncAt("student", SupabaseTimetableSync.CACHE_AGE_MS) }.isFailure)
            assertEquals("CS1-v1", f.db.studentSectionDao().getAllScheduleForStudent("student").first().single().subject)
            f.failTable = null
            assertTrue(sync.syncAt("student", SupabaseTimetableSync.CACHE_AGE_MS + 1))
            assertEquals("CS1-v2", f.db.studentSectionDao().getAllScheduleForStudent("student").first().single().subject)
        }
    }

    @Test
    fun missingStudentsAreCachedAndDeletedLocalDataInvalidatesCache() = runBlocking {
        withFixture { f ->
            f.missingStudent = true
            f.sync().syncAt("absent", 0)
            f.sync().syncAt("absent", 1)
            assertEquals(1, f.requests.size)
            f.missingStudent = false
            f.sync().syncAt("student", 2)
            assertEquals(4, f.requests.size)
            f.db.sectionDao().deleteAllSection()
            f.sync().syncAt("student", 3)
            assertEquals(7, f.requests.size)
            assertEquals(1, f.db.studentSectionDao().getAllScheduleForStudent("student").first().size)
        }
    }

    @Test
    fun kayaStartupFailureKeepsKayaSnapshotAndNeverFallsBackToSupabase() = runBlocking {
        withFixture { f ->
            val prefs = PrefsRepositoryImpl(f.data)
            prefs.setUserRollNumber("student")
            val store = KayaTimetableStore(f.data)
            store.save("student", listOf(SectionEntity(subject = "Cached KAYA")))
            // Missing secure credential deliberately fails refresh without accessing the portal.
            assertTrue(f.appSync().syncAll("student", "", "2026", "010").isFailure)
            assertTrue(f.requests.isEmpty(), "Connected KAYA must not query Supabase even on failure")
            assertEquals("Cached KAYA", store.observe("student").first()!!.single().subject)
        }
    }

    @Test
    fun attendanceOnlyDoesNotStartTimetableSyncEvenWithNoCachedSchedule() = runBlocking {
        withFixture { f ->
            assertTrue(f.appSync().syncAttendance("student", "", "2026", "010").isSuccess)
            assertTrue(f.requests.isEmpty())
            // A full startup does perform the cold fetch, then reuses it on subsequent startup.
            val sync = f.appSync()
            assertTrue(sync.syncAll("student", "", "2026", "010").isSuccess)
            assertEquals(3, f.requests.size)
            assertTrue(sync.syncAll("student", "", "2026", "010").isSuccess)
            assertEquals(3, f.requests.size)
        }
    }

    @Test
    fun rollChangeLeavesKayaAndLoadsNewRollFromSupabase() = runBlocking {
        withFixture { f ->
            val prefs = PrefsRepositoryImpl(f.data)
            prefs.setUserRollNumber("student")
            val store = KayaTimetableStore(f.data)
            store.save("student", listOf(SectionEntity(subject = "Old personal timetable")))
            prefs.setUserRollNumber("new-student")
            assertFalse(store.isConnected.first())
            assertFalse(prefs.kayaConnectedFlow.first())
            assertTrue(f.appSync().syncAll("new-student", "", "2026", "010").isSuccess)
            assertEquals(3, f.requests.size)
            assertEquals("CS1-v1", StudentSectionRepository(f.db.studentSectionDao(), store)
                .getAllScheduleForStudent("new-student").first().single().subject)
            prefs.setUserRollNumber("student")
            assertFalse(store.isConnected.first(), "Changing back must not revive the old KAYA connection")
        }
    }

    @Test
    fun kayaLogoutFetchesMissingSupabaseSnapshotAndClearsOnlyKayaCredential() = runBlocking {
        withFixture { f ->
            PrefsRepositoryImpl(f.data).setUserRollNumber("student")
            val store = KayaTimetableStore(f.data)
            store.save("student", listOf(SectionEntity(subject = "KAYA")))
            val secure = SecureStorage()
            secure.saveKayaPassword("test-kaya")
            secure.saveSapPassword("test-sap")
            val repo = KayaRepository(KayaPortalClient(), store, secure, AppSyncTrigger(), f.db.studentSectionDao(), f.sync())
            assertTrue(repo.disconnect("student").isSuccess)
            assertFalse(store.isConnected.first())
            assertEquals("", secure.getKayaPassword())
            assertEquals("test-sap", secure.getSapPassword())
            assertEquals(3, f.requests.size)
            assertEquals("CS1-v1", StudentSectionRepository(f.db.studentSectionDao(), store)
                .getAllScheduleForStudent("student").first().single().subject)
        }
    }

    @Test
    fun simultaneousSyncsMakeOneSetOfRequests() = runBlocking {
        withFixture { f ->
            val sync = f.sync()
            f.gate = CompletableDeferred()
            coroutineScope {
                val calls = List(12) { async { sync.sync("student") } }
                f.entered.await()
                f.gate!!.complete(Unit)
                calls.awaitAll()
            }
            assertEquals(listOf("students", "active_session", "timetable"), f.requests.toList())
        }
    }
}
