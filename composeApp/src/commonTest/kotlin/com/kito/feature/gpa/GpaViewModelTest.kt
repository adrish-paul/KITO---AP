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
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

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

    @Test
    fun defaultTemplateLoading_andBranchSemesterChanges_loadSubjects() = runTest(testDispatcher) {
        val vm = GPAViewmodel(prefsRepository, FakeGpaRepository(null), testDispatcher)

        assertEquals("CHEMISTRY", vm.uiState.value.subjects.first().name)
        assertEquals(20, vm.uiState.value.totalCredits)

        vm.onEvent(GPAEvent.UpdateSemester(3))
        advanceUntilIdle()

        assertEquals("DS", vm.uiState.value.subjects.first().name)
        assertEquals(8, vm.uiState.value.subjectCount)

        vm.onEvent(GPAEvent.UpdateBranch("ECS"))
        advanceUntilIdle()

        assertEquals("PS", vm.uiState.value.subjects.first().name)
        assertEquals(9, vm.uiState.value.subjectCount)
    }

    @Test
    fun addEditGradeDeleteAndUndo_recalculatesAndRestoresOriginalPosition() = runTest(testDispatcher) {
        val vm = GPAViewmodel(prefsRepository, FakeGpaRepository(null), testDispatcher)

        vm.onEvent(GPAEvent.UpdateNewSubjectName("CUSTOM"))
        vm.onEvent(GPAEvent.UpdateNewSubjectCredits("4"))
        vm.onEvent(GPAEvent.UpdateNewSubjectGrade(6))
        vm.onEvent(GPAEvent.AddSubject)
        advanceUntilIdle()

        val custom = vm.uiState.value.subjects.last()
        assertEquals("CUSTOM", custom.name)
        assertEquals(4, custom.credits)
        assertEquals(6, custom.gradeIndex)

        vm.onEvent(GPAEvent.UpdateSubjectName(custom.id, "RENAMED"))
        vm.onEvent(GPAEvent.UpdateSubjectCredits(custom.id, "5"))
        vm.onEvent(GPAEvent.UpdateSubjectGrade(custom.id, 5))
        advanceUntilIdle()

        val edited = vm.uiState.value.subjects.last()
        assertEquals("RENAMED", edited.name)
        assertEquals(5, edited.credits)
        assertEquals(5, edited.gradeIndex)

        vm.onEvent(GPAEvent.DeleteSubject(edited.id))
        val deleted = vm.uiState.value.deletedSubject
        assertEquals("RENAMED", deleted?.subject?.name)
        assertEquals(11, vm.uiState.value.subjectCount)

        vm.onEvent(GPAEvent.UndoDelete(deleted!!.token))
        advanceUntilIdle()

        assertEquals(12, vm.uiState.value.subjectCount)
        assertEquals(edited.id, vm.uiState.value.subjects.last().id)
        assertEquals("RENAMED", vm.uiState.value.subjects.last().name)
    }

    @Test
    fun duplicateSubjectNames_keepStableDistinctIds() = runTest(testDispatcher) {
        val vm = GPAViewmodel(prefsRepository, FakeGpaRepository(null), testDispatcher)

        repeat(2) {
            vm.onEvent(GPAEvent.UpdateNewSubjectName("DUPLICATE"))
            vm.onEvent(GPAEvent.UpdateNewSubjectCredits("1"))
            vm.onEvent(GPAEvent.AddSubject)
        }
        advanceUntilIdle()

        val duplicates = vm.uiState.value.subjects.filter { it.name == "DUPLICATE" }
        assertEquals(2, duplicates.size)
        assertNotEquals(duplicates[0].id, duplicates[1].id)

        vm.onEvent(GPAEvent.UpdateSubjectGrade(duplicates[1].id, 6))
        advanceUntilIdle()

        val refreshed = vm.uiState.value.subjects.filter { it.name == "DUPLICATE" }
        assertEquals(0, refreshed[0].gradeIndex)
        assertEquals(6, refreshed[1].gradeIndex)
    }

    @Test
    fun resetAndBranchChange_ignoreStaleUndo() = runTest(testDispatcher) {
        val vm = GPAViewmodel(prefsRepository, FakeGpaRepository(null), testDispatcher)
        val deletedId = vm.uiState.value.subjects.first().id

        vm.onEvent(GPAEvent.DeleteSubject(deletedId))
        val token = vm.uiState.value.deletedSubject!!.token

        vm.onEvent(GPAEvent.ResetSubjects)
        vm.onEvent(GPAEvent.UndoDelete(token))
        advanceUntilIdle()

        assertEquals(11, vm.uiState.value.subjectCount)

        vm.onEvent(GPAEvent.DeleteSubject(deletedId))
        val branchToken = vm.uiState.value.deletedSubject!!.token
        vm.onEvent(GPAEvent.UpdateBranch("ECS"))
        vm.onEvent(GPAEvent.UndoDelete(branchToken))
        advanceUntilIdle()

        assertEquals("ECS", vm.branch.value)
        assertEquals(11, vm.uiState.value.subjectCount)
        assertTrue(vm.uiState.value.subjects.none { it.id == deletedId })
    }

    @Test
    fun creditValidation_keepsDraftsOutOfCalculation_andWeightedSgpaUsesCommittedCredits() = runTest(testDispatcher) {
        val vm = GPAViewmodel(prefsRepository, FakeGpaRepository(null), testDispatcher)

        vm.onEvent(GPAEvent.ResetSubjects)
        vm.uiState.value.subjects.map { it.id }.forEach {
            vm.onEvent(GPAEvent.DeleteSubject(it))
        }

        vm.onEvent(GPAEvent.UpdateNewSubjectName("A"))
        vm.onEvent(GPAEvent.UpdateNewSubjectCredits("3"))
        vm.onEvent(GPAEvent.UpdateNewSubjectGrade(6))
        vm.onEvent(GPAEvent.AddSubject)
        vm.onEvent(GPAEvent.UpdateNewSubjectName("B"))
        vm.onEvent(GPAEvent.UpdateNewSubjectCredits("1"))
        vm.onEvent(GPAEvent.UpdateNewSubjectGrade(4))
        vm.onEvent(GPAEvent.AddSubject)
        advanceUntilIdle()

        assertEquals(9.5, vm.uiState.value.sgpa)

        val first = vm.uiState.value.subjects.first()
        vm.onEvent(GPAEvent.UpdateSubjectCredits(first.id, "0"))
        advanceUntilIdle()

        assertEquals("Credits must be greater than zero", vm.uiState.value.subjects.first().creditError)
        assertEquals(9.5, vm.uiState.value.sgpa)

        vm.onEvent(GPAEvent.UpdateSubjectCredits(first.id, "2.5"))
        assertEquals("Enter a whole number", vm.uiState.value.subjects.first().creditError)
    }

    @Test
    fun deleteEverySubject_makesSgpaUnavailableInsteadOfNanOrInfinity() = runTest(testDispatcher) {
        val vm = GPAViewmodel(prefsRepository, FakeGpaRepository(null), testDispatcher)

        vm.uiState.value.subjects.map { it.id }.forEach {
            vm.onEvent(GPAEvent.DeleteSubject(it))
        }
        advanceUntilIdle()

        assertEquals(0, vm.uiState.value.subjectCount)
        assertEquals(0, vm.uiState.value.totalCredits)
        assertNull(vm.uiState.value.sgpa)
    }

    @Test
    fun cgpaUpdatesReactivelyWhenCalculatedSgpaIsSelected() = runTest(testDispatcher) {
        val vm = GPAViewmodel(prefsRepository, FakeGpaRepository(null), testDispatcher)

        vm.uiState.value.subjects.map { it.id }.forEach {
            vm.onEvent(GPAEvent.DeleteSubject(it))
        }
        vm.onEvent(GPAEvent.UpdateNewSubjectName("A"))
        vm.onEvent(GPAEvent.UpdateNewSubjectCredits("3"))
        vm.onEvent(GPAEvent.UpdateNewSubjectGrade(6))
        vm.onEvent(GPAEvent.AddSubject)
        vm.onEvent(GPAEvent.UpdatePreviousCgpa("8"))
        vm.onEvent(GPAEvent.UpdateCompletedSemesters("3"))
        vm.onEvent(GPAEvent.UpdateManualCurrentSgpa("7"))
        advanceUntilIdle()

        assertEquals(7.75, vm.uiState.value.cgpa)

        vm.onEvent(GPAEvent.UseCalculatedSgpa(true))
        advanceUntilIdle()

        assertEquals(8.5, vm.uiState.value.cgpa)

        val subjectId = vm.uiState.value.subjects.single().id
        vm.onEvent(GPAEvent.UpdateSubjectGrade(subjectId, 4))
        advanceUntilIdle()

        assertEquals(8.0, vm.uiState.value.cgpa)
    }

    @Test
    fun profileDefaults_doNotOverwriteEditsOnRepeatedRollEmissions() = runTest(testDispatcher) {
        prefsRepository.setUserRollNumber("2205001")
        val vm = GPAViewmodel(prefsRepository, FakeGpaRepository(studentProfile(section = "CSE-A")), testDispatcher)
        val job = launch { vm.roll.collect {} }
        advanceUntilIdle()

        vm.onEvent(GPAEvent.UpdateSubjectName(vm.uiState.value.subjects.first().id, "EDITED"))
        prefsRepository.setUserRollNumber("2205001")
        advanceUntilIdle()

        assertEquals("CSE", vm.branch.value)
        assertEquals("EDITED", vm.uiState.value.subjects.first().name)
        job.cancel()
    }
}
