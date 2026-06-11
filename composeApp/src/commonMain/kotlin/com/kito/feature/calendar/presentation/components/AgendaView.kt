package com.kito.feature.calendar.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kito.feature.calendar.presentation.CalendarColors
import com.kito.feature.calendar.presentation.CalendarViewModel

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
                            Text("${ev.startTime} – ${ev.endTime}", fontSize = 10.sp,
                                color = Color.White.copy(.3f), fontFamily = FontFamily.Monospace)
                        }
                        CategoryTag(ev.category, evColor)
                    }
                }
            }
        }
    }
}
