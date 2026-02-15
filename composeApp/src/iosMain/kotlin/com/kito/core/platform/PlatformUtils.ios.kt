package com.kito.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import platform.UIKit.*
import platform.UserNotifications.*
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time
import platform.darwin.DISPATCH_TIME_NOW
import kotlin.coroutines.resume

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

@OptIn(ExperimentalForeignApi::class)
actual fun toast(message: String) {
    val alert = UIAlertController.alertControllerWithTitle(
        title = null,
        message = message,
        preferredStyle = UIAlertControllerStyleAlert
    )
    
    val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
    rootViewController?.presentViewController(
        alert,
        animated = true,
        completion = {
            // Auto-dismiss after 2 seconds
            val delay = dispatch_time(DISPATCH_TIME_NOW, 2_000_000_000) // 2 seconds in nanoseconds
            dispatch_after(delay, dispatch_get_main_queue()) {
                alert.dismissViewControllerAnimated(true, completion = null)
            }
        }
    )
}

actual fun sendEmail(to: String, subject: String, body: String) {
    // Build mailto URL as fallback or primary method
    val mailtoUrl = "mailto:$to?subject=${subject.encodeURLParameter()}&body=${body.encodeURLParameter()}"
    val nsUrl = NSURL.URLWithString(mailtoUrl) ?: return
    
    UIApplication.sharedApplication.openURL(
        nsUrl,
        options = emptyMap<Any?, Any>(),
        completionHandler = { success ->
            if (!success) {
                println("Failed to open mail client for: $to")
            }
        }
    )
}

// Helper function to URL encode strings
private fun String.encodeURLParameter(): String {
    return this.replace(" ", "%20")
        .replace("\n", "%0A")
        .replace("&", "%26")
        .replace("=", "%3D")
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

actual suspend fun areNotificationsEnabled(): Boolean = suspendCancellableCoroutine { continuation ->
    UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
        val isEnabled = settings?.authorizationStatus == UNAuthorizationStatusAuthorized ||
                       settings?.authorizationStatus == UNAuthorizationStatusProvisional
        continuation.resume(isEnabled)
    }
}

@Composable
actual fun NotificationPermissionEffect(onResult: (Boolean) -> Unit) {
    LaunchedEffect(Unit) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        
        // First check current status
        center.getNotificationSettingsWithCompletionHandler { settings ->
            when (settings?.authorizationStatus) {
                UNAuthorizationStatusAuthorized, UNAuthorizationStatusProvisional -> {
                    onResult(true)
                }
                UNAuthorizationStatusDenied -> {
                    onResult(false)
                }
                UNAuthorizationStatusNotDetermined -> {
                    // Request permission
                    val options = UNAuthorizationOptionAlert or 
                                 UNAuthorizationOptionSound or 
                                 UNAuthorizationOptionBadge
                    
                    center.requestAuthorizationWithOptions(
                        options = options,
                        completionHandler = { granted, error ->
                            onResult(granted)
                            if (error != null) {
                                println("Notification permission error: ${error.localizedDescription}")
                            }
                        }
                    )
                }
                else -> onResult(false)
            }
        }
    }
}