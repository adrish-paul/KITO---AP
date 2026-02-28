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

@Composable
fun ParallaxCarouselRow(
    itemCount: Int,
    modifier: Modifier = Modifier,
    itemWidth: Dp = 115.dp,
    itemSpacing: Dp = 8.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    parallaxFraction: Float = 0.4f, // how much the content lags (0f = no parallax, 1f = stays fully stationary)
    itemBoxColor: (index: Int) -> Color = { Color(0xFF5F2B04) },
    content: @Composable BoxScope.(index: Int) -> Unit,
) {
    val listState = rememberLazyListState()

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        contentPadding = contentPadding,
        modifier = modifier.clipToBounds()
    ) {
        items(itemCount) { index ->

            // How many px the content should be offset to create parallax
            val contentOffsetX by remember(listState) {
                derivedStateOf {
                    val layoutInfo = listState.layoutInfo
                    val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                        ?: return@derivedStateOf 0f

                    val viewportStart = layoutInfo.viewportStartOffset
                    val viewportEnd = layoutInfo.viewportEndOffset
                    val itemStart = itemInfo.offset
                    val itemEnd = itemInfo.offset + itemInfo.size

                    when {
                        // Card is partially off the left edge
                        itemStart < viewportStart -> {
                            val hiddenAmount = (viewportStart - itemStart).toFloat()
                            // Push content right so it "stays behind"
                            hiddenAmount * parallaxFraction
                        }
                        // Card is partially off the right edge
                        itemEnd > viewportEnd -> {
                            val hiddenAmount = (viewportEnd - itemEnd).toFloat() // negative
                            // Push content left so it "stays behind"
                            hiddenAmount * parallaxFraction
                        }
                        else -> 0f
                    }
                }
            }

            // Card box — full size, no scaling, clipped naturally by LazyRow viewport
            Box(
                modifier = Modifier
                    .width(itemWidth)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(itemBoxColor(index))
            ) {
                // Content box — same full size, but shifted with parallax
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = contentOffsetX
                        }
                ) {
                    content(index)
                }
            }
        }
    }
}