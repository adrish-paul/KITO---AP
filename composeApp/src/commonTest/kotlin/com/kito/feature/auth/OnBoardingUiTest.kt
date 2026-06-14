package com.kito.feature.auth

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import com.kito.feature.auth.presentation.onboarding.OnBoardingContent
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class OnBoardingUiTest {

    @Test
    fun onboarding_content_renders() = runComposeUiTest {
        setContent {
            OnBoardingContent(onOnboardingDone = {})
        }
        onNodeWithTag("onboarding_content").assertIsDisplayed()
    }
}
