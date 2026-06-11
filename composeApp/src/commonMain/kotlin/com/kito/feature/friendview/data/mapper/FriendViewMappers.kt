package com.kito.feature.friendview.data.mapper

import com.kito.core.database.entity.SectionEntity
import com.kito.feature.friendview.domain.model.FriendScheduleItem

fun SectionEntity.toDomain(): FriendScheduleItem = FriendScheduleItem(
    subject = subject,
    startTime = start_time,
    endTime = end_time,
    room = room,
    day = day,
    section = section,
    batch = batch,
)
