package com.kito.feature.calendar

import com.kito.core.network.supabase.model.CalendarEventModel
import com.kito.feature.calendar.data.mapper.toDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CalendarMapperTest {

    private fun dto(color: String? = "#FF0000") = CalendarEventModel(
        id = 1L, title = "Fest", description = "Annual", date = "2026-09-01",
        start_time = "09:00", end_time = "17:00", category = "Cultural",
        color = color, is_active = true
    )

    @Test
    fun toDomain_mapsAllFields() {
        val domain = dto().toDomain()
        assertEquals(1L, domain.id)
        assertEquals("Fest", domain.title)
        assertEquals("Annual", domain.description)
        assertEquals("2026-09-01", domain.date)
        assertEquals("09:00", domain.startTime)
        assertEquals("17:00", domain.endTime)
        assertEquals("Cultural", domain.category)
        assertEquals("#FF0000", domain.color)
        assertTrue(domain.isActive)
    }

    @Test
    fun toDomain_nullColor_mapsToNull() {
        assertNull(dto(color = null).toDomain().color)
    }

    @Test
    fun toDomain_nullId_defaultsToZero() {
        assertEquals(0L, CalendarEventModel(null, null, null, null, null, null, null, null, null).toDomain().id)
    }

    @Test
    fun toDomain_nullTitle_defaultsToEmpty() {
        assertEquals("", CalendarEventModel(null, null, null, null, null, null, null, null, null).toDomain().title)
    }
}
