package com.kito.feature.home.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.kito.core.designsystem.UIColors
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kito.composeapp.generated.resources.Res

@Composable
fun KhaooGullyBanner(
    modifier: Modifier = Modifier,
    onClick: (url: String) -> Unit
) {
    val uiColors = UIColors()
    var json by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        json = try {
            Res.readBytes("files/khaoo_gully_banner.json").decodeToString()
        } catch (e: Exception) {
            // Minimal valid Lottie JSON as fallback
            "{\"v\":\"5.5.7\",\"fr\":30,\"ip\":0,\"op\":60,\"w\":1800,\"h\":300,\"layers\":[]}"
        }
    }

    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(json)
    }
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = Int.MAX_VALUE
    )
    val painter = rememberLottiePainter(
        composition = composition,
        progress = { progress }
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(6f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = uiColors.cardBackground),
        onClick = { onClick(getKhaooGullyLink()) }
    ) {
        if (composition != null) {
            Image(
                painter = painter,
                contentDescription = "KhaooGully",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
