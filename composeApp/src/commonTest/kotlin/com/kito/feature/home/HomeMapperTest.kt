package com.kito.feature.home

import com.kito.core.network.supabase.model.EventAndAdModel
import com.kito.feature.home.data.mapper.toDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HomeMapperTest {

    @Test
    fun toDomain_mapsAllFields() {
        val dto = EventAndAdModel(id = 42L, media_url = "https://cdn/img.jpg", media_type = "image", click_url = "https://example.com", isAd = true)
        val domain = dto.toDomain()
        assertEquals(42L, domain.id)
        assertEquals("https://cdn/img.jpg", domain.mediaUrl)
        assertEquals("image", domain.mediaType)
        assertEquals("https://example.com", domain.clickUrl)
        assertTrue(domain.isAd)
    }

    @Test
    fun toDomain_nullId_defaultsToZero() {
        assertEquals(0L, EventAndAdModel().toDomain().id)
    }

    @Test
    fun toDomain_nullClickUrl_mapsToNull() {
        assertNull(EventAndAdModel(id = 1L, media_url = "url", media_type = "image", click_url = null, isAd = false).toDomain().clickUrl)
    }

    @Test
    fun toDomain_nullMediaUrl_defaultsToEmpty() {
        assertEquals("", EventAndAdModel().toDomain().mediaUrl)
    }

    @Test
    fun toDomain_nullIsAd_defaultsFalse() {
        assertFalse(EventAndAdModel().toDomain().isAd)
    }
}
