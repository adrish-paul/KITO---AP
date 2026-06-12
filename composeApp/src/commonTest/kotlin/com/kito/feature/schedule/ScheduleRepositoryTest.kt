package com.kito.feature.schedule

import com.kito.feature.schedule.domain.repository.ScheduleRepository
import com.kito.testing.FakeScheduleRepository
import com.kito.testing.scheduleItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduleRepositoryTest {

    private val items = listOf(
        scheduleItem(subject = "Maths"),
        scheduleItem(subject = "Physics"),
    )
    private val repo: ScheduleRepository = FakeScheduleRepository(items)

    @Test
    fun getAllSchedule_returnsAllItems() = runTest {
        val result = repo.getAllSchedule("roll").first()
        assertEquals(2, result.size)
    }

    @Test
    fun getScheduleForDay_returnsItems() = runTest {
        val result = repo.getScheduleForDay("roll", "MON").first()
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun emptyRepo_returnsEmptyList() = runTest {
        val result = FakeScheduleRepository().getAllSchedule("roll").first()
        assertTrue(result.isEmpty())
    }
}
