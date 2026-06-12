package com.kito.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PromotionWebView(
    url: String,
    modifier: Modifier = Modifier,
    onLoadingStateChange: (Boolean) -> Unit
)