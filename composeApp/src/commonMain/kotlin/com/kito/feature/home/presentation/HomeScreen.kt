package com.kito.feature.home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.kito.core.common.util.currentLocalDateTime
import com.kito.core.platform.openUrl
import com.kito.core.platform.sendEmail
import com.kito.core.platform.toast
import com.kito.core.presentation.components.state.SyncUiState
import com.kito.core.presentation.navigation3.Routes
import com.kito.core.presentation.navigation3.TabRoutes
import com.kito.core.presentation.navigation3.isTopAsState
import com.kito.core.presentation.navigation3.navigateTab
import kotlinx.coroutines.delay
import kotlinx.datetime.DayOfWeek
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun HomeScreen(
    viewmodel: HomeViewModel = koinInject(),
    rootNavBackStack: NavBackStack<NavKey>,
    tabNavBackStack: NavBackStack<NavKey>,
) {
    val name by viewmodel.name.collectAsState()
    val sapLoggedIn by viewmodel.sapLoggedIn.collectAsState()
    val attendance by viewmodel.attendance.collectAsState()
    val schedule by viewmodel.schedule.collectAsState()
    val nextSchedule by viewmodel.nextSchedule.collectAsState()
    val syncState by viewmodel.syncState.collectAsState()
    val loginState by viewmodel.loginState.collectAsState()
    val isOnline by viewmodel.isOnline.collectAsState()
    val isTabTop by tabNavBackStack.isTopAsState(TabRoutes.Home)
    val isRootTop by rootNavBackStack.isTopAsState(Routes.Tabs)
    val isTopScreen = isTabTop && isRootTop
    val lifecycleOwner = LocalLifecycleOwner.current
    val eventsAndAds by viewmodel.ads.collectAsState()
    val isScheduleEmpty by viewmodel.isScheduleEmpty.collectAsState()
    val isKhaooGullyEnabled by viewmodel.isKhaooGullyEnabled.collectAsState()

    LaunchedEffect(Unit, isTopScreen, lifecycleOwner) {
        if (isTopScreen) {
            val today = currentLocalDateTime().dayOfWeek
            val dayString = when (today) {
                DayOfWeek.MONDAY -> "MON"
                DayOfWeek.TUESDAY -> "TUE"
                DayOfWeek.WEDNESDAY -> "WED"
                DayOfWeek.THURSDAY -> "THU"
                DayOfWeek.FRIDAY -> "FRI"
                DayOfWeek.SATURDAY -> "SAT"
                DayOfWeek.SUNDAY -> "SUN"
            }
            viewmodel.onEvent(HomeEvent.UpdateDay(dayString))
        }
        lifecycleOwner.lifecycle.repeatOnLifecycle(
            Lifecycle.State.STARTED
        ) {
            val today = currentLocalDateTime().dayOfWeek
            val dayString = when (today) {
                DayOfWeek.MONDAY -> "MON"
                DayOfWeek.TUESDAY -> "TUE"
                DayOfWeek.WEDNESDAY -> "WED"
                DayOfWeek.THURSDAY -> "THU"
                DayOfWeek.FRIDAY -> "FRI"
                DayOfWeek.SATURDAY -> "SAT"
                DayOfWeek.SUNDAY -> "SUN"
            }
            viewmodel.onEvent(HomeEvent.UpdateDay(dayString))
        }
    }

    LaunchedEffect(Unit) {
        delay(1000.milliseconds)
        if (isOnline) {
            viewmodel.onEvent(HomeEvent.SyncOnStartup)
        } else {
            toast("No Internet Connection")
        }
    }

    val haptic = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        viewmodel.syncEvents.collect { event ->
            when (event) {
                is SyncUiState.Success -> {
                    haptic.performHapticFeedback(HapticFeedbackType.ToggleOff)
                    toast("Sync completed")
                }
                is SyncUiState.Error -> {
                    haptic.performHapticFeedback(HapticFeedbackType.Reject)
                    toast(event.message)
                }
                is SyncUiState.Loading -> {
                    haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                }
                else -> {}
            }
        }
    }

    HomeContent(
        name = name,
        sapLoggedIn = sapLoggedIn,
        attendance = attendance,
        schedule = schedule,
        nextSchedule = nextSchedule,
        syncState = syncState,
        loginState = loginState,
        isScheduleEmpty = isScheduleEmpty,
        isKhaooGullyEnabled = isKhaooGullyEnabled,
        eventsAndAds = eventsAndAds,
        onReportClick = {
            sendEmail(
                to = "elabs.kiito@gmail.com",
                subject = "KIITO Schedule Report",
                body = ""
            )
        },
        onNavigateToSchedule = {
            rootNavBackStack.add(Routes.Schedule)
        },
        onNavigateToAttendance = {
            tabNavBackStack.navigateTab(TabRoutes.Attendance)
        },
        onNavigateToUtility = { navKey ->
            if (navKey != null) {
                rootNavBackStack.add(navKey)
            }
        },
        onNavigateToPromotion = { url ->
            rootNavBackStack.add(Routes.Promotions(url = url))
        },
        onOpenUrl = { url ->
            openUrl(url)
        },
        onLoginConfirm = { sapPassword ->
            viewmodel.onEvent(HomeEvent.Login(sapPassword))
        },
        onSetLoginStateIdle = {
            viewmodel.onEvent(HomeEvent.SetLoginStateIdle)
        }
    )
}
