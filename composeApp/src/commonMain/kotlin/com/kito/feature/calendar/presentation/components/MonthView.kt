package com.kito.feature.calendar.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kito.feature.calendar.presentation.CalendarColors
import com.kito.feature.calendar.presentation.CalendarViewModel

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
