package com.kito.core.presentation.components

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.kito.core.database.AppDB
import com.kito.core.database.entity.toAttendanceEntity
import com.kito.core.database.repository.AttendanceRepository
import com.kito.core.database.repository.SectionRepository
import com.kito.core.database.repository.StudentRepository
import com.kito.core.database.repository.StudentSectionRepository
import com.kito.core.network.supabase.SupabaseRepository
import com.kito.core.platform.AppSyncTrigger
import com.kito.sap.AttendanceResult
import com.kito.sap.SapRepository
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class AppSyncUseCase(
    private val db: AppDB,
    private val syncTrigger: AppSyncTrigger,
    private val supabaseRepository: SupabaseRepository,
    private val studentRepository: StudentRepository,
    private val sectionRepository: SectionRepository,
    private val studentSectionRepository: StudentSectionRepository,
    private val attendanceRepository: AttendanceRepository,
    private val sapRepository: SapRepository,
) {
    suspend fun scheduleSync(
        roll: String
    ): Result<Unit> = supervisorScope {
        runCatching {
            val student = supabaseRepository.getStudentByRoll(roll)
            val timetable = supabaseRepository.getTimetableForStudent(
                section = student.section,
                batch = student.batch
            )
            db.useWriterConnection { transactor ->
                transactor.immediateTransaction {
                    studentRepository.insertStudent(listOf(student))
                    sectionRepository.insertSection(timetable)
                }
            }
        }
    }
    suspend fun syncAll(
        roll: String,
        sapPassword: String,
        year: String,
        term: String
    ): Result<Unit> = supervisorScope {
        runCatching {
            val student = supabaseRepository.getStudentByRoll(roll)
            val timetableDeferred = async {
                supabaseRepository.getTimetableForStudent(
                    section = student.section,
                    batch = student.batch
                )
            }
            val attendanceDeferred = if (sapPassword.isNotEmpty()) {
                async {
                    when (
                        val response = sapRepository.login(
                            username = roll,
                            password = sapPassword,
                            academicYear = year,
                            termCode = term
                        )
                    ) {
                        is AttendanceResult.Success -> response.data
                        is AttendanceResult.Error -> throw IllegalStateException(response.message)
                    }
                }
            } else null
            val timetable = timetableDeferred.await()
            val attendance = attendanceDeferred?.await()
            db.useWriterConnection { transactor ->
                transactor.immediateTransaction {
                    attendance?.let {
                        attendanceRepository.insertAttendance(
                            it.subjects.map { subject ->
                                subject.toAttendanceEntity(year, term)
                            }
                        )
                    }
                    studentRepository.insertStudent(listOf(student))
                    sectionRepository.insertSection(timetable)
                }
            }
            val sections =
                studentSectionRepository.getAllScheduleForStudent(rollNo = roll).first()
            syncTrigger.onSyncComplete(roll, sections)
        }
    }
}
