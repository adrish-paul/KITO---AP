package com.kito.feature.home

import com.kito.testing.FakeHomeRepository
import com.kito.testing.eventOrAd
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeRepositoryTest {

    @Test
    fun getEventsAndAds_returnsList() = runTest {
        val repo = FakeHomeRepository(events = listOf(eventOrAd(1L), eventOrAd(2L, isAd = true)))
        val result = repo.getEventsAndAds()
        assertEquals(2, result.size)
        assertTrue(result[1].isAd)
    }

    @Test
    fun isKhaooGullyEnabled_defaultsFalse() = runTest {
        assertFalse(FakeHomeRepository().isKhaooGullyEnabled())
    }

    @Test
    fun isKhaooGullyEnabled_whenSet_returnsTrue() = runTest {
        assertTrue(FakeHomeRepository(khaooGullyEnabled = true).isKhaooGullyEnabled())
    }
}
