package com.kito.feature.auth

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.kito.core.datastore.PrefsRepository
import com.kito.core.platform.SecureStorage
import com.kito.feature.auth.presentation.usersetup.SetupState
import com.kito.feature.auth.presentation.usersetup.UserSetupViewModel
import com.kito.testing.FakeAuthRepository
import com.kito.testing.FakeSyncUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class UserSetupViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val tempPath = "auth_prefs_test.preferences_pb".toPath()

    @BeforeTest fun setup() { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun teardown() { 
        Dispatchers.resetMain()
        try {
            FileSystem.SYSTEM.delete(tempPath)
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun prefs() = PrefsRepository(
        PreferenceDataStoreFactory.createWithPath(
            scope = CoroutineScope(testDispatcher + SupervisorJob()),
            produceFile = { tempPath }
        )
    )

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
