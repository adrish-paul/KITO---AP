package com.kito.feature.faculty

import com.kito.feature.faculty.domain.model.FacultyScheduleSlot
import com.kito.feature.faculty.domain.repository.FacultyRepository
import com.kito.testing.FakeFacultyRepository
import com.kito.testing.faculty
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FacultyRepositoryTest {

    private val schedule = listOf(
        FacultyScheduleSlot("Monday", "09:00", "10:00", "101", "Maths", "B1")
    )
    private val repo: FacultyRepository = FakeFacultyRepository(
        all = listOf(faculty(id = 1L, name = "Dr. A"), faculty(id = 2L, name = "Dr. B")),
        schedule = schedule,
    )

    @Test
    fun getAllFaculty_returnsList() = runTest {
        val result = repo.getAllFaculty()
        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals("Dr. A", result[0].name)
    }

    @Test
    fun searchFaculty_filtersByName() = runTest {
        val result = repo.searchFaculty("Dr. A")
        assertEquals(1, result.size)
        assertEquals("Dr. A", result[0].name)
    }

    @Test
    fun searchFaculty_noMatch_returnsEmpty() = runTest {
        assertTrue(repo.searchFaculty("XYZ").isEmpty())
    }

    @Test
    fun getFacultyById_knownId_returnsCorrectFaculty() = runTest {
        val result = repo.getFacultyById(2L)
        assertEquals("Dr. B", result?.name)
    }

    @Test
    fun getFacultyById_unknownId_returnsNull() = runTest {
        assertNull(repo.getFacultyById(999L))
    }

    @Test
    fun getFacultySchedule_returnsSchedule() = runTest {
        val result = repo.getFacultySchedule(1L)
        assertEquals(1, result.size)
        assertEquals("Monday", result[0].day)
        assertEquals("09:00", result[0].startTime)
    }
}
