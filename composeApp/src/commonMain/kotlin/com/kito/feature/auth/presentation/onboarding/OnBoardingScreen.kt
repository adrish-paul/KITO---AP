package com.kito.feature.auth.presentation.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.koinInject

@Composable
fun OnBoardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnBoardingViewModel = koinInject(),
) {
    LaunchedEffect(Unit) {
        viewModel.onboardingEvents.collect { event ->
            when (event) {
                is OnboardingUiEvent.OnboardingCompleted -> {
                    onOnboardingComplete()
                }
            }
        }
    }

    OnBoardingContent(
        onOnboardingDone = {
            viewModel.onEvent(OnboardingEvent.CompleteOnboarding)
        }
    )
}