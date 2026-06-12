package com.kito.feature.attendance.domain.model

/**
 * Pure domain model for a single subject's attendance. No Room/Ktor types.
 * (Persistence keys like year/term live on the data-layer entity, not here.)
 */
data class Attendance(
    val subjectCode: String,
    val subjectName: String,
    val attendedClasses: Int,
    val totalClasses: Int,
    val percentage: Double,
    val facultyName: String,
)

/**
 * Attendance list plus derived statistics. Computing the stats here (in a use case) keeps that
 * business logic out of the ViewModel and makes it unit-testable.
 */
data class AttendanceSummary(
    val items: List<Attendance>,
    val averagePercentage: Double,
    val highestPercentage: Double,
    val lowestPercentage: Double,
) {
    companion object {
        val Empty = AttendanceSummary(emptyList(), 0.0, 0.0, 0.0)
    }
}
