package com.kito.feature.khaoogully.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.kito.core.presentation.navigation3.Routes
import com.kito.feature.khaoogully.domain.model.KgDish
import com.kito.feature.khaoogully.domain.model.KgRestaurant
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

// ─────────────────────────────────────────────────────────────────────────────
//  Colours (light theme matching screenshots)
// ─────────────────────────────────────────────────────────────────────────────

private val PrimaryGreen    = Color(0xFF2ECC71)
private val DarkGreen       = Color(0xFF27AE60)
private val LightGreen      = Color(0xFFE8F8EF)
private val TextPrimary     = Color(0xFF1A1A1A)
private val TextSecondary   = Color(0xFF888888)
private val Divider         = Color(0xFFF0F0F0)
private val ScreenBg        = Color(0xFFF7F7F7)
private val CardBg          = Color.White
private val VegGreen        = Color(0xFF2E7D32)
private val NonVegRed       = Color(0xFFB71C1C)

// ─────────────────────────────────────────────────────────────────────────────
//  Screen
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RestaurantMenuScreen(
    route: Routes.RestaurantMenu,
    viewModel: KhaoogullyViewModel = koinInject(),
    onBack: () -> Unit
) {
    com.kito.SetSystemBarAppearance(isLightForeground = false)
    val state by viewModel.menuState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    var showBrowseMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Load menu once when screen enters
    LaunchedEffect(route.restaurantId) {
        viewModel.loadMenu(
            KgRestaurant(
                id             = route.restaurantId,
                name           = route.restaurantName,
                image          = route.restaurantImage,
                cuisine        = emptyList(),
                rating         = route.restaurantRating,
                campusName     = null,
                deliveryWindow = null,
                poolId         = null,
                browseOnly     = route.browseOnly
            )
        )
    }

    // Clear on leave
    DisposableEffect(Unit) {
        onDispose { viewModel.clearMenuState() }
    }

    Box(modifier = Modifier.fillMaxSize().background(ScreenBg)) {
        when {
            state.isLoading -> MenuLoader()
            state.error != null && state.allDishes.isEmpty() ->
                MenuError(message = state.error!!, onRetry = {
                    viewModel.loadMenu(
                        KgRestaurant(
                            id             = route.restaurantId,
                            name           = route.restaurantName,
                            image          = route.restaurantImage,
                            cuisine        = emptyList(),
                            rating         = route.restaurantRating,
                            campusName     = null,
                            deliveryWindow = null,
                            poolId         = null,
                            browseOnly     = route.browseOnly
                        )
                    )
                })
            else -> MenuContent(
                route           = route,
                state           = state,
                onBack          = onBack,
                onSearchChange  = viewModel::onMenuSearchChange,
                onToggleCategory = viewModel::toggleCategory,
                onMenuButtonClick = { showBrowseMenu = true },
                onOrderDish     = { dishId ->
                    val url = viewModel.buildRedirectUrl(route.restaurantId, dishId)
                    uriHandler.openUri(url)
                },
                listState       = listState
            )
        }

        // ── "Browse Menu" bottom sheet modal ─────────────────────────────────
        if (showBrowseMenu) {
            BrowseMenuDialog(
                categories     = state.dishesByCategory.keys.toList(),
                onCategoryClick = { category ->
                    showBrowseMenu = false
                    viewModel.expandCategory(category)
                    coroutineScope.launch {
                        var index = 4 // Banner, Name, Stats, Search
                        for ((cat, dishes) in state.dishesByCategory) {
                            if (cat == category) break
                            index += 1
                            if (state.expandedCategories.contains(cat)) {
                                index += dishes.size
                            }
                        }
                        delay(100)
                        listState.animateScrollToItem(index)
                    }
                },
                onDismiss = { showBrowseMenu = false }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Main scrollable content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MenuContent(
    route: Routes.RestaurantMenu,
    state: MenuUiState,
    onBack: () -> Unit,
    onSearchChange: (String) -> Unit,
    onToggleCategory: (String) -> Unit,
    onMenuButtonClick: () -> Unit,
    onOrderDish: (dishId: String) -> Unit,
    listState: LazyListState = rememberLazyListState()
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Restaurant banner image ───────────────────────────────────────
            item {
                Box {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(route.restaurantImage ?: "")
                            .crossfade(true)
                            .build(),
                        contentDescription = route.restaurantName,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    )
                    // Back button overlay
                    Box(
                        modifier = Modifier
                            .padding(
                                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                                start = 12.dp
                            )
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(
                                Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // ── Restaurant name ───────────────────────────────────────────────
            item {
                Text(
                    text       = route.restaurantName,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 24.sp,
                    color      = TextPrimary,
                    modifier   = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // ── Stats row: Rating | Mins | For Two ────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatChip(
                        label = "RATING",
                        value = if (route.restaurantRating > 0f) route.restaurantRating.toString() else "—",
                        showStar = route.restaurantRating > 0f,
                        modifier = Modifier.weight(1f)
                    )
                    StatChip(
                        label = "MINS",
                        value = "30",
                        modifier = Modifier.weight(1f)
                    )
                    StatChip(
                        label = "FOR TWO",
                        value = "₹300",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(14.dp))
            }

            // ── Search dishes ─────────────────────────────────────────────────
            item {
                OutlinedTextField(
                    value         = state.searchQuery,
                    onValueChange = onSearchChange,
                    placeholder   = { Text("Search dishes...", color = TextSecondary, fontSize = 14.sp) },
                    leadingIcon   = { Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
                    shape         = RoundedCornerShape(12.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor    = Color(0xFFE0E0E0),
                        focusedBorderColor      = PrimaryGreen,
                        unfocusedContainerColor = CardBg,
                        focusedContainerColor   = CardBg
                    ),
                    singleLine    = true,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(50.dp)
                )
                Spacer(Modifier.height(10.dp))
            }

            // ── Login to add items CTA (browse-only state) ────────────────────
//            item {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp)
//                        .clip(RoundedCornerShape(12.dp))
//                        .background(LightGreen)
//                        .padding(horizontal = 16.dp, vertical = 13.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(
//                        Icons.AutoMirrored.Default.ArrowForwardIos,
//                        contentDescription = null,
//                        tint     = PrimaryGreen,
//                        modifier = Modifier.size(18.dp)
//                    )
//                    Spacer(Modifier.width(10.dp))
//                    Text(
//                        "Login to add items to cart",
//                        color      = TextPrimary,
//                        fontWeight = FontWeight.Medium,
//                        fontSize   = 14.sp,
//                        modifier   = Modifier.weight(1f)
//                    )
//                    Icon(
//                        Icons.AutoMirrored.Default.ArrowForwardIos,
//                        contentDescription = null,
//                        tint     = TextSecondary,
//                        modifier = Modifier.size(14.dp)
//                    )
//                }
//                Spacer(Modifier.height(6.dp))
//                HorizontalDivider(color = Divider, thickness = 6.dp)
//            }

            // ── Categories with expandable dish lists ─────────────────────────
            state.dishesByCategory.forEach { (category, dishes) ->
                // Category header row
                item(key = "header_$category") {
                    CategoryHeader(
                        category   = category,
                        count      = dishes.size,
                        isExpanded = state.expandedCategories.contains(category),
                        onClick    = { onToggleCategory(category) }
                    )
                    HorizontalDivider(color = Divider)
                }

                // Dish rows (only when expanded)
                if (state.expandedCategories.contains(category)) {
                    items(
                        items = dishes,
                        key   = { "dish_${it.id}" }
                    ) { dish ->
                        DishRow(
                            dish       = dish,
                            onOrderClick = { onOrderDish(dish.id) }
                        )
                        HorizontalDivider(
                            color     = Divider,
                            modifier  = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            // Bottom padding so FAB doesn't cover last item
            item { Spacer(Modifier.height(100.dp)) }
        }

        // ── "≡ Menu" FAB ─────────────────────────────────────────────────────
        FloatingActionButton(
            onClick            = onMenuButtonClick,
            containerColor     = TextPrimary,
            contentColor       = Color.White,
            shape              = RoundedCornerShape(50),
            modifier           = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end    = 20.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp
                )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("Menu", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Stat chip  (Rating / Mins / For Two)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StatChip(
    label: String,
    value: String,
    showStar: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape           = RoundedCornerShape(10.dp),
        color           = CardBg,
        shadowElevation = 2.dp,
        modifier        = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                if (showStar) {
                    Spacer(Modifier.width(3.dp))
                    Icon(Icons.Default.Star, null, tint = DarkGreen, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Category header row  (STARTER (41) ▼)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CategoryHeader(
    category: String,
    count: Int,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label       = "arrow"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(ScreenBg)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text       = "$category ($count)",
            fontWeight = FontWeight.SemiBold,
            fontSize   = 13.sp,
            color      = TextSecondary,
            modifier   = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint               = TextSecondary,
            modifier           = Modifier
                .size(22.dp)
                .rotate(arrowRotation)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Dish row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DishRow(
    dish: KgDish,
    onOrderClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Left: veg dot + name + price + category
        Column(modifier = Modifier.weight(1f)) {
            // Veg / non-veg indicator square (matches screenshot style)
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.White)
                    .then(
                        Modifier.background(
                            if (dish.veg) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            RoundedCornerShape(3.dp)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (dish.veg) VegGreen else NonVegRed,
                            androidx.compose.foundation.shape.CircleShape
                        )
                )
            }

            Spacer(Modifier.height(5.dp))

            // Rating row (API gives 4.0 in screenshot but our API doesn't have per-dish rating)
            // We show a static 4.0 ⭐ to match the khaoogully app visual
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = DarkGreen, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(2.dp))
                Text("4.0", fontSize = 11.sp, color = DarkGreen, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(3.dp))

            Text(
                text       = dish.name,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 15.sp,
                color      = TextPrimary,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text     = dish.priceRupees,
                fontSize = 14.sp,
                color    = TextPrimary,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(3.dp))

            dish.category?.let { cat ->
                Text(
                    text     = cat,
                    fontSize = 12.sp,
                    color    = TextSecondary
                )
            }

            if (dish.hasCustomizations) {
                Spacer(Modifier.height(2.dp))
                Text("customisable", fontSize = 11.sp, color = TextSecondary)
            }
        }

        Spacer(Modifier.width(12.dp))

        // Right: dish image + ADD button
        Box(contentAlignment = Alignment.BottomCenter) {
            // Dish image or placeholder icon
            if (dish.image != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(dish.image)
                        .crossfade(true)
                        .build(),
                    contentDescription = dish.name,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF5F5F5))
                )
            } else {
                // Placeholder matching khaoogully's fork/plate icon style
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🍽️", fontSize = 32.sp)
                }
            }

            // ADD + button overlapping bottom of image
            Surface(
                shape  = RoundedCornerShape(8.dp),
                color  = CardBg,
                shadowElevation = 3.dp,
                modifier = Modifier.offset(y = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clickable(
                            enabled = dish.isAvailable,
                            onClick = onOrderClick
                        )
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text       = if (dish.isAvailable) "ADD" else "N/A",
                        color      = if (dish.isAvailable) PrimaryGreen else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 14.sp
                    )
                    if (dish.isAvailable) {
                        Spacer(Modifier.width(4.dp))
                        Text("+", color = PrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
    // Extra bottom padding to prevent ADD button being clipped
    Spacer(Modifier.height(8.dp).background(CardBg))
}

// ─────────────────────────────────────────────────────────────────────────────
//  "Browse Menu" dialog  (bottom sheet style modal)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BrowseMenuDialog(
    categories: List<String>,
    onCategoryClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = CardBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false, onClick = {}) // consume clicks inside
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Browse Menu",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 20.sp,
                                color      = TextPrimary
                            )
                            Text(
                                "Select a category to jump to",
                                fontSize = 13.sp,
                                color    = TextSecondary
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, null, tint = TextPrimary)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Divider)

                    // Category list
                    categories.forEach { category ->
                        val dishCount = category  // we just have the key here
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCategoryClick(category) }
                                .padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(TextSecondary.copy(alpha = 0.4f), CircleShape)
                            )
                            Spacer(Modifier.width(14.dp))
                            Text(
                                text       = category,
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 14.sp,
                                color      = TextPrimary,
                                modifier   = Modifier.weight(1f)
                            )
                        }
                        HorizontalDivider(color = Divider)
                    }

                    Spacer(Modifier.height(12.dp))

                    // Close button
                    TextButton(
                        onClick  = onDismiss,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Close", color = TextSecondary, fontSize = 15.sp)
                    }

                    // Nav bar spacing
                    Spacer(Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Loading / Error
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MenuLoader() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = PrimaryGreen)
    }
}

@Composable
private fun MenuError(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
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