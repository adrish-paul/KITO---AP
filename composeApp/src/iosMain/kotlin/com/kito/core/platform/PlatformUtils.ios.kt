package com.kito.core.platform

import androidx.compose.runtime.Composable
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

actual fun openUrl(url: String) {
    val finalUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
        "http://$url"
    } else {
        url
    }
    val nsUrl = NSURL.URLWithString(finalUrl) ?: return
    
    // Use the modern iOS API with completion handler
    UIApplication.sharedApplication.openURL(
        nsUrl,
        options = emptyMap<Any?, Any>(),
        completionHandler = { success ->
            if (!success) {
                println("Failed to open URL: $finalUrl")
            }
        }
    )
}

actual fun createHttpEngine(): HttpClientEngine = Darwin.create()

actual fun toast(message: String) {
    // TODO: Implement iOS toast or alert
    println("Toast: $message")
}

actual fun sendEmail(to: String, subject: String, body: String) {
    // TODO: Implement iOS email composition
    println("Send Email to $to: $subject")
}

actual fun openAppSettings() {
    val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
    if (url != null) {
        UIApplication.sharedApplication.openURL(url)
    }
}

actual fun openAlarmSettings() {
    // No direct equivalent in iOS, usually just settings
    openAppSettings()
}

actual fun canScheduleExactAlarms(): Boolean = true // iOS handles notifications differently

actual suspend fun areNotificationsEnabled(): Boolean = true // TODO: Implement iOS check

@Composable
actual fun NotificationPermissionEffect(onResult: (Boolean) -> Unit) {
    // TODO: Implement iOS permission request logic
    onResult(true) 
}
