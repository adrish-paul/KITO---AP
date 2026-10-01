package com.kito.core.sync.domain

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.kito.core.database.AppDB
import com.kito.feature.attendance.domain.model.Attendance
import com.kito.feature.attendance.domain.repository.AttendanceRepository
import com.kito.core.database.repository.SectionRepository
import com.kito.core.database.repository.StudentRepository
import com.kito.core.database.repository.StudentSectionRepository
import com.kito.core.sync.data.SyncRemoteDataSource
import com.kito.core.platform.AppConfig
import com.kito.core.platform.AppSyncTrigger
import com.kito.core.platform.ErrorSanitizer
import com.kito.sap.AttendanceResult
import com.kito.sap.SapRepository
import com.kito.sap.SubjectAttendance
import com.kito.core.database.entity.ActiveSessionEntity
import com.kito.core.database.entity.SectionEntity
import com.kito.core.database.entity.StudentElectiveEntity
import com.kito.core.database.entity.StudentEntity
import com.kito.core.datastore.domain.repository.PrefsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.supervisorScope
import org.koin.core.annotation.Provided

class AppSyncUseCase(
    private val db: AppDB,
    @Provided private val syncTrigger: AppSyncTrigger,
    private val syncRemoteDataSource: SyncRemoteDataSource,
    private val studentRepository: StudentRepository,
    private val sectionRepository: SectionRepository,
    private val studentSectionRepository: StudentSectionRepository,
    private val attendanceRepository: AttendanceRepository,
    private val sapRepository: SapRepository,
    private val prefs: PrefsRepository,
) : SyncUseCase {
    override suspend fun syncAll(
        roll: String,
        sapPassword: String,
        year: String,
        term: String
    ): Result<Unit> = supervisorScope {
        runCatching {
            val student = runCatching {
                syncRemoteDataSource.getStudentByRoll(roll)
            }.getOrElse { e ->
                if (AppConfig.isDebug) e.printStackTrace()
                null
            }

            if (student != null) {
                // Auto-healing: if student is now officially in Supabase, clear any legacy manual fallback
                prefs.clearManualSchedule()
            }

            val isManual = prefs.isManualScheduleFlow.first()
            val manualSection = prefs.manualSectionFlow.first()
            val manualBatch = prefs.manualBatchFlow.first()
            val manualElective1 = prefs.manualElective1Flow.first()
            val manualElective2 = prefs.manualElective2Flow.first()

            val effectiveStudent = student ?: if (isManual && manualSection.isNotBlank() && manualBatch.isNotBlank()) {
                StudentEntity(
                    roll_no = roll,
                    section = manualSection,
                    batch = manualBatch
                )
            } else null

            val activeSessionDeferred = if (effectiveStudent != null) async {
                runCatching {
                    syncRemoteDataSource.getActiveSessionConfig()
                }.getOrElse { e ->
                    val error = SyncError.StudentFetchFailed("Active session fetch failed: ${e.message}")
                    ErrorSanitizer.log(error)
                    if (AppConfig.isDebug) e.printStackTrace()
                    throw SyncException(ErrorSanitizer.sanitize(error))
                }
            } else null

            val timetableDeferred = if (effectiveStudent != null) async {
                runCatching {
                    syncRemoteDataSource.getTimetableForStudent(
                        section = effectiveStudent.section,
                        batch = effectiveStudent.batch
                    )
                }.getOrElse { e ->
                    val error = SyncError.TimetableFetchFailed(
                        section = effectiveStudent.section,
                        batch = effectiveStudent.batch,
                        cause = e.message ?: e::class.simpleName ?: "unknown"
                    )
                    ErrorSanitizer.log(error)
                    if (AppConfig.isDebug) e.printStackTrace()
                    throw SyncException(ErrorSanitizer.sanitize(error))
                }
            } else null

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
                            val error = SyncError.AttendanceSyncFailed(response.message)
                            ErrorSanitizer.log(error)
                            throw SyncException(ErrorSanitizer.sanitize(error))
                        }
                    }
                }
            } else null

            val activeSession = activeSessionDeferred?.await()
            val timetable = timetableDeferred?.await()
            val attendance = attendanceDeferred?.await()

            val electiveFetchAll: ElectiveFetch = if (
                student != null && activeSession != null &&
                student.batch == "batch_3" && activeSession.term_code == "010"
            ) {
                runCatching {
                    val elective = syncRemoteDataSource.getStudentElective(roll)
                    if (elective != null) {
                        ElectiveFetch(
                            timetable = syncRemoteDataSource.getTimetableForStudent(elective.elective_1, elective.batch)
                                .map { it.copy(source = "elective_1") } +
                                syncRemoteDataSource.getTimetableForStudent(elective.elective_2, elective.batch)
                                    .map { it.copy(source = "elective_2") },
                            entity = StudentElectiveEntity(
                                roll_no = elective.roll_no,
                                elective_1 = elective.elective_1,
                                elective_2 = elective.elective_2,
                                batch = elective.batch
                            )
                        )
                    } else ElectiveFetch(emptyList(), null)
                }.getOrElse { ElectiveFetch(emptyList(), null) }
            } else if (
                effectiveStudent != null && activeSession != null && isManual &&
                (manualElective1.isNotBlank() || manualElective2.isNotBlank())
            ) {
                val el1Timetable = if (manualElective1.isNotBlank()) {
                    runCatching {
                        syncRemoteDataSource.getTimetableForStudent(manualElective1, effectiveStudent.batch)
                            .map { it.copy(source = "elective_1") }
                    }.getOrDefault(emptyList())
                } else emptyList()

                val el2Timetable = if (manualElective2.isNotBlank()) {
                    runCatching {
                        syncRemoteDataSource.getTimetableForStudent(manualElective2, effectiveStudent.batch)
                            .map { it.copy(source = "elective_2") }
                    }.getOrDefault(emptyList())
                } else emptyList()

                ElectiveFetch(
                    timetable = el1Timetable + el2Timetable,
                    entity = StudentElectiveEntity(
                        roll_no = roll,
                        elective_1 = manualElective1,
                        elective_2 = manualElective2,
                        batch = effectiveStudent.batch
                    )
                )
            } else ElectiveFetch(emptyList(), null)

            runCatching {
                db.useWriterConnection { transactor ->
                    transactor.immediateTransaction {
                        attendance?.let {
                            attendanceRepository.insertAttendance(
                                it.subjects.map { subject -> subject.toDomain() },
                                year,
                                term
                            )
                        }
                        if (effectiveStudent != null && activeSession != null && timetable != null) {
                            sectionRepository.deleteAllSection()
                            db.studentElectiveDao().deleteStudentElective(roll)
                            studentRepository.insertStudent(listOf(effectiveStudent))
                            sectionRepository.insertSection(timetable + electiveFetchAll.timetable)
                            db.activeSessionDao().insertActiveSession(
                                ActiveSessionEntity(
                                    academic_year = activeSession.academic_year,
                                    term_code = activeSession.term_code,
                                    version = activeSession.version
                                )
                            )
                            electiveFetchAll.entity?.let { db.studentElectiveDao().upsertStudentElective(it) }
                        }
                    }
                }
            }.getOrElse { e ->
                val error = SyncError.DatabaseWriteFailed(e.message ?: e::class.simpleName ?: "unknown")
                ErrorSanitizer.log(error)
                if (AppConfig.isDebug) e.printStackTrace()
                throw SyncException(ErrorSanitizer.sanitize(error))
            }

            if (effectiveStudent != null) {
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
}


private data class ElectiveFetch(
    val timetable: List<SectionEntity>,
    val entity: StudentElectiveEntity?
)

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