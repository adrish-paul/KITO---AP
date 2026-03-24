package com.kito.feature.khaoogully.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
//  Home UI State
// ─────────────────────────────────────────────────────────────────────────────

data class FoodHomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val categories: List<KgCategory> = emptyList(),
    val popularDishes: List<KgPopularDish> = emptyList(),
    val restaurants: List<KgRestaurant> = emptyList(),
    val filteredRestaurants: List<KgRestaurant> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null
)

// ─────────────────────────────────────────────────────────────────────────────
//  Menu UI State  (restaurant detail screen)
// ─────────────────────────────────────────────────────────────────────────────

data class MenuUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val restaurant: KgRestaurant? = null,
    /** All dishes from API */
    val allDishes: List<KgDish> = emptyList(),
    /** Dishes filtered by search query */
    val filteredDishes: List<KgDish> = emptyList(),
    val searchQuery: String = "",
    /**
     * Categories that are currently expanded.
     * On first load the first category is expanded by default.
     */
    val expandedCategories: Set<String> = emptySet()
) {
    /** Dishes grouped by category, respecting current filter */
    val dishesByCategory: Map<String, List<KgDish>>
        get() = filteredDishes
            .groupBy { it.category ?: "OTHER" }
}

// ─────────────────────────────────────────────────────────────────────────────
//  ViewModel
// ─────────────────────────────────────────────────────────────────────────────

class KhaoogullyViewModel(
    private val repository: KhaoogullyRepository
) : ViewModel() {

    // ── Home state ────────────────────────────────────────────────────────────
    private val _uiState = MutableStateFlow(FoodHomeUiState())
    val uiState: StateFlow<FoodHomeUiState> = _uiState.asStateFlow()

    // ── Menu / restaurant detail state ────────────────────────────────────────
    private val _menuState = MutableStateFlow(MenuUiState())
    val menuState: StateFlow<MenuUiState> = _menuState.asStateFlow()

    init {
        loadHomeData()
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Home screen
    // ─────────────────────────────────────────────────────────────────────────

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val restaurantsDeferred = async { repository.getRestaurants() }
            val categoriesDeferred  = async { repository.getCategories() }

            val restaurantsResult = restaurantsDeferred.await()
            val categoriesResult  = categoriesDeferred.await()

            val error = listOf(restaurantsResult, categoriesResult)
                .filterIsInstance<KgResult.Error>()
                .firstOrNull()?.message

            val restaurants = (restaurantsResult as? KgResult.Success)?.data ?: emptyList()

            _uiState.update {
                it.copy(
                    isLoading           = false,
                    error               = error,
                    restaurants         = restaurants,
                    filteredRestaurants = restaurants,
                    categories          = categoriesResult
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery         = query,
                filteredRestaurants = applyHomeFilters(state.restaurants, query, state.selectedCategory)
            )
        }
    }

    fun onCategorySelected(category: KgCategory) {
        _uiState.update { state ->
            val newSelected = if (state.selectedCategory == category.name) null else category.name
            state.copy(
                selectedCategory    = newSelected,
                filteredRestaurants = applyHomeFilters(state.restaurants, state.searchQuery, newSelected)
            )
        }
    }

    private fun applyHomeFilters(
        restaurants: List<KgRestaurant>,
        query: String,
        selectedCategory: String?
    ): List<KgRestaurant> {
        var result = restaurants
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter { r ->
                r.name.lowercase().contains(q) ||
                        r.cuisine.any { it.lowercase().contains(q) }
            }
        }
        if (selectedCategory != null) {
            result = result.filter { r ->
                r.cuisine.any { it.equals(selectedCategory, ignoreCase = true) }
            }
        }
        return result
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Restaurant menu screen
    // ─────────────────────────────────────────────────────────────────────────

    fun loadMenu(restaurant: KgRestaurant) {
        viewModelScope.launch {
            _menuState.update {
                MenuUiState(isLoading = true, restaurant = restaurant)
            }

            when (val result = repository.getMenu(restaurant.id)) {
                is KgResult.Success -> {
                    val dishes = result.data.dishes
                    // Auto-expand the first category
                    val firstCategory = dishes.firstOrNull()?.category ?: ""
                    _menuState.update {
                        it.copy(
                            isLoading          = false,
                            allDishes          = dishes,
                            filteredDishes     = dishes,
                            expandedCategories = if (firstCategory.isNotEmpty()) setOf(firstCategory) else emptySet()
                        )
                    }
                }
                is KgResult.Error -> {
                    _menuState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun onMenuSearchChange(query: String) {
        _menuState.update { state ->
            val filtered = if (query.isBlank()) {
                state.allDishes
            } else {
                val q = query.trim().lowercase()
                state.allDishes.filter { it.name.lowercase().contains(q) }
            }
            // Auto-expand all categories when searching
            val expanded = if (query.isNotBlank()) {
                filtered.mapNotNull { it.category }.toSet()
            } else {
                state.expandedCategories
            }
            state.copy(
                searchQuery        = query,
                filteredDishes     = filtered,
                expandedCategories = expanded
            )
        }
    }

    fun toggleCategory(category: String) {
        _menuState.update { state ->
            val current = state.expandedCategories
            state.copy(
                expandedCategories = if (current.contains(category)) {
                    current - category
                } else {
                    current + category
                }
            )
        }
    }

    fun expandCategory(category: String) {
        _menuState.update { state ->
            state.copy(expandedCategories = state.expandedCategories + category)
        }
    }

    fun clearMenuState() {
        _menuState.value = MenuUiState()
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Shared
    // ─────────────────────────────────────────────────────────────────────────

    fun buildRedirectUrl(restaurantId: String, dishId: String): String =
        repository.buildRedirectUrl(restaurantId, dishId)

    override fun onCleared() {
        super.onCleared()
        repository.close()
    }
}