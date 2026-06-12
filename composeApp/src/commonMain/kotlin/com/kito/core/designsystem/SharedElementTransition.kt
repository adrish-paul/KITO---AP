package com.kito.core.designsystem

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.scaleToBounds
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay

/**
 * Duration of the card -> page container-transform expansion.
 * Kept identical for the bounds morph and the scene fade so they stay in sync.
 */
const val UTILITY_EXPAND_DURATION_MS = 400

/** Corner radius of the utility cards; the expansion starts rounded and straightens to 0. */
val UtilityCardCorner = 20.dp

/**
 * Smooth, consistent morph for the UtilityCard -> destination shared bounds.
 * Uses the app's Emphasized easing instead of the default spring so the
 * expansion timing matches the scene fade exactly.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val UtilityBoundsTransform = BoundsTransform { _, _ ->
    tween(durationMillis = UTILITY_EXPAND_DURATION_MS, easing = ExpressiveEasing.Emphasized)
}

/**
 * Per-entry transition metadata for destinations that expand from a shared element.
 *
 * Shared-element / container-transform animations must NOT be combined with a sliding
 * scene transition: the slide and the bounds morph fight each other and look broken.
 * Using a fade lets the shared element provide all the spatial motion while the rest
 * of the destination content simply cross-fades in.
 */
fun sharedElementEntryMetadata(): Map<String, Any> {
    val fade = tween<Float>(durationMillis = UTILITY_EXPAND_DURATION_MS)
    return NavDisplay.transitionSpec {
        fadeIn(fade) togetherWith fadeOut(fade)
    } + NavDisplay.popTransitionSpec {
        fadeIn(fade) togetherWith fadeOut(fade)
    } + NavDisplay.predictivePopTransitionSpec {
        fadeIn(fade) togetherWith fadeOut(fade)
    }
}

/**
 * Root container for a destination that expands out of a tapped UtilityCard.
 *
 * The whole screen is ONE shared element. [scaleToBounds] with [ContentScale.Crop]
 * makes the screen FILL the morphing bounds at every step (cropping the overflow)
 * instead of being letterboxed into the card's square aspect ratio. The corner
 * radius animates from the card's rounding down to 0 as it fills the screen, so the
 * page appears to grow straight out of the rounded utility card.
 *
 * Place this as the destination's root and put the existing screen content inside.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedExpandContainer(
    routeKey: Any,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val scope = LocalSharedTransitionScope.current
    if (scope == null) {
        Box(modifier.fillMaxSize().background(backgroundColor), content = content)
        return
    }
    val animScope = LocalNavAnimatedContentScope.current
    // 1f while collapsed (card), 0f once fully expanded — drives the corner radius.
    val cornerFraction by animScope.transition.animateFloat(
        transitionSpec = { tween(UTILITY_EXPAND_DURATION_MS, easing = ExpressiveEasing.Emphasized) },
        label = "utilityCorner"
    ) { state -> if (state == EnterExitState.Visible) 0f else 1f }

    with(scope) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "utility-card-$routeKey"),
                    animatedVisibilityScope = animScope,
                    boundsTransform = UtilityBoundsTransform,
                    resizeMode = scaleToBounds(
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.TopCenter,
                    ),
                    renderInOverlayDuringTransition = false,
                )
                .clip(RoundedCornerShape(UtilityCardCorner * cornerFraction))
                .background(backgroundColor),
            content = content
        )
    }
}
