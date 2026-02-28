package com.kito.core.presentation.navigation3

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Routes: NavKey {

    @Serializable
    object Tabs: Routes, NavKey

    @Serializable
    object Schedule: Routes, NavKey

    @Serializable
    data class FacultyDetail(val facultyId: Long) : Routes, NavKey
    @Serializable
    object ExamSchedule : Routes, NavKey

    @Serializable
    object Onboarding: Routes, NavKey

    @Serializable
    object UserSetup: Routes, NavKey

    @Serializable
    data class Promotions(val url: String): Routes, NavKey

    @Serializable
    object FriendView: Routes, NavKey

}

@Serializable
sealed interface TabRoutes: NavKey {

    @Serializable
    object Home : TabRoutes,NavKey

    @Serializable
    object Attendance : TabRoutes,NavKey

    @Serializable
    object Faculty : TabRoutes,NavKey

    @Serializable
    object Profile : TabRoutes,NavKey
}
