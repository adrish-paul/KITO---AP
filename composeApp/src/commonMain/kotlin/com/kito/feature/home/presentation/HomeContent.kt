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
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.kito.core.designsystem.AboutELabsDialog
import com.kito.core.designsystem.UIColors
import com.kito.core.designsystem.UtilityCard
import com.kito.core.presentation.components.state.SyncUiState
import com.kito.feature.attendance.domain.model.Attendance
import com.kito.feature.attendance.presentation.components.AttendanceBarCard
import com.kito.feature.home.domain.model.EventOrAd
import com.kito.feature.home.presentation.components.EventAndAdBanner
import com.kito.feature.schedule.domain.model.ScheduleItem
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
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalHazeApi::class,
    ExperimentalHazeMaterialsApi::class
)
@Composable
fun HomeContent(
    name: String,
    sapLoggedIn: Boolean,
    attendance: List<Attendance>,
    schedule: List<ScheduleItem>,
    nextSchedule: List<ScheduleItem>,
    syncState: SyncUiState,
    loginState: SyncUiState,
    isScheduleEmpty: Boolean,
    isKhaooGullyEnabled: Boolean,
    eventsAndAds: List<EventOrAd>,
    onReportClick: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToUtility: (NavKey?) -> Unit,
    onNavigateToPromotion: (String) -> Unit,
    onOpenUrl: (String) -> Unit,
    onLoginConfirm: (String) -> Unit,
    onSetLoginStateIdle: () -> Unit,
    modifier: Modifier = Modifier,
    enableAnimations: Boolean = true,
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    val uiColors = UIColors()
    val hazeState = rememberHazeState()
    val haptic = LocalHapticFeedback.current
    var isLoginDialogOpen by remember { mutableStateOf(false) }

    LaunchedEffect(loginState) {
        if (loginState is SyncUiState.Success) {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            isLoginDialogOpen = false
            onSetLoginStateIdle()
        }
    }

    Box(modifier = modifier.semantics { testTag = "home_content" }) {
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
                                Column(modifier = Modifier.semantics { testTag = "home_loading" }) {
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
                                IconButton(
                                    onClick = onReportClick,
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
                                        onNavigateToSchedule()
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
                                        onNavigateToSchedule()
                                    },
                                    enableAnimations = enableAnimations
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
                            }
                        }
                        item {
                            Spacer(Modifier.height(8.dp))
                        }

                        item {
                            Box(
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                UtilityCard(
                                    onCLick = onNavigateToUtility,
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
                                    onClick = { url, isAd ->
                                        if (isAd) {
                                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                            onNavigateToPromotion(url)
                                        } else {
                                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                            onOpenUrl(url)
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
                                        onNavigateToAttendance()
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
                                        onNavigateToAttendance()
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
    if (isLoginDialogOpen) {
        LoginDialogBox(
            onDismiss = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                isLoginDialogOpen = false
                onSetLoginStateIdle()
            },
            onConfirm = { sapPassword ->
                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                onLoginConfirm(sapPassword)
            },
            syncState = loginState,
            hazeState = hazeState
        )
    }
}

@Preview
@Composable
private fun HomeContentPreview() {
    HomeContent(
        name = "John",
        sapLoggedIn = true,
        attendance = emptyList(),
        schedule = emptyList(),
        nextSchedule = emptyList(),
        syncState = SyncUiState.Idle,
        loginState = SyncUiState.Idle,
        isScheduleEmpty = false,
        isKhaooGullyEnabled = true,
        eventsAndAds = emptyList(),
        onReportClick = {},
        onNavigateToSchedule = {},
        onNavigateToAttendance = {},
        onNavigateToUtility = {},
        onNavigateToPromotion = {},
        onOpenUrl = {},
        onLoginConfirm = {},
        onSetLoginStateIdle = {}
    )
}
