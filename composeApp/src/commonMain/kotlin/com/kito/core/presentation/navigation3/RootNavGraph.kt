package com.kito.core.presentation.navigation3

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.kito.core.presentation.components.ExpressiveEasing
import com.kito.feature.auth.presentation.OnBoardingScreen
import com.kito.feature.auth.presentation.UserSetupScreen
import com.kito.feature.calendar.presentation.CalendarScreen
import com.kito.feature.exam.presentation.UpcomingExamScreen
import com.kito.feature.faculty.presentation.FacultyDetailScreen
import com.kito.feature.friendview.presentation.FriendView
import com.kito.feature.gpa.presentation.GPAScreen
import com.kito.feature.holiday.presentation.HolidayListScreen
import com.kito.feature.khaoogully.presentation.KhaooGullyHomeScreen
import com.kito.feature.khaoogully.presentation.RestaurantMenuScreen
import com.kito.feature.promotions.presentations.PromotionsScreen
import com.kito.feature.schedule.presentation.ScheduleScreen

@Composable
fun RootNavGraph(
    rootNavBackStack: NavBackStack<NavKey>,
    tabNavBackStack: NavBackStack<NavKey>,
    snackbarHostState: SnackbarHostState,
) {
    NavDisplay(
        backStack = rootNavBackStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator()
        ),
        predictivePopTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -(fullWidth * 0.3f).toInt() },
                animationSpec = tween(durationMillis = 300, easing = ExpressiveEasing.Emphasized)
            ) togetherWith slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(durationMillis = 300, easing = ExpressiveEasing.Emphasized)
            )
        },
        popTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -(fullWidth * 0.3f).toInt() },
                animationSpec = tween(durationMillis = 300, easing = ExpressiveEasing.Emphasized)
            ) togetherWith slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(durationMillis = 300, easing = ExpressiveEasing.Emphasized)
            )
        },
        transitionSpec = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(durationMillis = 600, easing = ExpressiveEasing.Emphasized)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { fullWidth -> -(fullWidth * 0.3f).toInt() },
                animationSpec = tween(durationMillis = 600, easing = ExpressiveEasing.Emphasized)
            )
        },
        entryProvider = entryProvider {
            entry<Routes.Tabs> {
                TabNavGraph(
                    rootNavStack = rootNavBackStack,
                    tabNavStack = tabNavBackStack,
                    snackbarHostState = snackbarHostState
                )
            }
            entry<Routes.Schedule> {
                ScheduleScreen(
                    onBack = { rootNavBackStack.removeAt(rootNavBackStack.lastIndex) }
                )
            }
            entry<Routes.ExamSchedule> {
                UpcomingExamScreen(
                    onBack = { rootNavBackStack.removeAt(rootNavBackStack.lastIndex) }
                )
            }
            entry<Routes.FacultyDetail> {
                FacultyDetailScreen(
                    facultyId = it.facultyId,
                    onBack = { rootNavBackStack.removeAt(rootNavBackStack.lastIndex) }
                )
            }
            entry<Routes.Onboarding> {
                OnBoardingScreen(
                    onOnboardingComplete = {
                        rootNavBackStack.clear()
                        rootNavBackStack.add(Routes.UserSetup)
                    }
                )
            }
            entry<Routes.UserSetup> {
                UserSetupScreen(
                    onSetupComplete = {
                        rootNavBackStack.clear()
                        rootNavBackStack.add(Routes.Tabs)
                    }
                )
            }
            entry<Routes.Promotions> {
                PromotionsScreen(url = it.url)
            }
            entry<Routes.FriendView> {
                FriendView(
                    onBack = { rootNavBackStack.removeAt(rootNavBackStack.lastIndex) }
                )
            }
            entry<Routes.HolidayList> {
                HolidayListScreen(
                    onBack = { rootNavBackStack.removeAt(rootNavBackStack.lastIndex) }
                )
            }
            entry<Routes.GPACalc> {
                GPAScreen(
                    onBack = { rootNavBackStack.removeAt(rootNavBackStack.lastIndex) }
                )
            }
            entry<Routes.Calendar> {
                KhaooGullyHomeScreen(
                    onRestaurantClick = { restaurant ->
                        rootNavBackStack.add(
                            Routes.RestaurantMenu(
                                restaurantId     = restaurant.id,
                                restaurantName   = restaurant.name,
                                restaurantImage  = restaurant.image,
                                restaurantRating = restaurant.rating,
                                browseOnly       = restaurant.browseOnly
                            )
                        )
                    }
                )
            }
            // ── Restaurant menu detail ────────────────────────────────────────
            entry<Routes.RestaurantMenu> {
                RestaurantMenuScreen(
                    route  = it,
                    onBack = { rootNavBackStack.removeAt(rootNavBackStack.lastIndex) }
                )
            }
        }
    )
}