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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kito.feature.calendar.presentation.CalendarColors
import com.kito.feature.calendar.presentation.CalendarViewModel

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
                        Text("${ev.date} · ${ev.startTime}", fontSize = 10.sp,
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
