package com.kito.feature.settings.di

import com.kito.feature.settings.presentation.SettingsViewModel
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val settingsModule = module {
    single<SettingsViewModel>()
}
