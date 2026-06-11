package com.kito.feature.faculty.domain.repository

import com.kito.feature.faculty.domain.model.Faculty
import com.kito.feature.faculty.domain.model.FacultyScheduleSlot

interface FacultyRepository {
    suspend fun getAllFaculty(): List<Faculty>
    suspend fun searchFaculty(query: String): List<Faculty>
    suspend fun getFacultyById(id: Long): Faculty?
    suspend fun getFacultySchedule(id: Long): List<FacultyScheduleSlot>
}
