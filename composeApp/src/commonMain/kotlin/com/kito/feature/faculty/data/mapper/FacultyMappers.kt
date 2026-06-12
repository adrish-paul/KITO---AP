package com.kito.feature.faculty.data.mapper

import com.kito.core.network.supabase.model.TeacherFuzzySearchModel
import com.kito.core.network.supabase.model.TeacherModel
import com.kito.core.network.supabase.model.TeacherScheduleByIDModel
import com.kito.feature.faculty.domain.model.Faculty
import com.kito.feature.faculty.domain.model.FacultyScheduleSlot

fun TeacherModel.toDomain(): Faculty = Faculty(
    id = teacher_id ?: 0L,
    name = name.orEmpty(),
    email = email,
    officeRoom = office_room,
)

fun TeacherFuzzySearchModel.toDomain(): Faculty = Faculty(
    id = teacher_id ?: 0L,
    name = name.orEmpty(),
    email = email,
    officeRoom = office_room,
)

fun TeacherScheduleByIDModel.toDomain(): FacultyScheduleSlot = FacultyScheduleSlot(
    day = day,
    startTime = start_time,
    endTime = end_time,
    room = room,
    subject = subject,
    batch = batch,
)
