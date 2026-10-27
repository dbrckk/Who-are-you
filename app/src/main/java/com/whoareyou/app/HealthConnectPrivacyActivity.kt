package com.whoareyou.app

import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity

/**
 * Android 13 (Health Connect app) and Android 14+ (platform Health Connect)
 * permission rationale. Shows the same public policy that Play Console links to.
 */
class HealthConnectPrivacyActivity : ComponentActivity() {
    private var privacyWebView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this)
        privacyWebView = webView
        webView.settings.javaScriptEnabled = false
        webView.settings.allowFileAccess = false
        webView.settings.allowContentAccess = false
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                if (!request.isForMainFrame) return
                // A network outage must not turn the privacy link into a blank screen.
                view.loadDataWithBaseURL(
                    null,
                    """<!doctype html><html><head><meta name="viewport" content="width=device-width, initial-scale=1"></head>
                       <body style="font-family:sans-serif;padding:16px">
                       <h1>Who Are You? — Health Connect privacy</h1>
                       <p>Optional READ_STEPS access lets you view your daily steps, recent 7/30-day
                       activity trends and personal step-count goals. Step totals stay on this device
                       for up to 31 days and are not uploaded or used for advertising.</p>
                       <p>Disable Activity in My Habits to erase local step history, and revoke
                       the permission separately in Android Health Connect settings.</p>
                       <p>For the full privacy policy, connect to the internet and visit:
                       <a href="https://dbrckk.github.io/Who-are-you/privacy/">Who Are You? Privacy Policy</a>.</p>
                       </body></html>""",
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        }
        setContentView(webView)
        webView.loadUrl(PRIVACY_POLICY_URL)
    }

    override fun onDestroy() {
        privacyWebView?.destroy()
        privacyWebView = null
        super.onDestroy()
    }

    companion object {
        const val PRIVACY_POLICY_URL = "https://dbrckk.github.io/Who-are-you/privacy/"
    }
}
