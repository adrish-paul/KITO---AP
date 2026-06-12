package com.kito.feature.gpa.presentation.components

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kito.core.designsystem.meshGradient
import com.kito.core.designsystem.shimmer
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun GPAHeader(
    roll: String,
    isLoading: Boolean,
    selectedSemester: Int,
    selectedBranch: String,
    onSemesterSelected: (Int) -> Unit,
    onBranchSelected: (String) -> Unit
) {

    var semesterExpanded by remember { mutableStateOf(false) }
    var branchExpanded by remember { mutableStateOf(false) }

    val meshColors = listOf(
        Color(0xFF77280F).copy(alpha = 0.82f),
        Color(0xFF753107).copy(alpha = 0.82f),
        Color(0xFF62290A).copy(alpha = 0.82f),
        Color(0xFF46180C).copy(alpha = 0.82f),
        Color(0xFFA14B09).copy(alpha = 0.70f),
        Color(0xFF6B1414).copy(alpha = 0.75f),
    )

    val animatedPointMid = remember { Animatable(.8f) }
    val animatedPointTop = remember { Animatable(.8f) }

    val meshColorAnimators = remember {
        List(15) { index ->
            Animatable(meshColors[index % meshColors.size])
        }
    }

    LaunchedEffect(Unit) {
        meshColorAnimators.forEachIndexed { i, anim ->
            launch {
                val random = Random(i * 97)
                while (true) {
                    anim.animateTo(
                        targetValue = meshColors[random.nextInt(meshColors.size)],
                        animationSpec = tween(
                            durationMillis = random.nextInt(1800, 4200),
                            easing = LinearOutSlowInEasing
                        )
                    )
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(24.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .meshGradient(
                    points = listOf(

                        // TOP ROW
                        listOf(
                            Offset(0f, 0f) to meshColorAnimators[0].value,
                            Offset(0.25f, 0f) to meshColorAnimators[1].value,
                            Offset(0.5f, 0f) to meshColorAnimators[2].value,
                            Offset(0.75f, 0f) to meshColorAnimators[3].value,
                            Offset(1f, 0f) to meshColorAnimators[4].value,
                        ),

                        // MIDDLE CURVED BAND
                        listOf(
                            Offset(-0.05f, 0.55f) to meshColorAnimators[5].value,
                            Offset(0.2f, animatedPointTop.value) to meshColorAnimators[6].value,
                            Offset(0.5f, 0.6f) to meshColorAnimators[7].value,
                            Offset(0.8f, animatedPointMid.value) to meshColorAnimators[8].value,
                            Offset(1.05f, 0.55f) to meshColorAnimators[9].value,
                        ),

                        // BOTTOM ROW
                        listOf(
                            Offset(0f, 1f) to meshColorAnimators[10].value,
                            Offset(0.25f, 1f) to meshColorAnimators[11].value,
                            Offset(0.5f, 1f) to meshColorAnimators[12].value,
                            Offset(0.75f, 1f) to meshColorAnimators[13].value,
                            Offset(1f, 1f) to meshColorAnimators[14].value,
                        ),
                    ),
                    resolutionX = 30
                )
                .padding(20.dp)
        ) {

            if (isLoading) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.fillMaxWidth(0.6f).height(20.dp).shimmer())
                    Box(Modifier.fillMaxWidth(0.4f).height(14.dp).shimmer())
                }
            } else {

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Semester $selectedSemester",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFAC97),
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Box {
                            IconButton(onClick = { semesterExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }

                            DropdownMenu(
                                expanded = semesterExpanded,
                                onDismissRequest = { semesterExpanded = false },
                                shape = RoundedCornerShape(20.dp),
                                containerColor = Color(0xFF2A1A12).copy(alpha = 0.85f),
                                tonalElevation = 8.dp,
                                shadowElevation = 16.dp
                            ) {
                                (1..8).forEach { sem ->
                                    DropdownMenuItem(
                                        text = { Text("Semester $sem") },
                                        onClick = {
                                            semesterExpanded = false
                                            onSemesterSelected(sem)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Branch: $selectedBranch",
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFBFAAA2),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Box {
                            IconButton(onClick = { branchExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }

                            DropdownMenu(
                                expanded = branchExpanded,
                                onDismissRequest = { branchExpanded = false },
                                shape = RoundedCornerShape(20.dp),
                                containerColor = Color(0xFF2A1A12).copy(alpha = 0.85f),
                                tonalElevation = 8.dp,
                                shadowElevation = 16.dp
                            ) {
                                listOf(
                                    "CSE", "CSSE", "CSCE",
                                    "IT", "ECS"/*, "ECE"*/
                                ).forEach { branch ->
                                    DropdownMenuItem(
                                        text = { Text(branch) },
                                        onClick = {
                                            branchExpanded = false
                                            onBranchSelected(branch)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}