package com.kito.feature.schedule.data.mapper

import com.kito.core.database.entity.StudentSectionEntity
import com.kito.feature.schedule.domain.model.ScheduleItem

fun StudentSectionEntity.toDomain(): ScheduleItem = ScheduleItem(
    subject = subject,
    startTime = startTime,
    endTime = endTime,
    room = room,
    section = section,
    batch = batch,
)
