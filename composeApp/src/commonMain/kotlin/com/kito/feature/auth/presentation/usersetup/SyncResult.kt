package com.kito.feature.auth.presentation.usersetup

sealed class SyncResult {
    object Success : SyncResult()
    data class Error(val message: String) : SyncResult()
}
