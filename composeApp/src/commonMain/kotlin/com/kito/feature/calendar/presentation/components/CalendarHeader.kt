package com.kito.feature.calendar.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kito.feature.calendar.presentation.CalendarColors

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
