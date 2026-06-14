package com.kito.feature.home.data

import com.kito.core.network.supabase.model.EventAndAdModel
import com.kito.core.network.supabase.model.FeatureFlagModel
import com.kito.feature.home.data.mapper.toDomain
import com.kito.feature.home.domain.model.EventOrAd
import com.kito.feature.home.domain.repository.HomeRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

import org.koin.core.annotation.Provided

class HomeRepositoryImpl(
    @Provided private val client: HttpClient,
) : HomeRepository {

    override suspend fun getEventsAndAds(): List<EventOrAd> {
        val response = client.get("rest/v1/events_and_ads") {
            parameter("is_active", "eq.true")
            parameter("order", "display_order.asc")
        }.body<List<EventAndAdModel>>()
        return response.map { it.toDomain() }.shuffled()
    }

    override suspend fun isKhaooGullyEnabled(): Boolean {
        val response = client.get("rest/v1/feature_flag").body<List<FeatureFlagModel>>()
        return response.firstOrNull()?.isEnabled ?: false
    }
}
