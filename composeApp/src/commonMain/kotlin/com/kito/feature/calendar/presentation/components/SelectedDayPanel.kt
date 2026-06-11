package com.kito.feature.calendar.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kito.feature.calendar.presentation.CalendarColors
import com.kito.feature.calendar.presentation.CalendarViewModel

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
