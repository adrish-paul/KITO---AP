package com.kito.core.platform

import androidx.compose.runtime.Composable
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import java.awt.Desktop
import java.net.URI

actual fun openUrl(url: String) {
    if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(URI(url))
}

actual fun createHttpEngine(): HttpClientEngine = OkHttp.create()

actual fun toast(message: String) = println("[Toast] $message")

actual fun sendEmail(to: String, subject: String, body: String) {
    if (Desktop.isDesktopSupported()) Desktop.getDesktop()
        .mail(URI("mailto:$to?subject=${subject.encodeToByteArray()}&body=${body.encodeToByteArray()}"))
}

actual fun openAppSettings() {}

actual fun openNotificationSettings() {}

actual fun openAlarmSettings() {}

actual fun canScheduleExactAlarms(): Boolean = false

actual suspend fun areNotificationsEnabled(): Boolean = false

@Composable
actual fun NotificationPermissionEffect(onResult: (Boolean) -> Unit) {}
