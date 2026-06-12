package com.kito.feature.faculty

import com.kito.core.network.supabase.model.TeacherFuzzySearchModel
import com.kito.core.network.supabase.model.TeacherModel
import com.kito.core.network.supabase.model.TeacherScheduleByIDModel
import com.kito.feature.faculty.data.mapper.toDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FacultyMapperTest {

    // ── TeacherModel ──────────────────────────────────────────────────────────

    @Test
    fun teacherModel_mapsAllFields() {
        val dto = TeacherModel(email = "john@uni.edu", name = "John Doe", office_room = "Room 404", teacher_id = 123L)
        val domain = dto.toDomain()
        assertEquals(123L, domain.id)              // teacher_id → id (renamed)
        assertEquals("John Doe", domain.name)
        assertEquals("john@uni.edu", domain.email)
        assertEquals("Room 404", domain.officeRoom) // office_room → officeRoom (renamed)
    }

    @Test
    fun teacherModel_nullId_defaultsToZero() {
        assertEquals(0L, TeacherModel(null, null, null, null).toDomain().id)
    }

    @Test
    fun teacherModel_nullName_defaultsToEmpty() {
        assertEquals("", TeacherModel(null, null, null, null).toDomain().name)
    }

    @Test
    fun teacherModel_nullEmail_mapsToNull() {
        assertNull(TeacherModel(null, "Name", null, 1L).toDomain().email)
    }

    @Test
    fun teacherModel_nullOfficeRoom_mapsToNull() {
        assertNull(TeacherModel(null, "Name", null, 1L).toDomain().officeRoom)
    }

    // ── TeacherFuzzySearchModel ───────────────────────────────────────────────

    @Test
    fun fuzzySearchModel_mapsAllFields() {
        val dto = TeacherFuzzySearchModel(email = "jane@uni.edu", name = "Jane Smith", office_room = "Lab 101", score = 0.95, teacher_id = 456L)
        val domain = dto.toDomain()
        assertEquals(456L, domain.id)
        assertEquals("Jane Smith", domain.name)
        assertEquals("jane@uni.edu", domain.email)
        assertEquals("Lab 101", domain.officeRoom)
    }

    @Test
    fun fuzzySearchModel_nullsHandled() {
        val domain = TeacherFuzzySearchModel(null, null, null, null, null).toDomain()
        assertEquals(0L, domain.id)
        assertEquals("", domain.name)
        assertNull(domain.email)
        assertNull(domain.officeRoom)
    }

    // ── TeacherScheduleByIDModel ──────────────────────────────────────────────

    @Test
    fun scheduleModel_mapsAllFields() {
        val dto = TeacherScheduleByIDModel(batch = "B1", day = "Monday", end_time = "11:00", room = "303", start_time = "09:00", subject = "Maths", teacher = "Dr. A", week_type = 1)
        val domain = dto.toDomain()
        assertEquals("Monday", domain.day)
        assertEquals("09:00", domain.startTime)   // start_time → startTime (renamed)
        assertEquals("11:00", domain.endTime)     // end_time → endTime (renamed)
        assertEquals("303", domain.room)
        assertEquals("Maths", domain.subject)
        assertEquals("B1", domain.batch)
    }

    @Test
    fun scheduleModel_nullRoom_mapsToNull() {
        val dto = TeacherScheduleByIDModel("B1", "Mon", "11:00", null, "09:00", "Maths", "Dr. A", 1)
        assertNull(dto.toDomain().room)
    }
}
