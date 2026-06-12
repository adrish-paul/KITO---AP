package com.kito.feature.attendance

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.kito.core.datastore.PrefsRepository
import com.kito.core.platform.ConnectivityObserver
import com.kito.core.platform.SecureStorage
import com.kito.core.presentation.components.state.SyncUiState
import com.kito.feature.attendance.domain.usecase.GetAttendanceSummaryUseCase
import com.kito.feature.attendance.presentation.AttendanceListScreenViewModel
import com.kito.testing.FakeAttendanceRepository
import com.kito.testing.FakeSyncUseCase
import com.kito.testing.attendance
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
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceListScreenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repo: FakeAttendanceRepository
    private lateinit var vm: AttendanceListScreenViewModel
    private lateinit var tempFile: File

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        tempFile = File.createTempFile("prefs_vm_test_", ".preferences_pb")
        val dataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { tempFile.toOkioPath() }
        )
        repo = FakeAttendanceRepository()
        vm = AttendanceListScreenViewModel(
            getAttendanceSummary = GetAttendanceSummaryUseCase(repo),
            prefs = PrefsRepository(dataStore),
            secureStorage = SecureStorage(),
            appSyncUseCase = FakeSyncUseCase(),
            connectivityObserver = ConnectivityObserver(),
            dispatcher = testDispatcher,
        )
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
        tempFile.delete()
    }

    @Test
    fun attendance_initiallyEmpty() = runTest(testDispatcher) {
        advanceUntilIdle()
        assertEquals(emptyList(), vm.attendance.value)
    }

    @Test
    fun attendance_updatesWhenRepoEmits() = runTest(testDispatcher) {
        // Must collect the flow to start WhileSubscribed
        val job = launch { vm.attendance.collect {} }
        repo.emit(listOf(attendance(subjectCode = "CS101"), attendance(subjectCode = "CS102")))
        advanceUntilIdle()
        assertEquals(2, vm.attendance.value.size)
        assertEquals("CS101", vm.attendance.value[0].subjectCode)
        job.cancel()
    }

    @Test
    fun averagePercentage_derivedCorrectly() = runTest(testDispatcher) {
        val job = launch { vm.averageAttendancePercentage.collect {} }
        repo.emit(listOf(attendance(percentage = 60.0), attendance(percentage = 80.0)))
        advanceUntilIdle()
        assertEquals(70.0, vm.averageAttendancePercentage.value)
        job.cancel()
    }

    @Test
    fun highestAndLowest_correct() = runTest(testDispatcher) {
        val job1 = launch { vm.highestAttendancePercentage.collect {} }
        val job2 = launch { vm.lowestAttendancePercentage.collect {} }
        repo.emit(listOf(
            attendance(percentage = 50.0),
            attendance(percentage = 90.0),
            attendance(percentage = 70.0),
        ))
        advanceUntilIdle()
        assertEquals(90.0, vm.highestAttendancePercentage.value)
        assertEquals(50.0, vm.lowestAttendancePercentage.value)
        job1.cancel(); job2.cancel()
    }

    @Test
    fun syncState_initiallyIdle() = runTest(testDispatcher) {
        assertIs<SyncUiState.Idle>(vm.syncState.value)
    }
}
