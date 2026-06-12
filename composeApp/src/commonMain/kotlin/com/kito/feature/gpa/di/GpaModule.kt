package com.kito.feature.gpa.di

import com.kito.feature.gpa.data.GpaRepositoryImpl
import com.kito.feature.gpa.domain.repository.GpaRepository
import com.kito.feature.gpa.presentation.GPAViewmodel
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val gpaModule = module {
    single<GpaRepositoryImpl>() bind GpaRepository::class
    single<GPAViewmodel>()
}
