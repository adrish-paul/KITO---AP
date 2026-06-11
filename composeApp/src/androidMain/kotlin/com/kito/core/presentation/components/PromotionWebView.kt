package com.kito.core.designsystem

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import kotlinx.coroutines.delay

@Composable
actual fun PromotionWebView(
    url: String,
    modifier: Modifier,
    onLoadingStateChange: (Boolean) -> Unit
) {
    val context = LocalContext.current

    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            setBackgroundColor(Color.TRANSPARENT)

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(
                    view: WebView?,
                    url: String?,
                    favicon: Bitmap?
                ) {
                    onLoadingStateChange(true)
                }

                override fun onPageFinished(
                    view: WebView?,
                    url: String?
                ) {
                    onLoadingStateChange(false)
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {

                    val clickedUri = request?.url ?: return false
                    val clickedHost = clickedUri.host ?: return false

                    val baseHost = url.toUri().host

                    return if (clickedHost != baseHost) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, clickedUri)
                            view?.context?.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        true // open externally
                    } else {
                        false // load inside WebView
                    }
                }
            }
        }
    }

    LaunchedEffect(url) {
        delay(650)
        webView.loadUrl(url)
    }

    AndroidView(
        factory = { webView },
        modifier = modifier
    )
}