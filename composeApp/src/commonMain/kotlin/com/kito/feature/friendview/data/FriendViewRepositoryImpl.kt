package com.kito.feature.friendview.data

import com.kito.core.network.supabase.SupabaseRepository
import com.kito.feature.friendview.data.mapper.toDomain
import com.kito.feature.friendview.domain.model.FriendScheduleItem
import com.kito.feature.friendview.domain.repository.FriendViewRepository

class FriendViewRepositoryImpl(
    private val supabaseRepository: SupabaseRepository,
) : FriendViewRepository {
    override suspend fun getFriendSchedule(roll: String): List<FriendScheduleItem> {
        val student = supabaseRepository.getStudentByRoll(roll)
        if (student.section.isBlank()) return emptyList()
        return supabaseRepository.getTimetableForStudent(
            section = student.section,
            batch = student.batch,
        ).map { it.toDomain() }
    }
}
