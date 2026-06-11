package com.kito.feature.calendar.data

import com.kito.core.network.supabase.SupabaseRepository
import com.kito.feature.calendar.data.mapper.toDomain
import com.kito.feature.calendar.domain.model.CalendarEvent
import com.kito.feature.calendar.domain.repository.CalendarRepository

class CalendarRepositoryImpl(
    private val supabaseRepository: SupabaseRepository,
) : CalendarRepository {
    override suspend fun getEventsByMonth(year: Int, month: Int): List<CalendarEvent> =
        supabaseRepository.getCalendarEventsByMonth(year, month).map { it.toDomain() }
}
