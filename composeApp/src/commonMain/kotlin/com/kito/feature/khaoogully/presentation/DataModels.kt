package com.kito.feature.khaoogully.presentation

// ─────────────────────────────────────────────────────────────────────────────
//  KhaoogullyModels.kt
//  Exact mapping of the khaoogully Partner API v1.0 response shapes.
// ─────────────────────────────────────────────────────────────────────────────

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── GET /external/restaurants ─────────────────────────────────────────────────

@Serializable
data class KgRestaurant(
    val id: String,
    val name: String,
    val image: String?,
    val cuisine: List<String> = emptyList(),
    val rating: Float,
    @SerialName("campus_name")    val campusName: String?,
    @SerialName("delivery_window") val deliveryWindow: String?,
    @SerialName("pool_id")        val poolId: String?,
    @SerialName("browse_only")    val browseOnly: Boolean
) {
    /** True only when an active delivery window exists right now. */
    val isOrderable: Boolean get() = !browseOnly

    /** Display-safe cuisine string, e.g. "Burger · Snacks · American" */
    val cuisineLabel: String get() =
        if (cuisine.isEmpty()) "Multi-cuisine" else cuisine.joinToString(" · ")
}

// ── GET /external/restaurants/{id}/menu ──────────────────────────────────────

@Serializable
data class KgMenuResponse(
    @SerialName("restaurant_id")   val restaurantId: String,
    @SerialName("restaurant_name") val restaurantName: String,
    val dishes: List<KgDish>
)

@Serializable
data class KgDish(
    val id: String,
    val name: String,
    /** Price in paise — use [priceRupees] for display. */
    val price: Int,
    val image: String?,
    val veg: Boolean,
    @SerialName("is_available")       val isAvailable: Boolean,
    @SerialName("has_customizations") val hasCustomizations: Boolean,
    @SerialName("promo_label")        val promoLabel: String?,
    val category: String?
) {
    /** Price divided by 100, formatted as e.g. "₹180" */
    val priceRupees: String get() = "₹${price / 100}"
}

// ── UI-level derived models ───────────────────────────────────────────────────

/**
 * Derived from the cuisine tags across all restaurants.
 * Used to populate the horizontal category chip row.
 */
data class KgCategory(
    val name: String,
    /** Image of the first restaurant that has this cuisine tag. */
    val imageUrl: String?
)

/**
 * A dish surfaced as a "Popular Item" card.
 * Carries its parent restaurant context so we can build a redirect URL.
 */
data class KgPopularDish(
    val restaurantId: String,
    val dish: KgDish
)

// ── Redirect URL builder ──────────────────────────────────────────────────────

object KhaoogullyRedirect {
    private const val BASE = "https://khaoogully.com/"

    /**
     * @param partnerRef  Your partner slug registered with khaoogully (e.g. "kiito")
     * @param restaurantId UUID from [KgRestaurant.id]
     * @param dishId       UUID from [KgDish.id]
     */
    fun build(partnerRef: String, restaurantId: String, dishId: String): String =
        "$BASE?ref=$partnerRef&rid=$restaurantId&iid=$dishId"
}