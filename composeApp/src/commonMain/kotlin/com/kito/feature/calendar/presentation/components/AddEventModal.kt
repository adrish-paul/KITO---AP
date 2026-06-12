package com.kito.feature.calendar.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kito.feature.calendar.presentation.CalendarColors

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
