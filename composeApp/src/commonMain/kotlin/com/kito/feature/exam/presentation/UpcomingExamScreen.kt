package com.kito.feature.exam.presentation

import androidx.compose.animation.Animatable
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kito.core.common.util.formatDate
import com.kito.core.common.util.formatTo12Hour
import com.kito.core.designsystem.SharedExpandContainer
import com.kito.core.designsystem.UIColors
import com.kito.core.designsystem.meshGradient
import com.kito.core.presentation.components.animation.NoDataFoundAnimation
import com.kito.core.presentation.navigation3.Routes
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import kotlin.random.Random
import kotlin.time.Clock


@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalHazeApi::class, ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class
)
@Composable
fun UpcomingExamScreen(
    viewmodel: UpcomingExamViewModel = koinInject(),
    onBack:() -> Unit
) {
    val uiColors = UIColors()
    val hazeState = rememberHazeState()
    val examModel by viewmodel.exams.collectAsState()
    val uiState by viewmodel.uiState.collectAsState()
    val meshColors = listOf(
        Color(0xFF77280F).copy(alpha = 0.82f), // burnt orange
        Color(0xFF753107).copy(alpha = 0.82f), // amber-700
        Color(0xFF62290A).copy(alpha = 0.82f), // amber-800
        Color(0xFF46180C).copy(alpha = 0.82f), // deep orange-brown

        // 🔥 new additions (subtle!)
        Color(0xFFA14B09).copy(alpha = 0.70f), // muted yellow (amber-500 toned down)
        Color(0xFF6B1414).copy(alpha = 0.75f), // brick red (not crimson)
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
                    val nextColor = meshColors[random.nextInt(meshColors.size)]
                    anim.animateTo(
                        targetValue = nextColor,
                        animationSpec = tween(
                            durationMillis = random.nextInt(1800, 4200),
                            easing = LinearOutSlowInEasing
                        )
                    )
                }
            }
        }
    }
    SharedExpandContainer(
        routeKey = Routes.ExamSchedule,
        backgroundColor = Color(0xFF121116),
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                top = WindowInsets.statusBars.asPaddingValues()
                    .calculateTopPadding() + 46.dp
            ),
            verticalArrangement = Arrangement.spacedBy(2.5.dp),
            modifier = Modifier
                .hazeSource(hazeState)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            itemsIndexed(examModel) { index, item ->
                val today = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date

                val examDate = LocalDate.parse(item.date)

                val daysLeft = examDate.toEpochDays() - today.toEpochDays()

                val showGradient = index == 0 && daysLeft <= 7
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(
                        topStart = if (index == 0) 24.dp else 4.dp,
                        topEnd = if (index == 0) 24.dp else 4.dp,
                        bottomStart = if (index == examModel.size - 1) 24.dp else 4.dp,
                        bottomEnd = if (index == examModel.size - 1) 24.dp else 4.dp
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (showGradient) {
                                    Modifier.meshGradient(
                                        points = listOf(

                                            // ───── TOP ROW ─────
                                            listOf(
                                                Offset(0f, 0f) to meshColorAnimators[0].value,
                                                Offset(0.25f, 0f) to meshColorAnimators[1].value,
                                                Offset(0.5f, 0f) to meshColorAnimators[2].value,
                                                Offset(0.75f, 0f) to meshColorAnimators[3].value,
                                                Offset(1f, 0f) to meshColorAnimators[4].value,
                                            ),

                                            // ───── MIDDLE ROW (curved glow band) ─────
                                            listOf(
                                                Offset(-0.05f, 0.55f) to meshColorAnimators[5].value,
                                                Offset(0.2f, animatedPointTop.value) to meshColorAnimators[6].value,
                                                Offset(0.5f, 0.6f) to meshColorAnimators[7].value,
                                                Offset(0.8f, animatedPointMid.value) to meshColorAnimators[8].value,
                                                Offset(1.05f, 0.55f) to meshColorAnimators[9].value,
                                            ),

                                            // ───── BOTTOM ROW (independent animation per point) ─────
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
                                }else{
                                    Modifier.background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                uiColors.cardBackground,
                                                Color(0xFF2F222F),
                                                Color(0xFF2F222F),
                                                uiColors.cardBackgroundHigh
                                            )
                                        )
                                    )
                                }
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxSize()
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(48.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                uiColors.accentOrangeStart,
                                                uiColors.accentOrangeEnd
                                            )
                                        ),
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 6.dp)
                                        .weight(1f)
                                ) {
                                    Text(
                                        text = item.subject,
                                        color = uiColors.textPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        style = MaterialTheme.typography.titleMediumEmphasized,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${formatTo12Hour(item.startTime)} - ${formatTo12Hour(item.endTime)}",
                                        color = uiColors.textPrimary.copy(alpha = 0.85f),
                                        style = MaterialTheme.typography.labelSmallEmphasized,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                val formattedDate = remember(item.date) {
                                    formatDate(item.date)
                                }
                                Text(
                                    text = formattedDate,
                                    color = uiColors.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.titleSmallEmphasized,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
//            item {
//                Spacer(
//                    modifier = Modifier.height(
//                        WindowInsets.statusBars.asPaddingValues()
//                            .calculateTopPadding() + 16.dp
//                    )
//                )
//            }
        }
        if (examModel.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                NoDataFoundAnimation()
            }
        }
        Column(
            modifier = Modifier
                .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin()) {
                    blurRadius = 15.dp
                    noiseFactor = 0.05f
                    inputScale = HazeInputScale.Auto
                    alpha = 0.98f
                }
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Spacer(
                modifier = Modifier.height(
                    16.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        onBack()
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.08f),
                        contentColor = uiColors.progressAccent
                    ),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "pop back stack",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Upcoming Exams",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = uiColors.textPrimary,
                    style = MaterialTheme.typography.titleLargeEmphasized,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


