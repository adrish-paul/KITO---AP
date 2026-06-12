package com.kito.feature.friendview.domain.model

data class FriendScheduleItem(
    val subject: String,
    val startTime: String,
    val endTime: String,
    val room: String?,
    val day: String,
    val section: String,
    val batch: String,
)
