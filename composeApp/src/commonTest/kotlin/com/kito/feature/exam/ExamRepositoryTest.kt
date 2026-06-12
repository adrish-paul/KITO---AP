package com.kito.feature.exam

import com.kito.testing.FakeExamRepository
import com.kito.testing.examSchedule
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExamRepositoryTest {

    @Test
    fun getExamSchedule_returnsList() = runTest {
        val repo = FakeExamRepository(listOf(examSchedule("Maths"), examSchedule("Physics")))
        val result = repo.getExamSchedule("22CS001")
        assertEquals(2, result.size)
        assertEquals("Maths", result[0].subject)
    }

    @Test
    fun getExamSchedule_empty_returnsEmpty() = runTest {
        assertTrue(FakeExamRepository().getExamSchedule("roll").isEmpty())
    }
}
