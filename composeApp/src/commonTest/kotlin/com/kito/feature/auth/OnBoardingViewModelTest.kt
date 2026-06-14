package com.kito.feature.auth

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.kito.core.datastore.PrefsRepository
import com.kito.feature.auth.presentation.onboarding.OnBoardingViewModel
import com.kito.feature.auth.presentation.onboarding.OnboardingUiEvent
import com.kito.feature.auth.presentation.onboarding.OnboardingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class OnBoardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val tempPath = "onboarding_prefs_test.preferences_pb".toPath()
    private lateinit var prefsRepository: PrefsRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        prefsRepository = PrefsRepository(
            PreferenceDataStoreFactory.createWithPath(
                scope = CoroutineScope(testDispatcher + SupervisorJob()),
                produceFile = { tempPath }
            )
        )
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
        try {
            FileSystem.SYSTEM.delete(tempPath)
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun createViewModel() = OnBoardingViewModel(
        prefs = prefsRepository,
        dispatcher = testDispatcher,
    )

    @Test
    fun completeOnboarding_setsOnboardingDoneInPrefsAndEmitsCompletedEvent() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        val events = mutableListOf<OnboardingUiEvent>()

        val job = launch {
            viewModel.onboardingEvents.collect { events.add(it) }
        }

        viewModel.onEvent(OnboardingEvent.CompleteOnboarding)
        advanceUntilIdle()

        // Verify it sets onboarding done in datastore preferences
        assertTrue(prefsRepository.onBoardingFlow.first { it })

        // Verify it emits OnboardingCompleted event
        assertEquals(1, events.size)
        assertEquals(OnboardingUiEvent.OnboardingCompleted, events.first())

        job.cancel()
    }
}
