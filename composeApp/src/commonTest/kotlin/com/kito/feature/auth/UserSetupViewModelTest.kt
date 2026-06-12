package com.kito.feature.auth

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.kito.core.datastore.PrefsRepository
import com.kito.core.platform.SecureStorage
import com.kito.feature.auth.presentation.SetupState
import com.kito.feature.auth.presentation.UserSetupViewModel
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
class UserSetupViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var tempFile: File

    @BeforeTest fun setup() { Dispatchers.setMain(testDispatcher); tempFile = File.createTempFile("auth_prefs_", ".preferences_pb") }
    @AfterTest  fun teardown() { Dispatchers.resetMain(); tempFile.delete() }

    private fun prefs() = PrefsRepository(PreferenceDataStoreFactory.createWithPath { tempFile.toOkioPath() })

    private fun vm() = UserSetupViewModel(
        prefs = prefs(),
        secureStorage = SecureStorage(),
        appSyncUseCase = FakeSyncUseCase(),
        authRepository = FakeAuthRepository(),
        dispatcher = testDispatcher,
    )

    @Test
    fun setupState_initiallyIdle() = runTest(testDispatcher) {
        assertIs<SetupState.Idle>(vm().setupState.value)
    }
}
