package com.kito.core.presentation.navigation3

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun NavBackStack<NavKey>.navigateTab(
    route: NavKey,
) {
    if (lastOrNull() == route) return
    while (size > 1) {
        removeAt(size - 1)
    }
    if (lastOrNull() != route) {
        add(route)
    }
}