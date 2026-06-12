package com.kito.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.UIKit.UIColor
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
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

            @ObjCSignatureOverride
            override fun webView(
                webView: WKWebView,
                decidePolicyForNavigationAction: WKNavigationAction,
                decisionHandler: (WKNavigationActionPolicy) -> Unit
            ) {
                val requestUrl = decidePolicyForNavigationAction.request.URL
                val clickedHost = requestUrl?.host
                val baseHost = NSURL(string = url)?.host
                if (decidePolicyForNavigationAction.navigationType == 0L) {
                    if (clickedHost != null && baseHost != null &&
                        !clickedHost.contains(baseHost)
                    ) {
                        requestUrl?.let {
                            platform.UIKit.UIApplication.sharedApplication.openURL(
                                it,
                                options = emptyMap<Any?, Any?>(),
                                completionHandler = null
                            )
                        }

                        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
                        return
                    }
                }
                decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
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
//        delay(650)
        webView.loadRequest(
            NSURLRequest(NSURL(string = url))
        )
    }

    UIKitView(
        modifier = modifier,
        factory = { webView }
    )
}