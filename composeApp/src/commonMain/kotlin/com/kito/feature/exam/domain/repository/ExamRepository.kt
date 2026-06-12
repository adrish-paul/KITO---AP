package com.kito.feature.exam.domain.repository

import com.kito.feature.exam.domain.model.ExamSchedule

interface ExamRepository {
    suspend fun getExamSchedule(roll: String): List<ExamSchedule>
}
