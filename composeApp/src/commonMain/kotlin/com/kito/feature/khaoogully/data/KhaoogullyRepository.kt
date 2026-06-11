package com.kito.feature.khaoogully.data

import com.kito.feature.khaoogully.domain.model.KgCategory
import com.kito.feature.khaoogully.domain.model.KgMenuResponse
import com.kito.feature.khaoogully.domain.model.KgPopularDish
import com.kito.feature.khaoogully.domain.model.KgRestaurant
import com.kito.feature.khaoogully.domain.model.KhaoogullyRedirect
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Provided
import kotlin.time.Clock

sealed class KgResult<out T> {
    data class Success<T>(val data: T) : KgResult<T>()
    data class Error(val message: String, val code: Int? = null) : KgResult<Nothing>()
}

@Provided
class KhaoogullyRepository(
    private val apiKey: String,
    private val partnerRef: String = "kiito",
    private val baseUrl: String,
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
    }

    private val cacheTtlMs = 5 * 60 * 1000L
    private val cacheMutex = Mutex()
    private var restaurantCache: List<KgRestaurant>? = null
    private var restaurantCacheTime: Long = 0L
    private val menuCache = mutableMapOf<String, KgMenuResponse>()
    private val menuCacheTime = mutableMapOf<String, Long>()

    suspend fun getRestaurants(): KgResult<List<KgRestaurant>> {
        cacheMutex.withLock {
            restaurantCache?.let {
                if (Clock.System.now().toEpochMilliseconds() - restaurantCacheTime < cacheTtlMs)
                    return KgResult.Success(it)
            }
        }
        return safeApiCall {
            val result: List<KgRestaurant> = client.get("$baseUrl/external/restaurants") {
                headers { append("X-API-Key", apiKey) }
            }.body()
            cacheMutex.withLock {
                restaurantCache = result
                restaurantCacheTime = Clock.System.now().toEpochMilliseconds()
            }
            result
        }
    }

    suspend fun getMenu(restaurantId: String): KgResult<KgMenuResponse> {
        cacheMutex.withLock {
            menuCache[restaurantId]?.let {
                val t = menuCacheTime[restaurantId] ?: 0L
                if (Clock.System.now().toEpochMilliseconds() - t < cacheTtlMs)
                    return KgResult.Success(it)
            }
        }
        return safeApiCall {
            val result: KgMenuResponse = client.get(
                "$baseUrl/external/restaurants/$restaurantId/menu"
            ) { headers { append("X-API-Key", apiKey) } }.body()
            cacheMutex.withLock {
                menuCache[restaurantId] = result
                menuCacheTime[restaurantId] = Clock.System.now().toEpochMilliseconds()
            }
            result
        }
    }

    fun getCategories(): List<KgCategory> = listOf(
        KgCategory("Burger", "https://images.unsplash.com/photo-1571091718767-18b5b1457add?q=80&w=1744&auto=format&fit=crop"),
        KgCategory("Biryani", "https://images.unsplash.com/photo-1589302168068-964664d93dc0?q=80&w=774&auto=format&fit=crop"),
        KgCategory("Chicken", "https://plus.unsplash.com/premium_photo-1663840345377-3813d196d5da?q=80&w=774&auto=format&fit=crop"),
        KgCategory("Roll", "https://images.unsplash.com/photo-1662116765994-1e4200c43589?q=80&w=2064&auto=format&fit=crop"),
        KgCategory("Healthy", "https://images.unsplash.com/photo-1490645935967-10de6ba17061?q=80&w=1706&auto=format&fit=crop"),
        KgCategory("Pizza", "https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=1740&auto=format&fit=crop"),
    )

    suspend fun getPopularDishes(maxItems: Int = 6): KgResult<List<KgPopularDish>> {
        val result = getRestaurants()
        if (result is KgResult.Error) return result
        val restaurants = (result as KgResult.Success).data
        val candidates = restaurants.filter { !it.browseOnly }.ifEmpty { restaurants }.take(maxItems)
        val popular = mutableListOf<KgPopularDish>()
        for (r in candidates) {
            val menuResult = getMenu(r.id)
            if (menuResult is KgResult.Success) {
                val dish = menuResult.data.dishes.firstOrNull { it.isAvailable && it.image != null }
                    ?: menuResult.data.dishes.firstOrNull { it.isAvailable }
                dish?.let { popular.add(KgPopularDish(restaurantId = r.id, dish = it)) }
            }
            if (popular.size >= maxItems) break
        }
        return KgResult.Success(popular)
    }

    fun buildRedirectUrl(restaurantId: String, dishId: String): String =
        KhaoogullyRedirect.build(partnerRef, restaurantId, dishId)

    private suspend fun <T> safeApiCall(block: suspend () -> T): KgResult<T> = try {
        KgResult.Success(block())
    } catch (e: io.ktor.client.plugins.ClientRequestException) {
        val code = e.response.status.value
        KgResult.Error(when (code) {
            403 -> "API key is missing or invalid."
            404 -> "Resource not found."
            429 -> "Rate limit exceeded — please wait a moment."
            else -> "Request failed ($code)."
        }, code)
    } catch (e: Exception) {
        KgResult.Error(e.message ?: "Unknown error")
    }

    fun close() = client.close()
}
