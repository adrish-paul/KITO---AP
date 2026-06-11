package com.kito.feature.calendar.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kito.feature.calendar.domain.model.CalendarEvent
import com.kito.feature.calendar.presentation.CalendarColors

@Composable
fun DayEventCard(event: CalendarEvent, isExpanded: Boolean, onClick: () -> Unit) {
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
                Text("${event.startTime} – ${event.endTime}",
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
