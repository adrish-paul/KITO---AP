package com.kito.core.platform

import kotlinx.coroutines.flow.StateFlow

expect class ConnectivityObserver() {
    val isOnline: StateFlow<Boolean>
}
