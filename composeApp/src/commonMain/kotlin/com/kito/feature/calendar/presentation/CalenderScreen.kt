package com.kito.feature.calendar.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kito.core.network.supabase.model.CalendarEventModel
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
fun CalendarHeader(
    month: Int, year: Int, currentView: String,
    heatMode: Boolean, showStats: Boolean, isLoading: Boolean,
    onPrev: () -> Unit, onNext: () -> Unit,
    onViewChange: (String) -> Unit,
    onHeatToggle: () -> Unit, onStatsToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xF008060F), Color.Transparent)))
            .padding(top = 52.dp, start = 18.dp, end = 18.dp, bottom = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "$year · KITO",
                    color = CalendarColors.orange.copy(alpha = 0.7f),
                    fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp, fontFamily = FontFamily.Monospace
                )
                Text(
                    CalendarColors.months[month - 1],
                    fontSize = 30.sp, fontWeight = FontWeight.ExtraBold,
                    style = LocalTextStyle.current.copy(
                        brush = Brush.linearGradient(listOf(Color(0xFFF0ECF8), CalendarColors.orange))
                    )
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ToggleChip("🔥 HEAT",  heatMode,  CalendarColors.orange,      onHeatToggle)
                ToggleChip("📊 STATS", showStats, CalendarColors.purpleLight, onStatsToggle)
                NavArrow("‹", onPrev)
                NavArrow("›", onNext)
            }
        }

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                color = CalendarColors.orange,
                trackColor = CalendarColors.orange.copy(alpha = 0.15f)
            )
        } else Spacer(Modifier.height(4.dp))

        Spacer(Modifier.height(10.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("month","week","day","agenda").forEach { v ->
                ViewTabButton(v, currentView == v, Modifier.weight(1f)) { onViewChange(v) }
            }
        }

        Spacer(Modifier.height(10.dp))

        if (currentView == "month") {
            Row(Modifier.fillMaxWidth()) {
                CalendarColors.daysShort.forEachIndexed { i, d ->
                    Text(
                        d, modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 9.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp,
                        color = if (i == 0 || i == 6) CalendarColors.orange.copy(.5f)
                        else Color.White.copy(.25f)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
fun MonthView(
    viewModel: CalendarViewModel,
    animKey: Int, heatMode: Boolean, selectedDate: String,
    displayMonth: Int, displayYear: Int,
    onDayClick: (Int) -> Unit,
    onSwipe: (Float) -> Unit
) {
    val firstDay = viewModel.firstDayOfMonth(displayMonth, displayYear)
    val daysInM  = viewModel.daysInMonth(displayMonth, displayYear)
    val cells    = buildList<Int?> {
        repeat(firstDay) { add(null) }
        for (d in 1..daysInM) add(d)
        while (size % 7 != 0) add(null)
    }
    var dragTotal by remember { mutableFloatStateOf(0f) }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 5 }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd    = { if (kotlin.math.abs(dragTotal) > 55f) onSwipe(dragTotal); dragTotal = 0f },
                        onDragCancel = { dragTotal = 0f }
                    ) { _, d -> dragTotal += d }
                }
        ) {
            cells.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    week.forEach { day ->
                        if (day == null) Box(Modifier.weight(1f).height(62.dp))
                        else DayCell(
                            day = day, viewModel = viewModel,
                            heatMode = heatMode, selectedDate = selectedDate,
                            displayMonth = displayMonth, displayYear = displayYear,
                            modifier = Modifier.weight(1f),
                            onClick = { onDayClick(day) }
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
            }
        }
    }
}

@Composable
fun DayCell(
    day: Int, viewModel: CalendarViewModel,
    heatMode: Boolean, selectedDate: String,
    displayMonth: Int, displayYear: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val key       = viewModel.formatDateKey(day, displayMonth, displayYear)
    val isToday   = viewModel.isToday(day)
    val isSel     = key == selectedDate
    val evs       = viewModel.getEventsForDay(day)
    val heat      = viewModel.heatLevelForDay(day)
    val colIdx    = (day + viewModel.firstDayOfMonth(displayMonth, displayYear) - 1) % 7
    val isWeekend = colIdx == 0 || colIdx == 6

    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse)
    )
    val scaleAnim by animateFloatAsState(if (isSel) 1.0f else 1f, spring(Spring.DampingRatioMediumBouncy))

    Box(
        modifier = modifier
            .height(62.dp)
            .graphicsLayer { scaleX = scaleAnim; scaleY = scaleAnim }
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isSel            -> CalendarColors.orange.copy(.18f)
                    heatMode && heat > 0 -> CalendarColors.orange.copy(heat * 0.07f)
                    isToday          -> CalendarColors.orange.copy(.09f)
                    else             -> Color.White.copy(.025f)
                }
            )
            .border(
                width = if (isSel || isToday) 1.dp else 0.5.dp,
                color = when {
                    isSel   -> CalendarColors.orange.copy(.5f)
                    isToday -> CalendarColors.orange.copy(.28f + pulse * .15f)
                    else    -> Color.White.copy(.035f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, onClick = onClick
            )
            .padding(5.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            if (isToday) {
                Box(
                    Modifier.clip(RoundedCornerShape(6.dp))
                        .background(CalendarColors.orange)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text("$day", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            } else {
                Text(
                    "$day", fontSize = 12.sp,
                    fontWeight = if (isWeekend) FontWeight.Medium else FontWeight.Normal,
                    color = when {
                        isSel     -> CalendarColors.orangeLight
                        isWeekend -> CalendarColors.orange.copy(.6f)
                        else      -> Color.White.copy(.7f)
                    }
                )
            }
            Spacer(Modifier.height(3.dp))
            evs.take(2).forEach { ev ->
                val evColor = CalendarColors.fromHex(ev.color) ?: CalendarColors.categoryColor(ev.category)
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(evColor.copy(.18f))
                        .padding(horizontal = 3.dp, vertical = 1.5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.width(2.dp).height(8.dp).clip(RoundedCornerShape(1.dp)).background(evColor))
                    Spacer(Modifier.width(2.dp))
                    Text(
                        ev.title?.split(" ")?.firstOrNull() ?: "",
                        fontSize = 7.5.sp, fontWeight = FontWeight.SemiBold,
                        color = evColor, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(1.5.dp))
            }
            if (evs.size > 2)
                Text("+${evs.size - 2}", fontSize = 7.sp, color = Color.White.copy(.3f), modifier = Modifier.padding(start = 4.dp))
        }
        if (heatMode && heat > 0)
            Box(
                Modifier.size((heat * 3 + 3).dp).align(Alignment.TopEnd)
                    .offset(x = (-3).dp, y = 3.dp)
                    .clip(CircleShape)
                    .background(CalendarColors.orange.copy(heat * 0.25f + 0.15f))
            )
    }
}

@Composable
fun WeekView(viewModel: CalendarViewModel) {
    val displayMonth by viewModel.displayMonth.collectAsState()
    val displayYear  by viewModel.displayYear.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selDay = selectedDate.split("-")[2].toInt()
    val selDow = viewModel.getDayOfWeek(selectedDate)
    val weekStart = (selDay - selDow).coerceAtLeast(1)

    val weekDays = (0..6).map { i ->
        val d = (weekStart + i).coerceIn(1, viewModel.daysInMonth(displayMonth, displayYear))
        Triple(d, viewModel.formatDateKey(d, displayMonth, displayYear), i)
    }

    Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(36.dp))
            weekDays.forEach { (d, k, dow) ->
                val isTod = viewModel.isToday(d)
                val isSel = k == selectedDate
                val hasEv = viewModel.getEventsForDate(k).isNotEmpty()
                Column(
                    modifier = Modifier.weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) CalendarColors.orange.copy(.15f) else Color.Transparent)
                        .border(1.dp,
                            if (isSel) CalendarColors.orange.copy(.3f) else Color.Transparent,
                            RoundedCornerShape(10.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() }, indication = null
                        ) { viewModel.selectDate(k); viewModel.setView("day") }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(CalendarColors.daysShort[dow], fontSize = 9.sp, color = Color.White.copy(.35f), fontWeight = FontWeight.SemiBold)
                    Text(
                        "$d", fontSize = 15.sp,
                        fontWeight = if (isTod) FontWeight.Bold else FontWeight.Normal,
                        color = if (isTod) CalendarColors.orange else Color.White.copy(.7f)
                    )
                    if (hasEv) Box(Modifier.size(4.dp).clip(CircleShape).background(CalendarColors.orange))
                    else Spacer(Modifier.height(4.dp))
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        HorizontalDivider(color = Color.White.copy(.05f))
        Spacer(Modifier.height(4.dp))

        Column(modifier = Modifier.height(460.dp).verticalScroll(rememberScrollState())) {
            for (h in 7..20) {
                Row(Modifier.fillMaxWidth().height(46.dp), verticalAlignment = Alignment.Top) {
                    Text(
                        "${if (h > 12) h - 12 else h}${if (h >= 12) "p" else "a"}",
                        fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color.White.copy(.2f),
                        modifier = Modifier.width(36.dp).padding(top = 3.dp), textAlign = TextAlign.End
                    )
                    weekDays.forEach { (_, k, _) ->
                        val evs = viewModel.getEventsForDate(k).filter {
                            it.start_time?.split(":")?.firstOrNull()?.toIntOrNull() == h
                        }
                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight()
                                .border(BorderStroke(0.5.dp, Color.White.copy(.04f)))
                                .background(if (k == selectedDate) CalendarColors.orange.copy(.02f) else Color.Transparent)
                        ) {
                            evs.forEach { ev ->
                                val c = CalendarColors.categoryColor(ev.category)
                                Box(
                                    Modifier.fillMaxWidth().padding(1.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(c.copy(.22f))
                                        .border(1.dp, c.copy(.4f), RoundedCornerShape(5.dp))
                                        .padding(horizontal = 3.dp, vertical = 2.dp)
                                ) {
                                    Text(ev.title ?: "", fontSize = 7.sp, fontWeight = FontWeight.Bold,
                                        color = c, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayView(viewModel: CalendarViewModel) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val parts     = selectedDate.split("-")
    val day       = parts[2].toInt()
    val month     = parts[1].toInt()
    val selEvents = viewModel.getEventsForDate(selectedDate)
    var expandedId by remember { mutableStateOf<Long?>(null) }

    Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "$day", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold,
                    color = CalendarColors.orange, lineHeight = 38.sp
                )
                Text(
                    "${CalendarColors.daysFull[viewModel.getDayOfWeek(selectedDate)]} · ${CalendarColors.monthsShort[month - 1]}",
                    fontSize = 12.sp, color = Color.White.copy(.4f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                NavArrow("‹") { viewModel.prevDay() }
                NavArrow("›") { viewModel.nextDay() }
            }
        }

        Box {
            Box(Modifier.padding(start = 42.dp).width(1.dp).fillMaxHeight().background(Color.White.copy(.06f)))
            Column {
                for (h in 7..20) {
                    val evs = selEvents.filter { it.start_time?.split(":")?.firstOrNull()?.toIntOrNull() == h }
                    Row(Modifier.fillMaxWidth().defaultMinSize(minHeight = 50.dp), verticalAlignment = Alignment.Top) {
                        Text(
                            "${if (h > 12) h - 12 else h}:00${if (h >= 12) "p" else "a"}",
                            fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color.White.copy(.2f),
                            modifier = Modifier.width(44.dp).padding(top = 4.dp), textAlign = TextAlign.End
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f).padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            evs.forEach { ev ->
                                DayEventCard(ev, expandedId == ev.id) {
                                    expandedId = if (expandedId == ev.id) null else ev.id
                                }
                            }
                        }
                    }
                }
                if (selEvents.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🗓️", fontSize = 32.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Free day — nothing scheduled", fontSize = 13.sp, color = Color.White.copy(.2f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayEventCard(event: CalendarEventModel, isExpanded: Boolean, onClick: () -> Unit) {
    val evColor = CalendarColors.fromHex(event.color) ?: CalendarColors.categoryColor(event.category)
    val arrow by animateFloatAsState(if (isExpanded) 90f else 0f, spring(Spring.DampingRatioMediumBouncy))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(evColor.copy(.12f))
            .border(1.dp, evColor.copy(.3f), RoundedCornerShape(12.dp))
            .drawBehind { drawRect(evColor, size = Size(8f, size.height)) }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(start = 14.dp, end = 10.dp, top = 10.dp, bottom = 10.dp)
            .animateContentSize(spring(Spring.DampingRatioMediumBouncy))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(CalendarColors.categoryIcon(event.category), fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
            Column(Modifier.weight(1f)) {
                Text(event.title ?: "", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(.9f))
                Text("${event.start_time} – ${event.end_time}",
                    fontSize = 10.sp, color = Color.White.copy(.35f), fontFamily = FontFamily.Monospace)
            }
            Text("›", fontSize = 16.sp, color = Color.White.copy(.4f),
                modifier = Modifier.graphicsLayer { rotationZ = arrow })
        }
        if (isExpanded) {
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = evColor.copy(.2f))
            Spacer(Modifier.height(8.dp))
            Text(event.description ?: "No description.", fontSize = 12.sp, color = Color.White.copy(.45f), lineHeight = 18.sp)
            Spacer(Modifier.height(6.dp))
            CategoryTag(event.category, evColor)
        }
    }
}

// AGENDA VIEW
@Composable
fun AgendaView(viewModel: CalendarViewModel) {
    val agendaEvents = viewModel.getAgendaEvents()

    if (agendaEvents.isEmpty()) {
        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
            Text("No events this month", color = Color.White.copy(.2f), fontSize = 13.sp)
        }
        return
    }

    Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
        agendaEvents.entries.forEachIndexed { gi, (date, evs) ->
            val dateParts = date.split("-")
            val day   = dateParts[2].toInt()
            val month = dateParts[1].toInt()
            val isToday = viewModel.isToday(day)

            Row(
                Modifier.padding(top = if (gi == 0) 8.dp else 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                        .background(if (isToday) CalendarColors.orange else Color.White.copy(.07f))
                        .border(1.dp, if (isToday) Color.Transparent else Color.White.copy(.08f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$day", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold,
                            color = if (isToday) Color.Black else Color.White.copy(.8f), lineHeight = 15.sp)
                        Text(CalendarColors.monthsShort[month - 1].uppercase(),
                            fontSize = 7.sp, color = if (isToday) Color.Black.copy(.7f) else Color.White.copy(.3f))
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(CalendarColors.daysFull[viewModel.getDayOfWeek(date)],
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(.6f))
                    Text("${evs.size} event${if (evs.size > 1) "s" else ""}",
                        fontSize = 10.sp, color = Color.White.copy(.25f))
                }
            }

            Column(Modifier.padding(start = 48.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                evs.forEach { ev ->
                    val evColor = CalendarColors.fromHex(ev.color) ?: CalendarColors.categoryColor(ev.category)
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(11.dp))
                            .background(evColor.copy(.1f))
                            .border(1.dp, evColor.copy(.25f), RoundedCornerShape(11.dp))
                            .drawBehind { drawRect(evColor, size = Size(6f, size.height)) }
                            .padding(start = 10.dp, top = 10.dp, bottom = 10.dp, end = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(CalendarColors.categoryIcon(ev.category), fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(ev.title ?: "", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(.88f))
                            Text("${ev.start_time} – ${ev.end_time}", fontSize = 10.sp,
                                color = Color.White.copy(.3f), fontFamily = FontFamily.Monospace)
                        }
                        CategoryTag(ev.category, evColor)
                    }
                }
            }
        }
    }
}

// STATS PANEL
@Composable
fun StatsPanel(viewModel: CalendarViewModel) {
    Column(
        Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(.03f))
            .border(1.dp, Color.White.copy(.07f), RoundedCornerShape(20.dp))
    ) {
        Text("Month Overview", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(.8f),
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 8.dp))
        HorizontalDivider(color = Color.White.copy(.05f))

        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Triple("📅", "${viewModel.getTotalEvents()}", CalendarColors.orange),
                Triple("📚", "${viewModel.getCategoryCount("class")}", CalendarColors.teal),
                Triple("📝", "${viewModel.getCategoryCount("exam")}", CalendarColors.red),
                Triple("🔬", "${viewModel.getCategoryCount("lab")}", CalendarColors.blue),
            ).forEachIndexed { i, (icon, value, color) ->
                Column(
                    Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                        .background(color.copy(.1f))
                        .border(1.dp, color.copy(.25f), RoundedCornerShape(12.dp))
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(icon, fontSize = 16.sp)
                    Spacer(Modifier.height(3.dp))
                    Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
                    Text(listOf("Events","Classes","Exams","Labs")[i],
                        fontSize = 9.sp, color = Color.White.copy(.3f), letterSpacing = 0.5.sp)
                }
            }
        }

        Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Text("CATEGORY SPLIT", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                color = Color.White.copy(.3f), letterSpacing = 1.5.sp, modifier = Modifier.padding(bottom = 10.dp))
            listOf(
                Triple("Classes", CalendarColors.orange, 40),
                Triple("Labs",    CalendarColors.blue,   30),
                Triple("Exams",   CalendarColors.red,    15),
                Triple("Events",  CalendarColors.purple, 15),
            ).forEach { (name, color, pct) ->
                Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(.6f), modifier = Modifier.width(60.dp))
                    val animPct by animateFloatAsState(pct / 100f, spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow))
                    Box(Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(.06f))) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(animPct).clip(RoundedCornerShape(3.dp))
                            .background(Brush.horizontalGradient(listOf(color, color.copy(.5f)))))
                    }
                    Text("$pct%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color,
                        modifier = Modifier.width(32.dp), textAlign = TextAlign.End, fontFamily = FontFamily.Monospace)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
    }
}

// SELECTED DAY PANEL
@Composable
fun SelectedDayPanel(viewModel: CalendarViewModel, selectedDate: String) {
    val parts     = selectedDate.split("-")
    val day       = parts[2].toInt()
    val month     = parts[1].toInt()
    val selEvents = viewModel.getEventsForDate(selectedDate)
    var expandedId by remember { mutableStateOf<Long?>(null) }

    Column(
        Modifier.padding(horizontal = 12.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(.03f))
            .border(1.dp, Color.White.copy(.07f), RoundedCornerShape(20.dp))
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row {
                Text("$day ", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CalendarColors.orange)
                Text(CalendarColors.monthsShort[month - 1], fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(.7f))
            }
            Text(
                if (selEvents.isNotEmpty()) "${selEvents.size} events" else "No events",
                fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                color = if (selEvents.isNotEmpty()) CalendarColors.orange else Color.White.copy(.2f)
            )
        }
        HorizontalDivider(color = Color.White.copy(.05f))
        if (selEvents.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                Text("🌙 Free day — nothing scheduled", fontSize = 12.sp, color = Color.White.copy(.2f))
            }
        } else {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                selEvents.forEach { ev ->
                    DayEventCard(ev, expandedId == ev.id) { expandedId = if (expandedId == ev.id) null else ev.id }
                }
            }
        }
    }
}

