package com.kito.feature.schedule

import com.kito.feature.schedule.presentation.components.parseTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class ParseTimeTest {

    @Test
    fun parseTime_validTwoParts_returnsCorrectTime() {
        val result = parseTime("09:00")
        assertEquals(LocalTime(9, 0, 0), result)
    }

    @Test
    fun parseTime_validThreeParts_returnsCorrectTime() {
        val result = parseTime("14:30:15")
        assertEquals(LocalTime(14, 30, 15), result)
    }

    @Test
    fun parseTime_singleDigitHours_returnsCorrectTime() {
        val result = parseTime("7:05")
        assertEquals(LocalTime(7, 5, 0), result)
    }
}
