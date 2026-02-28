package com.kito.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp

data class CarouselItemTransform(
    val scale: Float,
    val contentOffsetX: Float
)

@Composable
fun ShrinkingParallaxCarouselRow(
    itemCount: Int,
    modifier: Modifier = Modifier,
    itemWidth: Dp = 115.dp,
    itemSpacing: Dp = 8.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    cardColor: Color = Color(0xFF5F2B04),
    cornerRadius: Dp = 28.dp,
    minScale: Float = 0.6f,
    parallaxFraction: Float = 0.4f,
    content: @Composable BoxScope.(index: Int) -> Unit
) {
    val listState = rememberLazyListState()

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        contentPadding = contentPadding,
        modifier = modifier.clipToBounds()
    ) {
        items(itemCount) { index ->

            val transform by remember(listState) {
                derivedStateOf {
                    val layoutInfo = listState.layoutInfo
                    val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }

                    if (itemInfo == null) {
                        CarouselItemTransform(scale = minScale, contentOffsetX = 0f)
                    } else {
                        val viewportStart = layoutInfo.viewportStartOffset
                        val viewportEnd = layoutInfo.viewportEndOffset
                        val itemStart = itemInfo.offset
                        val itemEnd = itemInfo.offset + itemInfo.size

                        val visibleWidth: Float = when {
                            itemStart < viewportStart -> (itemEnd - viewportStart).toFloat()
                            itemEnd > viewportEnd -> (viewportEnd - itemStart).toFloat()
                            else -> itemInfo.size.toFloat()
                        }.coerceAtLeast(0f)

                        val visibleFraction = (visibleWidth / itemInfo.size.toFloat()).coerceIn(0f, 1f)

                        // Scale shrinks as card moves off screen
                        val scale = lerp(minScale, 1f, ((visibleFraction - 0.2f) / 0.8f).coerceIn(0f, 1f))

                        // Parallax offset — content lags behind the card movement
                        val contentOffsetX = when {
                            itemStart < viewportStart -> (viewportStart - itemStart).toFloat() * parallaxFraction
                            itemEnd > viewportEnd -> (viewportEnd - itemEnd).toFloat() * parallaxFraction
                            else -> 0f
                        }

                        CarouselItemTransform(scale = scale, contentOffsetX = contentOffsetX)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .width(itemWidth)
                    .aspectRatio(1f)
                    .graphicsLayer {
                        scaleX = transform.scale
                        scaleY = transform.scale
                    }
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(cardColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = transform.contentOffsetX
                        }
                ) {
                    content(index)
                }
            }
        }
    }
}