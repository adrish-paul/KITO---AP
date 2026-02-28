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
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 100.dp, bottom = 80.dp)
    ) {

        groupedHolidays.forEach { (month, holidays) ->

            item(key = "header_$month") {
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
                        .aspectRatio(3f)
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

            // Holiday rows — unchanged
            itemsIndexed(holidays) { index, holiday ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
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