package com.kito.feature.calendar

import com.kito.testing.FakeCalendarRepository
import com.kito.testing.calendarEvent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalendarRepositoryTest {

    @Test
    fun getEventsByMonth_returnsList() = runTest {
        val repo = FakeCalendarRepository(listOf(calendarEvent(1L, "Fest"), calendarEvent(2L, "Holiday")))
        val result = repo.getEventsByMonth(2026, 9)
        assertEquals(2, result.size)
        assertEquals("Fest", result[0].title)
    }

    @Test
    fun getEventsByMonth_empty_returnsEmpty() = runTest {
        assertTrue(FakeCalendarRepository().getEventsByMonth(2026, 9).isEmpty())
    }
}
