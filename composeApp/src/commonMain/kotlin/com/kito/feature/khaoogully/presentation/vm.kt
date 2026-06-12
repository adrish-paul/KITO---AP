package com.kito.feature.khaoogully.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kito.feature.khaoogully.data.KgResult
import com.kito.feature.khaoogully.data.KhaoogullyRepository
import com.kito.feature.khaoogully.domain.model.KgCategory
import com.kito.feature.khaoogully.domain.model.KgDish
import com.kito.feature.khaoogully.domain.model.KgPopularDish
import com.kito.feature.khaoogully.domain.model.KgRestaurant
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FoodHomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val categories: List<KgCategory> = emptyList(),
    val popularDishes: List<KgPopularDish> = emptyList(),
    val restaurants: List<KgRestaurant> = emptyList(),
    val filteredRestaurants: List<KgRestaurant> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val selectedCampus: String? = null,
) {
    val availableCampuses: List<String>
        get() = restaurants.mapNotNull { it.campusName }.distinct().sorted()
}

data class MenuUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val restaurant: KgRestaurant? = null,
    val allDishes: List<KgDish> = emptyList(),
    val filteredDishes: List<KgDish> = emptyList(),
    val searchQuery: String = "",
    val expandedCategories: Set<String> = emptySet(),
) {
    val dishesByCategory: Map<String, List<KgDish>>
        get() = filteredDishes.groupBy { it.category ?: "OTHER" }
}

class KhaoogullyViewModel(
    private val repository: KhaoogullyRepository,
    private val dispatcher: kotlinx.coroutines.CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodHomeUiState())
    val uiState: StateFlow<FoodHomeUiState> = _uiState.asStateFlow()

    private val _menuState = MutableStateFlow(MenuUiState())
    val menuState: StateFlow<MenuUiState> = _menuState.asStateFlow()

    init { loadHomeData() }

    fun loadHomeData() {
        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val restaurantsDeferred = async { repository.getRestaurants() }
            val categoriesDeferred  = async { repository.getCategories() }
            val restaurantsResult = restaurantsDeferred.await()
            val categoriesResult  = categoriesDeferred.await()
            val error = (restaurantsResult as? KgResult.Error)?.message
            val restaurants = (restaurantsResult as? KgResult.Success)?.data ?: emptyList()
            _uiState.update {
                it.copy(
                    isLoading = false, error = error,
                    restaurants = restaurants, filteredRestaurants = restaurants,
                    categories = categoriesResult, selectedCampus = null,
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { s -> s.copy(searchQuery = query,
            filteredRestaurants = applyHomeFilters(s.restaurants, query, s.selectedCategory, s.selectedCampus)) }
    }

    fun clearCategory() {
        _uiState.update { s -> s.copy(selectedCategory = null,
            filteredRestaurants = applyHomeFilters(s.restaurants, s.searchQuery, null, s.selectedCampus)) }
    }

    fun onCategorySelected(category: KgCategory) {
        _uiState.update { s ->
            val newSel = if (s.selectedCategory == category.name) null else category.name
            s.copy(selectedCategory = newSel,
                filteredRestaurants = applyHomeFilters(s.restaurants, s.searchQuery, newSel, s.selectedCampus))
        }
    }

    fun onCampusSelected(campus: String?) {
        _uiState.update { s -> s.copy(selectedCampus = campus,
            filteredRestaurants = applyHomeFilters(s.restaurants, s.searchQuery, s.selectedCategory, campus)) }
    }

    private fun applyHomeFilters(
        restaurants: List<KgRestaurant>, query: String,
        selectedCategory: String?, selectedCampus: String?,
    ): List<KgRestaurant> {
        var result = restaurants
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter { r -> r.name.lowercase().contains(q) || r.cuisine.any { it.lowercase().contains(q) } }
        }
        if (selectedCategory != null) {
            result = result.filter { r -> r.cuisine.any { it.contains(selectedCategory, ignoreCase = true) || selectedCategory.contains(it, ignoreCase = true) } }
        }
        if (selectedCampus != null) result = result.filter { it.campusName == selectedCampus }
        return result
    }

    fun loadMenu(restaurant: KgRestaurant) {
        viewModelScope.launch(dispatcher) {
            _menuState.update { MenuUiState(isLoading = true, restaurant = restaurant) }
            when (val result = repository.getMenu(restaurant.id)) {
                is KgResult.Success -> {
                    val dishes = result.data.dishes
                    val firstCat = dishes.firstOrNull()?.category ?: ""
                    _menuState.update { it.copy(isLoading = false, allDishes = dishes, filteredDishes = dishes,
                        expandedCategories = if (firstCat.isNotEmpty()) setOf(firstCat) else emptySet()) }
                }
                is KgResult.Error -> _menuState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun onMenuSearchChange(query: String) {
        _menuState.update { s ->
            val filtered = if (query.isBlank()) s.allDishes
            else { val q = query.trim().lowercase(); s.allDishes.filter { it.name.lowercase().contains(q) } }
            val expanded = if (query.isNotBlank()) filtered.mapNotNull { it.category }.toSet() else s.expandedCategories
            s.copy(searchQuery = query, filteredDishes = filtered, expandedCategories = expanded)
        }
    }

    fun toggleCategory(category: String) {
        _menuState.update { s ->
            s.copy(expandedCategories = if (s.expandedCategories.contains(category))
                s.expandedCategories - category else s.expandedCategories + category)
        }
    }

    fun expandCategory(category: String) {
        _menuState.update { s -> s.copy(expandedCategories = s.expandedCategories + category) }
    }

    fun clearMenuState() { _menuState.value = MenuUiState() }

    fun buildRedirectUrl(restaurantId: String, dishId: String): String =
        repository.buildRedirectUrl(restaurantId, dishId)

    override fun onCleared() { super.onCleared(); repository.close() }
}
