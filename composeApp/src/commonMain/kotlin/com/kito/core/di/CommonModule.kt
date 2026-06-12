package com.kito.core.di

import com.kito.core.auth.AuthRepository
import com.kito.core.auth.SupabaseAuthRepository
import com.kito.core.auth.createSupabaseAuthClient
import com.kito.core.database.repository.AttendanceRepository
import com.kito.core.database.repository.SectionRepository
import com.kito.core.database.repository.StudentRepository
import com.kito.core.database.repository.StudentSectionRepository
import com.kito.core.datastore.PrefsRepository
import com.kito.core.designsystem.StartupSyncGuard
import com.kito.core.network.supabase.SupabaseRepository
import com.kito.core.sync.domain.AppSyncUseCase
import com.kito.core.sync.domain.SyncUseCase
import com.kito.feature.app.presentation.AppViewModel
import com.kito.sap.SapPortalClient
import com.kito.sap.SapRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import org.koin.plugin.module.dsl.single

val commonModule = module {

    single(named("ApplicationScope")) { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    single<SapPortalClient>()
    single<SapRepository>()
    single<SupabaseRepository>()
    single<AttendanceRepository>()
    single<SectionRepository>()
    single<StudentRepository>()
    single<StudentSectionRepository>()
    single<PrefsRepository>()
    single<StartupSyncGuard>()
    single<AppSyncUseCase>() bind SyncUseCase::class

    // Supabase Auth (SDK) — auth/GoTrue only; separate from the raw REST client.
    // create(::fn) registers SupabaseClient as a compiler-plugin-tracked provider so that
    // get<SupabaseClient>() call sites (deep-link handlers) pass compile-time safety.
    single { create(::createSupabaseAuthClient) }
    single<AuthRepository> {
        SupabaseAuthRepository(
            client = get(),
            scope = get(named("ApplicationScope"))
        )
    }
}

val commonViewModelModule = module {

    single<AppViewModel>()
}
