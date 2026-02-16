package com.kito.feature.schedule.notification

import com.kito.core.database.entity.StudentSectionEntity
import platform.Foundation.*

fun classStartMillis(section: StudentSectionEntity): Long {
    val calendar = NSCalendar.currentCalendar
    val now = NSDate()
    
    // Parse class time
    val (hour, minute) = section.startTime.split(":").map { it.trim().toInt() }
    
    // Get target weekday (1=Sunday, 2=Monday, ...)
    val targetWeekday = when (section.day.uppercase()) {
        "SUN" -> 1L
        "MON" -> 2L
        "TUE" -> 3L
        "WED" -> 4L
        "THU" -> 5L
        "FRI" -> 6L
        "SAT" -> 7L
        else -> 1L // Default
    }
    
    val currentComponents = calendar.components(
        NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or NSCalendarUnitWeekday,
        fromDate = now
    )
    
    val currentWeekday = currentComponents.weekday
    
    // Calculate days until next occurrence
    var daysUntil = targetWeekday - currentWeekday
    if (daysUntil < 0) {
        daysUntil += 7
    }
    
    // Check if it's today but time has passed
    if (daysUntil == 0L) {
        val nowComponents = calendar.components(
            NSCalendarUnitHour or NSCalendarUnitMinute,
            fromDate = now
        )
        val currentHour = nowComponents.hour
        val currentMinute = nowComponents.minute
        
        if (currentHour > hour.toLong() || (currentHour == hour.toLong() && currentMinute >= minute.toLong())) {
            daysUntil = 7
        }
    }
    
    val targetDate = calendar.dateByAddingUnit(
        NSCalendarUnitDay,
        value = daysUntil,
        toDate = now,
        options = 0UL
    ) ?: now

    // Set exact time on target date
    val targetComponents = calendar.components(
        NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
        fromDate = targetDate
    )
    targetComponents.hour = hour.toLong()
    targetComponents.minute = minute.toLong()
    targetComponents.second = 0
    
    return calendar.dateFromComponents(targetComponents)?.timeIntervalSince1970?.toLong()?.times(1000) ?: 0L
}

fun formatTime(time: String): String {
    val parts = time.split(":")
    if (parts.size >= 2) {
        val hour = parts[0].toInt()
        val minute = parts[1]
        val amPm = if (hour >= 12) "PM" else "AM"
        val hour12 = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
        return "$hour12:$minute $amPm"
    }
    return time
}

fun getDayOfWeekString(offsetDays: Int = 0): String {
    val calendar = NSCalendar.currentCalendar
    val now = NSDate()
    val futureDate = if (offsetDays == 0) now else {
        calendar.dateByAddingUnit(
            NSCalendarUnitDay,
            value = offsetDays.toLong(),
            toDate = now,
            options = 0UL
        ) ?: now
    }
    
    val components = calendar.components(NSCalendarUnitWeekday, fromDate = futureDate)
    return when (components.weekday) {
        1L -> "SUN"
        2L -> "MON"
        3L -> "TUE"
        4L -> "WED"
        5L -> "THU"
        6L -> "FRI"
        7L -> "SAT"
        else -> "MON"
    }
}
