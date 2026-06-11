package com.kito.feature.home.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.kito.core.common.util.currentLocalDateTime
import com.kito.core.designsystem.AboutELabsDialog
import com.kito.core.designsystem.UIColors
import com.kito.core.designsystem.UtilityCard
import com.kito.core.platform.openUrl
import com.kito.core.platform.sendEmail
import com.kito.core.platform.toast
import com.kito.core.presentation.components.state.SyncUiState
import com.kito.core.presentation.navigation3.Routes
import com.kito.core.presentation.navigation3.TabRoutes
import com.kito.core.presentation.navigation3.isTopAsState
import com.kito.core.presentation.navigation3.navigateTab
import com.kito.feature.attendance.presentation.components.AttendanceBarCard
import com.kito.feature.home.presentation.components.EventAndAdBanner
import com.kito.feature.schedule.presentation.components.ScheduleCard
import com.kito.feature.settings.presentation.components.LoginDialogBox
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import kito.composeapp.generated.resources.Res
import kito.composeapp.generated.resources.e_labs_logo
import kotlinx.coroutines.delay
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalHazeApi::class,
    ExperimentalHazeMaterialsApi::class
)
@Composable
fun HomeScreen(
    viewmodel: HomeViewModel = koinInject(),
    rootNavBackStack: NavBackStack<NavKey>,
    tabNavBackStack: NavBackStack<NavKey>,
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    val uiColors = UIColors()
    val name by viewmodel.name.collectAsState()
    val sapLoggedIn by viewmodel.sapLoggedIn.collectAsState()
    val attendance by viewmodel.attendance.collectAsState()
    val schedule by viewmodel.schedule.collectAsState()
    val nextSchedule by viewmodel.nextSchedule.collectAsState()
    val syncState by viewmodel.syncState.collectAsState()
    val hazeState = rememberHazeState()
    val haptic = LocalHapticFeedback.current
    var isLoginDialogOpen by remember { mutableStateOf(false) }
    val loginState by viewmodel.loginState.collectAsState()
    val isOnline by viewmodel.isOnline.collectAsState()
    val isTabTop by tabNavBackStack.isTopAsState(TabRoutes.Home)
    val isRootTop by rootNavBackStack.isTopAsState(Routes.Tabs)
    val isTopScreen = isTabTop && isRootTop
    val lifecycleOwner = LocalLifecycleOwner.current
    val eventsAndAds by viewmodel.ads.collectAsState()
    val isScheduleEmpty by viewmodel.isScheduleEmpty.collectAsState()
    val isKhaooGullyEnabled by viewmodel.isKhaooGullyEnabled.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is SyncUiState.Success) {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            isLoginDialogOpen = false
            viewmodel.setLoginStateIdle()
        }
    }

    LaunchedEffect(Unit,isTopScreen,lifecycleOwner) {
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
            viewmodel.updateDay(dayString)
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
            viewmodel.updateDay(dayString)
        }
    }

    LaunchedEffect(Unit) {
        delay(1000)
        if (isOnline) {
            viewmodel.syncOnStartup()
        }else{
            toast("No Internet Connection")
        }
    }

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

    Box() {
        Box(
            modifier = Modifier
                .hazeSource(hazeState)
        ) {
            Box(
                Modifier
                    .background(Color(0xFF121116))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
//                        .padding(horizontal = 12.dp)
                ) {
                    LazyColumn() {
                        item {
                            Spacer(
                                modifier = Modifier.height(
                                    72.dp + WindowInsets.statusBars.asPaddingValues()
                                        .calculateTopPadding()
                                )
                            )
                        }

                        item {
                            AnimatedVisibility(syncState is SyncUiState.Loading) {
                                Column {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearWavyProgressIndicator(
                                        color = uiColors.accentOrangeStart,
                                        trackColor = uiColors.progressAccent,
                                        modifier = Modifier.fillMaxWidth(),
                                        waveSpeed = 5.dp,
                                        wavelength = 70.dp,
                                    )
                                }
                            }
                        }


                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = "Schedule",
                                    color = uiColors.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .weight(1f)
                                )
                                if (false) {
                                    IconButton(
                                        onClick = {

                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (false) {
                                                Icons.Default.NotificationsActive
                                            } else {
                                                Icons.Outlined.NotificationsOff
                                            },
                                            contentDescription = "notifications",
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                IconButton(
                                    onClick = {
                                        sendEmail(
                                            to = "elabs.kiito@gmail.com",
                                            subject = "KIITO Schedule Report",
                                            body = ""
                                        )
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        contentColor = Color(0xFFB32727)
                                    ),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Report,
                                        contentDescription = "Report",
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                        rootNavBackStack.add(
                                            Routes.Schedule
                                        )
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                                        contentDescription = "Notifications",
                                        tint = uiColors.textPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }



                        item {
                            Spacer(Modifier.height(8.dp))
                        }

                        item {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                            ) {
                                ScheduleCard(
                                    colors = uiColors,
                                    schedule = schedule,
                                    nextSchedule = nextSchedule,
                                    isScheduleEmpty = isScheduleEmpty,
                                    onCLick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                        rootNavBackStack.add(Routes.Schedule)
                                    }
                                )
                            }
                        }
                        item {
                            Spacer(Modifier.height(8.dp))
                        }
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                ) {
                                    Text(
                                        text = "Utilities",
                                        color = uiColors.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (false) {
                                        IconButton(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                                tabNavBackStack.navigateTab(TabRoutes.Attendance)
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                                                contentDescription = "Back",
                                                tint = uiColors.textPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            item {
                                Spacer(Modifier.height(8.dp))
                            }

                            item {
                                Box(
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ){
                                    UtilityCard(
                                        onCLick = { navKey ->
                                            if (navKey != null) {
                                                rootNavBackStack.add(navKey)
                                            }
                                        },
                                            isKhaooGullyEnabled = isKhaooGullyEnabled
                                    )
                                }
                            }

                            item {
                                Spacer(Modifier.height(8.dp))
                            }

                        if (eventsAndAds.isNotEmpty()) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                ) {
                                    Text(
                                        text = "Events",
                                        color = uiColors.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier
                                            .weight(1f)
                                    )
                                }
                            }

                            item {
                                Spacer(Modifier.height(8.dp))
                            }

                            item {
                                EventAndAdBanner(
                                    eventsAndAds = eventsAndAds,
                                    onClick = {url, isAd ->
                                        if (isAd) {
                                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                            rootNavBackStack.add(
                                                Routes.Promotions(
                                                    url = url
                                                )
                                            )
                                        }else{
                                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                            openUrl(url)
                                        }
                                    }
                                )
                            }

                            item {
                                Spacer(Modifier.height(8.dp))
                            }
                        }

                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                            ) {
                                Text(
                                    text = "Attendance",
                                    color = uiColors.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                        tabNavBackStack.navigateTab(TabRoutes.Attendance)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Default.ArrowForwardIos,
                                        contentDescription = "Back",
                                        tint = uiColors.textPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        item {
                            Spacer(Modifier.height(8.dp))
                        }
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(horizontal = 12.dp)
                            ) {
                                AttendanceBarCard(
                                    attendance = attendance,
                                    onNavigate = {
                                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                        tabNavBackStack.navigateTab(TabRoutes.Attendance)
                                    },
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                        isLoginDialogOpen = true
                                    },
                                    sapLoggedIn = sapLoggedIn
                                )
                            }
                        }
                        item {
                            Spacer(
                                modifier = Modifier.height(
                                    86.dp + WindowInsets.navigationBars.asPaddingValues()
                                        .calculateBottomPadding()
                                )
                            )
                        }
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin()) {
                    blurRadius = 15.dp
                    noiseFactor = 0.05f
                    inputScale = HazeInputScale.Auto
                    alpha = 0.98f
                }
                .padding(horizontal = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(
                        top = 8.dp + WindowInsets.statusBars.asPaddingValues()
                            .calculateTopPadding()
                    )
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = "Welcome",
                        color = uiColors.progressAccent,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.titleMediumEmphasized
                    )
                    Text(
                        text = name.trim().substringBefore(" "),
                        color = uiColors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.headlineLargeEmphasized,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        showAboutDialog = !showAboutDialog
                    },
                    modifier = Modifier.size(60.dp)
                ) {
                    Image(
                        painter = painterResource(Res.drawable.e_labs_logo),
                        contentDescription = "Logo",
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
    if (showAboutDialog) {
        AboutELabsDialog(
            onDismiss = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                showAboutDialog = false
            },

            hazeState = hazeState
        )
    }
    if (isLoginDialogOpen){
        LoginDialogBox(
            onDismiss = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                isLoginDialogOpen = false
                viewmodel.setLoginStateIdle()
            },
            onConfirm = { sapPassword->
                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                viewmodel.login(sapPassword)
            },
            syncState = loginState,
            hazeState = hazeState
        )
    }
}
