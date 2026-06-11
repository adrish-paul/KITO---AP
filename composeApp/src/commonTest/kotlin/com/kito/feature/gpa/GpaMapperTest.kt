package com.kito.feature.gpa

import com.kito.core.database.entity.StudentEntity
import com.kito.feature.gpa.data.mapper.toDomain
import kotlin.test.Test
import kotlin.test.assertEquals

class GpaMapperTest {

    @Test
    fun toDomain_mapsAllFields() {
        val entity = StudentEntity(roll_no = "22CS001", section = "CS-A", batch = "B1")
        val domain = entity.toDomain()
        assertEquals("22CS001", domain.roll)
        assertEquals("CS-A", domain.section)
        assertEquals("B1", domain.batch)
    }

    @Test
    fun toDomain_sectionSubstringBefore_givesBranch() {
        val entity = StudentEntity(roll_no = "22EE001", section = "EE-B", batch = "B2")
        val domain = entity.toDomain()
        assertEquals("EE", domain.section.substringBefore("-"))
    }
}
