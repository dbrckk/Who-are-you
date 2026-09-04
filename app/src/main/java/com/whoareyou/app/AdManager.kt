package com.whoareyou.app

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class AdManager(private val context: Context) {
    companion object {
        // Google test interstitial. Replace with the production unit before release.
        private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        private const val RESULTS_BETWEEN_ADS = 3
        private const val MIN_MILLIS_BETWEEN_ADS = 7 * 60 * 1000L
    }

    private val consentInformation = UserMessagingPlatform.getConsentInformation(context)
    private var interstitial: InterstitialAd? = null
    private var resultTransitionsSinceAd = 0
    private var lastAdShownAtElapsedRealtime = Long.MIN_VALUE
    private var consentRequested = false
    private var adsInitialized = false

    fun start() = start(context as? Activity)

    fun start(activity: Activity?) {
        if (activity == null || consentRequested) return
        consentRequested = true

        val params = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    tryInitializeAds()
                }
                tryInitializeAds()
            },
            {
                // Cached consent may still allow requests if the network update fails.
                tryInitializeAds()
            }
        )
    }

    fun onResultFinished(activity: Activity?, adsRemoved: Boolean, onContinue: () -> Unit) {
        if (adsRemoved || activity == null || !adsInitialized || !consentInformation.canRequestAds()) {
            onContinue()
            return
        }

        resultTransitionsSinceAd++
        if (resultTransitionsSinceAd < RESULTS_BETWEEN_ADS || !timeCapSatisfied()) {
            onContinue()
            return
        }

        val ad = interstitial
        if (ad == null) {
            load()
            onContinue()
            return
        }

        resultTransitionsSinceAd = 0
        lastAdShownAtElapsedRealtime = SystemClock.elapsedRealtime()
        interstitial = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                load()
                onContinue()
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                load()
                onContinue()
            }

            override fun onAdImpression() {
                AppEvents.adImpression("result_interstitial")
            }
        }
        ad.show(activity)
    }

    private fun tryInitializeAds() {
        if (adsInitialized || !consentInformation.canRequestAds()) return
        adsInitialized = true
        MobileAds.initialize(context) { load() }
    }

    private fun timeCapSatisfied(): Boolean {
        if (lastAdShownAtElapsedRealtime == Long.MIN_VALUE) return true
        return SystemClock.elapsedRealtime() - lastAdShownAtElapsedRealtime >= MIN_MILLIS_BETWEEN_ADS
    }

    private fun load() {
        if (!adsInitialized || !consentInformation.canRequestAds() || interstitial != null) return
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                }
            }
        )
    }
}
