package com.kito.feature.gpa

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.kito.core.datastore.domain.repository.PrefsRepository
import com.kito.core.datastore.data.PrefsRepositoryImpl
import com.kito.feature.gpa.presentation.GPAViewmodel
import com.kito.feature.gpa.presentation.GPAEvent
import com.kito.testing.FakeGpaRepository
import com.kito.testing.studentProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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

@OptIn(ExperimentalCoroutinesApi::class)
class GpaViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val tempPath = "gpa_prefs_test.preferences_pb".toPath()
    private lateinit var prefsRepository: PrefsRepository
    private lateinit var datastoreScope: CoroutineScope

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
    fun branch_defaultsToCSE_whenNoProfile() = runTest(testDispatcher) {
        val vm = GPAViewmodel(prefsRepository, FakeGpaRepository(null), testDispatcher)
        val job = launch { vm.branch.collect {} }
        advanceUntilIdle()
        assertEquals("CSE", vm.branch.value)
        job.cancel()
    }

    @Test
    fun branch_derivedFromSectionWhenProfileAvailable() = runTest(testDispatcher) {
        prefsRepository.setUserRollNumber("2205001")
        val vm = GPAViewmodel(prefsRepository, FakeGpaRepository(studentProfile(section = "EE-A")), testDispatcher)
        val job1 = launch { vm.branch.collect {} }
        val job2 = launch { vm.roll.collect {} }
        advanceUntilIdle()
        assertEquals("EE", vm.branch.value)
        job1.cancel()
        job2.cancel()
    }

    @Test
    fun updateSemester_updatesSemesterState() = runTest(testDispatcher) {
        val vm = GPAViewmodel(prefsRepository, FakeGpaRepository(null), testDispatcher)
        val job = launch { vm.semester.collect {} }
        vm.onEvent(GPAEvent.UpdateSemester(5))
        advanceUntilIdle()
        assertEquals(5, vm.semester.value)
        job.cancel()
    }

    @Test
    fun updateBranch_updatesBranchState() = runTest(testDispatcher) {
        val vm = GPAViewmodel(prefsRepository, FakeGpaRepository(null), testDispatcher)
        val job = launch { vm.branch.collect {} }
        vm.onEvent(GPAEvent.UpdateBranch("ECE"))
        advanceUntilIdle()
        assertEquals("ECE", vm.branch.value)
        job.cancel()
    }
}
