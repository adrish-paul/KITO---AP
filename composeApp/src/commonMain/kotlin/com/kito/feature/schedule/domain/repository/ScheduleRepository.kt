package com.kito.feature.schedule.domain.repository

import com.kito.feature.schedule.domain.model.ScheduleItem
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun getScheduleForDay(rollNo: String, day: String): Flow<List<ScheduleItem>>
    fun getAllSchedule(rollNo: String): Flow<List<ScheduleItem>>
}
