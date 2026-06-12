package com.kito.feature.calendar.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kito.feature.calendar.presentation.CalendarColors
import com.kito.feature.calendar.presentation.CalendarViewModel

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
                    val evs = selEvents.filter { it.startTime.split(":").firstOrNull()?.toIntOrNull() == h }
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
