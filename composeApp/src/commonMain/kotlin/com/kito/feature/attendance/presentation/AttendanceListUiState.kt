package com.kito.feature.attendance.presentation

import com.kito.core.presentation.components.state.SyncUiState
import com.kito.feature.attendance.domain.model.Attendance

data class AttendanceListUiState(
    val attendance: List<Attendance> = emptyList(),
    val sapLoggedIn: Boolean = false,
    val syncState: SyncUiState = SyncUiState.Idle,
    val loginState: SyncUiState = SyncUiState.Idle,
    val requiredAttendance: Int = 75,
    val isOnline: Boolean = true,
    val averageAttendancePercentage: Double = 0.0,
    val highestAttendancePercentage: Double = 0.0,
    val lowestAttendancePercentage: Double = 0.0,
)
