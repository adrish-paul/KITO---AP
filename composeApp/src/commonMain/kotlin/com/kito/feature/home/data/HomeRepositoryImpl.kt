package com.kito.feature.home.data

import com.kito.core.network.supabase.SupabaseRepository
import com.kito.feature.home.data.mapper.toDomain
import com.kito.feature.home.domain.model.EventOrAd
import com.kito.feature.home.domain.repository.HomeRepository

class HomeRepositoryImpl(
    private val supabaseRepository: SupabaseRepository,
) : HomeRepository {

    override suspend fun getEventsAndAds(): List<EventOrAd> =
        supabaseRepository.getEventsAndAds().map { it.toDomain() }.shuffled()

    override suspend fun isKhaooGullyEnabled(): Boolean =
        supabaseRepository.getFeatureFlag().firstOrNull()?.isEnabled ?: false
}
