package com.kito.feature.calendar

import com.kito.core.network.supabase.model.CalendarEventModel
import com.kito.feature.calendar.data.mapper.toDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalendarMapperTest {

    private fun dto(
        id: Long?,
        title: String?,
        description: String?,
        date: String?,
        startTime: String?,
        endTime: String?,
        category: String?,
        color: String?,
        isActive: Boolean?
    ) = CalendarEventModel(
        id = id,
        title = title,
        description = description,
        date = date,
        start_time = startTime,
        end_time = endTime,
        category = category,
        color = color,
        is_active = isActive
    )

    @Test
    fun dto_maps_to_domain_preserving_fields() {
        val model = dto(
            id = 42L,
            title = "Test Event",
            description = "Description",
            date = "2026-06-11",
            startTime = "09:00",
            endTime = "10:00",
            category = "exam",
            color = "#FF0000",
            isActive = true
        ).toDomain()

        assertEquals(42L, model.id)
        assertEquals("Test Event", model.title)
        assertEquals("Description", model.description)
        assertEquals("2026-06-11", model.date)
        assertEquals("09:00", model.startTime)
        assertEquals("10:00", model.endTime)
        assertEquals("exam", model.category)
        assertEquals("#FF0000", model.color)
        assertTrue(model.isActive)
    }

    @Test
    fun dto_maps_nulls_to_sensible_defaults() {
        val model = dto(
            id = null,
            title = null,
            description = null,
            date = null,
            startTime = null,
            endTime = null,
            category = null,
            color = null,
            isActive = null
        ).toDomain()

        assertEquals(0L, model.id)
        assertEquals("", model.title)
        assertEquals("", model.description)
        assertEquals("", model.date)
        assertEquals("", model.startTime)
        assertEquals("", model.endTime)
        assertEquals("", model.category)
        assertEquals(null, model.color)
        assertTrue(model.isActive)
    }
}
