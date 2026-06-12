package com.kito.feature.gpa.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kito.core.designsystem.UIColors
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CGPAScreen() {

    val uiColors = UIColors()

    var oldCgpa by remember { mutableStateOf("") }
    var completedSem by remember { mutableStateOf("") }
    var currentSgpa by remember { mutableStateOf("") }

    val newCgpa by remember(oldCgpa, completedSem, currentSgpa) {
        derivedStateOf {
            val old = oldCgpa.toDoubleOrNull() ?: 0.0
            val sem = completedSem.toIntOrNull() ?: 0
            val sgpa = currentSgpa.toDoubleOrNull() ?: 0.0
            calculateCGPA(old, sem, sgpa)
        }
    }

    val cgpaInputs = listOf(
        "Previous CGPA",
        "Semesters Completed",
        "Current SGPA"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121116))
            .padding(horizontal = 16.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(2.5.dp),
        contentPadding = PaddingValues(
            top = WindowInsets().asPaddingValues().calculateTopPadding() + 288.dp
        )
    ) {

        item { Spacer(modifier = Modifier.height(16.dp)) }

        itemsIndexed(cgpaInputs) { index, label ->

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(
                    topStart = if (index == 0) 24.dp else 4.dp,
                    topEnd = if (index == 0) 24.dp else 4.dp,
                    bottomStart = if (index == cgpaInputs.lastIndex) 24.dp else 4.dp,
                    bottomEnd = if (index == cgpaInputs.lastIndex) 24.dp else 4.dp
                )
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    uiColors.cardBackground,
                                    Color(0xFF2F222F),
                                    Color(0xFF2F222F),
                                    uiColors.cardBackgroundHigh
                                )
                            )
                        )
                        .padding(16.dp)
                ) {

                    Column {

                        when (index) {

                            0 -> {
                                OutlinedTextField(
                                    value =oldCgpa,
                                    onValueChange = {
                                        oldCgpa = it
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(18.dp),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.BarChart,
                                            contentDescription = null,
                                            tint = Color(0xFFB8B2BC)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Previous CGPA",
                                            fontFamily = FontFamily.Monospace,
                                            style = MaterialTheme.typography.titleMediumEmphasized
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFFF8C00),
                                        unfocusedBorderColor = Color(0xFF3F3942),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        disabledTextColor = Color.White,
                                        errorTextColor = Color.White,
                                        focusedLabelColor = Color(0xFFFF8C00),
                                        cursorColor = Color(0xFFFF8C00)
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                )
                            }


                            1 -> {
                                OutlinedTextField(
                                    value = completedSem,
                                    onValueChange = {
                                        completedSem = it
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(18.dp),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.CalendarMonth,
                                            contentDescription = null,
                                            tint = Color(0xFFB8B2BC)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Semesters Completed",
                                            fontFamily = FontFamily.Monospace,
                                            style = MaterialTheme.typography.titleMediumEmphasized
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFFF8C00),
                                        unfocusedBorderColor = Color(0xFF3F3942),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        disabledTextColor = Color.White,
                                        errorTextColor = Color.White,
                                        focusedLabelColor = Color(0xFFFF8C00),
                                        cursorColor = Color(0xFFFF8C00)
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                )
                            }

                            2 -> {
                                OutlinedTextField(
                                    value = currentSgpa,
                                    onValueChange = {
                                        currentSgpa = it
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(18.dp),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Calculate,
                                            contentDescription = null,
                                            tint = Color(0xFFB8B2BC)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "Current Expected SGPA",
                                            fontFamily = FontFamily.Monospace,
                                            style = MaterialTheme.typography.titleMediumEmphasized
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFFF8C00),
                                        unfocusedBorderColor = Color(0xFF3F3942),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        disabledTextColor = Color.White,
                                        errorTextColor = Color.White,
                                        focusedLabelColor = Color(0xFFFF8C00),
                                        cursorColor = Color(0xFFFF8C00)
                                    ),
//                                trailingIcon = {
//                                    val image =
//                                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
//                                    val description =
//                                        if (passwordVisible) "Hide password" else "Show password"
//
//                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
//                                        Icon(imageVector = image, contentDescription = description)
//                                    }
//                                },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }

        //RESULT CARD
        item {

            val rounded = (newCgpa * 100).roundToInt() / 100.0

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(24.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    uiColors.cardBackground,
                                    Color(0xFF2F222F),
                                    Color(0xFF2F222F),
                                    uiColors.cardBackgroundHigh
                                )
                            )
                        )
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        Text(
                            text = "CGPA",
                            fontFamily = FontFamily.Monospace,
                            color = uiColors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = rounded.toString(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = uiColors.progressAccent,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
        }

        item {
            Spacer(
                modifier = Modifier.height(
                    42.dp + WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding()
                )
            )
        }
    }
}