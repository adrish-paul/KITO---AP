package com.kito.feature.auth.presentation.onboarding

sealed interface OnboardingEvent {
    object CompleteOnboarding : OnboardingEvent
}
