package com.kito.core.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

actual class SecureStorage {
    private var password: String = ""
    actual val isLoggedInFlow: Flow<Boolean> = MutableStateFlow(false)
    actual suspend fun saveSapPassword(password: String): Boolean {
        this.password = password; return true
    }
    actual suspend fun getSapPassword(): String = password
    actual suspend fun clearSapPassword(): Boolean {
        password = ""; return true
    }
}
