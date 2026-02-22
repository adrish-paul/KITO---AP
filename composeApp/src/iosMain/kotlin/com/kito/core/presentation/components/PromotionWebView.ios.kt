package com.kito.core.presentation.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.readValue
import kotlinx.coroutines.delay
import platform.CoreGraphics.CGRectZero
import platform.Foundation.*
import platform.UIKit.UIColor
import platform.WebKit.*
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PromotionWebView(
    url: String,
    modifier: Modifier,
    onLoadingStateChange: (Boolean) -> Unit
) {

    val delegate = remember {
        object : NSObject(), WKNavigationDelegateProtocol {

            @ObjCSignatureOverride
            override fun webView(
                webView: WKWebView,
                didStartProvisionalNavigation: WKNavigation?
            ) {
                onLoadingStateChange(true)
            }

            @ObjCSignatureOverride
            override fun webView(
                webView: WKWebView,
                didFinishNavigation: WKNavigation?
            ) {
                onLoadingStateChange(false)
            }

            @ObjCSignatureOverride
            override fun webView(
                webView: WKWebView,
                didFailNavigation: WKNavigation?,
                withError: NSError
            ) {
                onLoadingStateChange(false)
            }

            @ObjCSignatureOverride
            override fun webView(
                webView: WKWebView,
                didFailProvisionalNavigation: WKNavigation?,
                withError: NSError
            ) {
                onLoadingStateChange(false)
            }
        }
    }

    val webView = remember {

        val config = WKWebViewConfiguration()

        WKWebView(frame = CGRectZero.readValue(), configuration = config).apply {

            navigationDelegate = delegate

            setOpaque(false)
            setBackgroundColor(UIColor.clearColor)
        }
    }

    LaunchedEffect(url) {
        delay(650)
        webView.loadRequest(
            NSURLRequest(NSURL(string = url))
        )
    }

    UIKitView(
        modifier = modifier,
        factory = { webView }
    )
}