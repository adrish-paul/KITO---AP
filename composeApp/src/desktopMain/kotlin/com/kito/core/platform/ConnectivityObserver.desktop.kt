package com.kito.core.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual class ConnectivityObserver {
    actual val isOnline: StateFlow<Boolean> = MutableStateFlow(true)
}
