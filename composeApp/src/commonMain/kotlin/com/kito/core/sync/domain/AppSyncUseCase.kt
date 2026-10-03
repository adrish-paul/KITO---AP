package com.kito.core.sync.domain

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.kito.core.database.AppDB
import com.kito.core.database.repository.StudentSectionRepository
import com.kito.core.datastore.domain.repository.PrefsRepository
import com.kito.core.platform.AppSyncTrigger
import com.kito.feature.attendance.domain.model.Attendance
import com.kito.feature.attendance.domain.repository.AttendanceRepository
import com.kito.kaya.KayaRepository
import com.kito.kaya.KayaResult
import com.kito.sap.AttendanceResult
import com.kito.sap.SapRepository
import com.kito.sap.SubjectAttendance
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Provided

class AppSyncUseCase(
    private val db: AppDB,
    @Provided private val syncTrigger: AppSyncTrigger,
    private val studentSectionRepository: StudentSectionRepository,
    private val attendanceRepository: AttendanceRepository,
    private val sapRepository: SapRepository,
    private val prefs: PrefsRepository,
    private val kayaRepository: KayaRepository,
    private val timetableSync: SupabaseTimetableSync,
) : SyncUseCase {
    private class AttendanceKey(val roll: String, val password: String, val year: String, val term: String) {
        override fun equals(other: Any?) = other is AttendanceKey &&
            roll == other.roll && password == other.password && year == other.year && term == other.term
        override fun hashCode() = listOf(roll, password, year, term).hashCode()
        // Never expose credentials through toString/logging.
    }
    private val attendanceFlights = SingleFlight<AttendanceKey, Result<Unit>>()
    private val timetableFlights = SingleFlight<Pair<String, Boolean>, Result<Unit>>()
    private val sapSession = Mutex()

    override suspend fun syncAll(roll: String, sapPassword: String, year: String, term: String): Result<Unit> =
        supervisorScope {
            val attendance = async { syncAttendance(roll, sapPassword, year, term) }
            val timetable = async { syncTimetable(roll) }
            val attendanceResult = attendance.await()
            val timetableResult = timetable.await()
            if (attendanceResult.isFailure) attendanceResult else timetableResult
        }

    override suspend fun syncAttendance(roll: String, sapPassword: String, year: String, term: String): Result<Unit> {
        if (sapPassword.isBlank()) return Result.success(Unit)
        return attendanceFlights.run(AttendanceKey(roll, sapPassword, year, term)) {
            syncResult {
                // Different selections must not interleave inside SAP's stateful web session.
                sapSession.withLock {
                    when (val result = sapRepository.login(roll, sapPassword, year, term)) {
                        is AttendanceResult.Error -> throw SyncException(result.message)
                        is AttendanceResult.Success -> db.useWriterConnection { connection ->
                            connection.immediateTransaction {
                                db.attendanceDao().deleteForTerm(year, term)
                                attendanceRepository.insertAttendance(result.data.subjects.map { it.toDomain() }, year, term)
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun syncTimetable(roll: String): Result<Unit> {
        val useKaya = kayaRepository.isConnected.first()
        return timetableFlights.run(roll to useKaya) {
            syncResult {
                if (useKaya) {
                    // KAYA refresh already saves its snapshot and updates widgets once.
                    val result = kayaRepository.refresh(roll)
                    if (result is KayaResult.Error) throw SyncException(result.message)
                } else if (timetableSync.sync(roll)) {
                    // Resolve the currently selected source, including a KAYA login during this fetch.
                    val sections = studentSectionRepository.getAllScheduleForStudent(roll).first()
                    syncTrigger.onSyncComplete(roll, sections)
                }
            }
        }
    }
}

private suspend fun syncResult(block: suspend () -> Unit): Result<Unit> = try {
    block()
    Result.success(Unit)
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}

class SyncException(message: String) : Exception(message)

sealed class SyncError(
    val internalMessage: String,
    val userMessage: String,
    val code: String
) {

    class StudentFetchFailed(cause: String) : SyncError(
        internalMessage = "Failed to fetch student by roll from Supabase: $cause",
        userMessage = "Could not load your student profile. Please try again.",
        code = "SYNC_001"
    )

    class TimetableFetchFailed(section: String, batch: String, cause: String) : SyncError(
        internalMessage = "Failed to fetch timetable for section=$section batch=$batch: $cause",
        userMessage = "Could not load your timetable. Please try again.",
        code = "SYNC_002"
    )

    class DatabaseWriteFailed(cause: String) : SyncError(
        internalMessage = "Room transaction failed during sync write: $cause",
        userMessage = "Could not save data locally. Please try again.",
        code = "SYNC_003"
    )

    class AttendanceSyncFailed(sanitizedMessage: String) : SyncError(
        internalMessage = "SAP attendance fetch failed (message already sanitized): $sanitizedMessage",
        userMessage = sanitizedMessage,
        code = "SYNC_004"
    )

    class SyncTriggerFailed(cause: String) : SyncError(
        internalMessage = "AppSyncTrigger.onSyncComplete threw: $cause",
        userMessage = "Sync completed but the app state could not be updated. Please restart.",
        code = "SYNC_005"
    )

    class UnknownError(cause: String) : SyncError(
        internalMessage = "Unhandled exception during sync: $cause",
        userMessage = "An unexpected error occurred. Please try again.",
        code = "SYNC_006"
    )
}

private fun SubjectAttendance.toDomain(): Attendance = Attendance(
    subjectCode = subjectCode,
    subjectName = subjectName,
    attendedClasses = attendedClasses,
    totalClasses = totalClasses,
    percentage = percentage,
    facultyName = facultyName
)
