package com.kito.core.platform

import kotlinx.coroutines.flow.Flow

expect class SecureStorage() {
    suspend fun saveSapPassword(password: String): Boolean
    suspend fun getSapPassword(): String
    val isLoggedInFlow: Flow<Boolean>
    suspend fun clearSapPassword(): Boolean
}
