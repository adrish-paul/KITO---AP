package com.kito.feature.calendar.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.kito.feature.calendar.presentation.components.AddEventModal
import com.kito.feature.calendar.presentation.components.AgendaView
import com.kito.feature.calendar.presentation.components.CalendarHeader
import com.kito.feature.calendar.presentation.components.DayView
import com.kito.feature.calendar.presentation.components.MonthView
import com.kito.feature.calendar.presentation.components.SelectedDayPanel
import com.kito.feature.calendar.presentation.components.StatsPanel
import com.kito.feature.calendar.presentation.components.UpcomingPanel
import com.kito.feature.calendar.presentation.components.WeekView
import org.koin.compose.koinInject

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = koinInject()
) {
    val displayMonth by viewModel.displayMonth.collectAsState()
    val displayYear  by viewModel.displayYear.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val currentView  by viewModel.currentView.collectAsState()
    val heatMode     by viewModel.heatMode.collectAsState()
    val showStats    by viewModel.showStats.collectAsState()
    val isLoading    by viewModel.isLoading.collectAsState()
    val showAddModal by viewModel.showAddModal.collectAsState()
    val haptic = LocalHapticFeedback.current
    var animKey by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF08060F), Color(0xFF0D0B18), Color(0xFF0A0A14), Color(0xFF060810))
                )
            )
    ) {
        AmbientOrbs()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            item {
                CalendarHeader(
                    month = displayMonth, year = displayYear,
                    currentView = currentView,
                    heatMode = heatMode, showStats = showStats, isLoading = isLoading,
                    onPrev  = { haptic.performHapticFeedback(HapticFeedbackType.ContextClick); viewModel.prevMonth(); animKey++ },
                    onNext  = { haptic.performHapticFeedback(HapticFeedbackType.ContextClick); viewModel.nextMonth(); animKey++ },
                    onViewChange  = { viewModel.setView(it) },
                    onHeatToggle  = { haptic.performHapticFeedback(HapticFeedbackType.ContextClick); viewModel.toggleHeat() },
                    onStatsToggle = { haptic.performHapticFeedback(HapticFeedbackType.ContextClick); viewModel.toggleStats() }
                )
            }

            item {
                AnimatedVisibility(
                    visible = showStats,
                    enter = expandVertically(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow)) + fadeIn(),
                    exit  = shrinkVertically(tween(200)) + fadeOut()
                ) { StatsPanel(viewModel) }
            }

            item {
                AnimatedContent(
                    targetState = currentView,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) }
                ) { view ->
                    when (view) {
                        "month"  -> MonthView(
                            viewModel, animKey, heatMode, selectedDate, displayMonth, displayYear,
                            onDayClick = { day ->
                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                viewModel.selectDay(day)
                            },
                            onSwipe = { dir ->
                                if (dir > 0) viewModel.prevMonth() else viewModel.nextMonth()
                                animKey++
                            }
                        )
                        "week"   -> WeekView(viewModel)
                        "day"    -> DayView(viewModel)
                        "agenda" -> AgendaView(viewModel)
                        else     -> MonthView(viewModel, animKey, heatMode, selectedDate, displayMonth, displayYear,
                            onDayClick = { viewModel.selectDay(it) }, onSwipe = {})
                    }
                }
            }

            if (currentView == "month") {
                item { Spacer(Modifier.height(10.dp)); SelectedDayPanel(viewModel, selectedDate) }
                item { Spacer(Modifier.height(10.dp)); UpcomingPanel(viewModel) }
            }
        }

        FloatingActionButton(
            onClick = { haptic.performHapticFeedback(HapticFeedbackType.ContextClick); viewModel.setShowAddModal(true) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 32.dp),
            shape = RoundedCornerShape(18.dp),
            containerColor = CalendarColors.orange,
            contentColor = Color.Black,
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Event", modifier = Modifier.size(26.dp))
        }

        if (showAddModal) {
            AddEventModal(selectedDate = selectedDate, onDismiss = { viewModel.setShowAddModal(false) })
        }
    }
}

@Composable
fun AmbientOrbs() {
    val infiniteTransition = rememberInfiniteTransition()
    val float by infiniteTransition.animateFloat(
        0f, 1f, infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse)
    )
    Box(
        Modifier.offset(x = (-80).dp, y = (-80 + float * 20).dp).size(300.dp)
            .background(Brush.radialGradient(listOf(CalendarColors.orange.copy(.06f), Color.Transparent)), CircleShape)
    )
    Box(
        Modifier.fillMaxWidth().wrapContentWidth(Alignment.End)
            .offset(x = 60.dp, y = (600 - float * 30).dp).size(240.dp)
            .background(Brush.radialGradient(listOf(CalendarColors.purple.copy(.05f), Color.Transparent)), CircleShape)
    )
}