package com.kito.feature.attendance.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.PersonRemoveAlt1
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kito.core.designsystem.UIColors
import com.kito.feature.attendance.domain.model.Attendance
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

internal fun classesRequiredForPercentage(
    attended: Int,
    total: Int,
    requiredPercentage: Double
): Int {
    val r = requiredPercentage / 100.0
    if (attended >= r * total) return 0
    return max(0, ceil((r * total - attended) / (1 - r)).toInt())
}

internal fun calculateAttendancePercentage1Decimal(
    attendedClasses: Int,
    totalClasses: Int
): Double {
    if (totalClasses == 0) return 0.0
    return kotlin.math.round((attendedClasses.toDouble() / totalClasses) * 100 * 10) / 10
}

val sampleAttendance = listOf(
    Attendance(
        subjectCode = "00F4",
        subjectName = "Data Mining and Data Warehousing",
        attendedClasses = 4,
        totalClasses = 41,
        percentage = (4.0 / 41) * 100,
        facultyName = "Amiya Ranjan Panda"
    ),
    Attendance(
        subjectCode = "00F5",
        subjectName = "Engineering Economics",
        attendedClasses = 4,
        totalClasses = 39,
        percentage = (4.0 / 39) * 100,
        facultyName = "Arvind Kumar Yadav"
    ),
    Attendance(
        subjectCode = "00F6",
        subjectName = "Design and Analysis of Algorithms",
        attendedClasses = 1,
        totalClasses = 41,
        percentage = (1.0 / 41) * 100,
        facultyName = "Partha Sarathi Paul"
    ),
    Attendance(
        subjectCode = "00F7",
        subjectName = "Software Engineering",
        attendedClasses = 24,
        totalClasses = 52,
        percentage = (24.0 / 52) * 100,
        facultyName = "Ipsita Paul"
    ),
    Attendance(
        subjectCode = "00F8",
        subjectName = "Computer Networks",
        attendedClasses = 10,
        totalClasses = 40,
        percentage = (10.0 / 40) * 100,
        facultyName = "Nitin Varyani"
    )
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalHazeMaterialsApi::class,
    ExperimentalHazeApi::class
)
@Composable
fun AttendanceDialog(
    requiredAttendance: Int,
    hazeState: HazeState,
    attendance: Attendance,
    onDismiss: () -> Unit
) {
    val uiColors = UIColors()
    var targetProgress by remember { mutableFloatStateOf(0f) }

    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "attendance"
    )
    LaunchedEffect(Unit) {
        targetProgress = (attendance.percentage.toFloat() / 100f).coerceIn(0f, 1f)
    }
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 24.dp, spotColor = uiColors.progressAccent)
                .clip(shape = RoundedCornerShape(24.dp))
                .border(
                    width = Dp.Hairline,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.5f),
                            Color.White.copy(alpha = 0.1f),
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin()) {
                    blurRadius = 30.dp
                    noiseFactor = 0.05f
                    inputScale = HazeInputScale.Auto
                    alpha = 0.98f
                    tints = listOf(HazeTint(Color(0xFF86431D).copy(alpha = 0.15f)))
                }
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = attendance.subjectName,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = uiColors.textPrimary,
                style = MaterialTheme.typography.titleMediumEmphasized,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Text(
                text = attendance.facultyName,
                fontFamily = FontFamily.Monospace,
                color = uiColors.textSecondary,
                style = MaterialTheme.typography.bodySmallEmphasized,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearWavyProgressIndicator(
                progress = { progress },
                color = uiColors.accentOrangeStart,
                trackColor = uiColors.progressAccent,
                modifier = Modifier.fillMaxWidth(),
                amplitude = { 0.8f },
                waveSpeed = 20.dp,
                wavelength = 50.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PersonAddAlt1,
                            contentDescription = "Attended",
                            tint = Color(0xFF42B860)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = attendance.attendedClasses.toString(),
                            fontWeight = FontWeight.Bold,
                            color = uiColors.textPrimary,
                            style = MaterialTheme.typography.bodyMediumEmphasized,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Present",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBDBDB7),
                        style = MaterialTheme.typography.bodyMediumEmphasized,
                    )
                }
                VerticalDivider(modifier = Modifier.height(45.dp), color = Color(0xFF85857F))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PersonRemoveAlt1,
                            contentDescription = "Absent",
                            tint = Color(0xFFEB4945)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${attendance.totalClasses - attendance.attendedClasses}",
                            fontWeight = FontWeight.Bold,
                            color = uiColors.textPrimary,
                            style = MaterialTheme.typography.bodyMediumEmphasized,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Absent",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBDBDB7),
                        style = MaterialTheme.typography.bodyMediumEmphasized,
                    )
                }
                VerticalDivider(modifier = Modifier.height(45.dp), color = Color(0xFF85857F))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Percent,
                            contentDescription = "Percentage",
                            tint = Color(0xFF0290EE)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = attendance.percentage.roundToInt().toString() + "%",
                            fontWeight = FontWeight.Bold,
                            color = uiColors.textPrimary,
                            style = MaterialTheme.typography.bodyMediumEmphasized,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Percent",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBDBDB7),
                        style = MaterialTheme.typography.bodyMediumEmphasized,
                    )
                }
                VerticalDivider(modifier = Modifier.height(45.dp), color = Color(0xFF85857F))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.LibraryBooks,
                            contentDescription = "Total",
                            tint = Color(0xFFEE7402)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = attendance.totalClasses.toString(),
                            fontWeight = FontWeight.Bold,
                            color = uiColors.textPrimary,
                            style = MaterialTheme.typography.bodyMediumEmphasized,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Total",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBDBDB7),
                        style = MaterialTheme.typography.bodyMediumEmphasized,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                color = Color(0xFF85857F),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val attendanceDecrease = calculateAttendancePercentage1Decimal(
                    attendedClasses = attendance.attendedClasses,
                    totalClasses = attendance.totalClasses + 1
                )
                val attendanceIncrease = calculateAttendancePercentage1Decimal(
                    attendedClasses = attendance.attendedClasses + 1,
                    totalClasses = attendance.totalClasses + 1
                )
                Text(
                    text = "Next Class Impact:",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = uiColors.textPrimary,
                    style = MaterialTheme.typography.bodySmallEmphasized,
                )
                Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                    Box(
                        modifier = Modifier
                            .shadow(elevation = 24.dp, spotColor = uiColors.progressAccent)
                            .clip(shape = RoundedCornerShape(24.dp))
                            .border(
                                width = Dp.Hairline,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.5f),
                                        Color.White.copy(alpha = 0.1f),
                                    )
                                ),
                                shape = CircleShape
                            )
                            .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin()) {
                                blurRadius = 30.dp
                                noiseFactor = 0.05f
                                inputScale = HazeInputScale.Auto
                                alpha = 0.98f
                                tints = listOf(HazeTint(Color(0xFF169B27).copy(alpha = 0.3f)))
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$attendanceIncrease%",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmallEmphasized,
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Increase",
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
                Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                    Box(
                        modifier = Modifier
                            .shadow(elevation = 24.dp, spotColor = uiColors.progressAccent)
                            .clip(shape = RoundedCornerShape(24.dp))
                            .border(
                                width = Dp.Hairline,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.5f),
                                        Color.White.copy(alpha = 0.1f),
                                    )
                                ),
                                shape = CircleShape
                            )
                            .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin()) {
                                blurRadius = 30.dp
                                noiseFactor = 0.05f
                                inputScale = HazeInputScale.Auto
                                alpha = 0.98f
                                tints = listOf(HazeTint(Color(0xFFBD1014).copy(alpha = 0.3f)))
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$attendanceDecrease%",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmallEmphasized,
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Decrease",
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) {
                val requiredClasses = remember(attendance) {
                    classesRequiredForPercentage(
                        attended = attendance.attendedClasses,
                        total = attendance.totalClasses,
                        requiredPercentage = requiredAttendance.toDouble()
                    )
                }
                val belowRequired = attendance.percentage < requiredAttendance.toDouble()
                Box(
                    modifier = Modifier
                        .shadow(elevation = 24.dp, spotColor = uiColors.progressAccent)
                        .clip(shape = RoundedCornerShape(24.dp))
                        .border(
                            width = Dp.Hairline,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.5f),
                                    Color.White.copy(alpha = 0.1f),
                                )
                            ),
                            shape = CircleShape
                        )
                        .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin()) {
                            blurRadius = 30.dp
                            noiseFactor = 0.05f
                            inputScale = HazeInputScale.Auto
                            alpha = 0.98f
                            tints = listOf(
                                HazeTint(
                                    if (belowRequired) Color(0xFFA94F12).copy(alpha = 0.3f)
                                    else Color(0xFF169B27).copy(alpha = 0.3f)
                                )
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (belowRequired) Icons.Default.Dangerous else Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (belowRequired) Color(0xFFEA6E1D) else Color(0xFF169B27)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        when {
                            requiredClasses >= 50 -> Text(
                                text = "Tumse Na Ho Payega (required ${
                                    if (requiredClasses > 1000) "1000+" else requiredClasses
                                })",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmallEmphasized,
                            )
                            requiredClasses > 0 -> Text(
                                text = "Attend $requiredClasses more classes to reach $requiredAttendance%",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmallEmphasized,
                            )
                            else -> Text(
                                text = "Attendance is above $requiredAttendance%",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmallEmphasized,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
