package com.kito.feature.faculty.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kito.core.platform.ConnectivityObserver
import com.kito.core.presentation.components.state.SearchResultState
import com.kito.core.presentation.components.state.SyncUiState
import com.kito.feature.faculty.domain.model.Faculty
import com.kito.feature.faculty.domain.repository.FacultyRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Provided

class FacultyScreenViewModel(
    private val repository: FacultyRepository,
    @Provided private val connectivityObserver: ConnectivityObserver,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    val isOnline = connectivityObserver.isOnline

    private val _faculty = MutableStateFlow<List<Faculty>>(emptyList())
    val faculty = _faculty.asStateFlow()

    private val _searchResultState =
        MutableStateFlow<SearchResultState>(SearchResultState.Idle)
    val searchResultState = _searchResultState.asStateFlow()

    private val _facultySearchResult =
        MutableStateFlow<List<Faculty>>(emptyList())
    val facultySearchResult = _facultySearchResult.asStateFlow()

    private val _syncState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val syncState = _syncState.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            isOnline.collect { online ->
                if (!online) {
                    _syncState.value = SyncUiState.Idle
                    _faculty.value = emptyList()
                } else {
                    fetchFaculty()
                }
            }
        }
    }

    private suspend fun fetchFaculty() {
        _syncState.value = SyncUiState.Loading
        try {
            _faculty.value = repository.getAllFaculty()
            _syncState.value = SyncUiState.Success
        } catch (e: Exception) {
            _syncState.value =
                SyncUiState.Error(e.message ?: "Failed to load faculty")
        }
    }

    fun retry() {
        viewModelScope.launch(dispatcher) {
            if (isOnline.value) {
                fetchFaculty()
            } else {
                _syncState.value = SyncUiState.Idle
            }
        }
    }

    fun getSearchResult(query: String) {
        viewModelScope.launch(dispatcher) {
            if (query.isEmpty()) {
                _facultySearchResult.value = emptyList()
                _searchResultState.value = SearchResultState.Idle
            } else {
                val result = repository.searchFaculty(query)
                _facultySearchResult.value = result
                _searchResultState.value =
                    if (result.isEmpty()) SearchResultState.Empty
                    else SearchResultState.Success
            }
        }
    }

    fun clearSearchResult() {
        _facultySearchResult.value = emptyList()
        _searchResultState.value = SearchResultState.Idle
    }
}


