package com.kito.feature.friendview

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.kito.core.datastore.PrefsRepository
import com.kito.feature.friendview.presentation.FriendViewViewmodel
import com.kito.testing.FakeFriendViewRepository
import com.kito.testing.friendScheduleItem
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FriendViewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var tempFile: File

    @BeforeTest fun setup() { Dispatchers.setMain(testDispatcher); tempFile = File.createTempFile("fv_prefs_", ".preferences_pb") }
    @AfterTest  fun teardown() { Dispatchers.resetMain(); tempFile.delete() }

    private fun prefs() = PrefsRepository(PreferenceDataStoreFactory.createWithPath { tempFile.toOkioPath() })

    @Test
    fun weeklySchedule_initiallyEmpty() = runTest(testDispatcher) {
        val vm = FriendViewViewmodel(FakeFriendViewRepository(listOf(friendScheduleItem())), prefs(), testDispatcher)
        val job = launch { vm.weeklySchedule.collect {} }
        advanceUntilIdle()
        // No selected friend → empty map
        assertTrue(vm.weeklySchedule.value.isEmpty())
        job.cancel()
    }

    @Test
    fun friendRolls_initiallyEmpty() = runTest(testDispatcher) {
        val vm = FriendViewViewmodel(FakeFriendViewRepository(), prefs(), testDispatcher)
        val job = launch { vm.friendRolls.collect {} }
        advanceUntilIdle()
        assertTrue(vm.friendRolls.value.isEmpty())
        job.cancel()
    }
}
