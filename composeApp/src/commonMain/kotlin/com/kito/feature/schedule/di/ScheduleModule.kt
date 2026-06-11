package com.kito.feature.schedule.di

import com.kito.feature.schedule.data.ScheduleRepositoryImpl
import com.kito.feature.schedule.domain.repository.ScheduleRepository
import com.kito.feature.schedule.presentation.ScheduleScreenViewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val scheduleModule = module {
    single<ScheduleRepositoryImpl>() bind ScheduleRepository::class
    single<ScheduleScreenViewModel>()
}
