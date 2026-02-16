package com.kito.feature.schedule.notification

import platform.UserNotifications.UNNotificationCategory
import platform.UserNotifications.UNNotificationCategoryOptionCustomDismissAction
import platform.UserNotifications.UNUserNotificationCenter

class IosNotificationSetup {
    
    fun initialize() {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        
        // Set delegate
        val delegate = IosNotificationDelegate()
        center.delegate = delegate
        
        // Keep a strong reference to delegate if needed, though usually center holds it? 
        // Actually on iOS center.delegate is weak, so we must hold a reference.
        // We'll store it in a companion object or global variable.
        IosNotificationDelegateHolder.delegate = delegate
        
        registerCategories(center)
    }
    
    private fun registerCategories(center: UNUserNotificationCenter) {
        val upcomingCategory = UNNotificationCategory.categoryWithIdentifier(
            identifier = "class_upcoming",
            actions = emptyList<Any>(),
            intentIdentifiers = emptyList<String>(),
            options = UNNotificationCategoryOptionCustomDismissAction
        )
        
        val ongoingCategory = UNNotificationCategory.categoryWithIdentifier(
            identifier = "class_ongoing",
            actions = emptyList<Any>(),
            intentIdentifiers = emptyList<String>(),
            options = UNNotificationCategoryOptionCustomDismissAction
        )
        
        val refreshCategory = UNNotificationCategory.categoryWithIdentifier(
            identifier = "midnight_refresh",
            actions = emptyList<Any>(),
            intentIdentifiers = emptyList<String>(),
            options = 0UL
        )
        
        center.setNotificationCategories(setOf(upcomingCategory, ongoingCategory, refreshCategory))
    }
}

// Object to hold strong reference to delegate
private object IosNotificationDelegateHolder {
    var delegate: IosNotificationDelegate? = null
}
