package com.kito.feature.attendance.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.kito.core.designsystem.UIColors

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun InstagramPullIndicator(
    pullState: PullToRefreshState,
    isRefreshing: Boolean
) {
    val uiColors = UIColors()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val fraction = pullState.distanceFraction.coerceIn(0f, 1f)

    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 46.dp

    var thresholdHapticTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(pullState.distanceFraction) {
        if (pullState.distanceFraction >= 1f && !thresholdHapticTriggered) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            thresholdHapticTriggered = true
        }
        if (pullState.distanceFraction < 1f) {
            thresholdHapticTriggered = false
        }
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    if (fraction > 0f || isRefreshing) {
        Box(
            modifier = Modifier
                .zIndex(2f)
                .fillMaxWidth()
                .height(48.dp)
                .graphicsLayer {
                    val maxOffset = with(density) { 12.dp.toPx() }
                    translationY = with(density) {
                        topInset.toPx() + if (isRefreshing) maxOffset else fraction * maxOffset
                    }
                    alpha = if (isRefreshing) 1f else fraction
                    scaleX = if (isRefreshing) 1f else 0.8f + (0.2f * fraction)
                    scaleY = scaleX
                },
            contentAlignment = Alignment.Center
        ) {
            LinearWavyProgressIndicator(
                color = uiColors.accentOrangeStart,
                trackColor = uiColors.progressAccent,
                modifier = Modifier.fillMaxWidth(),
                waveSpeed = 5.dp,
                wavelength = 70.dp,
            )
        }
    }
}
