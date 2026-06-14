package com.kito.feature.home.presentation

sealed interface HomeEvent {
    data object SyncOnStartup : HomeEvent
    data class Login(val password: String) : HomeEvent
    data object SetLoginStateIdle : HomeEvent
    data class UpdateDay(val day: String) : HomeEvent
}
