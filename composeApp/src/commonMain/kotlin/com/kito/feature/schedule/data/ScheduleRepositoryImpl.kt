package com.kito.feature.schedule.data

import com.kito.core.database.repository.StudentSectionRepository
import com.kito.feature.schedule.data.mapper.toDomain
import com.kito.feature.schedule.domain.model.ScheduleItem
import com.kito.feature.schedule.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ScheduleRepositoryImpl(
    private val studentSectionRepository: StudentSectionRepository,
) : ScheduleRepository {
    override fun getScheduleForDay(rollNo: String, day: String): Flow<List<ScheduleItem>> =
        studentSectionRepository.getScheduleForStudent(rollNo, day).map { it.map { e -> e.toDomain() } }

    override fun getAllSchedule(rollNo: String): Flow<List<ScheduleItem>> =
        studentSectionRepository.getAllScheduleForStudent(rollNo).map { it.map { e -> e.toDomain() } }
}
