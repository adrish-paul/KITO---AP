package com.kito.core.network.supabase.model

data class LatestAppVersionModel(
    val force_update: Boolean,
    val latest_version: String,
    val min_required_platform: Int,
    val platform: String
)