// UPCOMING PANEL
@Composable
fun UpcomingPanel(viewModel: CalendarViewModel) {
    val upcoming = viewModel.getUpcomingEvents()
    if (upcoming.isEmpty()) return

    Column(Modifier.padding(horizontal = 12.dp)) {
        Text("UPCOMING DEADLINES", fontSize = 10.sp, fontWeight = FontWeight.Bold,
            color = Color.White.copy(.3f), letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 2.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            upcoming.forEach { ev ->
                val evColor = CalendarColors.fromHex(ev.color) ?: CalendarColors.categoryColor(ev.category)
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(.03f))
                        .border(1.dp, Color.White.copy(.06f), RoundedCornerShape(14.dp))
                        .padding(11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                            .background(evColor.copy(.15f))
                            .border(1.dp, evColor.copy(.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text(CalendarColors.categoryIcon(ev.category), fontSize = 16.sp) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(ev.title ?: "", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(.85f))
                        Text("${ev.date} · ${ev.start_time}", fontSize = 10.sp,
                            color = Color.White.copy(.3f), fontFamily = FontFamily.Monospace)
                    }
                    Box(
                        Modifier.clip(RoundedCornerShape(20.dp)).background(evColor.copy(.15f))
                            .border(1.dp, evColor.copy(.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) { Text("›", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = evColor) }
                }
            }
        }
    }
}

// ADD EVENT MODAL
@Composable
fun AddEventModal(selectedDate: String, onDismiss: () -> Unit) {
    var title    by remember { mutableStateOf("") }
    var desc     by remember { mutableStateOf("") }
    var time     by remember { mutableStateOf("09:00") }
    var endTime  by remember { mutableStateOf("10:00") }
    var category by remember { mutableStateOf("class") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF1A1625), Color(0xFF120F1E))))
                .border(1.dp, Color.White.copy(.08f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Box(Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(.15f)).align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(16.dp))
            Text("New Event", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, modifier = Modifier.padding(bottom = 16.dp))

            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Event Title", color = Color.White.copy(.4f)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CalendarColors.orange, unfocusedBorderColor = Color.White.copy(.12f),
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = CalendarColors.orange
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(8.dp))

            Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(.04f)).border(1.dp, Color.White.copy(.08f), RoundedCornerShape(12.dp)).padding(14.dp)
            ) { Text("📅 $selectedDate", fontSize = 13.sp, color = Color.White.copy(.5f)) }
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = time, onValueChange = { time = it },
                    label = { Text("Start", color = Color.White.copy(.4f)) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CalendarColors.orange, unfocusedBorderColor = Color.White.copy(.12f),
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = CalendarColors.orange
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = endTime, onValueChange = { endTime = it },
                    label = { Text("End", color = Color.White.copy(.4f)) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CalendarColors.orange, unfocusedBorderColor = Color.White.copy(.12f),
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = CalendarColors.orange
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = desc, onValueChange = { desc = it },
                label = { Text("Description", color = Color.White.copy(.4f)) },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CalendarColors.orange, unfocusedBorderColor = Color.White.copy(.12f),
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = CalendarColors.orange
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(12.dp))

            Text("Category", fontSize = 11.sp, color = Color.White.copy(.4f), letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 6.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("class","lab","exam","event","study").forEach { cat ->
                    val catColor = CalendarColors.categoryColor(cat)
                    val isSel   = category == cat
                    Box(
                        Modifier.clip(RoundedCornerShape(20.dp))
                            .background(if (isSel) catColor.copy(.25f) else catColor.copy(.1f))
                            .border(1.dp, if (isSel) catColor.copy(.6f) else catColor.copy(.2f), RoundedCornerShape(20.dp))
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { category = cat }
                            .padding(horizontal = 11.dp, vertical = 6.dp)
                    ) {
                        Text("${CalendarColors.categoryIcon(cat)} $cat", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = catColor)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { /* TODO: POST to Supabase */ onDismiss() },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CalendarColors.orange, contentColor = Color.Black),
                elevation = ButtonDefaults.buttonElevation(6.dp)
            ) {
                Text("Add Event", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// REUSABLES
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

@Composable
fun ToggleChip(label: String, active: Boolean, activeColor: Color, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(20.dp))
            .background(if (active) activeColor.copy(.18f) else Color.White.copy(.06f))
            .border(1.dp, if (active) activeColor.copy(.35f) else Color.White.copy(.07f), RoundedCornerShape(20.dp))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold,
            color = if (active) activeColor else Color.White.copy(.4f), letterSpacing = 0.3.sp)
    }
}

@Composable
fun NavArrow(icon: String, onClick: () -> Unit) {
    Box(
        Modifier.size(32.dp).clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(.04f))
            .border(1.dp, Color.White.copy(.07f), RoundedCornerShape(10.dp))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(icon, fontSize = 16.sp, color = Color.White.copy(.7f))
    }
}

@Composable
fun ViewTabButton(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(10.dp))
            .background(if (selected) CalendarColors.orange.copy(.18f) else Color.Transparent)
            .border(1.dp, if (selected) CalendarColors.orange.copy(.3f) else Color.Transparent, RoundedCornerShape(10.dp))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) CalendarColors.orange else Color.White.copy(.35f))
    }
}

@Composable
fun CategoryTag(category: String?, color: Color) {
    Box(
        Modifier.clip(RoundedCornerShape(20.dp)).background(color.copy(.2f))
            .border(1.dp, color.copy(.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text((category ?: "").uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = color, letterSpacing = 0.5.sp)
    }
}