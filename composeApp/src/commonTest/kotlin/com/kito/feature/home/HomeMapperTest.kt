package com.kito.feature.home

import com.kito.core.network.supabase.model.EventAndAdModel
import com.kito.feature.home.data.mapper.toDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeMapperTest {

    @Test
    fun eventAndAdModel_maps_to_domain() {
        val dto = EventAndAdModel(
            id = 42L,
            media_url = "https://example.com/image.png",
            media_type = "image",
            click_url = "https://example.com/click",
            display_order = 1,
            is_active = true,
            isAd = true
        )
        val domain = dto.toDomain()

        assertEquals(42L, domain.id)
        assertEquals("https://example.com/image.png", domain.mediaUrl)
        assertEquals("image", domain.mediaType)
        assertEquals("https://example.com/click", domain.clickUrl)
        assertTrue(domain.isAd)
    }

    @Test
    fun eventAndAdModel_maps_nulls_to_defaults() {
        val dto = EventAndAdModel(
            id = null,
            media_url = null,
            media_type = null,
            click_url = null,
            display_order = null,
            is_active = null,
            isAd = null
        )
        val domain = dto.toDomain()

        assertEquals(0L, domain.id)
        assertEquals("", domain.mediaUrl)
        assertEquals("", domain.mediaType)
        assertEquals(null, domain.clickUrl)
        assertFalse(domain.isAd)
    }
}
