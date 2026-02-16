package com.kito.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSURL
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time
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
@OptIn(ExperimentalForeignApi::class)
actual fun toast(message: String) {
    val window = UIApplication.sharedApplication.keyWindow ?: return
    
    val toastLabel = platform.UIKit.UILabel()
    toastLabel.backgroundColor = platform.UIKit.UIColor.blackColor.colorWithAlphaComponent(0.8)
    toastLabel.textColor = platform.UIKit.UIColor.whiteColor
    toastLabel.textAlignment = platform.UIKit.NSTextAlignmentCenter
    toastLabel.text = message
    toastLabel.alpha = 0.0
    toastLabel.layer.cornerRadius = 10.0
    toastLabel.clipsToBounds = true
    toastLabel.numberOfLines = 0
    
    val windowFrame = window.frame
    val width = windowFrame.useContents { size.width }
    val height = windowFrame.useContents { size.height }
    
    // Calculate size
    val toastWidth = width - 60.0
    val toastHeight = 50.0 // Minimum height
    
    // Position at bottom
    toastLabel.frame = platform.CoreGraphics.CGRectMake(
        x = 30.0,
        y = height - 100.0,
        width = toastWidth,
        height = toastHeight
    )
    
    window.addSubview(toastLabel)
    
    // Animate In
    platform.UIKit.UIView.animateWithDuration(0.5) {
        toastLabel.alpha = 1.0
    }
    
    // Animate Out
    val delay = dispatch_time(DISPATCH_TIME_NOW, 2_000_000_000)
    dispatch_after(delay, dispatch_get_main_queue()) {
        platform.UIKit.UIView.animateWithDuration(
            duration = 0.5,
            animations = {
                toastLabel.alpha = 0.0
            },
            completion = { _ ->
                toastLabel.removeFromSuperview()
            }
        )
    }
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
    val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
    
    // Try the simpler API first (sometimes works better in simulator)
    if (UIApplication.sharedApplication.canOpenURL(url)) {
        UIApplication.sharedApplication.openURL(
            url,
            options = emptyMap<Any?, Any>(),
            completionHandler = { success ->
                if (!success) {
                    println("Failed to open app settings with completion handler")
                }
            }
        )
    } else {
        println("Cannot open settings URL: $url")
    }
}

actual fun openNotificationSettings() {
    // On iOS, we can't deep link directly to notification settings reliably
    // Open the app settings page instead, where users can access notifications
    openAppSettings()
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