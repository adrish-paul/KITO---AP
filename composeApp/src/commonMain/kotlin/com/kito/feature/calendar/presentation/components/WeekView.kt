package com.kito.feature.calendar.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kito.feature.calendar.presentation.CalendarColors
import com.kito.feature.calendar.presentation.CalendarViewModel

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
                            it.startTime.split(":").firstOrNull()?.toIntOrNull() == h
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
