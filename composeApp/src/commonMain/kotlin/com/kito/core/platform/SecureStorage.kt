package com.kito.core.platform

import kotlinx.coroutines.flow.Flow

expect class SecureStorage() {
    suspend fun saveKayaPassword(password: String): Boolean
    suspend fun getKayaPassword(): String
    suspend fun clearKayaPassword(): Boolean
    suspend fun saveSapPassword(password: String): Boolean
    suspend fun getSapPassword(): String
    val isLoggedInFlow: Flow<Boolean>
    suspend fun clearSapPassword(): Boolean
}
