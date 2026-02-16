package com.kito.feature.schedule.notification

import com.kito.core.platform.openUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationPresentationOptionBanner
import platform.UserNotifications.UNNotificationPresentationOptionList
import platform.UserNotifications.UNNotificationPresentationOptionNone
import platform.UserNotifications.UNNotificationPresentationOptionSound
import platform.UserNotifications.UNNotificationPresentationOptions
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

class IosNotificationDelegate : NSObject(), UNUserNotificationCenterDelegateProtocol {
    
    // Handle foreground notifications
    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: UNNotification,
        withCompletionHandler: (UNNotificationPresentationOptions) -> Unit
    ) {
        val identifier = willPresentNotification.request.identifier
        
        if (identifier == "midnight_refresh") {
            // It's the silent daily trigger
            refreshSchedule()
            // Don't show to user
            withCompletionHandler(UNNotificationPresentationOptionNone)
        } else {
            // Regular notification - show even if app is in foreground
            withCompletionHandler(
                UNNotificationPresentationOptionBanner or 
                UNNotificationPresentationOptionSound or
                UNNotificationPresentationOptionList
            )
        }
    }

    // Handle notification tap
    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        didReceiveNotificationResponse: UNNotificationResponse,
        withCompletionHandler: () -> Unit
    ) {
        val userInfo = didReceiveNotificationResponse.notification.request.content.userInfo
        val deepLink = userInfo["deepLink"] as? String
        
        if (deepLink != null) {
            // Navigate to schedule screen
            openUrl(deepLink)
        }
        
        // Also refresh schedule to ensure ONGOING/UPCOMING state is correct
        refreshSchedule()
        
        withCompletionHandler()
    }
    
    private fun refreshSchedule() {
        CoroutineScope(Dispatchers.Main).launch {
            val controller = IosNotificationController.instance
            if (controller != null) {
                controller.scheduleUpcomingClasses()
            }
        }
    }
}
