package com.kito.feature.schedule

import com.kito.core.database.entity.StudentSectionEntity
import com.kito.feature.schedule.data.mapper.toDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ScheduleMapperTest {

    private fun entity(room: String? = "Lab 1") = StudentSectionEntity(
        sectionId = 1, rollNo = "22CS001", section = "CS-A",
        batch = "B1", day = "MON", startTime = "09:00", endTime = "10:00",
        subject = "Maths", room = room,
    )

    @Test
    fun toDomain_mapsAllFields() {
        val domain = entity().toDomain()
        assertEquals("Maths", domain.subject)
        assertEquals("09:00", domain.startTime)
        assertEquals("10:00", domain.endTime)
        assertEquals("Lab 1", domain.room)
        assertEquals("CS-A", domain.section)
        assertEquals("B1", domain.batch)
    }

    @Test
    fun toDomain_nullRoom_mapsToNull() {
        assertNull(entity(room = null).toDomain().room)
    }

    @Test
    fun toDomain_persistenceKeyRollNo_notLeaked() {
        // rollNo is storage-only — must not appear on domain model
        val fields = entity().toDomain()::class.members.map { it.name }
        assert(!fields.contains("rollNo")) { "domain ScheduleItem leaks rollNo" }
        assert(!fields.contains("sectionId")) { "domain ScheduleItem leaks sectionId" }
    }
}
