package com.kito.feature.exam

import com.kito.core.network.supabase.model.MidsemScheduleModel
import com.kito.feature.exam.data.mapper.toDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExamMapperTest {

    private fun dto(subject: String = "Maths", subjectCode: String? = "CS101") =
        MidsemScheduleModel(
            subject = subject,
            subject_code = subjectCode,
            date = "2026-06-20",
            day = "Saturday",
            start_time = "09:00:00",
            end_time = "11:00:00",
            batch = "B1",
            branch = "CS",
            semester = 4,
        )

    @Test
    fun toDomain_mapsAllFields() {
        val domain = dto().toDomain()
        assertEquals("Maths", domain.subject)
        assertEquals("CS101", domain.subjectCode)
        assertEquals("2026-06-20", domain.date)
        assertEquals("Saturday", domain.day)
        assertEquals("09:00:00", domain.startTime)
        assertEquals("11:00:00", domain.endTime)
        assertEquals("B1", domain.batch)
        assertEquals("CS", domain.branch)
        assertEquals(4, domain.semester)
    }

    @Test
    fun toDomain_nullSubjectCode_mapsToNull() {
        val domain = dto(subjectCode = null).toDomain()
        assertNull(domain.subjectCode)
    }
}
