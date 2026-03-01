package com.kito.feature.gpa.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kito.core.presentation.components.GradeSlider
import com.kito.core.presentation.components.UIColors
import com.kito.core.presentation.components.grades
import kotlin.math.roundToInt

data class SubjectInput(
    val name: String,
    val credit: Int,
    var gradeIndex: Int = 0
)

@Composable
fun SGPAScreen(
    selectedSemester: Int,
    selectedBranch: String
) {

    val uiColors = UIColors()

    // 🔥 ONLY BUSINESS LOGIC CHANGED HERE
    val subjects = remember(selectedSemester, selectedBranch) {

        val data = gpaDatabase.find {
            it.semester == selectedSemester &&
                    it.branch == selectedBranch
        }

        mutableStateListOf<SubjectInput>().apply {
            data?.subjects?.forEach {
                add(SubjectInput(it.name, it.credit))
            }
        }
    }

    val sgpa by remember(subjects) {
        derivedStateOf {
            calculateSGPA(
                subjects.map {
                    it.credit to gradePoints[grades[it.gradeIndex]]!!
                }
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121116))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.5.dp)
    ) {

        item { Spacer(modifier = Modifier.height(16.dp)) }

        itemsIndexed(subjects) { index, subject ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 110.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(
                    topStart = if (index == 0) 24.dp else 4.dp,
                    topEnd = if (index == 0) 24.dp else 4.dp,
                    bottomStart = if (index == subjects.lastIndex) 24.dp else 4.dp,
                    bottomEnd = if (index == subjects.lastIndex) 24.dp else 4.dp
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = subject.name,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = uiColors.textPrimary,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = "Credits: ${subject.credit}",
                                fontFamily = FontFamily.Monospace,
                                color = uiColors.textSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        GradeSlider(
                            selectedIndex = subject.gradeIndex,
                            onGradeChange = {
                                subjects[index] =
                                    subjects[index].copy(gradeIndex = it)
                            }
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item {

            val rounded = (sgpa * 100).roundToInt() / 100.0

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
                            text = "SGPA",
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
