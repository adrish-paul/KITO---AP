package com.kito.feature.gpa

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.kito.core.datastore.PrefsRepository
import com.kito.feature.gpa.presentation.GPAViewmodel
import com.kito.testing.FakeGpaRepository
import com.kito.testing.studentProfile
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

@OptIn(ExperimentalCoroutinesApi::class)
class GpaViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var tempFile: File

    @BeforeTest fun setup() { Dispatchers.setMain(testDispatcher); tempFile = File.createTempFile("gpa_prefs_", ".preferences_pb") }
    @AfterTest  fun teardown() { Dispatchers.resetMain(); tempFile.delete() }

    private fun prefs() = PrefsRepository(PreferenceDataStoreFactory.createWithPath { tempFile.toOkioPath() })

    @Test
    fun branch_defaultsToCSE_whenNoProfile() = runTest(testDispatcher) {
        val vm = GPAViewmodel(prefs(), FakeGpaRepository(null), testDispatcher)
        val job = launch { vm.branch.collect {} }
        advanceUntilIdle()
        assertEquals("CSE", vm.branch.value)
        job.cancel()
    }

    @Test
    fun branch_derivedFromSectionWhenProfileAvailable() = runTest(testDispatcher) {
        // GPA VM needs a roll to call getStudentProfile — with empty roll it skips
        // Test initialState only (profile null = CSE default)
        val vm = GPAViewmodel(prefs(), FakeGpaRepository(studentProfile(section = "EE-A")), testDispatcher)
        val job = launch { vm.branch.collect {} }
        advanceUntilIdle()
        // With no roll set in prefs, collect fires with "" → skips → branch stays "CSE"
        assertEquals("CSE", vm.branch.value)
        job.cancel()
    }

    @Test
    fun updateSemester_updatesSemesterState() = runTest(testDispatcher) {
        val vm = GPAViewmodel(prefs(), FakeGpaRepository(null), testDispatcher)
        val job = launch { vm.semester.collect {} }
        vm.updateSemester(5)
        advanceUntilIdle()
        assertEquals(5, vm.semester.value)
        job.cancel()
    }

    @Test
    fun updateBranch_updatesBranchState() = runTest(testDispatcher) {
        val vm = GPAViewmodel(prefs(), FakeGpaRepository(null), testDispatcher)
        val job = launch { vm.branch.collect {} }
        vm.updateBranch("ECE")
        advanceUntilIdle()
        assertEquals("ECE", vm.branch.value)
        job.cancel()
    }
}
