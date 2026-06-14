package com.kito.feature.auth.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kito.core.datastore.PrefsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class OnBoardingViewModel(
    private val prefs: PrefsRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val _onboardingEvents = MutableSharedFlow<OnboardingUiEvent>()
    val onboardingEvents: SharedFlow<OnboardingUiEvent> = _onboardingEvents

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.CompleteOnboarding -> completeOnboarding()
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch(dispatcher) {
            prefs.setOnboardingDone()
            _onboardingEvents.emit(OnboardingUiEvent.OnboardingCompleted)
        }
    }
}