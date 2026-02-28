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

import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIColor
import platform.UIKit.UILabel
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIView
import platform.UIKit.UIBlurEffect
import platform.UIKit.UIBlurEffectStyle
import platform.UIKit.UIVisualEffectView

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

actual fun createHttpEngine(): HttpClientEngine = Darwin.create {
    configureSession {
        // Disable native cookie handling to let Ktor's HttpCookies plugin handle it exclusively
        // This prevents conflicts and ensures our custom ClearableCookiesStorage is the single source of truth
        HTTPCookieAcceptPolicy = platform.Foundation.NSHTTPCookieAcceptPolicy.NSHTTPCookieAcceptPolicyNever
        HTTPShouldSetCookies = false
    }
}


// Internal hook for Swift to provide its own Toast implementation
var swiftToastHandler: ((String) -> Unit)? = null

@OptIn(ExperimentalForeignApi::class)
actual fun toast(message: String) {
    // If Swift has registered a handler, use it (Generic Native SwiftUI implementation)
    val handler = swiftToastHandler
    if (handler != null) {
        // Ensure we dispatch to main thread, as Kotlin might be calling this from background
        platform.darwin.dispatch_async(platform.darwin.dispatch_get_main_queue()) {
            handler(message)
        }
        return
    }

    // Fallback to UIKit implementation (e.g. for simple usage or if handler not set)
    val window = UIApplication.sharedApplication.keyWindow ?: return
    
    // Create Blur Effect - Adaptive "Liquid Glass"
    // Use UIBlurEffectStyle.UIBlurEffectStyleSystemUltraThinMaterial for a modern, glass-like look
    // that adapts to light/dark mode.
    val blurEffect = UIBlurEffect.effectWithStyle(UIBlurEffectStyle.UIBlurEffectStyleSystemThickMaterial)
    val visualEffectView = UIVisualEffectView(effect = blurEffect)
    
    // Liquid Glass Styling
    visualEffectView.layer.cornerRadius = 25.0 // Pill shape
    visualEffectView.clipsToBounds = true
    visualEffectView.alpha = 0.0
    
    // Add subtle shadow for depth
    visualEffectView.layer.shadowColor = UIColor.blackColor.CGColor
    visualEffectView.layer.shadowOpacity = 0.2f
    visualEffectView.layer.shadowRadius = 10.0
    visualEffectView.layer.shadowOffset = platform.CoreGraphics.CGSizeMake(0.0, 5.0)

    // Configure Label
    val toastLabel = UILabel()
    toastLabel.text = message
    // Use label color so it adapts (black on light mode, white on dark mode) 
    toastLabel.textColor = UIColor.whiteColor // Fallback to white for now as it works well with thick material dark/light
    toastLabel.textAlignment = NSTextAlignmentCenter
    toastLabel.numberOfLines = 0
    toastLabel.backgroundColor = UIColor.clearColor
    
    // Add Label to Visual Effect View
    visualEffectView.contentView.addSubview(toastLabel)

    val windowFrame = window.frame
    val width = windowFrame.useContents { size.width }
    val height = windowFrame.useContents { size.height }
    
    // Calculate size
    val toastWidth = width - 60.0
    val toastHeight = 50.0 // You might want dynamic height calculation here
    
    // Position the Visual Effect View
    visualEffectView.setFrame(CGRectMake(
        x = 30.0,
        y = height - 100.0,
        width = toastWidth,
        height = toastHeight
    ))
    
    // Position the Label inside the Visual Effect View (fill entire view)
    toastLabel.setFrame(visualEffectView.bounds)
    
    window.addSubview(visualEffectView)
    
    // Animate In
    UIView.animateWithDuration(0.5) {
        visualEffectView.alpha = 1.0
    }
    
    // Animate Out
    val delay = dispatch_time(DISPATCH_TIME_NOW, 2_000_000_000)
    dispatch_after(delay, dispatch_get_main_queue()) {
        UIView.animateWithDuration(
            duration = 0.5,
            animations = {
                visualEffectView.alpha = 0.0
            },
            completion = { _ ->
                visualEffectView.removeFromSuperview()
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