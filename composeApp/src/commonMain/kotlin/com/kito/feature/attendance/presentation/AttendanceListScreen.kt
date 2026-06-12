package com.kito.feature.attendance.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kito.core.designsystem.UIColors
import com.kito.core.platform.toast
import com.kito.core.presentation.components.state.SyncUiState
import com.kito.feature.attendance.domain.model.Attendance
import com.kito.feature.attendance.presentation.components.AttendanceCard
import com.kito.feature.attendance.presentation.components.AttendanceDialog
import com.kito.feature.attendance.presentation.components.InstagramPullIndicator
import com.kito.feature.attendance.presentation.components.OverallAttendanceCard
import com.kito.feature.attendance.presentation.components.sampleAttendance
import com.kito.feature.settings.presentation.components.LoginDialogBox
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalHazeMaterialsApi::class, ExperimentalHazeApi::class
)
@Composable
fun AttendanceListScreen(
    viewModel: AttendanceListScreenViewModel = koinInject(),
) {
    val cardHaze = rememberHazeState()
    val uiColors = UIColors()
    val hazeState = rememberHazeState()
    val attendance by viewModel.attendance.collectAsState()
    val sapLoggedIn by viewModel.sapLoggedIn.collectAsState()
    val currentAttendance = remember { mutableStateOf<Attendance?>(null) }
    val pullToRefreshState = rememberPullToRefreshState()
    val density = LocalDensity.current
    val fraction = pullToRefreshState.distanceFraction.coerceIn(0f, 1f)
    val syncState by viewModel.syncState.collectAsState()
    val requiredAttendance = viewModel.requiredAttendance.collectAsState()
    val haptic = LocalHapticFeedback.current
    val pullOffsetPx = with(density) { (42.dp * fraction).toPx() }
    var isLoginDialogOpen by remember { mutableStateOf(false) }
    val loginState by viewModel.loginState.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val averageAttendancePercentage by viewModel.averageAttendancePercentage.collectAsState()
    val highestAttendancePercentage by viewModel.highestAttendancePercentage.collectAsState()
    val lowestAttendancePercentage by viewModel.lowestAttendancePercentage.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is SyncUiState.Success) {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            isLoginDialogOpen = false
            viewModel.setLoginStateIdle()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.syncEvents.collect { event ->
            when (event) {
                is SyncUiState.Success -> {
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    toast("Sync completed")
                    viewModel.setSyncStateIdle()
                }
                is SyncUiState.Error -> toast(event.message)
                else -> toast("unknown error")
            }
        }
    }

    Box(modifier = Modifier.hazeSource(cardHaze)) {
        Box(modifier = Modifier.background(Color(0xFF121116))) {
            PullToRefreshBox(
                state = pullToRefreshState,
                isRefreshing = syncState is SyncUiState.Loading,
                onRefresh = {
                    if (isOnline) viewModel.refresh() else toast("No Internet Connection")
                },
                indicator = {},
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 46.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(2.5.dp),
                    modifier = Modifier
                        .graphicsLayer { translationY = pullOffsetPx }
                        .hazeSource(hazeState)
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                ) {
                    item { Spacer(modifier = Modifier.height(20.dp)) }

                    item {
                        OverallAttendanceCard(
                            colors = uiColors,
                            sapLoggedIn = sapLoggedIn,
                            percentageOverall = averageAttendancePercentage,
                            percentageHighest = highestAttendancePercentage,
                            percentageLowest = lowestAttendancePercentage,
                        )
                    }

                    item { Spacer(modifier = Modifier.height(4.dp)) }

                    if (sapLoggedIn) {
                        itemsIndexed(attendance) { index, item ->
                            Card(
                                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                shape = RoundedCornerShape(
                                    topStart = if (index == 0) 24.dp else 4.dp,
                                    topEnd = if (index == 0) 24.dp else 4.dp,
                                    bottomStart = if (index == attendance.size - 1) 24.dp else 4.dp,
                                    bottomEnd = if (index == attendance.size - 1) 24.dp else 4.dp
                                ),
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    currentAttendance.value = item
                                }
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                uiColors.cardBackground,
                                                Color(0xFF2F222F),
                                                Color(0xFF2F222F),
                                                uiColors.cardBackgroundHigh
                                            )
                                        )
                                    )
                                ) { AttendanceCard(item) }
                            }
                        }
                        item {
                            Spacer(
                                modifier = Modifier.height(
                                    86.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                                )
                            )
                        }
                    } else {
                        itemsIndexed(sampleAttendance) { index, item ->
                            Card(
                                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                                colors = CardDefaults.cardColors(containerColor = uiColors.cardBackground),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                shape = RoundedCornerShape(
                                    topStart = if (index == 0) 24.dp else 4.dp,
                                    topEnd = if (index == 0) 24.dp else 4.dp,
                                    bottomStart = if (index == attendance.size - 1) 24.dp else 4.dp,
                                    bottomEnd = if (index == attendance.size - 1) 24.dp else 4.dp
                                )
                            ) { AttendanceCard(item) }
                        }
                        item {
                            Spacer(
                                modifier = Modifier.height(
                                    86.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                                )
                            )
                        }
                    }
                }
                InstagramPullIndicator(
                    pullState = pullToRefreshState,
                    isRefreshing = syncState is SyncUiState.Loading
                )
            }
            Column(
                modifier = Modifier
                    .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin()) {
                        blurRadius = 15.dp
                        noiseFactor = 0.05f
                        inputScale = HazeInputScale.Auto
                        alpha = 0.98f
                    }
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(
                    modifier = Modifier.height(
                        16.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    )
                )
                Row {
                    Text(
                        text = "Attendance",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = uiColors.textPrimary,
                        style = MaterialTheme.typography.titleLargeEmphasized,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (!sapLoggedIn) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 46.dp
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin()) {
                                blurRadius = 15.dp
                                noiseFactor = 0.05f
                                inputScale = HazeInputScale.Auto
                                alpha = 0.98f
                            }
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        awaitPointerEvent().changes.forEach { it.consume() }
                                    }
                                }
                            }
                    )
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            isLoginDialogOpen = true
                        },
                        modifier = Modifier.align(Alignment.Center),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = uiColors.progressAccent,
                            contentColor = uiColors.textPrimary
                        )
                    ) {
                        Text(
                            text = "Connect to sap",
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.labelMediumEmphasized
                        )
                    }
                }
            }
        }
    }

    currentAttendance.value?.let { att ->
        AttendanceDialog(
            requiredAttendance = requiredAttendance.value,
            hazeState = cardHaze,
            attendance = att,
            onDismiss = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                currentAttendance.value = null
            }
        )
    }

    if (isLoginDialogOpen) {
        LoginDialogBox(
            onDismiss = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                isLoginDialogOpen = false
                viewModel.setLoginStateIdle()
            },
            onConfirm = { sapPassword ->
                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                viewModel.login(sapPassword)
            },
            syncState = loginState,
            hazeState = hazeState
        )
    }
}


