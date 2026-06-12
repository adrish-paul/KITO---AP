package com.kito.feature.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.kito.core.datastore.PrefsRepository
import com.kito.core.platform.SecureStorage
import com.kito.core.presentation.components.state.SyncUiState
import com.kito.feature.schedule.notification.NotificationController
import com.kito.feature.settings.presentation.SettingsViewModel
import com.kito.testing.FakeAttendanceRepository
import com.kito.testing.FakeAuthRepository
import com.kito.testing.FakeSyncUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okio.Path.Companion.toOkioPath
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var tempFile: File

    @BeforeTest fun setup() { Dispatchers.setMain(testDispatcher); tempFile = File.createTempFile("settings_prefs_", ".preferences_pb") }
    @AfterTest  fun teardown() { Dispatchers.resetMain(); tempFile.delete() }

    private fun prefs() = PrefsRepository(PreferenceDataStoreFactory.createWithPath { tempFile.toOkioPath() })

    private fun vm() = SettingsViewModel(
        prefs = prefs(),
        secureStorage = SecureStorage(),
        attendanceRepository = FakeAttendanceRepository(),
        appSyncUseCase = FakeSyncUseCase(),
        notificationController = object : NotificationController { override suspend fun sync() {} },
        authRepository = FakeAuthRepository(),
        dispatcher = testDispatcher,
    )

    @Test
    fun syncState_initiallyIdle() = runTest(testDispatcher) {
        assertIs<SyncUiState.Idle>(vm().syncState.value)
    }

    @Test
    fun setSyncStateIdle_resetsToIdle() = runTest(testDispatcher) {
        val v = vm()
        v.syncStateIdle()
        assertIs<SyncUiState.Idle>(v.syncState.value)
    }
}
