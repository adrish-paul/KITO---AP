package com.kito.feature.faculty

import com.kito.core.platform.ConnectivityObserver
import com.kito.core.presentation.components.state.SyncUiState
import com.kito.feature.faculty.domain.model.FacultyScheduleSlot
import com.kito.feature.faculty.presentation.FacultyDetailViewModel
import com.kito.feature.faculty.presentation.FacultyScreenViewModel
import com.kito.testing.FakeFacultyRepository
import com.kito.testing.faculty
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
import kotlin.test.assertIs
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class FacultyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val facultyList = listOf(faculty(id = 1L, name = "Dr. A"), faculty(id = 2L, name = "Dr. B"))
    private val schedule = listOf(FacultyScheduleSlot("Mon", "09:00", "10:00", "101", "Maths", "B1"))

    @BeforeTest fun setup() { Dispatchers.setMain(testDispatcher) }
    @AfterTest fun teardown() { Dispatchers.resetMain() }

    // ── FacultyScreenViewModel ────────────────────────────────────────────────

    @Test
    fun screen_faculty_initiallyEmpty() = runTest(testDispatcher) {
        val vm = FacultyScreenViewModel(FakeFacultyRepository(), ConnectivityObserver(), testDispatcher)
        advanceUntilIdle()
        // isOnline = true (desktop actual), so fetchFaculty runs → empty list from fake
        assertEquals(emptyList(), vm.faculty.value)
    }

    @Test
    fun screen_faculty_loadsFromRepo() = runTest(testDispatcher) {
        val vm = FacultyScreenViewModel(FakeFacultyRepository(all = facultyList), ConnectivityObserver(), testDispatcher)
        advanceUntilIdle()
        assertEquals(2, vm.faculty.value.size)
        assertEquals("Dr. A", vm.faculty.value[0].name)
    }

    @Test
    fun screen_search_filtersResults() = runTest(testDispatcher) {
        val vm = FacultyScreenViewModel(FakeFacultyRepository(all = facultyList), ConnectivityObserver(), testDispatcher)
        val job = launch { vm.facultySearchResult.collect {} }
        vm.getSearchResult("Dr. A")
        advanceUntilIdle()
        assertEquals(1, vm.facultySearchResult.value.size)
        assertEquals("Dr. A", vm.facultySearchResult.value[0].name)
        job.cancel()
    }

    @Test
    fun screen_search_emptyQuery_clearsResults() = runTest(testDispatcher) {
        val vm = FacultyScreenViewModel(FakeFacultyRepository(all = facultyList), ConnectivityObserver(), testDispatcher)
        vm.getSearchResult("")
        advanceUntilIdle()
        assertEquals(emptyList(), vm.facultySearchResult.value)
    }

    // ── FacultyDetailViewModel ────────────────────────────────────────────────

    @Test
    fun detail_initiallyIdle() = runTest(testDispatcher) {
        val vm = FacultyDetailViewModel(FakeFacultyRepository(), testDispatcher)
        assertIs<SyncUiState.Idle>(vm.syncState.value)
        assertNull(vm.faculty.value)
    }

    @Test
    fun detail_loadsCorrectFaculty() = runTest(testDispatcher) {
        val vm = FacultyDetailViewModel(FakeFacultyRepository(all = facultyList, schedule = schedule), testDispatcher)
        val job1 = launch { vm.faculty.collect {} }
        val job2 = launch { vm.schedule.collect {} }
        vm.loadFacultyDetail(1L)
        advanceUntilIdle()
        assertEquals("Dr. A", vm.faculty.value?.name)
        assertEquals(1, vm.schedule.value.size)
        assertIs<SyncUiState.Success>(vm.syncState.value)
        job1.cancel(); job2.cancel()
    }

    @Test
    fun detail_unknownId_facultyNull() = runTest(testDispatcher) {
        val vm = FacultyDetailViewModel(FakeFacultyRepository(all = facultyList), testDispatcher)
        val job = launch { vm.faculty.collect {} }
        vm.loadFacultyDetail(999L)
        advanceUntilIdle()
        assertNull(vm.faculty.value)
        job.cancel()
    }
}
