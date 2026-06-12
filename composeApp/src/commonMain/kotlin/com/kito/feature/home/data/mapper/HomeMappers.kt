package com.kito.feature.home.data.mapper

import com.kito.core.network.supabase.model.EventAndAdModel
import com.kito.feature.home.domain.model.EventOrAd

/**
 * Maps the Supabase network DTO to the pure domain model.
 * This is the ONLY file in the home feature allowed to import both types.
 */
fun EventAndAdModel.toDomain(): EventOrAd = EventOrAd(
    id = id ?: 0L,
    mediaUrl = media_url.orEmpty(),
    mediaType = media_type.orEmpty(),
    clickUrl = click_url,
    isAd = isAd ?: false,
)
