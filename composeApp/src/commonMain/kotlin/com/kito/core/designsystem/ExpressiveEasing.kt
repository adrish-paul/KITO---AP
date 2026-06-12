package com.kito.core.designsystem

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

/**
 * Material Design 3 Expressive easing functions.
 * Pure Compose implementation — no android.graphics.Path.
 */
object ExpressiveEasing {
    val Emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    // ⏩ Emphasized Accelerate: starts slow, ends fast
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    // 🛑 Emphasized Decelerate: starts fast, ends slow
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
}
