package com.kito.feature.exam.data

import com.kito.core.network.supabase.SupabaseRepository
import com.kito.feature.exam.data.mapper.toDomain
import com.kito.feature.exam.domain.model.ExamSchedule
import com.kito.feature.exam.domain.repository.ExamRepository

class ExamRepositoryImpl(
    private val supabaseRepository: SupabaseRepository,
) : ExamRepository {
    override suspend fun getExamSchedule(roll: String): List<ExamSchedule> =
        supabaseRepository.getMidSemSchedule(roll).map { it.toDomain() }
}
