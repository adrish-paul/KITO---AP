package com.kito.core.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter

@Composable
fun GradientIcon(
    image: Painter?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    gradient: Brush
) {
//    val painter = rememberVectorPainter(imageVector)

    Icon(
        painter = image?: rememberVectorPainter(Icons.Default.CrisisAlert),
        contentDescription = contentDescription,
        tint = Color.White,
        modifier = modifier
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawWithCache {
                onDrawWithContent {
                    drawContent() // draw the white icon first
                    drawRect(
                        brush = gradient,
                        blendMode = BlendMode.SrcIn
                    )
                }
            }
    )
}