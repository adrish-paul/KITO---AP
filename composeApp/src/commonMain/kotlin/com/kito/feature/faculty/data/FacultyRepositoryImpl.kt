package com.kito.feature.faculty.data

import com.kito.core.network.supabase.SupabaseRepository
import com.kito.feature.faculty.data.mapper.toDomain
import com.kito.feature.faculty.domain.model.Faculty
import com.kito.feature.faculty.domain.model.FacultyScheduleSlot
import com.kito.feature.faculty.domain.repository.FacultyRepository

class FacultyRepositoryImpl(
    private val supabaseRepository: SupabaseRepository,
) : FacultyRepository {
    override suspend fun getAllFaculty(): List<Faculty> =
        supabaseRepository.getAllTeacherDetail().map { it.toDomain() }

    override suspend fun searchFaculty(query: String): List<Faculty> =
        supabaseRepository.getTeacherSearchResponse(query).map { it.toDomain() }

    override suspend fun getFacultyById(id: Long): Faculty? =
        supabaseRepository.getTeacherDetailByID(id).firstOrNull()?.toDomain()

    override suspend fun getFacultySchedule(id: Long): List<FacultyScheduleSlot> =
        supabaseRepository.getTeacherScheduleById(id).map { it.toDomain() }
}
