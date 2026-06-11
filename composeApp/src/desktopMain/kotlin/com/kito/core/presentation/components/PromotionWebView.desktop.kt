package com.kito.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun PromotionWebView(url: String, modifier: Modifier, onLoadingStateChange: (Boolean) -> Unit) {
    Box(modifier, contentAlignment = Alignment.Center) { Text("WebView not available on Desktop") }
}
