package com.kito.core.di

import com.kito.core.auth.AuthRepository
import com.kito.core.auth.SupabaseAuthRepository
import com.kito.core.auth.createSupabaseAuthClient
import com.kito.core.auth.data.CredentialsRepositoryImpl
import com.kito.core.auth.domain.repository.CredentialsRepository
import com.kito.core.database.repository.SectionRepository
import com.kito.core.database.repository.StudentRepository
import com.kito.core.database.repository.StudentSectionRepository
import com.kito.core.datastore.domain.repository.PrefsRepository
import com.kito.core.datastore.data.PrefsRepositoryImpl
import com.kito.core.designsystem.StartupSyncGuard
import com.kito.core.connectivity.data.ConnectivityRepositoryImpl
import com.kito.core.connectivity.domain.repository.ConnectivityRepository
import com.kito.core.sync.data.SyncRemoteDataSource
import com.kito.core.sync.domain.AppSyncUseCase
import com.kito.core.sync.domain.SupabaseTimetableSync
import com.kito.core.sync.domain.SyncUseCase
import com.kito.core.sync.domain.usecase.ChangeYearTermUseCase
import com.kito.feature.app.presentation.AppViewModel
import com.kito.kaya.KayaRepository
import com.kito.kaya.KayaTimetableStore
import com.kito.kaya.sensitive.KayaPortalClient
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
    single<KayaPortalClient>()
    single<KayaRepository>()
    single<KayaTimetableStore>()
    single<SyncRemoteDataSource>()
    single<SectionRepository>()
    single<StudentRepository>()
    single<StudentSectionRepository>()
    single<PrefsRepositoryImpl>() bind PrefsRepository::class
    single<StartupSyncGuard>()
    single<AppSyncUseCase>() bind SyncUseCase::class
    single<SupabaseTimetableSync>()
    single<ChangeYearTermUseCase>()
    single<ConnectivityRepository> { ConnectivityRepositoryImpl(get()) }
    single<CredentialsRepositoryImpl>() bind CredentialsRepository::class
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
