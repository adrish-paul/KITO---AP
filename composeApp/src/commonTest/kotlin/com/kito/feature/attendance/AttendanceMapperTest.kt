package com.kito.feature.attendance

import com.kito.core.database.entity.AttendanceEntity
import com.kito.feature.attendance.data.mapper.toDomain
import kotlin.test.Test
import kotlin.test.assertEquals

class AttendanceMapperTest {

    private fun entity(name: String, pct: Double) = AttendanceEntity(
        subjectCode = "CS$name",
        subjectName = name,
        attendedClasses = 8,
        totalClasses = 10,
        percentage = pct,
        facultyName = "Dr. $name",
        year = "2025",
        term = "020",
    )

    @Test
    fun entity_maps_to_domain_preserving_display_fields() {
        val domain = entity("Maths", 80.0).toDomain()
        assertEquals("Maths", domain.subjectName)
        assertEquals("CSMaths", domain.subjectCode)
        assertEquals(8, domain.attendedClasses)
        assertEquals(10, domain.totalClasses)
        assertEquals(80.0, domain.percentage)
        assertEquals("Dr. Maths", domain.facultyName)
    }
}
