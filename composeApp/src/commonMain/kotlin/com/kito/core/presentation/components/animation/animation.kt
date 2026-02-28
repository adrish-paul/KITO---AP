package com.kito.core.presentation.components.animation

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.alexzhirkevich.compottie.LottieClipSpec
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kito.composeapp.generated.resources.Res

@Composable
fun PageNotFoundAnimation() {
    val json = rememberLottieJson("files/page_not_found.json")
    val composition by rememberLottieComposition (
        LottieCompositionSpec.JsonString(json)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = Int.MAX_VALUE
    )

    val painter = rememberLottiePainter(
        composition = composition,
        progress = { progress }
    )

    Image(
        painter = painter,
        contentDescription = "Page Not Found"
    )
}

@Composable
fun RelaxAnimation() {
    val json = rememberLottieJson("files/relax.json")
    val composition by rememberLottieComposition (
        LottieCompositionSpec.JsonString(json)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = Int.MAX_VALUE
    )

    val painter = rememberLottiePainter(
        composition = composition,
        progress = { progress }
    )

    Image(
        painter = painter,
        contentDescription = "Relax"
    )
}

@Composable
fun PandaSleepingAnimation() {
    val json = rememberLottieJson("files/panda_sleeping.json")
    val composition by rememberLottieComposition(
        LottieCompositionSpec.JsonString(json)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = Int.MAX_VALUE
    )
    val painter = rememberLottiePainter(
        composition = composition,
        progress = { progress }
    )

    Image(
        painter = painter,
        contentDescription = "Panda Sleeping"
    )
}

@Composable
fun LockAnimation() {
    val json = rememberLottieJson("files/lock.json")
    val composition by rememberLottieComposition(
        LottieCompositionSpec.JsonString(json)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        clipSpec = LottieClipSpec.Progress(
            min = 0.30f,
            max = 1f
        )
    )

    val painter = rememberLottiePainter(
        composition = composition,
        progress = { progress }
    )

    Image(
        painter = painter,
        contentDescription = "Lock Animation"
    )
}

@Composable
fun NoInternetAnimation() {
    val json = rememberLottieJson("files/no_internet_connection.json")
    val composition by rememberLottieComposition(
        LottieCompositionSpec.JsonString(json)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
    )

    val painter = rememberLottiePainter(
        composition = composition,
        progress = { progress }
    )

    Image(
        painter = painter,
        contentDescription = "No Internet Connection"
    )
}

/**
 * Loads a Lottie JSON file from Compose Resources asynchronously.
 * Files must be placed in commonMain/composeResources/files/
 */
@Composable
private fun rememberLottieJson(path: String): String {
    var json by remember { mutableStateOf("") }
    LaunchedEffect(path) {
        json = try {
            Res.readBytes(path).decodeToString()
        } catch (e: Exception) {
            // Minimal valid Lottie JSON as fallback
            "{\"v\":\"5.5.7\",\"fr\":30,\"ip\":0,\"op\":60,\"w\":100,\"h\":100,\"layers\":[]}"
        }
    }
    return json
}
