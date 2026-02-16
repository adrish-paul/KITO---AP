package com.kito

import androidx.compose.ui.window.ComposeUIViewController
import com.kito.core.di.initKoin
import com.kito.core.presentation.theme.KitoTheme
import com.kito.feature.app.presentation.MainUI

import androidx.compose.runtime.LaunchedEffect
import com.kito.feature.schedule.notification.NotificationController
import org.koin.mp.KoinPlatform

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
        com.kito.feature.schedule.notification.IosNotificationSetup().initialize()
    }
) {
    LaunchedEffect(Unit) {
        val controller = KoinPlatform.getKoin().get<NotificationController>()
        controller.sync()
    }
    
    KitoTheme {
        MainUI()
    }
}
