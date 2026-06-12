package com.kito.feature.calendar

import com.kito.feature.calendar.presentation.CalendarViewModel
import com.kito.testing.FakeCalendarRepository
import com.kito.testing.calendarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun setup() { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun teardown() { Dispatchers.resetMain() }

    @Test
    fun events_loadedFromRepo() = runTest(testDispatcher) {
        val repo = FakeCalendarRepository(listOf(calendarEvent(1L, "Fest"), calendarEvent(2L, "Holiday")))
        val vm = CalendarViewModel(repo, testDispatcher)
        val job = launch { vm.events.collect {} }
        advanceUntilIdle()
        assertEquals(2, vm.events.value.size)
        job.cancel()
    }

    @Test
    fun events_emptyWhenRepoEmpty() = runTest(testDispatcher) {
        val vm = CalendarViewModel(FakeCalendarRepository(), testDispatcher)
        val job = launch { vm.events.collect {} }
        advanceUntilIdle()
        assertTrue(vm.events.value.isEmpty())
        job.cancel()
    }

    @Test
    fun isLoading_falseAfterFetch() = runTest(testDispatcher) {
        val vm = CalendarViewModel(FakeCalendarRepository(), testDispatcher)
        val job = launch { vm.isLoading.collect {} }
        advanceUntilIdle()
        assertFalse(vm.isLoading.value)
        job.cancel()
    }

    @Test
    fun nextMonth_incrementsMonth() = runTest(testDispatcher) {
        val vm = CalendarViewModel(FakeCalendarRepository(), testDispatcher)
        val initial = vm.displayMonth.value
        vm.nextMonth()
        advanceUntilIdle()
        val expected = if (initial == 12) 1 else initial + 1
        assertEquals(expected, vm.displayMonth.value)
    }

    @Test
    fun prevMonth_decrementsMonth() = runTest(testDispatcher) {
        val vm = CalendarViewModel(FakeCalendarRepository(), testDispatcher)
        val initial = vm.displayMonth.value
        vm.prevMonth()
        advanceUntilIdle()
        val expected = if (initial == 1) 12 else initial - 1
        assertEquals(expected, vm.displayMonth.value)
    }
}
