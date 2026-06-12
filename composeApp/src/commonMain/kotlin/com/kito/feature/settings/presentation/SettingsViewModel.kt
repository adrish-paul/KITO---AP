package com.kito.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kito.core.datastore.PrefsRepository
import com.kito.core.platform.SecureStorage
import com.kito.core.presentation.components.state.SyncUiState
import com.kito.core.sync.domain.SyncUseCase
import com.kito.feature.attendance.domain.repository.AttendanceRepository
import com.kito.feature.schedule.notification.NotificationController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Provided
import kotlin.time.Duration.Companion.milliseconds

class SettingsViewModel(
    private val prefs: PrefsRepository,
    @Provided private val secureStorage: SecureStorage,
    private val attendanceRepository: AttendanceRepository,
    private val appSyncUseCase: SyncUseCase,
    private val notificationController: NotificationController,
    @Provided private val authRepository: com.kito.core.auth.AuthRepository,
    private val dispatcher: kotlinx.coroutines.CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Default,
): ViewModel(){
    private val _syncState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val syncState = _syncState.asStateFlow()
    val name = prefs.userNameFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ""
    )
    val rollNumber = prefs.userRollFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ""
    )
    val year = prefs.academicYearFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ""
    )
    val term: StateFlow<String> =
        prefs.termCodeFlow
            .map { code ->
                when (code) {
                    "010" -> "Autumn"
                    "020" -> "Spring"
                    else -> "Unknown"
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = "Unknown"
            )
    val requiredAttendance = prefs.requiredAttendanceFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )
    val isLoggedIn = secureStorage.isLoggedInFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
            )
    val notificationState = prefs.notificationStateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    private val _pendingNotificationEnable = MutableStateFlow(false)
    val pendingNotificationEnable = _pendingNotificationEnable.asStateFlow()

    fun requestEnableNotifications() {
        _pendingNotificationEnable.value = true
    }

    fun clearPendingNotificationEnable() {
        _pendingNotificationEnable.value = false
    }

    fun retryPendingNotificationEnable() {
        if (pendingNotificationEnable.value) {
            _pendingNotificationEnable.value = true
        }
    }

    fun syncStateIdle(){
        _syncState.value = SyncUiState.Idle
    }
    fun changeName(name: String){
        viewModelScope.launch(dispatcher) {
            try {
                _syncState.value = SyncUiState.Loading
                val formattedName = name
                    .trim()
                    .replace("\\s+".toRegex(), " ")
                    .lowercase()
                    .split(" ")
                    .joinToString(" ") { word ->
                        word.replaceFirstChar { it.uppercaseChar() }
                    }
                delay(1000.milliseconds)
                prefs.setUserName(formattedName)
                authRepository.updateDisplayName(formattedName)
                _syncState.value = SyncUiState.Success
            } catch (e: Exception) {
                _syncState.value = SyncUiState.Error(e.message ?: "Sync failed")
            }
        }
    }
    fun changeRoll(roll: String){
        viewModelScope.launch(dispatcher) {
            val result = runCatching {
                _syncState.value = SyncUiState.Loading
                delay(1000.milliseconds)
                prefs.setUserRollNumber(roll)
                secureStorage.clearSapPassword()
                attendanceRepository.deleteAllAttendance()
                appSyncUseCase.scheduleSync(
                    roll = roll
                )
            }
            result.fold(
                onSuccess = {
                    _syncState.value = SyncUiState.Success
                },
                onFailure = {
                    _syncState.value = SyncUiState.Error(it.message ?: "Sync Failed")
                }
            )
        }
    }

    fun changeAttendance(attendance: Int){
        viewModelScope.launch(dispatcher) {
            try {
                _syncState.value = SyncUiState.Loading
                delay(1000.milliseconds)
                prefs.setRequiredAttendance(attendance)
                _syncState.value = SyncUiState.Success
            }catch (e: Exception) {
                _syncState.value = SyncUiState.Error(e.message ?: "Sync failed")
            }
        }
    }
    fun changeYearTerm(year: String, term: String) {
        viewModelScope.launch(dispatcher) {
            try {
                _syncState.value = SyncUiState.Loading
                delay(1000.milliseconds)
                prefs.setAcademicYear(year)
                prefs.setTermCode(term)
                attendanceRepository.deleteAllAttendance()
                val result = appSyncUseCase.syncAll(
                    roll = prefs.userRollFlow.first(),
                    sapPassword = secureStorage.getSapPassword(),
                    year = year,
                    term = term
                )
                _syncState.value = result.fold(
                    onSuccess = {
                        SyncUiState.Success
                    },
                    onFailure = {
                        SyncUiState.Error(it.message ?: "Sync failed")
                    }
                )
            } catch (e: Exception) {
                _syncState.value = SyncUiState.Error(e.message ?: "Sync failed")
            }
        }
    }
    fun logOut(){
        viewModelScope.launch(dispatcher) {
            _syncState.value = SyncUiState.Loading
            delay(1000.milliseconds)
            try {
                secureStorage.clearSapPassword()
                attendanceRepository.deleteAllAttendance()
                _syncState.value = SyncUiState.Success
            } catch (e: Exception) {
                _syncState.value = SyncUiState.Error(e.message ?: "Sync failed")
            }
        }
    }
    fun logIn(password: String) {
        viewModelScope.launch(dispatcher) {
            _syncState.value = SyncUiState.Loading
            delay(1000.milliseconds)
            val roll = prefs.userRollFlow.first()
            val year = prefs.academicYearFlow.first()
            val term = prefs.termCodeFlow.first()

            val result = appSyncUseCase.syncAll(
                roll = roll,
                sapPassword = password,
                year = year,
                term = term
            )

            _syncState.value = result.fold(
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
    fun setNotificationState(state: Boolean){
        viewModelScope.launch(dispatcher) {
            prefs.setNotificationState(state)
            notificationController.sync()
        }
    }
}
