package com.kito

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

@androidx.compose.runtime.Composable
actual fun SetSystemBarAppearance(isLightForeground: Boolean) {
    // Handled by iOS / unchanged
}
