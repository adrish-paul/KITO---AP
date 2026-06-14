package com.kito.feature.calendar.data

import com.kito.core.network.supabase.model.CalendarEventModel
import com.kito.feature.calendar.data.mapper.toDomain
import com.kito.feature.calendar.domain.model.CalendarEvent
import com.kito.feature.calendar.domain.repository.CalendarRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

import org.koin.core.annotation.Provided

class CalendarRepositoryImpl(
    @Provided private val client: HttpClient,
) : CalendarRepository {
    override suspend fun getEventsByMonth(year: Int, month: Int): List<CalendarEvent> {
        val monthStr = "${year}-${month.toString().padStart(2, '0')}"
        val response = client.get("rest/v1/calendar_events") {
            parameter("date", "gte.${monthStr}-01")
            parameter("date", "lte.${monthStr}-31")
            parameter("is_active", "eq.true")
            parameter("order", "date.asc,start_time.asc")
            parameter("select", "*")
        }.body<List<CalendarEventModel>>()
        return response.map { it.toDomain() }
    }
}
