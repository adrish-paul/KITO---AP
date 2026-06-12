package com.kito.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kito.core.auth.AuthEvent
import com.kito.core.auth.AuthRepository
import com.kito.core.auth.AuthState
import com.kito.core.common.util.currentLocalDateTime
import com.kito.core.datastore.PrefsRepository
import com.kito.core.platform.SecureStorage
import com.kito.core.sync.domain.SyncUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.number
import org.koin.core.annotation.Provided

class UserSetupViewModel(
    private val prefs: PrefsRepository,
    @Provided private val secureStorage: SecureStorage,
    private val appSyncUseCase: SyncUseCase,
    @Provided private val authRepository: AuthRepository,
    private val dispatcher: kotlinx.coroutines.CoroutineDispatcher = kotlinx.coroutines.Dispatchers.Default,
) : ViewModel(){
    private val _setupState = MutableStateFlow<SetupState>(SetupState.Idle)
    val setupState = _setupState.asStateFlow()

    // Tracks which button triggered loading so the UI can show the spinner on the right button.
    private val _loadingSource = MutableStateFlow<LoadingSource>(LoadingSource.None)
    val loadingSource = _loadingSource.asStateFlow()

    init {
        // Replace-and-gate: when Google sign-in succeeds (and passes the @kiit.ac.in / allowlist
        // gate inside AuthRepository), auto-fill identity and complete setup.
        viewModelScope.launch(dispatcher) {
            authRepository.authState.collect { state ->
                if (state is AuthState.Authenticated && _setupState.value != SetupState.Success) {
                    _loadingSource.value = LoadingSource.None
                    val (year, term) = deriveYearTerm()
                    completeSetup(
                        name = state.user.name,
                        roll = state.user.rollNumber,
                        year = year,
                        term = term
                    )
                }
            }
        }
        // Surface sign-in rejections/failures in the existing error UI.
        viewModelScope.launch(dispatcher) {
            authRepository.events.collect { event ->
                _loadingSource.value = LoadingSource.None
                _setupState.value = when (event) {
                    is AuthEvent.NotAllowed -> SetupState.Error("Only @kiit.ac.in accounts can sign in")
                    is AuthEvent.Failure -> SetupState.Error(event.message)
                    AuthEvent.Cancelled -> SetupState.Idle
                }
            }
        }
    }

    /** Redirect (browser) fallback — used when native Credential Manager isn't available. */
    fun signInWithGoogle() {
        _setupState.value = SetupState.Loading
        viewModelScope.launch(dispatcher) { authRepository.signInWithGoogle() }
    }

    /** Native account-picker flow launched; show loading until result/session arrives. */
    fun onSignInStarted() {
        _loadingSource.value = LoadingSource.Google
        _setupState.value = SetupState.Loading
    }

    fun onSignInError(message: String) {
        _loadingSource.value = LoadingSource.None
        _setupState.value = SetupState.Error(message)
    }

    fun onSignInCancelled() {
        _loadingSource.value = LoadingSource.None
        _setupState.value = SetupState.Idle
    }

    private fun deriveYearTerm(): Pair<String, String> {
        val now = currentLocalDateTime()
        val month = now.month.number
        val year = if (month < 5) now.year - 1 else now.year
        val term = when (month) {
            12, 1, 2, 3, 4 -> "020"
            in 7..11 -> "010"
            else -> "020"
        }
        return year.toString() to term
    }
    suspend fun setUserName(name: String) {
        val formattedName = name
            .trim()
            .replace("\\s+".toRegex(), " ")
            .lowercase()
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercaseChar() }
            }

        prefs.setUserName(formattedName)
    }
    suspend fun setUserRoll(roll: String){
        prefs.setUserRollNumber(roll)
    }
    suspend fun setSapPassword(sapPassword: String){
        secureStorage.saveSapPassword(sapPassword)
    }
    suspend fun setUserSetupDone() {
        prefs.setUserSetupDone()
    }
    suspend fun setAcademicYear(year: String) {
        prefs.setAcademicYear(year)
    }
    suspend fun setTermCode(term: String) {
        prefs.setTermCode(term)
    }

    fun completeSetup(
        name: String,
        roll: String,
        year: String = "2025",
        term: String = "020"
    ) {
        viewModelScope.launch(dispatcher) {
            _loadingSource.value = LoadingSource.Manual
            _setupState.value = SetupState.Loading
            try {
                setUserName(name)
                setUserRoll(roll)
                setAcademicYear(year)
                setTermCode(term)
                appSyncUseCase.scheduleSync(roll)
                setUserSetupDone()
                _loadingSource.value = LoadingSource.None
                _setupState.value = SetupState.Success
            } catch (e: Exception) {
                _loadingSource.value = LoadingSource.None
                _setupState.value = SetupState.Error(
                    e.message ?: "Something went wrong"
                )
            }
        }
    }

}

sealed class SyncResult {
    object Success : SyncResult()
    data class Error(val message: String) : SyncResult()
}
sealed class SetupState {
    object Idle : SetupState()
    object Loading : SetupState()
    object Success : SetupState()
    data class Error(val message: String) : SetupState()
}

enum class LoadingSource { None, Manual, Google }



