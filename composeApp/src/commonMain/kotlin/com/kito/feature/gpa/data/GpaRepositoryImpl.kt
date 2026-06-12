package com.kito.feature.gpa.data

import com.kito.core.database.repository.StudentRepository
import com.kito.feature.gpa.data.mapper.toDomain
import com.kito.feature.gpa.domain.model.StudentProfile
import com.kito.feature.gpa.domain.repository.GpaRepository

class GpaRepositoryImpl(
    private val studentRepository: StudentRepository,
) : GpaRepository {
    override suspend fun getStudentProfile(roll: String): StudentProfile? =
        studentRepository.getStudentByRoll(roll)?.toDomain()
}
