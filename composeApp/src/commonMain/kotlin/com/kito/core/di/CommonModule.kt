package com.kito.core.di

import com.kito.core.database.repository.AttendanceRepository
import com.kito.core.database.repository.SectionRepository
import com.kito.core.database.repository.StudentRepository
import com.kito.core.database.repository.StudentSectionRepository
import com.kito.core.datastore.PrefsRepository
import com.kito.core.network.supabase.SupabaseRepository
import com.kito.core.presentation.components.AppSyncUseCase
import com.kito.core.presentation.components.StartupSyncGuard
import com.kito.feature.app.presentation.AppViewModel
import com.kito.feature.attendance.presentation.AttendanceListScreenViewModel
import com.kito.feature.auth.presentation.UserSetupViewModel
import com.kito.feature.exam.presentation.UpcomingExamViewModel
import com.kito.feature.faculty.presentation.FacultyDetailViewModel
import com.kito.feature.faculty.presentation.FacultyScreenViewModel
import com.kito.feature.home.presentation.HomeViewModel
import com.kito.feature.schedule.presentation.ScheduleScreenViewModel
import com.kito.feature.settings.presentation.SettingsViewModel
import com.kito.sap.SapPortalClient
import com.kito.sap.SapRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val commonModule = module {

    single(named("ApplicationScope")) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    singleOf(::SapPortalClient)
    singleOf(::SapRepository)
    singleOf(::SupabaseRepository)
    singleOf(::AttendanceRepository)
    singleOf(::SectionRepository)
    singleOf(::StudentRepository)
    singleOf(::StudentSectionRepository)
    singleOf(::PrefsRepository)
    singleOf(::StartupSyncGuard)
    singleOf(::AppSyncUseCase)
}

val commonViewModelModule = module {

    single { AppViewModel(get(), get()) }
    singleOf(::UserSetupViewModel)
    single { UpcomingExamViewModel(get(), get()) }
    single { FacultyScreenViewModel(get(), get()) }
    single { FacultyDetailViewModel(get()) }
    single { ScheduleScreenViewModel(get(), get()) }
    single { SettingsViewModel(get(), get(), get(), get(), get()) }
    single { HomeViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    single { AttendanceListScreenViewModel(get(), get(), get(), get(), get()) }
}
