package com.kito.feature.auth.presentation.onboarding

sealed interface OnboardingUiEvent {
    object OnboardingCompleted : OnboardingUiEvent
}
