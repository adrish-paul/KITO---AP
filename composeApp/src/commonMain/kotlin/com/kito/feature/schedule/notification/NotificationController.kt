package com.kito.feature.schedule.notification

import org.koin.core.annotation.Provided

@Provided
interface NotificationController {
    suspend fun sync()
}
