package com.kito.feature.exam

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.kito.core.datastore.PrefsRepository
import com.kito.core.presentation.components.state.SyncUiState
import com.kito.feature.exam.presentation.UpcomingExamViewModel
import com.kito.testing.FakeExamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okio.Path.Companion.toOkioPath
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ExamViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var tempFile: File

    @BeforeTest fun setup() { Dispatchers.setMain(testDispatcher); tempFile = File.createTempFile("exam_prefs_", ".preferences_pb") }
    @AfterTest  fun teardown() { Dispatchers.resetMain(); tempFile.delete() }

    private fun prefs() = PrefsRepository(PreferenceDataStoreFactory.createWithPath { tempFile.toOkioPath() })

    @Test
    fun uiState_initiallyIdle() = runTest(testDispatcher) {
        val vm = UpcomingExamViewModel(prefs(), FakeExamRepository(), testDispatcher)
        // Before init coroutine runs, state starts Idle (launched but not yet executed)
        assertIs<SyncUiState.Idle>(vm.uiState.value)
    }

    @Test
    fun exams_initiallyEmpty() = runTest(testDispatcher) {
        val vm = UpcomingExamViewModel(prefs(), FakeExamRepository(), testDispatcher)
        val job = launch { vm.exams.collect {} }
        advanceUntilIdle()
        assertTrue(vm.exams.value.isEmpty())
        job.cancel()
    }

    @Test
    fun refreshAfterInit_completes() = runTest(testDispatcher) {
        val vm = UpcomingExamViewModel(prefs(), FakeExamRepository(), testDispatcher)
        val job = launch { vm.uiState.collect {} }
        vm.getExamSchedule()
        advanceUntilIdle()
        // Should not crash; state is Success or Error (never stuck loading)
        assertTrue(vm.uiState.value !is SyncUiState.Loading)
        job.cancel()
    }
}
