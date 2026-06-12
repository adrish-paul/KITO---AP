package com.kito.feature.calendar.data.mapper

import com.kito.core.network.supabase.model.CalendarEventModel
import com.kito.feature.calendar.domain.model.CalendarEvent

/**
 * Maps the Supabase network DTO to the pure domain model.
 * This is the ONLY file in the calendar feature allowed to import both types.
 */
fun CalendarEventModel.toDomain(): CalendarEvent = CalendarEvent(
    id = id ?: 0L,
    title = title.orEmpty(),
    description = description.orEmpty(),
    date = date.orEmpty(),
    startTime = start_time.orEmpty(),
    endTime = end_time.orEmpty(),
    category = category.orEmpty(),
    color = color,
    isActive = is_active ?: true,
)
