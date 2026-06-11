package com.kito.feature.calendar.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.kito.feature.calendar.presentation.CalendarViewModel

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
