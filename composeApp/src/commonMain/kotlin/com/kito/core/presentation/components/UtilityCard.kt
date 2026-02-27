package com.kito.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UtilityCard() {
    val colors = UIColors()
    Box(
        modifier = Modifier
            .clip(
                RoundedCornerShape(26.dp)
            )
            .background(
                color = colors.cardBackground
            )
    ) {
        Column {
//            ShrinkingCarouselRow(
//                itemCount = 6,
//                itemWidth = 100.dp,
//                itemSpacing = 8.dp,
//                minScale = 0.6f
//            ) { index ->
//                GradientIcon(
//                    imageVector = Icons.Rounded.CalendarMonth,
//                    contentDescription = "Calendar",
//                    modifier = Modifier
//                        .size(64.dp)
//                        .align(Alignment.BottomEnd)
//                        .offset(x = 8.dp, y = 4.dp)
//                        .graphicsLayer { scaleX = 1.2f; scaleY = 1.2f},
//                    gradient = Brush.horizontalGradient(
//                        colors = listOf(Color(0xFFC7895F), Color(0xFF765138))
//                    )
//                )
//                Text(
//                    text = "GPA",
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis,
//                    modifier = Modifier.padding(8.dp),
//                    fontFamily = FontFamily.Monospace,
//                    style = MaterialTheme.typography.titleMediumEmphasized,
//                    fontWeight = FontWeight.SemiBold,
//                    color = Color(0xFFC7895F)
//                )
//            }
            ParallaxCarouselRow(
                itemCount = 6,
                itemWidth = 100.dp,
                itemSpacing = 8.dp,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                parallaxFraction = 0.4f
            ) { index ->
                GradientIcon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = "Calendar",
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 4.dp)
                        .graphicsLayer { scaleX = 1.2f; scaleY = 1.2f },
                    gradient = Brush.horizontalGradient(
                        colors = listOf(Color(0xFFC7895F), Color(0xFF765138))
                    )
                )
                Text(
                    text = "GPA",
                    modifier = Modifier.padding(8.dp),
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFC7895F)
                )
            }
//            ShrinkingParallaxCarouselRow(
//                itemCount = 6,
//                itemWidth = 100.dp,
//                itemSpacing = 8.dp,
//                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
//                cardColor = Color(0xFF5F2B04),
//                cornerRadius = 28.dp,
//                minScale = 0.6f,
//                parallaxFraction = 0.4f
//            ) { index ->
//                GradientIcon(
//                    imageVector = Icons.Rounded.CalendarMonth,
//                    contentDescription = "Calendar",
//                    modifier = Modifier
//                        .size(64.dp)
//                        .align(Alignment.BottomEnd)
//                        .offset(x = 8.dp, y = 4.dp)
//                        .graphicsLayer {
//                            scaleX = 1.2f
//                            scaleY = 1.2f
//                        },
//                    gradient = Brush.horizontalGradient(
//                        colors = listOf(
//                            Color(0xFFC7895F),
//                            Color(0xFF765138)
//                        )
//                    )
//                )
//                Text(
//                    text = "GPA",
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis,
//                    modifier = Modifier.padding(8.dp),
//                    fontFamily = FontFamily.Monospace,
//                    style = MaterialTheme.typography.titleMediumEmphasized,
//                    fontWeight = FontWeight.SemiBold,
//                    color = Color(0xFFC7895F)
//                )
//            }
        }
    }
}