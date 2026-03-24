package com.kito.feature.khaoogully.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.koin.compose.koinInject

private val PrimaryGreen  = Color(0xFF2ECC71)
private val DarkGreen     = Color(0xFF27AE60)
private val TextPrimary   = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF888888)
private val CardBg        = Color.White
private val ScreenBg      = Color(0xFFF7F7F7)
private val BrowseBadge   = Color(0xFFFFA726)

// ─────────────────────────────────────────────────────────────────────────────
//  Entry point — now accepts nav callback
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun KhaooGullyHomeScreen(
    viewModel: KhaoogullyViewModel = koinInject(),
    onRestaurantClick: (KgRestaurant) -> Unit
) {
    com.kito.SetSystemBarAppearance(isLightForeground = false)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    FoodHomeContent(
        state             = state,
        onSearchChange    = viewModel::onSearchQueryChange,
        onCategoryClick   = viewModel::onCategorySelected,
        onRestaurantClick = onRestaurantClick,
        onRetry           = viewModel::loadHomeData
    )
}

@Composable
private fun FoodHomeContent(
    state: FoodHomeUiState,
    onSearchChange: (String) -> Unit,
    onCategoryClick: (KgCategory) -> Unit,
    onRestaurantClick: (KgRestaurant) -> Unit,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        // ── Green top gradient (matches screenshot) ───────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.15f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFA8EDCA),  // mint green at top
                            ScreenBg           // blends into page bg
                        )
                    )
                )
        )

        when {
            state.isLoading -> FullScreenLoader()
            state.error != null && state.restaurants.isEmpty() ->
                FullScreenError(message = state.error, onRetry = onRetry)
            else -> HomeScrollContent(
                state             = state,
                onSearchChange    = onSearchChange,
                onCategoryClick   = onCategoryClick,
                onRestaurantClick = onRestaurantClick
            )
        }
    }
}

@Composable
private fun HomeScrollContent(
    state: FoodHomeUiState,
    onSearchChange: (String) -> Unit,
    onCategoryClick: (KgCategory) -> Unit,
    onRestaurantClick: (KgRestaurant) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(44.dp))

        LocationHeader()
        Spacer(Modifier.height(12.dp))

        KgSearchBar(query = state.searchQuery, onQueryChange = onSearchChange)
        Spacer(Modifier.height(16.dp))

        if (state.categories.isNotEmpty()) {
            CategoryRow(
                categories       = state.categories,
                selectedCategory = state.selectedCategory,
                onCategoryClick  = onCategoryClick
            )
            Spacer(Modifier.height(16.dp))
        }

        SectionHeader("All Restaurants")
        Spacer(Modifier.height(10.dp))
        RestaurantList(
            restaurants       = state.filteredRestaurants,
            onRestaurantClick = onRestaurantClick
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun LocationHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.LocationOn, null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Column {
            Text("Get Started", fontSize = 11.sp, color = TextSecondary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Select Campus", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Default.ArrowForward, null, tint = PrimaryGreen, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun KgSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search food, groceries, etc...", color = TextSecondary, fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor    = Color(0xFFE0E0E0),
            focusedBorderColor      = PrimaryGreen,
            unfocusedContainerColor = CardBg,
            focusedContainerColor   = CardBg
        ),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(52.dp)
    )
}

@Composable
private fun CategoryRow(
    categories: List<KgCategory>,
    selectedCategory: String?,
    onCategoryClick: (KgCategory) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        categories.forEach { cat ->
            CategoryChip(
                category   = cat,
                isSelected = cat.name == selectedCategory,
                onClick    = { onCategoryClick(cat) }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    category: KgCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).width(64.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(if (isSelected) PrimaryGreen.copy(alpha = 0.12f) else Color(0xFFF0F0F0))
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) PrimaryGreen else Color.Transparent,
                    shape = CircleShape
                )
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(category.imageUrl ?: "")
                    .crossfade(true)
                    .build(),
                contentDescription = category.name,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize().clip(CircleShape)
            )
        }
        Spacer(Modifier.height(5.dp))
        Text(
            text       = category.name,
            fontSize   = 11.sp,
            color      = if (isSelected) PrimaryGreen else TextPrimary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text       = title,
        modifier   = Modifier.padding(horizontal = 16.dp),
        fontSize   = 18.sp,
        fontWeight = FontWeight.Bold,
        color      = TextPrimary
    )
}

@Composable
private fun RestaurantList(
    restaurants: List<KgRestaurant>,
    onRestaurantClick: (KgRestaurant) -> Unit
) {
    if (restaurants.isEmpty()) {
        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No restaurants found", color = TextSecondary, fontSize = 14.sp)
        }
        return
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        restaurants.forEach { RestaurantCard(restaurant = it, onClick = { onRestaurantClick(it) }) }
    }
}

@Composable
private fun RestaurantCard(restaurant: KgRestaurant, onClick: () -> Unit) {
    Surface(
        shape           = RoundedCornerShape(14.dp),
        color           = CardBg,
        shadowElevation = 3.dp,
        modifier        = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(restaurant.image ?: "")
                    .crossfade(true)
                    .build(),
                contentDescription = restaurant.name,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF0F0F0))
                    .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(10.dp))
            )
            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        restaurant.name,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp,
                        color      = TextPrimary,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f)
                    )
                    if (restaurant.browseOnly) {
                        Spacer(Modifier.width(6.dp))
                        Surface(shape = RoundedCornerShape(6.dp), color = BrowseBadge.copy(alpha = 0.15f)) {
                            Text(
                                "Soon", color = BrowseBadge, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (restaurant.rating > 0f) {
                        Surface(shape = RoundedCornerShape(4.dp), color = DarkGreen) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(10.dp))
                                Spacer(Modifier.width(2.dp))
                                Text(restaurant.rating.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(Modifier.width(6.dp))
                        Text("·", color = TextSecondary, fontSize = 12.sp)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(restaurant.deliveryWindow ?: "30 mins", color = TextSecondary, fontSize = 12.sp)
                }
                Spacer(Modifier.height(3.dp))
                Text(restaurant.cuisineLabel, color = TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                restaurant.campusName?.let {
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(it, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun FullScreenLoader() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = PrimaryGreen)
    }
}

@Composable
private fun FullScreenError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, color = TextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors  = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Icon(Icons.Default.Refresh, null)
            Spacer(Modifier.width(6.dp))
            Text("Retry")
        }
    }
}