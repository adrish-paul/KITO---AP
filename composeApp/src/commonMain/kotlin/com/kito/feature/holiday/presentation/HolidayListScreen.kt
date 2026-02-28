package com.kito.feature.holiday.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
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
        contentPadding = PaddingValues(top = 100.dp, bottom = 80.dp)
    ) {

        groupedHolidays.entries.forEachIndexed { groupIndex, (month, holidays) ->

            item(key = "header_$month") {
                if (groupIndex > 0) {
                    Spacer(modifier = Modifier.height(8.dp)) // ← big gap between groups
                }
                val itemInfo = listState.layoutInfo.visibleItemsInfo
                    .firstOrNull { it.key == "header_$month" }
                val viewportHeight = (
                        listState.layoutInfo.viewportEndOffset
                                - listState.layoutInfo.viewportStartOffset
                        ).toFloat()
                val cardOffset = itemInfo?.offset?.toFloat() ?: 0f
                var cardWidthPx by remember { mutableStateOf(0f) }
                val imageHeightPx = cardWidthPx * (21f / 9f)
                val translateY = (viewportHeight - imageHeightPx) - cardOffset
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f)
                        .onSizeChanged { cardWidthPx = it.width.toFloat() },
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {

                        Image(
                            painter = painterResource(getMonthImage(month)),
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .layout { measurable, constraints ->
                                    // remove the max height constraint → image measures at its natural height
                                    val placeable = measurable.measure(
                                        constraints.copy(maxHeight = Int.MAX_VALUE)
                                    )
                                    layout(placeable.width, placeable.height) {
                                        placeable.place(0, 0)
                                    }
                                }
                                .graphicsLayer {
                                    scaleY = 1.2f
                                    translationY = translateY + 700f
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
                                .align(Alignment.CenterStart)
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

            // Holiday rows — unchanged
            itemsIndexed(holidays) { index, holiday ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = if (index == 0) 8.dp else 2.5.dp
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(
                        topStart    = if (index == 0)               24.dp else 4.dp,
                        topEnd      = if (index == 0)               24.dp else 4.dp,
                        bottomStart = if (index == holidays.lastIndex) 24.dp else 4.dp,
                        bottomEnd   = if (index == holidays.lastIndex) 24.dp else 4.dp
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(uiColors.cardBackground)
                            .padding(horizontal = 20.dp, vertical = 18.dp)
                    ) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            // 📅 Date Block (Left)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(70.dp)
                            ) {

                                Text(
                                    text = holiday.startDate.take(2), // Day
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = uiColors.textPrimary,
                                    fontFamily = FontFamily.Monospace
                                )

                                Text(
                                    text = holiday.month.take(3).uppercase(), // Month
                                    style = MaterialTheme.typography.labelMedium,
                                    color = uiColors.textSecondary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // 🏷 Holiday Details (Right)
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {

                                Text(
                                    text = holiday.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = uiColors.textPrimary,
                                    fontFamily = FontFamily.Monospace
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Multi-day support
                                if (holiday.numberOfDays > 1) {
                                    Text(
                                        text = "${holiday.startDate} - ${holiday.endDate}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = uiColors.textSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                } else {
                                    Text(
                                        text = holiday.startDay,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = uiColors.textSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                if (holiday.numberOfDays > 1){
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "• ${holiday.numberOfDays} Days",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = uiColors.textSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
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
        month.contains("February") -> Res.drawable.feb_bg
        month.contains("March") -> Res.drawable.march_bg
        month.contains("April") -> Res.drawable.april_bg
        month.contains("May") -> Res.drawable.may_bg
        month.contains("June") -> Res.drawable.june_bg
        month.contains("July") -> Res.drawable.july_bg
        month.contains("August") -> Res.drawable.aug_bg
        month.contains("September") -> Res.drawable.sept_bg
        month.contains("October") -> Res.drawable.oct_bg
        month.contains("November") -> Res.drawable.nov_bg
        month.contains("December") -> Res.drawable.dec_bg
        else -> Res.drawable.january1_bg
    }
}