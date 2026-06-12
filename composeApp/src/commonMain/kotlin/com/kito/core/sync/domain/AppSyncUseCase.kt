package com.kito.core.sync.domain

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.kito.core.database.AppDB
import com.kito.core.database.entity.toAttendanceEntity
import com.kito.core.database.repository.AttendanceRepository
import com.kito.core.database.repository.SectionRepository
import com.kito.core.database.repository.StudentRepository
import com.kito.core.database.repository.StudentSectionRepository
import com.kito.core.network.supabase.SupabaseRepository
import com.kito.core.platform.AppConfig
import com.kito.core.platform.AppSyncTrigger
import com.kito.core.platform.ErrorSanitizer
import com.kito.sap.AttendanceResult
import com.kito.sap.SapRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.supervisorScope
import org.koin.core.annotation.Provided

class AppSyncUseCase(
    private val db: AppDB,
    @Provided private val syncTrigger: AppSyncTrigger,
    private val supabaseRepository: SupabaseRepository,
    private val studentRepository: StudentRepository,
    private val sectionRepository: SectionRepository,
    private val studentSectionRepository: StudentSectionRepository,
    private val attendanceRepository: AttendanceRepository,
    private val sapRepository: SapRepository,
) : SyncUseCase {
    override suspend fun scheduleSync(roll: String): Result<Unit> = supervisorScope {
        runCatching {
            val student = runCatching {
                supabaseRepository.getStudentByRoll(roll)
            }.getOrElse { e ->
                val error = SyncError.StudentFetchFailed(e.message ?: e::class.simpleName ?: "unknown")
                ErrorSanitizer.log(error)
                if (AppConfig.isDebug) e.printStackTrace()
                throw SyncException(ErrorSanitizer.sanitize(error))
            }

            val timetable = runCatching {
                supabaseRepository.getTimetableForStudent(
                    section = student.section,
                    batch = student.batch
                )
            }.getOrElse { e ->
                val error = SyncError.TimetableFetchFailed(
                    section = student.section,
                    batch = student.batch,
                    cause = e.message ?: e::class.simpleName ?: "unknown"
                )
                ErrorSanitizer.log(error)
                if (AppConfig.isDebug) e.printStackTrace()
                throw SyncException(ErrorSanitizer.sanitize(error))
            }

            runCatching {
                db.useWriterConnection { transactor ->
                    transactor.immediateTransaction {
                        studentRepository.insertStudent(listOf(student))
                        sectionRepository.insertSection(timetable)
                    }
                }
            }.getOrElse { e ->
                val error = SyncError.DatabaseWriteFailed(e.message ?: e::class.simpleName ?: "unknown")
                ErrorSanitizer.log(error)
                if (AppConfig.isDebug) e.printStackTrace()
                throw SyncException(ErrorSanitizer.sanitize(error))
            }
        }
    }

    override suspend fun syncAll(
        roll: String,
        sapPassword: String,
        year: String,
        term: String
    ): Result<Unit> = supervisorScope {
        runCatching {
            val student = runCatching {
                supabaseRepository.getStudentByRoll(roll)
            }.getOrElse { e ->
                val error = SyncError.StudentFetchFailed(e.message ?: e::class.simpleName ?: "unknown")
                ErrorSanitizer.log(error)
                if (AppConfig.isDebug) e.printStackTrace()
                throw SyncException(ErrorSanitizer.sanitize(error))
            }

            val timetableDeferred = async {
                runCatching {
                    supabaseRepository.getTimetableForStudent(
                        section = student.section,
                        batch = student.batch
                    )
                }.getOrElse { e ->
                    val error = SyncError.TimetableFetchFailed(
                        section = student.section,
                        batch = student.batch,
                        cause = e.message ?: e::class.simpleName ?: "unknown"
                    )
                    ErrorSanitizer.log(error)
                    if (AppConfig.isDebug) e.printStackTrace()
                    throw SyncException(ErrorSanitizer.sanitize(error))
                }
            }

            val attendanceDeferred = if (sapPassword.isNotEmpty()) {
                async {
                    when (val response = sapRepository.login(
                        username = roll,
                        password = sapPassword,
                        academicYear = year,
                        termCode = term
                    )) {
                        is AttendanceResult.Success -> response.data

                        is AttendanceResult.Error -> {
                            // response.message is already sanitized by ErrorSanitizer inside
                            // SapPortalClient — we wrap it so syncAll's Result<Unit> carries
                            // a clean message without re-exposing any internal details.
                            val error = SyncError.AttendanceSyncFailed(response.message)
                            ErrorSanitizer.log(error)
                            throw SyncException(ErrorSanitizer.sanitize(error))
                        }
                    }
                }
            } else null

            val timetable = timetableDeferred.await()
            val attendance = attendanceDeferred?.await()

            runCatching {
                db.useWriterConnection { transactor ->
                    transactor.immediateTransaction {
                        attendance?.let {
                            attendanceRepository.insertAttendance(
                                it.subjects.map { subject -> subject.toAttendanceEntity(year, term) }
                            )
                        }
                        studentRepository.insertStudent(listOf(student))
                        sectionRepository.insertSection(timetable)
                    }
                }
            }.getOrElse { e ->
                val error = SyncError.DatabaseWriteFailed(e.message ?: e::class.simpleName ?: "unknown")
                ErrorSanitizer.log(error)
                if (AppConfig.isDebug) e.printStackTrace()
                throw SyncException(ErrorSanitizer.sanitize(error))
            }

            runCatching {
                val sections = studentSectionRepository.getAllScheduleForStudent(rollNo = roll).first()
                syncTrigger.onSyncComplete(roll, sections)
            }.getOrElse { e ->
                val error = SyncError.SyncTriggerFailed(e.message ?: e::class.simpleName ?: "unknown")
                ErrorSanitizer.log(error)
                if (AppConfig.isDebug) e.printStackTrace()
                throw SyncException(ErrorSanitizer.sanitize(error))
            }
        }
    }
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
        userMessage = sanitizedMessage, // already safe — came from ErrorSanitizer.sanitize()
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