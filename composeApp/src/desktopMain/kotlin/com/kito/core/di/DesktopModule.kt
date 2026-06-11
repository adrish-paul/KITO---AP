package com.kito.core.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.kito.core.database.AppDB
import com.kito.core.network.supabase.createSupabaseClient
import com.kito.core.platform.AppSyncTrigger
import com.kito.core.platform.ConnectivityObserver
import com.kito.core.platform.SecureStorage
import com.kito.feature.schedule.notification.NotificationController
import okio.Path.Companion.toOkioPath
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.io.File

val desktopModule = module {
    single {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { File(System.getProperty("user.home"), ".kito/app_prefs.preferences_pb").also { it.parentFile.mkdirs() }.toOkioPath() }
        )
    }
    single {
        Room.databaseBuilder<AppDB>(name = File(System.getProperty("user.home"), ".kito/kito_desktop.db").also { it.parentFile?.mkdirs() }.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .build()
    }
    single { get<AppDB>().attendanceDao() }
    single { get<AppDB>().studentDao() }
    single { get<AppDB>().sectionDao() }
    single { get<AppDB>().studentSectionDao() }
    single<io.ktor.client.HttpClient> { createSupabaseClient() }
    single { ConnectivityObserver() }
    single { SecureStorage() }
    single { AppSyncTrigger() }
    single<NotificationController> { object : NotificationController { override suspend fun sync() {} } }
}

fun initKoin() {
    startKoin {
        modules(commonModule, commonViewModelModule, desktopModule, com.kito.feature.attendance.di.attendanceModule, com.kito.feature.faculty.di.facultyModule, com.kito.feature.schedule.di.scheduleModule, com.kito.feature.home.di.homeModule, com.kito.feature.calendar.di.calendarModule, com.kito.feature.exam.di.examModule, com.kito.feature.gpa.di.gpaModule, com.kito.feature.friendview.di.friendViewModule, com.kito.feature.settings.di.settingsModule, com.kito.feature.khaoogully.di.khaoogullyModule, com.kito.feature.auth.di.authModule)
    }
}
