package com.kito.feature.attendance

import com.kito.core.database.entity.AttendanceEntity
import com.kito.feature.attendance.data.mapper.toDomain
import kotlin.test.Test
import kotlin.test.assertEquals

class AttendanceMapperTest {

    private fun entity(
        subjectCode: String = "CS101",
        subjectName: String = "Maths",
        attendedClasses: Int = 8,
        totalClasses: Int = 10,
        percentage: Double = 80.0,
        facultyName: String = "Dr. Test",
    ) = AttendanceEntity(
        subjectCode = subjectCode,
        subjectName = subjectName,
        attendedClasses = attendedClasses,
        totalClasses = totalClasses,
        percentage = percentage,
        facultyName = facultyName,
        year = "2025",
        term = "020",
    )

    @Test
    fun toDomain_mapsAllDisplayFields() {
        val domain = entity().toDomain()
        assertEquals("CS101", domain.subjectCode)
        assertEquals("Maths", domain.subjectName)
        assertEquals(8, domain.attendedClasses)
        assertEquals(10, domain.totalClasses)
        assertEquals(80.0, domain.percentage)
        assertEquals("Dr. Test", domain.facultyName)
    }

    @Test
    fun toDomain_persistenceKeysNotLeaked() {
        // year and term are storage keys — they must NOT appear on the domain model
        val domain = entity().toDomain()
        // Compile-time proof: Attendance has no 'year' or 'term' property
        // If this compiles, the domain model is clean
        val fields = domain::class.members.map { it.name }
        assert(!fields.contains("year")) { "domain model leaks 'year'" }
        assert(!fields.contains("term")) { "domain model leaks 'term'" }
    }

    @Test
    fun toDomain_zeroAttendance() {
        val domain = entity(attendedClasses = 0, totalClasses = 0, percentage = 0.0).toDomain()
        assertEquals(0, domain.attendedClasses)
        assertEquals(0, domain.totalClasses)
        assertEquals(0.0, domain.percentage)
    }

    @Test
    fun toDomain_perfectAttendance() {
        val domain = entity(attendedClasses = 50, totalClasses = 50, percentage = 100.0).toDomain()
        assertEquals(50, domain.attendedClasses)
        assertEquals(100.0, domain.percentage)
    }
}