// ─── Stateless content composable ─────────────────────────────────────────────
// AttendanceListContent is the single, previewable entry point for the UI.
// AttendanceListScreen (above) is the thin stateful wrapper that feeds it state.

sealed interface AttendanceListEvent {
    data object Refresh : AttendanceListEvent
    data class Login(val password: String) : AttendanceListEvent
    data object DismissLogin : AttendanceListEvent
    data object ClearSyncState : AttendanceListEvent
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalHazeMaterialsApi::class, ExperimentalHazeApi::class
)
@Composable
fun AttendanceListContent(
    state: AttendanceListUiState,
    onEvent: (AttendanceListEvent) -> Unit,
) {
    val cardHaze = rememberHazeState()
    val uiColors = UIColors()
    val hazeState = rememberHazeState()
    val currentAttendance = remember { mutableStateOf<Attendance?>(null) }
    val pullToRefreshState = rememberPullToRefreshState()
    val density = LocalDensity.current
    val fraction = pullToRefreshState.distanceFraction.coerceIn(0f, 1f)
    val haptic = LocalHapticFeedback.current
    val pullOffsetPx = with(density) { (42.dp * fraction).toPx() }
    var isLoginDialogOpen by remember { mutableStateOf(false) }

    LaunchedEffect(state.loginState) {
        if (state.loginState is SyncUiState.Success) {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            isLoginDialogOpen = false
            onEvent(AttendanceListEvent.DismissLogin)
        }
    }

    Box(modifier = Modifier.hazeSource(cardHaze)) {
        Box(modifier = Modifier.background(Color(0xFF121116))) {
            PullToRefreshBox(
                state = pullToRefreshState,
                isRefreshing = state.syncState is SyncUiState.Loading,
                onRefresh = {
                    onEvent(AttendanceListEvent.Refresh)
                },
                indicator = {},
                modifier = Modifier.semantics { testTag = "attendance_content" },
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 46.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(2.5.dp),
                    modifier = Modifier
                        .graphicsLayer { translationY = pullOffsetPx }
                        .hazeSource(hazeState)
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                ) {
                    item { Spacer(modifier = Modifier.height(20.dp)) }

                    item {
                        OverallAttendanceCard(
                            colors = uiColors,
                            sapLoggedIn = state.sapLoggedIn,
                            percentageOverall = state.averageAttendancePercentage,
                            percentageHighest = state.highestAttendancePercentage,
                            percentageLowest = state.lowestAttendancePercentage,
                        )
                    }

                    item { Spacer(modifier = Modifier.height(4.dp)) }

                    val list = if (state.sapLoggedIn) state.attendance else sampleAttendance
                    itemsIndexed(list) { index, item ->
                        val transparent = state.sapLoggedIn
                        Card(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (transparent) Color.Transparent else uiColors.cardBackground
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            shape = RoundedCornerShape(
                                topStart = if (index == 0) 24.dp else 4.dp,
                                topEnd = if (index == 0) 24.dp else 4.dp,
                                bottomStart = if (index == list.size - 1) 24.dp else 4.dp,
                                bottomEnd = if (index == list.size - 1) 24.dp else 4.dp
                            ),
                            onClick = if (transparent) ({
                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                currentAttendance.value = item
                            }) else ({})
                        ) {
                            if (transparent) {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                uiColors.cardBackground,
                                                Color(0xFF2F222F),
                                                Color(0xFF2F222F),
                                                uiColors.cardBackgroundHigh
                                            )
                                        )
                                    )
                                ) { AttendanceCard(item) }
                            } else {
                                AttendanceCard(item)
                            }
                        }
                    }
                    item {
                        Spacer(
                            modifier = Modifier.height(
                                86.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                            )
                        )
                    }
                }
                InstagramPullIndicator(
                    pullState = pullToRefreshState,
                    isRefreshing = state.syncState is SyncUiState.Loading
                )
            }
            Column(
                modifier = Modifier
                    .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin()) {
                        blurRadius = 15.dp
                        noiseFactor = 0.05f
                        inputScale = HazeInputScale.Auto
                        alpha = 0.98f
                    }
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(
                    modifier = Modifier.height(
                        16.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    )
                )
                Row {
                    Text(
                        text = "Attendance",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = uiColors.textPrimary,
                        style = MaterialTheme.typography.titleLargeEmphasized,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (!state.sapLoggedIn) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 46.dp
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin()) {
                                blurRadius = 15.dp
                                noiseFactor = 0.05f
                                inputScale = HazeInputScale.Auto
                                alpha = 0.98f
                            }
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        awaitPointerEvent().changes.forEach { it.consume() }
                                    }
                                }
                            }
                    )
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            isLoginDialogOpen = true
                        },
                        modifier = Modifier.align(Alignment.Center),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = uiColors.progressAccent,
                            contentColor = uiColors.textPrimary
                        )
                    ) {
                        Text(
                            text = "Connect to sap",
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.labelMediumEmphasized
                        )
                    }
                }
            }
        }
    }

    currentAttendance.value?.let { att ->
        AttendanceDialog(
            requiredAttendance = state.requiredAttendance,
            hazeState = cardHaze,
            attendance = att,
            onDismiss = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                currentAttendance.value = null
            }
        )
    }

    if (isLoginDialogOpen) {
        LoginDialogBox(
            onDismiss = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                isLoginDialogOpen = false
                onEvent(AttendanceListEvent.DismissLogin)
            },
            onConfirm = { sapPassword ->
                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                onEvent(AttendanceListEvent.Login(sapPassword))
            },
            syncState = state.loginState,
            hazeState = hazeState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalHazeMaterialsApi::class, ExperimentalHazeApi::class
)
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun AttendanceListContentPreview() {
    AttendanceListContent(
        state = AttendanceListUiState(
            attendance = sampleAttendance,
            sapLoggedIn = true,
            requiredAttendance = 75,
            averageAttendancePercentage = 62.5,
            highestAttendancePercentage = 85.0,
            lowestAttendancePercentage = 9.8,
        ),
        onEvent = {}
    )
}
