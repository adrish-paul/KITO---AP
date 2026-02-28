package com.kito.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.kito.core.database.AppDB
import com.kito.core.database.entity.StudentEntity
import com.kito.core.database.entity.toAttendanceEntity
import com.kito.core.database.repository.SectionRepository
import com.kito.core.database.repository.StudentRepository
import com.kito.core.datastore.PrefsRepository
import com.kito.core.network.supabase.SupabaseRepository
import com.kito.core.platform.SecureStorage
import com.kito.core.presentation.components.AppSyncUseCase
import com.kito.sap.SapRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.runCatching

class UserSetupViewModel(
    private val prefs: PrefsRepository,
    private val secureStorage: SecureStorage,
    private val appSyncUseCase: AppSyncUseCase,
    private val supaBaseRepo: SupabaseRepository,
    private val db: AppDB,
    private val studentRepository: StudentRepository,
    private val sectionRepository: SectionRepository
) : ViewModel(){
    private val _setupState = MutableStateFlow<SetupState>(SetupState.Idle)
    val setupState = _setupState.asStateFlow()
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
        viewModelScope.launch {
            _setupState.value = SetupState.Loading
            try {
                setUserName(name)
                setUserRoll(roll)
                setAcademicYear(year)
                setTermCode(term)
                appSyncUseCase.scheduleSync(roll)
                setUserSetupDone()
                _setupState.value = SetupState.Success
            } catch (e: Exception) {
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



