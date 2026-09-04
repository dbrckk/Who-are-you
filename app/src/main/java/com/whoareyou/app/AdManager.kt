package com.whoareyou.app

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdManager(private val context: Context) {
    companion object {
        // Google test interstitial. Replace with the production unit before release.
        private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        private const val RESULTS_BETWEEN_ADS = 3
    }

    private var interstitial: InterstitialAd? = null
    private var resultTransitionsSinceAd = 0
    private var initialized = false

    fun start() {
        if (initialized) return
        initialized = true
        MobileAds.initialize(context) { load() }
    }

    fun onResultFinished(activity: Activity?, adsRemoved: Boolean, onContinue: () -> Unit) {
        if (adsRemoved || activity == null) {
            onContinue()
            return
        }

        resultTransitionsSinceAd++
        if (resultTransitionsSinceAd < RESULTS_BETWEEN_ADS) {
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

    private fun load() {
        if (interstitial != null) return
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
