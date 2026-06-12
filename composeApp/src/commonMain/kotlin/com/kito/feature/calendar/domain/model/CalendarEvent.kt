package com.kito.feature.calendar.domain.model

/**
 * Pure domain model for a calendar event. No Supabase/serialization types.
 * Fields are non-nullable with sensible defaults mapped from the DTO.
 */
data class CalendarEvent(
    val id: Long,
    val title: String,
    val description: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val category: String,
    val color: String?,
    val isActive: Boolean,
)
