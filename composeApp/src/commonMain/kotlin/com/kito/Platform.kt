package com.kito

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

@androidx.compose.runtime.Composable
expect fun SetSystemBarAppearance(isLightForeground: Boolean)
