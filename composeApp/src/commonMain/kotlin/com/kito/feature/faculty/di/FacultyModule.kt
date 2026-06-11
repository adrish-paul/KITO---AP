package com.kito.feature.faculty.di

import com.kito.feature.faculty.data.FacultyRepositoryImpl
import com.kito.feature.faculty.domain.repository.FacultyRepository
import com.kito.feature.faculty.presentation.FacultyDetailViewModel
import com.kito.feature.faculty.presentation.FacultyScreenViewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val facultyModule = module {
    single<FacultyRepositoryImpl>() bind FacultyRepository::class
    single<FacultyScreenViewModel>()
    single<FacultyDetailViewModel>()
}
