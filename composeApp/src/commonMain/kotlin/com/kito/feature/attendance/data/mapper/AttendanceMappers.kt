package com.kito.feature.attendance.data.mapper

import com.kito.core.database.entity.AttendanceEntity
import com.kito.feature.attendance.domain.model.Attendance

/**
 * The ONLY place allowed to reference both the Room entity and the domain model.
 */
fun AttendanceEntity.toDomain(): Attendance = Attendance(
    subjectCode = subjectCode,
    subjectName = subjectName,
    attendedClasses = attendedClasses,
    totalClasses = totalClasses,
    percentage = percentage,
    facultyName = facultyName,
)

fun Attendance.toEntity(year: String, term: String): AttendanceEntity = AttendanceEntity(
    subjectCode = subjectCode,
    subjectName = subjectName,
    attendedClasses = attendedClasses,
    totalClasses = totalClasses,
    percentage = percentage,
    facultyName = facultyName,
    year = year,
    term = term
)

