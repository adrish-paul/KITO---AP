package com.kito.core.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

actual class SecureStorage {
    private var kayaPassword = ""
    actual suspend fun saveKayaPassword(password: String): Boolean {
        kayaPassword = password
        return true
    }
    actual suspend fun getKayaPassword(): String = kayaPassword
    actual suspend fun clearKayaPassword(): Boolean {
        kayaPassword = ""
        return true
    }
    private var password: String = ""
    private val _isLoggedInFlow = MutableStateFlow(false)
    actual val isLoggedInFlow: Flow<Boolean> = _isLoggedInFlow

    actual suspend fun saveSapPassword(password: String): Boolean {
        this.password = password
        _isLoggedInFlow.value = password.isNotEmpty()
        return true
    }
    actual suspend fun getSapPassword(): String = password
    actual suspend fun clearSapPassword(): Boolean {
        password = ""
        _isLoggedInFlow.value = false
        return true
    }
}
