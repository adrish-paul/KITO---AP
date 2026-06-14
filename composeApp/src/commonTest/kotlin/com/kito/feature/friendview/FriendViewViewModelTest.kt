package com.kito.feature.friendview

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.kito.core.datastore.PrefsRepository
import com.kito.feature.friendview.presentation.FriendViewViewmodel
import com.kito.feature.friendview.presentation.FriendViewEvent
import com.kito.testing.FakeFriendViewRepository
import com.kito.testing.friendScheduleItem
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FriendViewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val tempPath = "friendview_prefs_test.preferences_pb".toPath()
    private lateinit var prefsRepository: PrefsRepository
    private lateinit var datastoreScope: CoroutineScope

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        datastoreScope = CoroutineScope(testDispatcher + SupervisorJob())
        prefsRepository = PrefsRepository(
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
        } catch (e: Exception) {
            // ignore
        }
    }

    @Test
    fun weeklySchedule_initiallyEmpty() = runTest(testDispatcher) {
        val vm = FriendViewViewmodel(FakeFriendViewRepository(listOf(friendScheduleItem())), prefsRepository, testDispatcher)
        val job = launch { vm.weeklySchedule.collect {} }
        advanceUntilIdle()
        // No selected friend → empty map
        assertTrue(vm.weeklySchedule.value.isEmpty())
        job.cancel()
    }

    @Test
    fun friendRolls_initiallyEmpty() = runTest(testDispatcher) {
        val vm = FriendViewViewmodel(FakeFriendViewRepository(), prefsRepository, testDispatcher)
        val job = launch { vm.friendRolls.collect {} }
        advanceUntilIdle()
        assertTrue(vm.friendRolls.value.isEmpty())
        job.cancel()
    }

    @Test
    fun onEvent_addFriend_updatesRollsAndSelects() = runTest(testDispatcher) {
        val vm = FriendViewViewmodel(FakeFriendViewRepository(), prefsRepository, testDispatcher)
        val rollsJob = launch { vm.friendRolls.collect {} }
        val selectedJob = launch { vm.selectedFriendRoll.collect {} }

        vm.onEvent(FriendViewEvent.AddFriend("2205001"))
        advanceUntilIdle()

        assertEquals(listOf("2205001"), vm.friendRolls.value)
        assertEquals("2205001", vm.selectedFriendRoll.value)

        rollsJob.cancel()
        selectedJob.cancel()
    }

    @Test
    fun onEvent_selectFriend_updatesSelectedRoll() = runTest(testDispatcher) {
        val vm = FriendViewViewmodel(FakeFriendViewRepository(), prefsRepository, testDispatcher)
        val selectedJob = launch { vm.selectedFriendRoll.collect {} }

        vm.onEvent(FriendViewEvent.SelectFriend("2205002"))
        advanceUntilIdle()

        assertEquals("2205002", vm.selectedFriendRoll.value)

        selectedJob.cancel()
    }

    @Test
    fun onEvent_removeFriend_removesFromListAndAdjustsSelection() = runTest(testDispatcher) {
        val vm = FriendViewViewmodel(FakeFriendViewRepository(), prefsRepository, testDispatcher)
        val rollsJob = launch { vm.friendRolls.collect {} }
        val selectedJob = launch { vm.selectedFriendRoll.collect {} }

        vm.onEvent(FriendViewEvent.AddFriend("2205001"))
        vm.onEvent(FriendViewEvent.AddFriend("2205002"))
        advanceUntilIdle()

        // Remove active selection "2205002" -> should fall back to first ("2205001")
        vm.onEvent(FriendViewEvent.RemoveFriend("2205002"))
        advanceUntilIdle()

        assertEquals(listOf("2205001"), vm.friendRolls.value)
        assertEquals("2205001", vm.selectedFriendRoll.value)

        rollsJob.cancel()
        selectedJob.cancel()
    }
}
