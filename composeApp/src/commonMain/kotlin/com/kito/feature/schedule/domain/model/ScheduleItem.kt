package com.kito.feature.schedule.domain.model

data class ScheduleItem(
    val subject: String,
    val startTime: String,
    val endTime: String,
    val room: String?,
    val section: String,
    val batch: String,
)
