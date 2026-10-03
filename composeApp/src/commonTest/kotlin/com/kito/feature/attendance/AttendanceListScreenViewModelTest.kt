package com.kito.feature.attendance

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.kito.core.datastore.domain.repository.PrefsRepository
import com.kito.core.datastore.data.PrefsRepositoryImpl
import com.kito.testing.FakeConnectivityRepository
import com.kito.testing.FakeCredentialsRepository
import com.kito.core.ui.state.SyncUiState
import com.kito.feature.attendance.domain.usecase.GetAttendanceSummaryUseCase
import com.kito.feature.attendance.presentation.AttendanceListScreenViewModel
import com.kito.testing.FakeAttendanceRepository
import com.kito.core.sync.domain.SyncUseCase
import com.kito.testing.attendance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceListScreenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val tempPath = "attendance_prefs_test.preferences_pb".toPath()
    private lateinit var prefsRepository: PrefsRepository
    private lateinit var datastoreScope: CoroutineScope

    private lateinit var repo: FakeAttendanceRepository
    private lateinit var fakeCredentials: FakeCredentialsRepository
    private lateinit var spySyncUseCase: SpySyncUseCase
    private lateinit var vm: AttendanceListScreenViewModel

    class SpySyncUseCase : SyncUseCase {
        override suspend fun syncAll(roll: String, sapPassword: String, year: String, term: String): Result<Unit> =
            error("The attendance screen must never sync the timetable")
        var syncAllRoll: String? = null
        var syncAllPassword: String? = null
        var syncAllYear: String? = null
        var syncAllTerm: String? = null
        var result = Result.success(Unit)

        override suspend fun syncAttendance(
            roll: String,
            sapPassword: String,
            year: String,
            term: String
        ): Result<Unit> {
            syncAllRoll = roll
            syncAllPassword = sapPassword
            syncAllYear = year
            syncAllTerm = term
            return result
        }
    }

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        datastoreScope = CoroutineScope(testDispatcher + SupervisorJob())
        prefsRepository = PrefsRepositoryImpl(
            PreferenceDataStoreFactory.createWithPath(
                scope = datastoreScope,
                produceFile = { tempPath }
            )
        )
        repo = FakeAttendanceRepository()
        fakeCredentials = FakeCredentialsRepository()
        spySyncUseCase = SpySyncUseCase()
        val changeYearTermUseCase = com.kito.core.sync.domain.usecase.ChangeYearTermUseCase(
            prefs = prefsRepository,
            credentialsRepository = fakeCredentials,
            attendanceRepository = repo,
            syncUseCase = spySyncUseCase
        )
        vm = AttendanceListScreenViewModel(
            getAttendanceSummary = GetAttendanceSummaryUseCase(repo),
            prefs = prefsRepository,
            credentialsRepository = fakeCredentials,
            appSyncUseCase = spySyncUseCase,
            changeYearTermUseCase = changeYearTermUseCase,
            connectivityRepository = FakeConnectivityRepository(),
            dispatcher = testDispatcher,
        )
    }

    @AfterTest
    fun teardown() {
        datastoreScope.cancel()
        Dispatchers.resetMain()
        try {
            FileSystem.SYSTEM.delete(tempPath)
        } catch (_: Exception) {
            // ignore
        }
    }

    @Test
    fun attendance_initiallyEmpty() = runTest(testDispatcher) {
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        assertEquals(emptyList(), vm.uiState.value.attendance)
        job.cancel()
    }

    @Test
    fun attendance_updatesWhenRepoEmits() = runTest(testDispatcher) {
        val job = launch { vm.uiState.collect {} }
        repo.emit(listOf(attendance(subjectCode = "CS101"), attendance(subjectCode = "CS102")))
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.attendance.size)
        assertEquals("CS101", vm.uiState.value.attendance[0].subjectCode)
        job.cancel()
    }

    @Test
    fun averagePercentage_derivedCorrectly() = runTest(testDispatcher) {
        val job = launch { vm.uiState.collect {} }
        repo.emit(listOf(attendance(percentage = 60.0), attendance(percentage = 80.0)))
        advanceUntilIdle()
        assertEquals(70.0, vm.uiState.value.averageAttendancePercentage)
        job.cancel()
    }

    @Test
    fun highestAndLowest_correct() = runTest(testDispatcher) {
        val job = launch { vm.uiState.collect {} }
        repo.emit(listOf(
            attendance(percentage = 50.0),
            attendance(percentage = 90.0),
            attendance(percentage = 70.0),
        ))
        advanceUntilIdle()
        assertEquals(90.0, vm.uiState.value.highestAttendancePercentage)
        assertEquals(50.0, vm.uiState.value.lowestAttendancePercentage)
        job.cancel()
    }

    @Test
    fun syncState_initiallyIdle() = runTest(testDispatcher) {
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        assertIs<SyncUiState.Idle>(vm.uiState.value.syncState)
        job.cancel()
    }

    @Test
    fun refresh_success_updatesSyncStateAndEmitsEvent() = runTest(testDispatcher) {
        prefsRepository.setUserRollNumber("123456")
        prefsRepository.setAcademicYear("2026")
        prefsRepository.setTermCode("010")
        fakeCredentials.saveSapPassword("pwd")

        val events = mutableListOf<SyncUiState>()
        val jobEvents = launch { vm.syncEvents.collect { events.add(it) } }
        val jobState = launch { vm.uiState.collect {} }

        vm.refresh()
        advanceUntilIdle()

        assertEquals("123456", spySyncUseCase.syncAllRoll)
        assertEquals("pwd", spySyncUseCase.syncAllPassword)
        assertEquals("2026", spySyncUseCase.syncAllYear)
        assertEquals("010", spySyncUseCase.syncAllTerm)

        assertIs<SyncUiState.Success>(vm.uiState.value.syncState)
        assertEquals(1, events.size)
        assertIs<SyncUiState.Success>(events[0])

        jobEvents.cancel()
        jobState.cancel()
    }

    @Test
    fun refresh_failure_updatesSyncStateWithError() = runTest(testDispatcher) {
        spySyncUseCase.result = Result.failure(Exception("network error"))
        val job = launch { vm.uiState.collect {} }

        vm.refresh()
        advanceUntilIdle()

        assertIs<SyncUiState.Error>(vm.uiState.value.syncState)
        assertEquals("network error", (vm.uiState.value.syncState as SyncUiState.Error).message)
        job.cancel()
    }

    @Test
    fun setSyncStateIdle_resetsState() = runTest(testDispatcher) {
        spySyncUseCase.result = Result.failure(Exception("network error"))
        val job = launch { vm.uiState.collect {} }
        
        vm.refresh()
        advanceUntilIdle()
        assertIs<SyncUiState.Error>(vm.uiState.value.syncState)

        vm.setSyncStateIdle()
        advanceUntilIdle()
        assertIs<SyncUiState.Idle>(vm.uiState.value.syncState)
        job.cancel()
    }

    @Test
    fun login_success_savesPasswordAndUpdatesState() = runTest(testDispatcher) {
        prefsRepository.setUserRollNumber("123456")
        prefsRepository.setAcademicYear("2026")
        prefsRepository.setTermCode("010")
        val job = launch { vm.uiState.collect {} }

        vm.login("new_password")
        advanceUntilIdle()

        assertEquals("123456", spySyncUseCase.syncAllRoll)
        assertEquals("new_password", spySyncUseCase.syncAllPassword)
        assertEquals("new_password", fakeCredentials.getSapPassword())
        assertIs<SyncUiState.Success>(vm.uiState.value.loginState)
        job.cancel()
    }

    @Test
    fun login_failure_doesNotSavePasswordAndSetsError() = runTest(testDispatcher) {
        spySyncUseCase.result = Result.failure(Exception("auth failed"))
        val job = launch { vm.uiState.collect {} }

        vm.login("new_password")
        advanceUntilIdle()

        assertEquals("", fakeCredentials.getSapPassword())
        assertIs<SyncUiState.Error>(vm.uiState.value.loginState)
        assertEquals("auth failed", (vm.uiState.value.loginState as SyncUiState.Error).message)
        job.cancel()
    }

    @Test
    fun setLoginStateIdle_resetsState() = runTest(testDispatcher) {
        spySyncUseCase.result = Result.failure(Exception("auth failed"))
        val job = launch { vm.uiState.collect {} }
        
        vm.login("pwd")
        advanceUntilIdle()
        assertIs<SyncUiState.Error>(vm.uiState.value.loginState)

        vm.setLoginStateIdle()
        advanceUntilIdle()
        assertIs<SyncUiState.Idle>(vm.uiState.value.loginState)
        job.cancel()
    }

    @Test
    fun requiredAttendance_updatesCorrectly() = runTest(testDispatcher) {
        val job = launch { vm.uiState.collect {} }
        prefsRepository.setRequiredAttendance(85)
        advanceUntilIdle()
        assertEquals(85, vm.uiState.value.requiredAttendance)
        job.cancel()
    }

    @Test
    fun changeYearTerm_success_updatesPrefsDeletesAttendanceAndSyncs() = runTest(testDispatcher) {
        prefsRepository.setUserRollNumber("123456")
        fakeCredentials.saveSapPassword("pwd")
        repo.emit(listOf(attendance())) // Seed attendance

        val job = launch { vm.uiState.collect {} }
        vm.changeYearTerm("2026", "020")
        advanceUntilIdle()

        // Verify preferences updated
        assertEquals("2026", prefsRepository.academicYearFlow.first())
        assertEquals("020", prefsRepository.termCodeFlow.first())
        
        // Verify local attendance database cleared
        assertEquals(emptyList(), vm.uiState.value.attendance)

        // Verify sync triggered
        assertEquals("123456", spySyncUseCase.syncAllRoll)
        assertEquals("pwd", spySyncUseCase.syncAllPassword)
        assertEquals("2026", spySyncUseCase.syncAllYear)
        assertEquals("020", spySyncUseCase.syncAllTerm)
        assertIs<SyncUiState.Success>(vm.uiState.value.syncState)
        job.cancel()
    }
}
