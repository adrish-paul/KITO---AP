package com.kito.feature.attendance.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kito.core.datastore.PrefsRepository
import com.kito.core.platform.ConnectivityObserver
import com.kito.core.platform.SecureStorage
import com.kito.core.presentation.components.state.SyncUiState
import com.kito.core.sync.domain.SyncUseCase
import com.kito.feature.attendance.domain.model.Attendance
import com.kito.feature.attendance.domain.model.AttendanceSummary
import com.kito.feature.attendance.domain.usecase.GetAttendanceSummaryUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
import org.koin.core.annotation.Provided

class AttendanceListScreenViewModel(
    private val getAttendanceSummary: GetAttendanceSummaryUseCase,
    private val prefs: PrefsRepository,
    @Provided private val secureStorage: SecureStorage,
    private val appSyncUseCase: SyncUseCase,
    @Provided private val connectivityObserver: ConnectivityObserver,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
): ViewModel(){

    val isOnline = connectivityObserver.isOnline
    private val _syncState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val syncState = _syncState.asStateFlow()

    private val _syncEvents = MutableSharedFlow<SyncUiState>()
    val syncEvents: SharedFlow<SyncUiState> = _syncEvents

    fun refresh(){
        viewModelScope.launch(dispatcher) {
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
    private val summary: StateFlow<AttendanceSummary> =
        getAttendanceSummary()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AttendanceSummary.Empty
            )

    val attendance: StateFlow<List<Attendance>> =
        summary
            .map { it.items }
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
        summary.map { it.averagePercentage }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)
    val highestAttendancePercentage: StateFlow<Double> =
        summary.map { it.highestPercentage }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)
    val lowestAttendancePercentage: StateFlow<Double> =
        summary.map { it.lowestPercentage }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    fun login(
        password: String
    ){
        viewModelScope.launch(dispatcher) {
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




