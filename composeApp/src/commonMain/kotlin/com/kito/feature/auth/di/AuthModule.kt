package com.kito.feature.auth.di

import com.kito.feature.auth.presentation.UserSetupViewModel
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val authModule = module {
    single<UserSetupViewModel>()
}
