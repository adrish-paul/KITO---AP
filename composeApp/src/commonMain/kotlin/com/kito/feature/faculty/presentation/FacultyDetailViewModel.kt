package com.kito.feature.faculty.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kito.core.presentation.components.state.SyncUiState
import com.kito.feature.faculty.domain.model.Faculty
import com.kito.feature.faculty.domain.model.FacultyScheduleSlot
import com.kito.feature.faculty.domain.repository.FacultyRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FacultyDetailViewModel(
    private val repository: FacultyRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val _faculty = MutableStateFlow<Faculty?>(null)
    val faculty = _faculty.asStateFlow()
    private val _schedule = MutableStateFlow<List<FacultyScheduleSlot>>(emptyList())
    val schedule = _schedule.asStateFlow()
    private val _syncState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val syncState = _syncState.asStateFlow()

    fun loadFacultyDetail(facultyId: Long) {
        viewModelScope.launch(dispatcher) {
            _syncState.value = SyncUiState.Loading
            try {
                val facultyDeferred = async { repository.getFacultyById(facultyId) }
                val scheduleDeferred = async { repository.getFacultySchedule(facultyId) }
                _faculty.value = facultyDeferred.await()
                _schedule.value = scheduleDeferred.await()
                _syncState.value = SyncUiState.Success
            } catch (e: Exception) {
                println("FacultyDetail: FacultyDetailLoadingError: ${e.message ?: ""}")
                _syncState.value = SyncUiState.Error(message = e.message ?: "")
            }
        }
    }
}
