package com.kito

import androidx.compose.runtime.Composable

class DesktopPlatform : Platform {
    override val name: String = "Desktop JVM"
}

actual fun getPlatform(): Platform = DesktopPlatform()

@Composable
actual fun SetSystemBarAppearance(isLightForeground: Boolean) {}
