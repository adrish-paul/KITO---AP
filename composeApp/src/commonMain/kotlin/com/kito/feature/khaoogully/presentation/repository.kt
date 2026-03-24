package com.kito.feature.khaoogully.presentation


import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlin.time.Clock

sealed class KgResult<out T> {
    data class Success<T>(val data: T) : KgResult<T>()
    data class Error(val message: String, val code: Int? = null) : KgResult<Nothing>()
}

class KhaoogullyRepository(
    private val apiKey: String,
    private val partnerRef: String = "kiito",
    private val baseUrl: String
) {

    // ── Ktor client ───────────────────────────────────────────────────────────
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    // ── 5-minute in-memory cache (per API docs recommendation) ────────────────
    private val cacheTtlMs = 5 * 60 * 1000L
    private val cacheMutex = Mutex()

    private var restaurantCache: List<KgRestaurant>? = null
    private var restaurantCacheTime: Long = 0L

    private val menuCache = mutableMapOf<String, KgMenuResponse>()
    private val menuCacheTime = mutableMapOf<String, Long>()

    // ── GET /external/restaurants ─────────────────────────────────────────────
    suspend fun getRestaurants(): KgResult<List<KgRestaurant>> {
        cacheMutex.withLock {
            val now = currentTimeMillis()
            restaurantCache?.let { cached ->
                if (now - restaurantCacheTime < cacheTtlMs) return KgResult.Success(cached)
            }
        }

        return safeApiCall {
            val result: List<KgRestaurant> = client.get("$baseUrl/external/restaurants") {
                headers { append("X-API-Key", apiKey) }
            }.body()
            cacheMutex.withLock {
                restaurantCache = result
                restaurantCacheTime = currentTimeMillis()
            }
            result
        }
    }

    // ── GET /external/restaurants/{id}/menu ───────────────────────────────────
    suspend fun getMenu(restaurantId: String): KgResult<KgMenuResponse> {
        cacheMutex.withLock {
            val now = currentTimeMillis()
            menuCache[restaurantId]?.let { cached ->
                val t = menuCacheTime[restaurantId] ?: 0L
                if (now - t < cacheTtlMs) return KgResult.Success(cached)
            }
        }

        return safeApiCall {
            val result: KgMenuResponse = client.get(
                "$baseUrl/external/restaurants/$restaurantId/menu"
            ) {
                headers { append("X-API-Key", apiKey) }
            }.body()
            cacheMutex.withLock {
                menuCache[restaurantId] = result
                menuCacheTime[restaurantId] = currentTimeMillis()
            }
            result
        }
    }

    // ── Derived: categories from all restaurant cuisine tags ──────────────────
    /**
     * Builds the horizontal category chip list from the cuisine tags present
     * across all restaurants. Each category gets the image of the first
     * restaurant that advertises that cuisine.
     */
    val staticCategories = listOf(
        KgCategory(
            name = "Burger",
            imageUrl = "https://images.unsplash.com/photo-1571091718767-18b5b1457add?q=80&w=1744&auto=format&fit=crop"
        ),
        KgCategory(
            name = "Pizza",
            imageUrl = "https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=1740&auto=format&fit=crop"
        ),
        KgCategory(
            name = "Biryani",
            imageUrl = "https://images.unsplash.com/photo-1589302168068-964664d93dc0?q=80&w=774&auto=format&fit=crop"
        ),
        KgCategory(
            name = "Coffee",
            imageUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?q=80&w=774&auto=format&fit=crop"
        ),
        KgCategory(
            name = "Dessert",
            imageUrl = "https://images.unsplash.com/photo-1563805042-7684c019e1cb?q=80&w=627&auto=format&fit=crop"
        ),
        KgCategory(
            name = "Healthy",
            imageUrl = "https://images.unsplash.com/photo-1490645935967-10de6ba17061?q=80&w=1706&auto=format&fit=crop"
        ),
    )
    fun getCategories(): List<KgCategory> {
        return staticCategories
    }
//    suspend fun getCategories(): KgResult<List<KgCategory>> {
//        val restaurantResult = getRestaurants()
//        if (restaurantResult is KgResult.Error) return restaurantResult
//
//        val restaurants = (restaurantResult as KgResult.Success).data
//        val seen = mutableSetOf<String>()
//        val categories = mutableListOf<KgCategory>()
//
//        restaurants.forEach { r ->
//            r.cuisine.forEach { tag ->
//                val key = tag.lowercase().trim()
//                if (seen.add(key)) {
//                    categories.add(KgCategory(name = tag, imageUrl = r.image))
//                }
//            }
//        }
//        return KgResult.Success(categories)
//    }

    // ── Derived: popular items — first available dish from each restaurant ────
    /**
     * Surfaces one available dish per orderable restaurant.
     * Falls back to browse-only restaurants if no orderable ones exist.
     */
    suspend fun getPopularDishes(maxItems: Int = 6): KgResult<List<KgPopularDish>> {
        val restaurantResult = getRestaurants()
        if (restaurantResult is KgResult.Error) return restaurantResult
        val restaurants = (restaurantResult as KgResult.Success).data

        // Prefer orderable restaurants, fall back to browse-only
        val candidates = restaurants.filter { !it.browseOnly }.ifEmpty { restaurants }
            .take(maxItems)

        val popular = mutableListOf<KgPopularDish>()
        for (r in candidates) {
            val menuResult = getMenu(r.id)
            if (menuResult is KgResult.Success) {
                val firstAvailable = menuResult.data.dishes
                    .firstOrNull { it.isAvailable && it.image != null }
                    ?: menuResult.data.dishes.firstOrNull { it.isAvailable }
                firstAvailable?.let {
                    popular.add(KgPopularDish(restaurantId = r.id, dish = it))
                }
            }
            if (popular.size >= maxItems) break
        }
        return KgResult.Success(popular)
    }

    // ── Redirect URL helper ───────────────────────────────────────────────────
    fun buildRedirectUrl(restaurantId: String, dishId: String): String =
        KhaoogullyRedirect.build(partnerRef, restaurantId, dishId)

    // ── Utility ───────────────────────────────────────────────────────────────
    private suspend fun <T> safeApiCall(block: suspend () -> T): KgResult<T> {
        return try {
            KgResult.Success(block())
        } catch (e: io.ktor.client.plugins.ClientRequestException) {
            val code = e.response.status.value
            val msg = when (code) {
                403 -> "API key is missing or invalid."
                404 -> "Resource not found."
                429 -> "Rate limit exceeded — please wait a moment."
                else -> "Request failed ($code)."
            }
            KgResult.Error(msg, code)
        } catch (e: Exception) {
            KgResult.Error(e.message ?: "Unknown error")
        }
    }

    /** Platform-agnostic current time in ms. Implement per-platform if needed. */
    private fun currentTimeMillis(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }

    fun close() = client.close()
}