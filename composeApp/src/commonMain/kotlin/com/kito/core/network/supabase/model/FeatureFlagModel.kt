package com.kito.core.network.supabase.model

import kotlinx.serialization.Serializable

@Serializable
data class FeatureFlagModel(
    val created_at: String,
    val feature_name: String,
    val id: Int,
    val isEnabled: Boolean
)