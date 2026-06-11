package com.kito.feature.attendance.domain.usecase

import com.kito.feature.attendance.domain.model.AttendanceSummary
import com.kito.feature.attendance.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Observes attendance and derives summary statistics (average / highest / lowest).
 * This logic previously lived inline in the ViewModel; it now lives in the domain layer where it
 * is pure and unit-testable.
 */
class GetAttendanceSummaryUseCase(
    private val repository: AttendanceRepository,
) {
    operator fun invoke(): Flow<AttendanceSummary> =
        repository.observeAttendance().map { items ->
            if (items.isEmpty()) {
                AttendanceSummary.Empty
            } else {
                val percentages = items.map { it.percentage }
                AttendanceSummary(
                    items = items,
                    averagePercentage = percentages.average(),
                    highestPercentage = percentages.max(),
                    lowestPercentage = percentages.min(),
                )
            }
        }
}
