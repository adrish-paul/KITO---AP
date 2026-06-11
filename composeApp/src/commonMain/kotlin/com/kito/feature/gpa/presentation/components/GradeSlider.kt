package com.kito.feature.gpa.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kito.core.designsystem.UIColors
import kotlin.math.roundToInt

val grades = listOf("F","D","C","B","A","E","O")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeSlider(
    selectedIndex: Int,
    onGradeChange: (Int) -> Unit
) {

    val uiColors = UIColors()
    val interaction = remember { MutableInteractionSource() }
    val isDragging by interaction.collectIsDraggedAsState()
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val thumbSize = 32.dp

    val offsetHeight by animateFloatAsState(
        targetValue = with(density) {
            if (isDragging) 36.dp.toPx() else 0f
        },
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioLowBouncy
        ),
        label = "thumbOffset"
    )

    val animatedValue by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = tween(300),
        label = "valueAnim"
    )

    Slider(
        value = animatedValue,
        onValueChange = { onGradeChange(it.roundToInt()) },
        valueRange = 0f..6f,
        steps = 5,
        interactionSource = interaction,
        thumb = {},
        track = { sliderState ->

            val fraction =
                (animatedValue - sliderState.valueRange.start) /
                        (sliderState.valueRange.endInclusive -
                                sliderState.valueRange.start)

            var widthPx by remember { mutableStateOf(0) }

            Box(
                modifier = Modifier
                    .height(thumbSize)
                    .padding(horizontal = thumbSize/3)
                    .fillMaxWidth()
                    .onSizeChanged { widthPx = it.width }
            ) {

                val strokeColor = uiColors.progressAccent
                val isLtr = layoutDirection == LayoutDirection.Ltr

                Box(
                    Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .drawWithCache {
                            onDrawBehind {
                                scale(
                                    scaleX = if (isLtr) 1f else -1f,
                                    scaleY = 1f
                                ) {
                                    drawSliderPath(
                                        fraction = fraction,
                                        offsetHeight = offsetHeight,
                                        color = strokeColor,
                                        steps = sliderState.steps
                                    )
                                }
                            }
                        }
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset {
                            IntOffset(
                                x = (widthPx * fraction - thumbSize.toPx() / 2)
                                    .roundToInt(),
                                y = -offsetHeight.roundToInt()
                            )
                        }
                        .size(thumbSize)
                        .shadow(12.dp, CircleShape)
                        .background(
                            uiColors.accentOrangeStart,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = grades[selectedIndex],
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                }
            }
        }
    )
}

private fun DrawScope.drawSliderPath(
    fraction: Float,
    offsetHeight: Float,
    color: Color,
    steps: Int,
) {

    val path = Path()

    val activeWidth = size.width * fraction
    val midPointHeight = size.height / 2
    val curveHeight = midPointHeight - offsetHeight
    val beyondBounds = size.width * 2
    val ramp = 72.dp.toPx()

    path.moveTo(beyondBounds, midPointHeight)

    path.lineTo(activeWidth + ramp, midPointHeight)

    path.cubicTo(
        activeWidth + (ramp / 2), midPointHeight,
        activeWidth + (ramp / 2), curveHeight,
        activeWidth, curveHeight
    )

    path.cubicTo(
        activeWidth - (ramp / 2), curveHeight,
        activeWidth - (ramp / 2), midPointHeight,
        activeWidth - ramp, midPointHeight
    )

    path.lineTo(-beyondBounds, midPointHeight)

    val variation = .1f

    path.lineTo(-beyondBounds, midPointHeight + variation)

    path.lineTo(activeWidth - ramp, midPointHeight + variation)

    path.cubicTo(
        activeWidth - (ramp / 2), midPointHeight + variation,
        activeWidth - (ramp / 2), curveHeight + variation,
        activeWidth, curveHeight + variation
    )

    path.cubicTo(
        activeWidth + (ramp / 2), curveHeight + variation,
        activeWidth + (ramp / 2), midPointHeight + variation,
        activeWidth + ramp, midPointHeight + variation
    )

    path.lineTo(beyondBounds, midPointHeight + variation)

    val exclude = Path().apply {
        addRect(Rect(-beyondBounds, -beyondBounds, 0f, beyondBounds))
        addRect(Rect(size.width, -beyondBounds, beyondBounds, beyondBounds))
    }

    val trimmedPath = Path()
    trimmedPath.op(path, exclude, PathOperation.Difference)

    val pathMeasure = PathMeasure()
    pathMeasure.setPath(trimmedPath, false)

    val graduations = steps + 1

    for (i in 0..graduations) {

        val pos = pathMeasure.getPosition(
            (i / graduations.toFloat()) * pathMeasure.length / 2
        )

        val height = 10f

        when (i) {
            0, graduations -> drawCircle(
                color = color,
                radius = 8f,
                center = pos
            )

            else -> drawLine(
                strokeWidth = if (pos.x < activeWidth) 4f else 2f,
                color = color,
                start = pos + Offset(0f, height),
                end = pos + Offset(0f, -height)
            )
        }
    }

    clipRect(
        left = -beyondBounds,
        top = -beyondBounds,
        bottom = beyondBounds,
        right = activeWidth
    ) {
        drawPath(
            path = trimmedPath,
            color = color,
            style = Stroke(
                width = 10f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }

    clipRect(
        left = activeWidth,
        top = -beyondBounds,
        bottom = beyondBounds,
        right = beyondBounds
    ) {
        drawPath(
            path = trimmedPath,
            color = color.copy(alpha = .2f),
            style = Stroke(
                width = 10f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}