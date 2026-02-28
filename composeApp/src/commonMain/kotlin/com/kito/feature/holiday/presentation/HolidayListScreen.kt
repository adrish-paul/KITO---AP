////package com.kito.feature.holiday.presentation
////
////import androidx.compose.foundation.background
////import androidx.compose.foundation.layout.*
////import androidx.compose.foundation.lazy.LazyColumn
////import androidx.compose.foundation.lazy.itemsIndexed
////import androidx.compose.foundation.shape.RoundedCornerShape
////import androidx.compose.material3.*
////import androidx.compose.runtime.Composable
////import androidx.compose.ui.*
////import androidx.compose.ui.graphics.Brush
////import androidx.compose.ui.graphics.Color
////import androidx.compose.ui.text.font.FontFamily
////import androidx.compose.ui.text.font.FontWeight
////import androidx.compose.ui.unit.dp
////import com.kito.core.presentation.components.UIColors
////
////@Composable
////fun HolidayListScreen() {
////    val uiColors = UIColors()
////    val groupedHolidays = holidayList2026.groupBy { it.month }
////
////    LazyColumn(
////        modifier = Modifier
////            .fillMaxSize()
////            .background(Color(0xFF121116))
////            .padding(horizontal = 16.dp),
////        verticalArrangement = Arrangement.spacedBy(2.5.dp),
////        contentPadding = PaddingValues(
////            top = 80.dp,
////            bottom = 80.dp
////        )
////    ) {
////
////        groupedHolidays.forEach { (month, holidays) ->
////
////            item {
////                Text(
////                    text = month,
////                    fontFamily = FontFamily.Monospace,
////                    fontWeight = FontWeight.Bold,
////                    color = uiColors.textPrimary,
////                    style = MaterialTheme.typography.headlineMedium,
////                    modifier = Modifier.padding(vertical = 16.dp)
////                )
////            }
////
////            itemsIndexed(holidays) { index, holiday ->
////
////                Card(
////                    modifier = Modifier.fillMaxWidth(),
////                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
////                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
////                    shape = RoundedCornerShape(
////                        topStart = if (index == 0) 24.dp else 4.dp,
////                        topEnd = if (index == 0) 24.dp else 4.dp,
////                        bottomStart = if (index == holidays.lastIndex) 24.dp else 4.dp,
////                        bottomEnd = if (index == holidays.lastIndex) 24.dp else 4.dp
////                    )
////                ) {
////
////                    Box(
////                        modifier = Modifier
////                            .fillMaxWidth()
////                            .background(
////                                brush = Brush.linearGradient(
////                                    colors = listOf(
////                                        uiColors.cardBackground,
////                                        Color(0xFF2F222F),
////                                        Color(0xFF2F222F),
////                                        uiColors.cardBackgroundHigh
////                                    )
////                                )
////                            )
////                            .padding(20.dp)
////                    ) {
////
////                        Row(
////                            modifier = Modifier.fillMaxWidth(),
////                            horizontalArrangement = Arrangement.SpaceBetween
////                        ) {
////
////                            Text(
////                                text = holiday.name,
////                                fontFamily = FontFamily.Monospace,
////                                fontWeight = FontWeight.SemiBold,
////                                color = uiColors.textPrimary,
////                                style = MaterialTheme.typography.titleMedium
////                            )
////
////                            Text(
////                                text = holiday.date,
////                                fontFamily = FontFamily.Monospace,
////                                color = uiColors.textSecondary,
////                                style = MaterialTheme.typography.titleMedium
////                            )
////                        }
////                    }
////                }
////            }
////        }
////
////        item {
////            Spacer(
////                modifier = Modifier.height(
////                    42.dp + WindowInsets.navigationBars.asPaddingValues()
////                        .calculateBottomPadding()
////                )
////            )
////        }
////    }
////}
//package com.kito.feature.holiday.presentation
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.itemsIndexed
//import androidx.compose.foundation.lazy.rememberLazyListState // 🔥 NEW
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp // 🔥 NEW
//import com.kito.core.presentation.components.UIColors
//import kito.composeapp.generated.resources.Res
//import kito.composeapp.generated.resources.april_bg
//import kito.composeapp.generated.resources.august_bg
//import kito.composeapp.generated.resources.december_bg
//import kito.composeapp.generated.resources.february_bg
//import kito.composeapp.generated.resources.january_bg
//import kito.composeapp.generated.resources.july_bg
//import kito.composeapp.generated.resources.june_bg
//import kito.composeapp.generated.resources.march_bg
//import kito.composeapp.generated.resources.may_bg
//import kito.composeapp.generated.resources.november_bg
//import kito.composeapp.generated.resources.october_bg
//import kito.composeapp.generated.resources.september_bg
//import org.jetbrains.compose.resources.DrawableResource
//import org.jetbrains.compose.resources.painterResource
//
//@Composable
//fun HolidayListScreen() {
//
//    val uiColors = UIColors()
//
//    val groupedHolidays = holidayList2026.groupBy { it.month }
//
//    val listState = rememberLazyListState() // 🔥 AUTO SCROLL READY
//
//
//    LazyColumn(
//        state = listState, // 🔥 NEW
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFF121116))
//            .padding(horizontal = 16.dp),
//        verticalArrangement = Arrangement.spacedBy(8.dp), // 🔥 CHANGED spacing
//        contentPadding = PaddingValues(
//            top = 100.dp, // 🔥 slightly increased
//            bottom = 80.dp
//        )
//    ) {
//
//        groupedHolidays.forEach { (month, holidays) ->
//
//            // 🔥 MONTH IMAGE CARD (Instead of Plain Text)
//            item {
//
//                val scrollOffset = listState.firstVisibleItemScrollOffset
//
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .aspectRatio(3f),
//                    shape = RoundedCornerShape(28.dp),
//                    elevation = CardDefaults.cardElevation(8.dp)
//                ) {
//
//                    Box {
//                        val parallaxOffset = (scrollOffset * 0.1f)
//                        // 🔥 Replace with your own month image
//                        Image(
//                            painter = painterResource(getMonthImage(month)),
//                            contentDescription = null,
//                            contentScale = ContentScale.Crop,
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .graphicsLayer {
//                                    scaleY = 1.2f
//                                    translationY = parallaxOffset
//                                }
//                        )
//
//                        // 🔥 Dark overlay for text visibility
//                        Box(
//                            modifier = Modifier
//                                .fillMaxSize()
//                                .background(Color.Black.copy(alpha = 0.35f))
//                        )
//
//                        Text(
//                            text = month,
//                            modifier = Modifier
//                                .align(Alignment.BottomStart)
//                                .padding(20.dp),
//                            fontFamily = FontFamily.Monospace,
//                            fontWeight = FontWeight.SemiBold,
//                            letterSpacing = 1.sp, // 🔥 CHANGED
//                            color = Color.White,
//                            style = MaterialTheme.typography.headlineLarge // 🔥 CHANGED
//                        )
//                    }
//                }
//            }
//
//            // 🔥 HOLIDAY CARDS
//            itemsIndexed(holidays) { index, holiday ->
//
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
//                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
//                    shape = RoundedCornerShape(
//                        topStart = if (index == 0) 24.dp else 4.dp,
//                        topEnd = if (index == 0) 24.dp else 4.dp,
//                        bottomStart = if (index == holidays.lastIndex) 24.dp else 4.dp,
//                        bottomEnd = if (index == holidays.lastIndex) 24.dp else 4.dp
//                    )
//                ) {
//
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .background(
//                                brush = Brush.linearGradient(
//                                    colors = listOf(
//                                        uiColors.cardBackground,
//                                        Color(0xFF2F222F),
//                                        Color(0xFF2F222F),
//                                        uiColors.cardBackgroundHigh
//                                    )
//                                )
//                            )
//                            .padding(20.dp)
//                    ) {
//
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//
//                            // 🔥 Holiday Name (Improved Typography)
//                            Text(
//                                text = holiday.name,
//                                fontFamily = FontFamily.Monospace,
//                                fontWeight = FontWeight.SemiBold,
//                                color = uiColors.textPrimary,
//                                style = MaterialTheme.typography.titleMedium
//                            )
//
//                            // 🔥 Date Style Slightly Adjusted
//                            Text(
//                                text = holiday.date,
//                                fontFamily = FontFamily.Monospace,
//                                color = uiColors.textSecondary,
//                                style = MaterialTheme.typography.bodyLarge // 🔥 CHANGED
//                            )
//                        }
//                    }
//                }
//            }
//        }
//
//        item {
//            Spacer(
//                modifier = Modifier.height(
//                    42.dp + WindowInsets.navigationBars.asPaddingValues()
//                        .calculateBottomPadding()
//                )
//            )
//        }
//    }
//}
//
//private fun getMonthImage(month: String): DrawableResource {
//    return when {
//        month.contains("January") -> Res.drawable.january_bg
//        month.contains("February") -> Res.drawable.february_bg
//        month.contains("March") -> Res.drawable.march_bg
//        month.contains("April") -> Res.drawable.april_bg
//        month.contains("May") -> Res.drawable.may_bg
//        month.contains("June") -> Res.drawable.june_bg
//        month.contains("July") -> Res.drawable.july_bg
//        month.contains("August") -> Res.drawable.august_bg
//        month.contains("September") -> Res.drawable.september_bg
//        month.contains("October") -> Res.drawable.october_bg
//        month.contains("November") -> Res.drawable.november_bg
//        month.contains("December") -> Res.drawable.december_bg
//        else -> Res.drawable.january_bg
//    }
//}
package com.kito.feature.holiday.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kito.core.presentation.components.UIColors
import kito.composeapp.generated.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun HolidayListScreen() {

    val uiColors = UIColors()
    val groupedHolidays = holidayList2026.groupBy { it.month }
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121116))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 100.dp, bottom = 80.dp)
    ) {

        groupedHolidays.forEach { (month, holidays) ->

            item {

                val layoutInfo = listState.layoutInfo

                val itemInfo = layoutInfo.visibleItemsInfo
                    .firstOrNull { it.index == layoutInfo.visibleItemsInfo.firstOrNull()?.index }

                val parallaxOffset = itemInfo?.let {

                    val viewportHeight =
                        layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset

                    // how far item is from bottom of screen
                    val distanceFromBottom = viewportHeight - it.offset

                    // clamp to avoid overscroll
                    val clamped = distanceFromBottom.coerceIn(0, it.size)

                    // move image upward only
                    -clamped * 0.25f   // 🔥 adjust strength here (0.15–0.35)
                } ?: 0f

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Box {

                        Image(
                            painter = painterResource(getMonthImage(month)),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleY = 1.4f
                                    translationY = parallaxOffset
                                }
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f))
                        )

                        Text(
                            text = month,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(20.dp),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                }
            }

            // Normal holiday cards (no parallax)
            itemsIndexed(holidays) { index, holiday ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(
                        topStart = if (index == 0) 24.dp else 4.dp,
                        topEnd = if (index == 0) 24.dp else 4.dp,
                        bottomStart = if (index == holidays.lastIndex) 24.dp else 4.dp,
                        bottomEnd = if (index == holidays.lastIndex) 24.dp else 4.dp
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(uiColors.cardBackground)
                            .padding(20.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Text(
                                text = holiday.name,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = uiColors.textPrimary,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = holiday.date,
                                fontFamily = FontFamily.Monospace,
                                color = uiColors.textSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(
                modifier = Modifier.height(
                    42.dp + WindowInsets.navigationBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                )
            )
        }
    }
}

private fun getMonthImage(month: String): DrawableResource {
    return when {
        month.contains("January") -> Res.drawable.january1_bg
        month.contains("February") -> Res.drawable.february_bg
        month.contains("March") -> Res.drawable.march_bg
        month.contains("April") -> Res.drawable.january1_bg
        month.contains("May") -> Res.drawable.may_bg
        month.contains("June") -> Res.drawable.june_bg
        month.contains("July") -> Res.drawable.july_bg
        month.contains("August") -> Res.drawable.august_bg
        month.contains("September") -> Res.drawable.september_bg
        month.contains("October") -> Res.drawable.october_bg
        month.contains("November") -> Res.drawable.november_bg
        month.contains("December") -> Res.drawable.december_bg
        else -> Res.drawable.january1_bg
    }
}