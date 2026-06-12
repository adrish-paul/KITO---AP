package com.kito.core.designsystem

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf

val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/**
 * The [AnimatedContentScope] of the ROOT NavDisplay.
 *
 * The utility cards live inside a nested NavDisplay (the tab graph), so their
 * own `LocalNavAnimatedContentScope` belongs to the tab switcher and is idle
 * when we navigate the root back stack. A shared-element transition only runs
 * when both ends are bound to the same AnimatedContent transition, so the card
 * must use the root scope provided here — the same scope the destination
 * screens (root entries) use.
 */
val LocalRootNavAnimatedContentScope = compositionLocalOf<AnimatedContentScope?> { null }
