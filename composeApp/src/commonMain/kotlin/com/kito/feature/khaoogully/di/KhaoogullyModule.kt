package com.kito.feature.khaoogully.di

import com.kito.core.platform.AppConfig
import com.kito.feature.khaoogully.data.KhaoogullyRepository
import com.kito.feature.khaoogully.presentation.KhaoogullyViewModel
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val khaoogullyModule = module {
    single {
        KhaoogullyRepository(
            apiKey  = AppConfig.kgAPIKey,
            baseUrl = AppConfig.kgBaseURL,
        )
    }
    single<KhaoogullyViewModel>()
}
