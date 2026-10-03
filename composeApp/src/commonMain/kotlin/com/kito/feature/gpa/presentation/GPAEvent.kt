package com.kito.feature.gpa.presentation

sealed interface GPAEvent {
    data class UpdateSemester(val semester: Int) : GPAEvent
    data class UpdateBranch(val branch: String) : GPAEvent
}
