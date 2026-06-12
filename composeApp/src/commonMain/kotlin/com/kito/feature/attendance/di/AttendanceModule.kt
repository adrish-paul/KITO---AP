package com.kito.feature.attendance.di

import com.kito.feature.attendance.data.AttendanceRepositoryImpl
import com.kito.feature.attendance.domain.repository.AttendanceRepository
import com.kito.feature.attendance.domain.usecase.GetAttendanceSummaryUseCase
import com.kito.feature.attendance.presentation.AttendanceListScreenViewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

/**
 * DI for the attendance feature. Per-feature module (doc 02 §8) — keeps wiring local and off the
 * central CommonModule merge-hotspot.
 */
val attendanceModule = module {
    single<AttendanceRepositoryImpl>() bind AttendanceRepository::class
    single<GetAttendanceSummaryUseCase>()
    single<AttendanceListScreenViewModel>()
}
