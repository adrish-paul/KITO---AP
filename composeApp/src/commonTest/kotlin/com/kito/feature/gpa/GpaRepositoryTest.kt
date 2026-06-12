package com.kito.feature.gpa

import com.kito.testing.FakeGpaRepository
import com.kito.testing.studentProfile
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GpaRepositoryTest {

    @Test
    fun getStudentProfile_returnsProfile() = runTest {
        val profile = studentProfile(roll = "22CS001", section = "CS-A")
        val result = FakeGpaRepository(profile).getStudentProfile("22CS001")
        assertEquals("CS-A", result?.section)
    }

    @Test
    fun getStudentProfile_nullProfile_returnsNull() = runTest {
        assertNull(FakeGpaRepository(null).getStudentProfile("roll"))
    }
}
