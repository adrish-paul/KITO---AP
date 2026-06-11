package com.kito.feature.gpa.data.mapper

import com.kito.core.database.entity.StudentEntity
import com.kito.feature.gpa.domain.model.StudentProfile

fun StudentEntity.toDomain(): StudentProfile = StudentProfile(
    roll = roll_no,
    section = section,
    batch = batch,
)
