package com.kito.feature.exam.domain.model

data class ExamSchedule(
    val subject: String,
    val subjectCode: String?,
    val date: String,       // ISO "yyyy-MM-dd" — kept as String to avoid kotlinx-datetime dep in domain
    val day: String,
    val startTime: String,  // "HH:mm:ss"
    val endTime: String,
    val batch: String,
    val branch: String,
    val semester: Int,
)
