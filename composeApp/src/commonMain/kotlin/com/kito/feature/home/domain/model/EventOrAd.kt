package com.kito.feature.home.domain.model

/**
 * Pure domain model for an event or advertisement card displayed on the home screen.
 * No Supabase/Ktor/serialization types — those stay in data/mapper.
 */
data class EventOrAd(
    val id: Long,
    val mediaUrl: String,
    val mediaType: String,
    val clickUrl: String?,
    val isAd: Boolean,
)
