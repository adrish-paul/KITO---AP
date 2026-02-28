package com.kito.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.kito.core.database.repository.AttendanceRepository
import com.kito.core.datastore.PrefsRepository
import com.kito.core.platform.SecureStorage
import com.kito.core.presentation.components.AppSyncUseCase
import com.kito.core.presentation.components.state.SyncUiState
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

class SettingsViewModel(
    private val prefs: PrefsRepository,
    private val secureStorage: SecureStorage,
    private val attendanceRepository: AttendanceRepository,
    private val appSyncUseCase: AppSyncUseCase,
    private val notificationController: NotificationController
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
        viewModelScope.launch {
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
                delay(1000)
                prefs.setUserName(formattedName)
                _syncState.value = SyncUiState.Success
            } catch (e: Exception) {
                _syncState.value = SyncUiState.Error(e.message ?: "Sync failed")
            }
        }
    }
    fun changeRoll(roll: String){
        viewModelScope.launch {
            val result = runCatching {
                _syncState.value = SyncUiState.Loading
                delay(1000)
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
        viewModelScope.launch {
            try {
                _syncState.value = SyncUiState.Loading
                delay(1000)
                prefs.setRequiredAttendance(attendance)
                _syncState.value = SyncUiState.Success
            }catch (e: Exception) {
                _syncState.value = SyncUiState.Error(e.message ?: "Sync failed")
            }
        }
    }
    fun changeYearTerm(year: String, term: String) {
        viewModelScope.launch {
            try {
                _syncState.value = SyncUiState.Loading
                delay(1000)
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
        viewModelScope.launch {
            _syncState.value = SyncUiState.Loading
            delay(1000)
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
        viewModelScope.launch {
            _syncState.value = SyncUiState.Loading
            delay(1000)
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
                    val friendlyMessage = when {
                        it.message?.contains("Unable to resolve host") == true -> 
                            "Network error: Cannot connect to server. Check your internet connection."
                        it.message?.contains("supabase") == true -> 
                            "Network error: Cannot reach server. Please try again."
                        else -> "Login failed: Unknown Error"
                    }
                    SyncUiState.Error(friendlyMessage)
                }
            )
        }
    }
    fun setNotificationState(state: Boolean){
        viewModelScope.launch {
            prefs.setNotificationState(state)
            notificationController.sync()
        }
    }
}
