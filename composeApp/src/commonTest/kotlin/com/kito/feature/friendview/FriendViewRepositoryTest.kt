package com.kito.feature.friendview

import com.kito.testing.FakeFriendViewRepository
import com.kito.testing.friendScheduleItem
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FriendViewRepositoryTest {

    @Test
    fun getFriendSchedule_returnsList() = runTest {
        val repo = FakeFriendViewRepository(listOf(friendScheduleItem("Maths"), friendScheduleItem("Physics")))
        val result = repo.getFriendSchedule("22CS001")
        assertEquals(2, result.size)
    }

    @Test
    fun getFriendSchedule_empty_returnsEmpty() = runTest {
        assertTrue(FakeFriendViewRepository().getFriendSchedule("roll").isEmpty())
    }
}
