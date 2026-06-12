package com.kito.feature.gpa.domain.repository

import com.kito.feature.gpa.domain.model.StudentProfile

interface GpaRepository {
    suspend fun getStudentProfile(roll: String): StudentProfile?
}
