package com.kito.feature.exam.di

import com.kito.feature.exam.data.ExamRepositoryImpl
import com.kito.feature.exam.domain.repository.ExamRepository
import com.kito.feature.exam.presentation.UpcomingExamViewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val examModule = module {
    single<ExamRepositoryImpl>() bind ExamRepository::class
    single<UpcomingExamViewModel>()
}
