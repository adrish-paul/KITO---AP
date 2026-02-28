package com.kito

import android.app.Application
import com.kito.core.di.initKoin
import com.kito.core.platform.PlatformContext
import com.kito.feature.schedule.notification.createClassNotificationChannel

class KitoApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        PlatformContext.init(this)
        initKoin(this)
        createClassNotificationChannel(this)

    }
}
