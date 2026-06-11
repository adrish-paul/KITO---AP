package com.kito.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.kito.core.database.AppDB
import com.kito.core.datastore.ProtoDatastoreRepository
import com.kito.core.network.supabase.createSupabaseClient
import com.kito.core.platform.AppSyncTrigger
import com.kito.core.platform.ConnectivityObserver
import com.kito.core.platform.ESP
import com.kito.core.platform.SecureStorage
import com.kito.feature.schedule.notification.NotificationController
import com.kito.feature.schedule.notification.NotificationPipelineController
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

private const val DATASTORE_NAME = "app_prefs"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

/**
 * Android-specific Koin module — contains platform-dependent bindings
 * that cannot live in commonMain.
 */
val androidModule = module {
    // DataStore
    single { androidContext().dataStore }

    // Room Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDB::class.java,
            "kito_db"
        ).build()
    }

    // DAOs
    single { get<AppDB>().attendanceDao() }
    single { get<AppDB>().studentDao() }
    single { get<AppDB>().sectionDao() }
    single { get<AppDB>().studentSectionDao() }

    // Supabase HttpClient (created via common function)
    single <HttpClient> { createSupabaseClient() }

    // Platform services that need Android Context
    single { ConnectivityObserver(androidContext(), get(named("ApplicationScope"))) }
    single { SecureStorage(androidContext()) }
    single { ESP(androidContext()) }
    single { AppSyncTrigger(androidContext()) }
    single<ProtoDatastoreRepository>()
    
    // Notification Controller
    single<NotificationController> { NotificationPipelineController.get(androidContext()) }
}

/**
 * Android-only ViewModels (screens that stay in androidMain).
 * SettingsViewModel is moving to commonMain but kept here temporarily until migration is complete.
 */
val androidViewModelModule = module {
}

fun initKoin(appContext: Context) {
    startKoin {
        androidContext(appContext)
        modules(commonModule, commonViewModelModule, androidModule, androidViewModelModule, com.kito.feature.attendance.di.attendanceModule, com.kito.feature.faculty.di.facultyModule, com.kito.feature.schedule.di.scheduleModule, com.kito.feature.home.di.homeModule, com.kito.feature.calendar.di.calendarModule, com.kito.feature.exam.di.examModule, com.kito.feature.gpa.di.gpaModule, com.kito.feature.friendview.di.friendViewModule, com.kito.feature.settings.di.settingsModule, com.kito.feature.khaoogully.di.khaoogullyModule, com.kito.feature.auth.di.authModule)
    }
}
