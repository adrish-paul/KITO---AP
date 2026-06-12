package com.kito.feature.home.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kito.core.common.util.currentLocalDateTime
import com.kito.core.designsystem.UIColors
import kito.composeapp.generated.resources.Res
import kito.composeapp.generated.resources.e_labs_logo
import kito.composeapp.generated.resources.header
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import org.jetbrains.compose.resources.painterResource

@Composable
fun JoinELabsBanner(
    colors: UIColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Check if we should show the banner (before or on Feb 22, 2026)
    val currentDate = currentLocalDateTime().date
    val recruitmentStartDate = LocalDate(2026, 2, 21)
    val recruitmentEndDate = LocalDate(2026, 2, 22)
    val shouldShowBanner = currentDate <= recruitmentEndDate

    if (!shouldShowBanner) {
        return // Don't render anything after Feb 22
    }

    // Calculate countdown
    val isRecruitmentLive = currentDate >= recruitmentStartDate && currentDate <= recruitmentEndDate
    val daysUntilRecruitment = if (!isRecruitmentLive) {
        val currentInstant = currentDate.atStartOfDayIn(TimeZone.currentSystemDefault())
        val recruitmentInstant = recruitmentStartDate.atStartOfDayIn(TimeZone.currentSystemDefault())
        val diff = recruitmentInstant.minus(currentInstant).inWholeDays
        diff.toInt()
    } else {
        0
    }

    // Carousel state: 0 = Logo, 1 = Header, 2 = Countdown
    var currentSlide by remember { mutableStateOf(0) }
    val totalSlides = 3

    // Switch slides every 2.5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(2500)
            currentSlide = (currentSlide + 1) % totalSlides
        }
    }

    // Infinite animations
    val infiniteTransition = rememberInfiniteTransition()

    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val colorShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        colors.cardBackground,
                        colors.cardBackgroundHigh,
                        colors.cardBackground
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        colors.progressAccent.copy(alpha = 0.4f + colorShift * 0.4f),
                        colors.accentOrangeStart.copy(alpha = 0.6f + colorShift * 0.4f),
                        colors.accentOrangeEnd.copy(alpha = 0.5f + colorShift * 0.3f),
                        colors.accentOrangeStart.copy(alpha = 0.6f + colorShift * 0.4f),
                        colors.progressAccent.copy(alpha = 0.4f + colorShift * 0.4f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .drawWithContent {
                drawContent()

                // Animated shimmer overlay
                val shimmerBrush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        colors.accentOrangeStart.copy(alpha = 0.3f),
                        colors.accentOrangeEnd.copy(alpha = 0.5f),
                        colors.accentOrangeStart.copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    start = Offset(shimmerOffset - 200f, 0f),
                    end = Offset(shimmerOffset + 200f, size.height),
                    tileMode = TileMode.Clamp
                )

                drawRect(
                    brush = shimmerBrush,
                    size = size
                )
            }
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        // Animated content with fade transitions
        AnimatedContent(
            targetState = currentSlide,
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(600, easing = FastOutSlowInEasing)
                ).togetherWith(
                    fadeOut(
                        animationSpec = tween(600, easing = FastOutSlowInEasing)
                    )
                )
            },
            modifier = Modifier.align(Alignment.Center)
        ) { slide ->
            when (slide) {
                0 -> {
                    // Logo slide
                    Image(
                        painter = painterResource(Res.drawable.header),
                        contentDescription = "E-Labs Header",
                        modifier = Modifier
                            .fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                1 -> {
                    // Header slide with logo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.e_labs_logo),
                            contentDescription = "E-Labs Logo",
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "JOIN E-LABS",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.Serif
                            ),
                            color = colors.accentOrangeStart
                        )
                    }
                }
                2 -> {
                    // Countdown slide
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isRecruitmentLive) {
                            Text(
                                text = "🔴 RECRUITMENT IS LIVE",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 23.sp,
                                    letterSpacing = 1.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = colors.accentOrangeEnd
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Join us now!",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = colors.textSecondary
                            )
                        } else {
                            Text(
                                text = when {
                                    daysUntilRecruitment == 1 -> "1 DAY TO GO"
                                    daysUntilRecruitment > 1 -> "$daysUntilRecruitment DAYS TO GO"
                                    else -> "STARTING SOON"
                                },
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 25.sp,
                                    letterSpacing = 1.5.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = colors.accentOrangeStart
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Feb 21-22, 2026",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
