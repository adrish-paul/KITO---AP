package com.kito.feature.home.domain.repository

import com.kito.feature.home.domain.model.EventOrAd

/**
 * Domain boundary for home-screen remote data.
 * The implementation (data layer) maps Supabase DTOs to domain models.
 * Presentation never sees [EventAndAdModel] or [FeatureFlagModel].
 */
interface HomeRepository {
    /** Returns a shuffled list of active events and ads. */
    suspend fun getEventsAndAds(): List<EventOrAd>

    /** Returns whether the KhaooGully feature is enabled. */
    suspend fun isKhaooGullyEnabled(): Boolean
}
