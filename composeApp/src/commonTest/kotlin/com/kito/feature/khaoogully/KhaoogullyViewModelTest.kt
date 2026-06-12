package com.kito.feature.khaoogully

import com.kito.feature.khaoogully.data.KhaoogullyRepository
import com.kito.feature.khaoogully.presentation.KhaoogullyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class KhaoogullyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest fun setup() { Dispatchers.setMain(testDispatcher) }
    @AfterTest  fun teardown() { Dispatchers.resetMain() }

    // Use a real repo instance with empty keys (no network calls in tests — loadHomeData is
    // async and won't complete without a real HTTP response, so initial state is all we test)
    private val repo = KhaoogullyRepository(apiKey = "", baseUrl = "http://localhost")
    private fun vm() = KhaoogullyViewModel(repo, testDispatcher)

    @Test
    fun uiState_initiallyLoading() = runTest(testDispatcher) {
        val v = vm()
        assertTrue(v.uiState.value.isLoading)
    }

    @Test
    fun menuState_initiallyNotLoading() = runTest(testDispatcher) {
        assertFalse(vm().menuState.value.isLoading)
    }

    @Test
    fun menuState_restaurantInitiallyNull() = runTest(testDispatcher) {
        assertNull(vm().menuState.value.restaurant)
    }

    @Test
    fun clearMenuState_resetsState() = runTest(testDispatcher) {
        val v = vm()
        v.clearMenuState()
        assertFalse(v.menuState.value.isLoading)
        assertNull(v.menuState.value.restaurant)
    }
}
