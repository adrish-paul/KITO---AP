package com.kito.feature.schedule

import com.kito.feature.schedule.presentation.components.isClassUpcoming
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsClassUpcomingTest {

    @Test
    fun isClassUpcoming_timeInsideWindow_returnsTrue() {
        val upcoming = isClassUpcoming(
            startTime = "09:00",
            windowMinutes = 15,
            now = LocalTime(8, 50, 0)
        )
        assertTrue(upcoming)
    }

    @Test
    fun isClassUpcoming_timeExactlyAtWindowStart_returnsTrue() {
        val upcoming = isClassUpcoming(
            startTime = "09:00",
            windowMinutes = 15,
            now = LocalTime(8, 45, 0)
        )
        assertTrue(upcoming)
    }

    @Test
    fun isClassUpcoming_timeBeforeWindow_returnsFalse() {
        val upcoming = isClassUpcoming(
            startTime = "09:00",
            windowMinutes = 15,
            now = LocalTime(8, 44, 59)
        )
        assertFalse(upcoming)
    }

    @Test
    fun isClassUpcoming_timeExactlyAtStart_returnsFalse() {
        // According to the implementation, now < start is required, so exactly at start is false
        val upcoming = isClassUpcoming(
            startTime = "09:00",
            windowMinutes = 15,
            now = LocalTime(9, 0, 0)
        )
        assertFalse(upcoming)
    }

    @Test
    fun isClassUpcoming_timeAfterStart_returnsFalse() {
        val upcoming = isClassUpcoming(
            startTime = "09:00",
            windowMinutes = 15,
            now = LocalTime(9, 0, 1)
        )
        assertFalse(upcoming)
    }

    @Test
    fun isClassUpcoming_hourRollover_returnsCorrectResult() {
        // Class at 09:05, window 15 minutes -> window starts at 08:50.
        // Test time 08:55 is inside the window -> should be true.
        val upcoming = isClassUpcoming(
            startTime = "09:05",
            windowMinutes = 15,
            now = LocalTime(8, 55, 0)
        )
        assertTrue(upcoming)
    }

    @Test
    fun isClassUpcoming_hourRollover_beforeWindow_returnsFalse() {
        // Class at 09:05, window 15 minutes -> window starts at 08:50.
        // Test time 08:49 is before the window -> should be false.
        val upcoming = isClassUpcoming(
            startTime = "09:05",
            windowMinutes = 15,
            now = LocalTime(8, 49, 0)
        )
        assertFalse(upcoming)
    }

    @Test
    fun isClassUpcoming_invalidFormat_returnsFalse() {
        val upcoming = isClassUpcoming(
            startTime = "invalid",
            windowMinutes = 15,
            now = LocalTime(8, 50, 0)
        )
        assertFalse(upcoming)
    }
}
