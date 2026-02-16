package com.kito.feature.schedule.notification

import com.kito.core.database.entity.StudentSectionEntity
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

import platform.UserNotifications.UNTimeIntervalNotificationTrigger

class IosClassNotificationScheduler {

    fun scheduleMidnightRefresh() {
        val content = UNMutableNotificationContent().apply {
            setTitle("") // Silent/invisible
            setCategoryIdentifier("midnight_refresh")
        }
        
        val components = NSDateComponents().apply {
            hour = 0
            minute = 0
        }
        
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            components,
            repeats = true
        )
        
        val request = UNNotificationRequest.requestWithIdentifier(
            "midnight_refresh",
            content,
            trigger
        )
        
        UNUserNotificationCenter.currentNotificationCenter()
            .addNotificationRequest(request, withCompletionHandler = null)
    }

    @OptIn(ExperimentalForeignApi::class)
    fun scheduleClass(classData: StudentSectionEntity, type: ClassNotificationType) {
        val startTimeMillis = classStartMillis(classData)
        val triggerAtMillis = when (type) {
            ClassNotificationType.UPCOMING -> startTimeMillis - 10 * 60 * 1000 // 10 mins before
            ClassNotificationType.ONGOING -> startTimeMillis
        }

        // Generate a unique identifier
        val identifier = "${classData.subject}_${type.name}_${triggerAtMillis}"
        
        scheduleNotification(
            triggerAtMillis = triggerAtMillis,
            identifier = identifier,
            type = type,
            classData = classData
        )
    }
    
    fun cancelAll() {
        UNUserNotificationCenter.currentNotificationCenter()
            .removeAllPendingNotificationRequests()
    }

    private fun scheduleNotification(
        triggerAtMillis: Long,
        identifier: String,
        type: ClassNotificationType,
        classData: StudentSectionEntity
    ) {
        val content = createNotificationContent(type, classData)
        val trigger = createTrigger(triggerAtMillis)
        
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = identifier,
            content = content,
            trigger = trigger
        )
        
        // For logging: Convert Unix timestamp to NSDate (subtract offset for correct display)
        val logSeconds = triggerAtMillis / 1000.0
        val offset = 978307200.0
        val logDate = NSDate(logSeconds - offset)
        println("Scheduling notification: $identifier at $logDate (UTC)")
        
        UNUserNotificationCenter.currentNotificationCenter()
            .addNotificationRequest(request, withCompletionHandler = null)
    }

    private fun createNotificationContent(
        type: ClassNotificationType,
        classData: StudentSectionEntity
    ): UNMutableNotificationContent {
        val content = UNMutableNotificationContent()
        content.setTitle(when (type) {
            ClassNotificationType.UPCOMING -> "Upcoming Class"
            ClassNotificationType.ONGOING -> "Class Started"
        })
        
        val timeRange = "${formatTime(classData.startTime)} - ${formatTime(classData.endTime)}"
        val room = classData.room?.let { " • Room $it" } ?: ""
        content.setBody("${classData.subject}$room\n$timeRange")
        
        content.setSound(UNNotificationSound.defaultSound)
        content.setCategoryIdentifier("class_${type.name.lowercase()}")
        content.setUserInfo(mapOf("deepLink" to "kito://schedule"))
        
        return content
    }

    private fun createTrigger(triggerAtMillis: Long): UNCalendarNotificationTrigger {
        // NSDate(Double) expects timeIntervalSinceReferenceDate (seconds since Jan 1 2001)
        // Unix timestamp is seconds since Jan 1 1970, so SUBTRACT the 31-year offset
        val secondsSince1970 = triggerAtMillis / 1000.0
        val offset = 978307200.0  // Seconds between Jan 1 1970 and Jan 1 2001
        val date = NSDate(secondsSince1970 - offset)  // SUBTRACT to convert correctly
        
        val calendar = NSCalendar.currentCalendar
        val components = calendar.components(
            NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or
            NSCalendarUnitHour or NSCalendarUnitMinute,
            fromDate = date
        )
        return UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            components,
            repeats = false
        )
    }
}
