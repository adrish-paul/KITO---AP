package com.kito.feature.faculty.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kito.core.designsystem.UIColors
import com.kito.feature.faculty.domain.model.Faculty

@Composable
fun FacultyCard(
    faculty: Faculty,
    index: Int,
    listSize: Int,
    uiColors: UIColors,
    onFacultyClick: (Faculty) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(
            topStart = if (index == 0) 24.dp else 4.dp,
            topEnd = if (index == 0) 24.dp else 4.dp,
            bottomStart = if (index == listSize - 1) 24.dp else 4.dp,
            bottomEnd = if (index == listSize - 1) 24.dp else 4.dp
        ),
        onClick = { onFacultyClick(faculty) }
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    FacultyCardContent(
                        facultyName = faculty.name ?: "",
                        facultyOffice = faculty.officeRoom,
                        facultyEmail = faculty.email
                    )
                }

                IconButton(
                    onClick = { onFacultyClick(faculty) }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Open",
                        tint = uiColors.textSecondary,
                        modifier = Modifier.size(35.dp)
                    )
                }
            }
        }
    }
}
