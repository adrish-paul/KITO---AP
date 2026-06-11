package com.kito.feature.faculty

import com.kito.core.network.supabase.model.TeacherFuzzySearchModel
import com.kito.core.network.supabase.model.TeacherModel
import com.kito.core.network.supabase.model.TeacherScheduleByIDModel
import com.kito.feature.faculty.data.mapper.toDomain
import kotlin.test.Test
import kotlin.test.assertEquals

class FacultyMapperTest {

    @Test
    fun teacherModel_maps_to_domain() {
        val dto = TeacherModel(
            email = "john.doe@university.edu",
            name = "John Doe",
            office_room = "Room 404",
            teacher_id = 123L
        )
        val domain = dto.toDomain()

        assertEquals(123L, domain.id)
        assertEquals("John Doe", domain.name)
        assertEquals("john.doe@university.edu", domain.email)
        assertEquals("Room 404", domain.officeRoom)
    }

    @Test
    fun teacherModel_maps_nulls_to_defaults() {
        val dto = TeacherModel(
            email = null,
            name = null,
            office_room = null,
            teacher_id = null
        )
        val domain = dto.toDomain()

        assertEquals(0L, domain.id)
        assertEquals("", domain.name)
        assertEquals(null, domain.email)
        assertEquals(null, domain.officeRoom)
    }

    @Test
    fun fuzzySearchModel_maps_to_domain() {
        val dto = TeacherFuzzySearchModel(
            email = "jane.smith@university.edu",
            name = "Jane Smith",
            office_room = "Lab 101",
            score = 0.95,
            teacher_id = 456L
        )
        val domain = dto.toDomain()

        assertEquals(456L, domain.id)
        assertEquals("Jane Smith", domain.name)
        assertEquals("jane.smith@university.edu", domain.email)
        assertEquals("Lab 101", domain.officeRoom)
    }

    @Test
    fun scheduleByIDModel_maps_to_domain() {
        val dto = TeacherScheduleByIDModel(
            batch = "CS-2026",
            day = "Monday",
            end_time = "11:00",
            room = "Room 303",
            start_time = "09:00",
            subject = "Advanced Coding",
            teacher = "Dr. Code",
            week_type = 1
        )
        val domain = dto.toDomain()

        assertEquals("Monday", domain.day)
        assertEquals("09:00", domain.startTime)
        assertEquals("11:00", domain.endTime)
        assertEquals("Room 303", domain.room)
        assertEquals("Advanced Coding", domain.subject)
        assertEquals("CS-2026", domain.batch)
    }
}
