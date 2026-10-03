package com.kito.core.sync.domain

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.kito.core.database.AppDB
import com.kito.core.database.entity.ActiveSessionEntity
import com.kito.core.database.entity.StudentEntity
import com.kito.core.database.entity.StudentElectiveEntity
import com.kito.core.sync.data.ActiveSessionConfig
import com.kito.core.sync.data.StudentElectiveConfig
import com.kito.core.sync.data.SyncRemoteDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Provided
import kotlin.time.Clock

/** Remote access is GET-only. Room holds classes; DataStore holds cache freshness. */
class SupabaseTimetableSync(
    private val db: AppDB,
    private val remote: SyncRemoteDataSource,
    @Provided private val dataStore: DataStore<Preferences>,
) {
    private val flights = SingleFlight<String, Boolean>()
    private val writer = Mutex()
    private val cacheKey = stringPreferencesKey("supabase_timetable_cache_v1")
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        const val CACHE_AGE_MS = 6 * 60 * 60 * 1000L
    }

    /** Returns true only when the local timetable was replaced. */
    suspend fun sync(roll: String): Boolean = flights.run(roll) {
        writer.withLock { syncAt(roll, Clock.System.now().toEpochMilliseconds()) }
    }

    internal suspend fun syncAt(roll: String, now: Long): Boolean {
        val cache = dataStore.data.first()[cacheKey]?.let {
            runCatching { json.decodeFromString<TimetableCache>(it) }.getOrNull()
        }?.takeIf { it.roll == roll }
        val localStudent = db.studentDao().getStudentByRoll(roll)
        val localSession = db.activeSessionDao().getActiveSession()
        val localCount = db.studentSectionDao().getAllScheduleForStudent(roll).first().size
        val localIntact = cache != null && localStudent == cache.student &&
            (cache.session == null || localSession == cache.session.toEntity()) && localCount == cache.rowCount
        if (localIntact && now - cache.checkedAt in 0 until CACHE_AGE_MS) return false

        val student = remote.getStudentByRoll(roll)
        if (student == null) {
            // A real not-found response is cached too; network failures throw and never replace the cache.
            localStudent?.let { db.studentDao().deleteStudent(it) }
            save(TimetableCache(roll, now, null, null, null, 0))
            return localStudent != null
        }
        val session = remote.getActiveSessionConfig()
        val elective = if (student.batch == "batch_3" && session.term_code == "010") {
            remote.getStudentElective(roll)
        } else null
        // Refresh the actual rows after expiry: edits are not guaranteed to bump version.
        val rows = remote.getTimetableForStudent(student.section, student.batch).map { it.copy(source = "core") } +
            if (elective == null) emptyList() else {
                remote.getTimetableForStudent(elective.elective_1, elective.batch).map { it.copy(source = "elective_1") } +
                    remote.getTimetableForStudent(elective.elective_2, elective.batch).map { it.copy(source = "elective_2") }
            }

        // All requests succeeded before touching the last usable snapshot.
        db.useWriterConnection { connection ->
            connection.immediateTransaction {
                db.studentDao().insertStudent(listOf(student))
                db.sectionDao().deleteAllSection()
                db.sectionDao().insertSection(rows)
                db.activeSessionDao().insertActiveSession(session.toEntity())
                db.studentElectiveDao().deleteForStudent(roll)
                elective?.let {
                    db.studentElectiveDao().upsertStudentElective(
                        StudentElectiveEntity(it.roll_no, it.elective_1, it.elective_2, it.batch)
                    )
                }
            }
        }
        val count = db.studentSectionDao().getAllScheduleForStudent(roll).first().size
        save(TimetableCache(roll, now, student, session, elective, count))
        return true
    }

    private suspend fun save(cache: TimetableCache) {
        dataStore.edit { it[cacheKey] = json.encodeToString(cache) }
    }
}

private fun ActiveSessionConfig.toEntity() = ActiveSessionEntity(
    academic_year = academic_year, term_code = term_code, version = version,
)

@Serializable
private data class TimetableCache(
    val roll: String,
    val checkedAt: Long,
    val student: StudentEntity?,
    val session: ActiveSessionConfig?,
    val elective: StudentElectiveConfig?,
    val rowCount: Int,
)
