package com.kito.feature.exam.data.mapper

import com.kito.core.network.supabase.model.MidsemScheduleModel
import com.kito.feature.exam.domain.model.ExamSchedule

fun MidsemScheduleModel.toDomain(): ExamSchedule = ExamSchedule(
    subject = subject,
    subjectCode = subject_code,
    date = date,
    day = day,
    startTime = start_time,
    endTime = end_time,
    batch = batch,
    branch = branch,
    semester = semester,
)
