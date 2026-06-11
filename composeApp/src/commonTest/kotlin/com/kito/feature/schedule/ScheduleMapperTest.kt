package com.kito.feature.schedule

import com.kito.core.database.entity.StudentSectionEntity
import com.kito.feature.schedule.data.mapper.toDomain
import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduleMapperTest {

    @Test
    fun entity_maps_to_domain() {
        val entity = StudentSectionEntity(
            sectionId = 1,
            rollNo = "12345",
            section = "CS-A",
            batch = "2026",
            day = "Monday",
            startTime = "09:00",
            endTime = "10:00",
            subject = "Algorithms",
            room = "Lab 1"
        )
        val domain = entity.toDomain()

        assertEquals("Algorithms", domain.subject)
        assertEquals("09:00", domain.startTime)
        assertEquals("10:00", domain.endTime)
        assertEquals("Lab 1", domain.room)
        assertEquals("CS-A", domain.section)
        assertEquals("2026", domain.batch)
    }
}
