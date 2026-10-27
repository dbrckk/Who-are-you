package com.whoareyou.app

import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity

/**
 * Android 13 (Health Connect app) and Android 14+ (platform Health Connect)
 * permission rationale. Shows the same public policy that Play Console links to.
 */
class HealthConnectPrivacyActivity : ComponentActivity() {
    private var privacyWebView: WebView? = null
    private var fallbackDisplayed = false

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
                if (request.isForMainFrame) showFallback(view)
            }

            override fun onReceivedHttpError(
                view: WebView,
                request: WebResourceRequest,
                errorResponse: WebResourceResponse
            ) {
                // A 404/5xx page is not an acceptable Health Connect privacy disclosure.
                if (request.isForMainFrame && errorResponse.statusCode >= 400) showFallback(view)
            }
        }
        setContentView(webView)
        webView.loadUrl(PRIVACY_POLICY_URL)
    }

    private fun showFallback(view: WebView) {
        if (fallbackDisplayed) return
        fallbackDisplayed = true
        // A failed network request must not leave Google's permission rationale blank.
        // The offline summary follows the public policy, including dormant-device retention.
        val html = if (resources.configuration.locales[0].language == "fr") {
            """<!doctype html><html lang="fr"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"></head>
                <body style="font-family:sans-serif;padding:16px;line-height:1.5">
                <h1>Who Are You? — Confidentialité Santé Connect</h1>
                <p>Dans Mes habitudes, le suivi facultatif lit uniquement les pas (READ_STEPS) après votre autorisation pour afficher les pas quotidiens, les tendances sur 7/30 jours et les objectifs personnels. Les questionnaires restent accessibles sans autorisation.</p>
                <p>Les totaux sont enregistrés uniquement sur cet appareil, sans partage ni utilisation publicitaire. L'historique couvre le jour actuel et les 30 jours précédents lorsque l'application est utilisée. Les anciennes observations sont supprimées à la prochaine ouverture : elles peuvent rester plus longtemps si l'application n'est pas ouverte.</p>
                <p>Désactivez Activité dans Mes habitudes pour effacer les observations de pas enregistrées par l'application. Révoquez séparément l'autorisation dans les réglages Android Santé Connect ; la révocation seule ne supprime pas l'historique déjà enregistré.</p>
                <p>Politique complète (connexion requise) : <a href="https://dbrckk.github.io/Who-are-you/privacy/">Who Are You? — Politique de confidentialité</a>.</p>
                </body></html>"""
        } else {
            """<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"></head>
                <body style="font-family:sans-serif;padding:16px;line-height:1.5">
                <h1>Who Are You? — Health Connect privacy</h1>
                <p>Optional READ_STEPS access in My Habits reads step counts only, with your permission, to show daily steps, 7/30-day trends and personal step goals. Quizzes work without this permission.</p>
                <p>Step totals stay on this device and are never shared or used for ads. Local history covers today and the preceding 30 days while the app runs. Older observations are cleared when the app is next opened, so they can remain longer if you stop using the app.</p>
                <p>Disable Activity in My Habits to erase saved step observations. Revoke permission separately in Android Health Connect settings; revoking permission alone does not erase existing local history.</p>
                <p>Full policy (internet required): <a href="https://dbrckk.github.io/Who-are-you/privacy/">Who Are You? — Privacy Policy</a>.</p>
                </body></html>"""
        }
        view.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
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
