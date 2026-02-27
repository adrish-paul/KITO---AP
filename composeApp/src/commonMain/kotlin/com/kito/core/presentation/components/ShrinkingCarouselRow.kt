package com.kito.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp

@Composable
fun ShrinkingCarouselRow(
    itemCount: Int,
    modifier: Modifier = Modifier,
    itemWidth: Dp = 100.dp,
    itemSpacing: Dp = 8.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    minScale: Float = 0.6f,
    content: @Composable BoxScope.(index: Int) -> Unit
) {
    val listState = rememberLazyListState()

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        contentPadding = contentPadding,
        modifier = modifier
    ) {
        items(itemCount) { index ->
            val shrinkFraction by remember(listState) {
                derivedStateOf {
                    val layoutInfo = listState.layoutInfo
                    val itemInfo = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }

                    if (itemInfo == null) {
                        minScale
                    } else {
                        val viewportStart = layoutInfo.viewportStartOffset
                        val viewportEnd = layoutInfo.viewportEndOffset
                        val itemStart = itemInfo.offset
                        val itemEnd = itemInfo.offset + itemInfo.size

                        val visibleWidth: Float = when {
                            itemStart < viewportStart -> (itemEnd - viewportStart).toFloat()
                            itemEnd > viewportEnd -> (viewportEnd - itemStart).toFloat()
                            else -> itemInfo.size.toFloat()
                        }

                        val visibleFraction = (visibleWidth / itemInfo.size.toFloat()).coerceIn(0f, 1f)
                        lerp(minScale, 1f, ((visibleFraction - 0.2f) / 0.8f).coerceIn(0f, 1f))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .width(itemWidth)
                    .graphicsLayer {
                        scaleX = shrinkFraction
                        scaleY = shrinkFraction
                    }
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(Color(0xFF5F2B04))
            ) {
                Box(
                    modifier = Modifier
                        .width(itemWidth)
                        .aspectRatio(1f)
                ) {
                    content(index)
                }
            }
        }
    }
}