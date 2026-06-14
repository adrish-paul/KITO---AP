package com.kito.feature.schedule

import com.kito.feature.schedule.presentation.components.isClassOngoing
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsClassOngoingTest {

    @Test
    fun isClassOngoing_timeBeforeStart_returnsFalse() {
        val ongoing = isClassOngoing(
            startTime = "09:00",
            endTime = "10:00",
            now = LocalTime(8, 59, 59)
        )
        assertFalse(ongoing)
    }

    @Test
    fun isClassOngoing_timeExactlyStart_returnsTrue() {
        val ongoing = isClassOngoing(
            startTime = "09:00",
            endTime = "10:00",
            now = LocalTime(9, 0, 0)
        )
        assertTrue(ongoing)
    }

    @Test
    fun isClassOngoing_timeDuringClass_returnsTrue() {
        val ongoing = isClassOngoing(
            startTime = "09:00",
            endTime = "10:00",
            now = LocalTime(9, 30, 0)
        )
        assertTrue(ongoing)
    }

    @Test
    fun isClassOngoing_timeExactlyEnd_returnsTrue() {
        val ongoing = isClassOngoing(
            startTime = "09:00",
            endTime = "10:00",
            now = LocalTime(10, 0, 0)
        )
        assertTrue(ongoing)
    }

    @Test
    fun isClassOngoing_timeAfterEnd_returnsFalse() {
        val ongoing = isClassOngoing(
            startTime = "09:00",
            endTime = "10:00",
            now = LocalTime(10, 0, 1)
        )
        assertFalse(ongoing)
    }

    @Test
    fun isClassOngoing_invalidFormat_returnsFalse() {
        val ongoing = isClassOngoing(
            startTime = "invalid",
            endTime = "10:00",
            now = LocalTime(9, 30, 0)
        )
        assertFalse(ongoing)
    }
}
