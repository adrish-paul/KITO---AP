
package com.kito.core.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.kito.core.database.AppDB
import com.kito.core.datastore.IosPrefsRepository
import com.kito.core.network.supabase.createSupabaseClient
import com.kito.core.platform.AppSyncTrigger
import com.kito.core.platform.ConnectivityObserver
import com.kito.core.platform.SecureStorage
import com.kito.feature.schedule.notification.IosClassNotificationScheduler
import com.kito.feature.schedule.notification.IosNotificationController
import com.kito.feature.schedule.notification.NotificationController
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import org.koin.plugin.module.dsl.single
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
val iosModule = module {
    // DataStore
    single {
        val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        val path = requireNotNull(documentDirectory).path + "/app_prefs.preferences_pb"
        
        androidx.datastore.preferences.core.PreferenceDataStoreFactory.createWithPath(
            produceFile = { path.toPath() }
        )
    }

    // Room Database
    single {
        val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        val dbFilePath = requireNotNull(documentDirectory).path + "/kito_db.sqlite"
        
        Room.databaseBuilder<AppDB>(
            name = dbFilePath,
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    // DAOs
    single { get<AppDB>().attendanceDao() }
    single { get<AppDB>().studentDao() }
    single { get<AppDB>().sectionDao() }
    single { get<AppDB>().studentSectionDao() }
    
    // Supabase HttpClient
    single <io.ktor.client.HttpClient> { createSupabaseClient() }

    // Platform Implementations
    single<ConnectivityObserver>()
    single<AppSyncTrigger>()
    single<SecureStorage>()
    single<IosPrefsRepository>()

    // Notification Controller
    single<IosClassNotificationScheduler>()
    single<IosNotificationController>() bind NotificationController::class
}



fun initKoin() {
    try {
        KoinPlatform.getKoin()
    } catch (_: Exception) {
        startKoin {
            modules(commonModule, commonViewModelModule, iosModule, com.kito.feature.attendance.di.attendanceModule, com.kito.feature.faculty.di.facultyModule, com.kito.feature.schedule.di.scheduleModule, com.kito.feature.home.di.homeModule, com.kito.feature.calendar.di.calendarModule, com.kito.feature.exam.di.examModule, com.kito.feature.gpa.di.gpaModule, com.kito.feature.friendview.di.friendViewModule, com.kito.feature.settings.di.settingsModule, com.kito.feature.khaoogully.di.khaoogullyModule, com.kito.feature.auth.di.authModule)
        }
    }
}
