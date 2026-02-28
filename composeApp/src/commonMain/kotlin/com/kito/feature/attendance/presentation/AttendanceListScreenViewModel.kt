package com.kito.feature.attendance.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kito.core.database.entity.AttendanceEntity
import com.kito.core.database.repository.AttendanceRepository
import com.kito.core.datastore.PrefsRepository
import com.kito.core.platform.ConnectivityObserver
import com.kito.core.platform.SecureStorage
import com.kito.core.presentation.components.AppSyncUseCase
import com.kito.core.presentation.components.state.SyncUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AttendanceListScreenViewModel(
    private val attendanceRepository: AttendanceRepository,
    private val prefs: PrefsRepository,
    private val secureStorage: SecureStorage,
    private val appSyncUseCase: AppSyncUseCase,
    private val connectivityObserver: ConnectivityObserver
): ViewModel(){

    val isOnline = connectivityObserver.isOnline
    private val _syncState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val syncState = _syncState.asStateFlow()

    private val _syncEvents = MutableSharedFlow<SyncUiState>()
    val syncEvents: SharedFlow<SyncUiState> = _syncEvents

    fun refresh(){
        viewModelScope.launch {
            _syncState.value = SyncUiState.Loading

            val roll = prefs.userRollFlow.first()
            val sapPassword = secureStorage.getSapPassword()
            val year = prefs.academicYearFlow.first()
            val term = prefs.termCodeFlow.first()

            val result = appSyncUseCase.syncAll(
                roll = roll,
                sapPassword = sapPassword,
                year = year,
                term = term
            )

            _syncState.value = result.fold(
                onSuccess = {
                    _syncEvents.emit(SyncUiState.Success)
                    SyncUiState.Success
                },
                onFailure = {
                    SyncUiState.Error(it.message ?: "Sync failed")
                }
            )
        }
    }

    fun setSyncStateIdle(){
        _syncState.value = SyncUiState.Idle
    }

    val sapLoggedIn = secureStorage.isLoggedInFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )
    val attendance: StateFlow<List<AttendanceEntity>> =
        attendanceRepository
            .getAllAttendance()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
    val requiredAttendance = prefs.requiredAttendanceFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    private val _loginState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val loginState = _loginState.asStateFlow()

    val averageAttendancePercentage: StateFlow<Double> =
        attendance
            .map { list ->
                if (list.isEmpty()) {
                    0.0
                } else {
                    list.map { it.percentage }.average()
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0.0
            )
    val highestAttendancePercentage: StateFlow<Double> =
        attendance
            .map { list ->
                list.maxOfOrNull { it.percentage } ?: 0.0
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0.0
            )
    val lowestAttendancePercentage: StateFlow<Double> =
        attendance
            .map { list ->
                list.minOfOrNull { it.percentage } ?: 0.0
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0.0
            )

    fun login(
        password: String
    ){
        viewModelScope.launch {
            _loginState.value = SyncUiState.Loading
            val roll = prefs.userRollFlow.first()
            val year = prefs.academicYearFlow.first()
            val term = prefs.termCodeFlow.first()

            val result = appSyncUseCase.syncAll(
                roll = roll,
                sapPassword = password,
                year = year,
                term = term
            )

            _loginState.value = result.fold(
                onSuccess = {
                    secureStorage.saveSapPassword(password)
                    SyncUiState.Success
                },
                onFailure = {
                    SyncUiState.Error(it.message ?: "Sync failed")
                }
            )
        }
    }

    fun setLoginStateIdle(){
        _loginState.value = SyncUiState.Idle
    }
}




