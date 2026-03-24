package com.kito

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.RELEASE}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

@androidx.compose.runtime.Composable
actual fun SetSystemBarAppearance(isLightForeground: Boolean) {
    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.DisposableEffect(isLightForeground) {
            val window = (view.context as android.app.Activity).window
            val controller = androidx.core.view.WindowCompat.getInsetsController(window, view)
            
            val wasLight = controller.isAppearanceLightStatusBars
            controller.isAppearanceLightStatusBars = !isLightForeground
            
            onDispose {
                controller.isAppearanceLightStatusBars = wasLight
            }
        }
    }
}
