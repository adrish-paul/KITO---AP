package com.kito.core.network.supabase.model

import kotlinx.serialization.Serializable

@Serializable
data class AdModel(
    val id: Long? = null,
    val media_url: String? = null,
    val media_type: String? = null,
    val click_url: String? = null,
    val display_order: Int? = null,
    val is_active: Boolean? = null
)