package com.kito.feature.gpa.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kito.core.platform.sendEmail
import com.kito.core.presentation.components.RopeTabRow
import com.kito.core.presentation.components.UIColors
import com.kito.feature.gpa.presentation.components.CGPAScreen
import com.kito.feature.gpa.presentation.components.GPAHeader
import com.kito.feature.gpa.presentation.components.SGPAScreen
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalHazeApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun GPAScreen(
    viewModel: GPAViewmodel = koinInject(),
    onBack:() -> Unit
) {

    val uiColors = UIColors()
    val student by viewModel.student.collectAsState()
    val selectedSemester by viewModel.semester.collectAsState()
    val selectedBranch by viewModel.branch.collectAsState()
    val hazeState = rememberHazeState()
    val roll by viewModel.roll.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = {
            2
        }
    )
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121116))
    ) {


        Box(
            modifier = Modifier.hazeSource(hazeState)
        ) {
            HorizontalPager(
                state = pagerState
            ){
                when(it) {
                    0 -> SGPAScreen(
                        selectedSemester = selectedSemester,
                        selectedBranch = selectedBranch
                    )

                    1 -> CGPAScreen()
                }
            }
        }

        Box(
            modifier = Modifier
                .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin()) {
                    blurRadius = 15.dp
                    noiseFactor = 0.05f
                    inputScale = HazeInputScale.Auto
                    alpha = 0.98f
                }
        ) {
            Column(
                modifier = Modifier
                    .padding(
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                        start = 16.dp,
                        end = 16.dp
                    )
            ) {
                Spacer(modifier = Modifier.height(16.dp))
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
                    ){
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Report",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GPA Calc",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = uiColors.textPrimary,
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        modifier = Modifier
                            .weight(1f)
                    )
                    IconButton(
                        onClick = {
                            sendEmail(
                                to = "elabs.kiito@gmail.com",
                                subject = "KIITO GPA Calc Screen Report",
                                body = ""
                            )
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.08f),
                            contentColor = Color(0xFFB32727)
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Report,
                            contentDescription = "Report",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box() {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(
                                RoundedCornerShape(24.dp)
                            )
                            .background(Color(0xFF121116))
                    )
                    GPAHeader(
                        roll = roll,
                        isLoading = roll.isEmpty(),
                        selectedSemester = selectedSemester,
                        selectedBranch = selectedBranch,
                        onSemesterSelected = {
                            viewModel.updateSemester(it)
                        },
                        onBranchSelected = {
                            viewModel.updateBranch(it)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                val tabProgress = pagerState.currentPage + pagerState.currentPageOffsetFraction
                RopeTabRow(
                    tabPosition = tabProgress,
                    onTabSelected = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                page = it,
                                animationSpec = tween(
                                    durationMillis = 400,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
