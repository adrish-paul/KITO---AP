package com.kito.feature.app.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.kito.core.datastore.PrefsRepository
import com.kito.core.presentation.navigation.BottomBarTabs
import com.kito.core.presentation.navigation3.NavigationItems
import com.kito.core.presentation.navigation3.RootNavGraph
import com.kito.core.presentation.navigation3.Routes
import com.kito.core.presentation.navigation3.TabRoutes
import com.kito.core.presentation.navigation3.navigateTab
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.first
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalHazeMaterialsApi::class, ExperimentalHazeApi::class
)
@Composable
fun MainUI(
    appViewModel: AppViewModel = koinInject(),
    deepLinkTarget: String? = null,
    onDeepLinkConsumed: () -> Unit = {},
    initialDestination: NavKey? = null
) {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .crossfade(true)
            .build()
    }

    val prefs: PrefsRepository = koinInject()
    var startDestination by remember { mutableStateOf(initialDestination) }
    
    // Only fetch if initialDestination was null (mainly for iOS or fallback)
    LaunchedEffect(Unit) {
        if (startDestination == null) {
            val onboardingDone = prefs.onBoardingFlow.first()
            val isUserSetupDone = prefs.userSetupDoneFlow.first()
            startDestination = when {
                !onboardingDone -> Routes.Onboarding
                !isUserSetupDone -> Routes.UserSetup
                else -> Routes.Tabs
            }
        }
    }

    val currentStartDestination = startDestination
    // Show Splash until startDestination is determined
    if (currentStartDestination == null) {
        AppSplash()
        return
    }

    val rootBackStack = rememberNavBackStack(
        configuration = SavedStateConfiguration{
            serializersModule = SerializersModule{
                polymorphic(NavKey::class){
                    subclass(Routes.Tabs::class, Routes.Tabs.serializer())
                    subclass(Routes.Schedule::class, Routes.Schedule.serializer())
                    subclass(Routes.ExamSchedule::class, Routes.ExamSchedule.serializer())
                    subclass(Routes.FacultyDetail::class, Routes.FacultyDetail.serializer())
                    subclass(Routes.Onboarding::class, Routes.Onboarding.serializer())
                    subclass(Routes.UserSetup::class, Routes.UserSetup.serializer())
                    subclass(Routes.Promotions::class, Routes.Promotions.serializer())
                    subclass(Routes.FriendView::class, Routes.FriendView.serializer())
                    subclass(Routes.HolidayList::class, Routes.HolidayList.serializer())
                    subclass(Routes.GPACalc::class, Routes.GPACalc.serializer())
                }
            }
        },
        currentStartDestination // Default to Tabs, but we gate the UI until startDestination is non-null
    )
    val tabBackStack =  rememberNavBackStack(
        configuration = SavedStateConfiguration{
            serializersModule = SerializersModule{
                polymorphic(NavKey::class){
                    subclass(TabRoutes.Home::class, TabRoutes.Home.serializer())
                    subclass(TabRoutes.Profile::class, TabRoutes.Profile.serializer())
                    subclass(TabRoutes.Faculty::class, TabRoutes.Faculty.serializer())
                    subclass(TabRoutes.Attendance::class, TabRoutes.Attendance.serializer())
                }
            }
        },
        TabRoutes.Home
    )
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val shouldShowBottomBar = rootBackStack.last() == Routes.Tabs
    val snackbarHostState = remember { SnackbarHostState() }
    val navigationBarType = rememberNavigationBarType()
    LaunchedEffect(deepLinkTarget) {
        if (deepLinkTarget == "schedule") {
            rootBackStack.add(Routes.Schedule)
            onDeepLinkConsumed()
        }
    }
    LaunchedEffect(Unit) {
        appViewModel.checkResetFix()
    }
    LaunchedEffect(tabBackStack.last()) {
        selectedTabIndex = when {
            tabBackStack.last() == TabRoutes.Home -> 0
            tabBackStack.last() == TabRoutes.Attendance -> 1
            tabBackStack.last() == TabRoutes.Faculty -> 2
            tabBackStack.last() == TabRoutes.Profile -> 3
            else -> selectedTabIndex
        }
    }

    val hazeState = rememberHazeState()
    Surface {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                AnimatedVisibility(
                    visible = shouldShowBottomBar,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                            .padding(vertical = 10.dp, horizontal = 64.dp)
                            .fillMaxWidth()
                            .height(64.dp)
                            .clip(CircleShape)
                            .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin()) {
                                blurRadius = 15.dp
                                noiseFactor = 0.05f
                                inputScale = HazeInputScale.Auto
                                alpha = 0.98f
                            }
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
                    ) {
                        val animatedSelectedTabIndex by animateFloatAsState(
                            targetValue = selectedTabIndex.toFloat(),
                            label = "animatedSelectedTabIndex",
                            animationSpec = spring(
                                stiffness = Spring.StiffnessLow,
                                dampingRatio = Spring.DampingRatioLowBouncy,
                            )
                        )
                        val animatedColor by animateColorAsState(
                            targetValue = NavigationItems[selectedTabIndex].color,
                            label = "animatedColor",
                            animationSpec = spring(
                                stiffness = Spring.StiffnessLow,
                            )
                        )

                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val tabWidth = size.width / NavigationItems.size
                            val centerOffset = tabWidth * animatedSelectedTabIndex + tabWidth / 2

                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        animatedColor.copy(alpha = 0.3f),
                                        Color.Transparent
                                    ),
                                    center = Offset(centerOffset, size.height * 0.55f),
                                    radius = tabWidth * 0.7f
                                ),
                                radius = tabWidth * 0.7f,
                                center = Offset(centerOffset, size.height * 0.55f)
                            )

                            val path = Path().apply {
                                addRoundRect(
                                    RoundRect(
                                        size.toRect(),
                                        CornerRadius(size.height / 2f)
                                    )
                                )
                            }
                            val measure = PathMeasure()
                            measure.setPath(path, false)
                            drawPath(
                                path = path,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        animatedColor.copy(alpha = 0.5f),
                                        animatedColor,
                                        animatedColor.copy(alpha = 0.5f),
                                        Color.Transparent,
                                    ),
                                    startX = centerOffset - (tabWidth * 0.6f),
                                    endX = centerOffset + (tabWidth * 0.6f),
                                ),
                                style = Stroke(width = 5f)
                            )
                        }

                        BottomBarTabs(
                            tabs = NavigationItems,
                            selectedTab = selectedTabIndex,
                            onTabSelected = { item ->
                                tabBackStack.navigateTab(item.destination)
                            }
                        )
                    }
                }
            }
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .hazeSource(hazeState)
                ) {
                    RootNavGraph(
                        rootNavBackStack = rootBackStack,
                        tabNavBackStack = tabBackStack,
                        snackbarHostState = snackbarHostState
                    )
                }
                if (isAndroid() && navigationBarType == NavigationBarType.ThreeButton) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(
                                WindowInsets.navigationBars.asPaddingValues()
                                    .calculateBottomPadding()
                            )
                            .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin()) {
                                blurRadius = 15.dp
                                noiseFactor = 0.05f
                                inputScale = HazeInputScale.Auto
                                alpha = 0.98f
                            }
                    )
                }
            }
        }
    }
}

expect fun isAndroid(): Boolean
@Composable
fun rememberNavigationBarType(): NavigationBarType {
    val density = LocalDensity.current
    val bottomInset = WindowInsets.navigationBars.getBottom(density)

    return if (bottomInset > with(density) { 24.dp.roundToPx() })
        NavigationBarType.ThreeButton
    else
        NavigationBarType.Gesture
}

enum class NavigationBarType {
    Gesture,
    ThreeButton
}

@Composable
private fun AppSplash() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121116))
            .hazeEffect(
                state = rememberHazeState(),
                style = HazeMaterials.regular()
            )
    )
}