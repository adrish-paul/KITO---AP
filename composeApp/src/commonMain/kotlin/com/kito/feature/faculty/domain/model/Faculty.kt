package com.kito.feature.faculty.domain.model

data class Faculty(
    val id: Long,
    val name: String,
    val email: String?,
    val officeRoom: String?,
)

data class FacultyScheduleSlot(
    val day: String?,
    val startTime: String?,
    val endTime: String?,
    val room: String?,
    val subject: String?,
    val batch: String?,
)
