package com.kito.feature.schedule.notification

import com.kito.core.database.repository.StudentSectionRepository
import com.kito.core.datastore.PrefsRepository
import com.kito.core.platform.areNotificationsEnabled
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.first

class IosNotificationController(
    private val scheduler: IosClassNotificationScheduler,
    private val prefsRepository: PrefsRepository,
    private val studentSectionRepository: StudentSectionRepository
) : NotificationController {

    init {
        instance = this
    }

    override suspend fun sync() {
        val userEnabled = prefsRepository.notificationStateFlow.first()
        val systemAllowed = areNotificationsEnabled()
        
        println("IosNotificationController: Syncing... UserEnabled=$userEnabled, SystemAllowed=$systemAllowed")
        
        if (userEnabled && systemAllowed) {
            scheduler.scheduleMidnightRefresh() // Ensure daily refresh is scheduled
            scheduleUpcomingClasses() // Schedule classes for next 7 days immediately
        } else {
            println("IosNotificationController: Notifications disabled or permission denied")
            if (userEnabled && !systemAllowed) {
                // If user wants notifications but system disabled them, update preference
                prefsRepository.setNotificationState(false)
            }
            scheduler.cancelAll()
        }
    }
    
    suspend fun scheduleUpcomingClasses() {
        val rollNo = prefsRepository.userRollFlow.first()
        println("IosNotificationController: Scheduling for RollNo: $rollNo")
        if (rollNo.isBlank()) return
        
        // Schedule for the next 7 days (Today + 6 days) to ensure reliability
        // This handles cases where the user doesn't open the app daily
        for (i in 0..6) {
            val targetDay = getDayOfWeekString(i) // i=0 is Today, i=1 is Tomorrow...
            println("IosNotificationController: Scheduling for day: $targetDay (offset $i)")
            
            val dayClasses = studentSectionRepository.getScheduleForStudent(rollNo, targetDay).first()
            println("IosNotificationController: Found ${dayClasses.size} classes for $targetDay")
            
            dayClasses.forEach { classData ->
                // Schedule UPCOMING notification (10 min before)
                scheduler.scheduleClass(classData, ClassNotificationType.UPCOMING)
                
                // Schedule ONGOING notification (at start time)
                scheduler.scheduleClass(classData, ClassNotificationType.ONGOING)
            }
        }
        

    }
    
    companion object {
        // Singleton instance will be provided by DI
        var instance: IosNotificationController? = null
    }
}
