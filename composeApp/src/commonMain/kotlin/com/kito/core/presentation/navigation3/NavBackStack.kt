package com.kito.core.presentation.navigation3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.snapshotFlow
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

@Composable
fun NavBackStack<NavKey>.isTopAsState(key: NavKey): State<Boolean> {
    return produceState(initialValue = lastOrNull() == key, this, key) {
        snapshotFlow { lastOrNull() }
            .collect { top ->
                value = top == key
            }
    }
}