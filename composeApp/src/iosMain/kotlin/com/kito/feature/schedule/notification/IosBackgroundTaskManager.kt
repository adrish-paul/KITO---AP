package com.kito.feature.schedule.notification

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.BackgroundTasks.BGAppRefreshTask
import platform.BackgroundTasks.BGTask
import platform.BackgroundTasks.BGTaskScheduler
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.NSLog
import platform.darwin.NSObject

object IosBackgroundTaskManager {

    @OptIn(ExperimentalForeignApi::class)
    fun register() {
        val identifier = "com.kito.app.refresh"
        
        // Register the background task
        // We use 'null' for the queue to use the default background queue
        val success = BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
            identifier,
            usingQueue = null
        ) { task ->
            if (task is BGAppRefreshTask) {
                handleAppRefresh(task)
            } else {
                // Should not happen if registered correctly
                task?.setTaskCompletedWithSuccess(false)
            }
        }
        
        if (!success) {
            println("Failed to register background task: $identifier")
        } else {
            println("Registered background task: $identifier")
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    fun schedule() {
        val identifier = "com.kito.app.refresh"
        val request = BGAppRefreshTaskRequest(identifier)
        
        // Earliest begin date: Tomorrow at 1 AM
        // Or simply 1 AM tomorrow?
        // User asked for "everyday 1 AM".
        // Let's calculate next 1 AM.
        
        val calendar = NSCalendar.currentCalendar
        val now = NSDate()
        
        // Get tomorrow
        val tomorrow = calendar.dateByAddingUnit(
            NSCalendarUnitDay,
            value = 1,
            toDate = now,
            options = 0UL
        ) ?: now
        
        // Set to 1 AM
        val components = calendar.components(
            NSCalendarUnitDay or NSCalendarUnitMonth or NSCalendarUnitYear,
            fromDate = tomorrow
        )
        components.hour = 1
        components.minute = 0
        
        val triggerDate = calendar.dateFromComponents(components)
        request.earliestBeginDate = triggerDate
        
        try {
            BGTaskScheduler.sharedScheduler.submitTaskRequest(request, error = null)
            println("Scheduled background refresh for $triggerDate")
        } catch (e: Exception) {
            println("Failed to submit background task request: ${e.message}")
        }
    }

    private fun handleAppRefresh(task: BGAppRefreshTask) {
        // Schedule the next refresh immediately
        schedule()
        
        // Set expiration handler
        task.expirationHandler = {
            // Task took too long, cancel work
            println("Background task expired")
            task.setTaskCompletedWithSuccess(false)
        }
        
        // Perform the work
        CoroutineScope(Dispatchers.Main).launch {
            println("Running background refresh...")
            try {
                val controller = IosNotificationController.instance
                if (controller != null) {
                    controller.scheduleUpcomingClasses()
                    println("Background refresh successful")
                    task.setTaskCompletedWithSuccess(true)
                } else {
                    println("IosNotificationController instance not found")
                    task.setTaskCompletedWithSuccess(false)
                }
            } catch (e: Exception) {
                println("Error during background refresh: ${e.message}")
                task.setTaskCompletedWithSuccess(false)
            }
        }
    }

}
