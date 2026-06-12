package com.kito.feature.home.di

import com.kito.feature.home.data.HomeRepositoryImpl
import com.kito.feature.home.domain.repository.HomeRepository
import com.kito.feature.home.presentation.HomeViewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val homeModule = module {
    single<HomeRepositoryImpl>() bind HomeRepository::class
    single<HomeViewModel>()
}
