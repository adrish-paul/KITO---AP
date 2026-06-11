package com.kito.feature.khaoogully.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    @SerialName("browse_only")    val browseOnly: Boolean,
) {
    val isOrderable: Boolean get() = !browseOnly
    val cuisineLabel: String get() =
        if (cuisine.isEmpty()) "Multi-cuisine" else cuisine.joinToString(" · ")
}

@Serializable
data class KgMenuResponse(
    @SerialName("restaurant_id")   val restaurantId: String,
    @SerialName("restaurant_name") val restaurantName: String,
    val dishes: List<KgDish>,
)

@Serializable
data class KgDish(
    val id: String,
    val name: String,
    val price: Int,
    val image: String?,
    val veg: Boolean,
    @SerialName("is_available")       val isAvailable: Boolean,
    @SerialName("has_customizations") val hasCustomizations: Boolean,
    @SerialName("promo_label")        val promoLabel: String?,
    val category: String?,
) {
    val priceRupees: String get() = "₹${price / 100}"
}

data class KgCategory(val name: String, val imageUrl: String?)

data class KgPopularDish(val restaurantId: String, val dish: KgDish)

object KhaoogullyRedirect {
    private const val BASE = "https://khaoogully.com/"
    fun build(partnerRef: String, restaurantId: String, dishId: String): String =
        "$BASE?ref=$partnerRef&rid=$restaurantId&iid=$dishId"
}
