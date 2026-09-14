package com.whoareyou.app

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class AdManager(
    context: Context,
    private val onPrivacyOptionsRequirementChanged: (Boolean) -> Unit = {}
) {
    companion object {
        private const val RESULTS_BETWEEN_ADS = 3
        private const val MIN_MILLIS_BETWEEN_ADS = 7 * 60 * 1000L
    }

    private val appContext = context.applicationContext
    private val consentInformation = UserMessagingPlatform.getConsentInformation(appContext)
    private var interstitial: InterstitialAd? = null
    private var resultTransitionsSinceAd = 0
    private var lastAdShownAtElapsedRealtime = Long.MIN_VALUE
    private var adsInitialized = false

    fun start() = Unit

    fun close() {
        interstitial = null
        resultTransitionsSinceAd = 0
        onPrivacyOptionsRequirementChanged(false)
    }

    fun start(activity: Activity?) {
        if (activity == null) return

        val params = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                publishPrivacyOptionsRequirement()
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    publishPrivacyOptionsRequirement()
                }
            },
            {
                publishPrivacyOptionsRequirement()
            }
        )
    }

    fun showPrivacyOptions(activity: Activity?) {
        if (activity == null || !isPrivacyOptionsRequired()) return
        UserMessagingPlatform.showPrivacyOptionsForm(activity) {
            publishPrivacyOptionsRequirement()
        }
    }

    fun isPrivacyOptionsRequired(): Boolean =
        consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun onResultFinished(activity: Activity?, adsRemoved: Boolean, onContinue: () -> Unit) {
        if (adsRemoved || activity == null || !consentInformation.canRequestAds()) {
            onContinue()
            return
        }

        resultTransitionsSinceAd++

        if (
            resultTransitionsSinceAd == RESULTS_BETWEEN_ADS - 1 &&
            timeCapSatisfied()
        ) {
            initializeAndLoadIfNeeded()
            onContinue()
            return
        }

        if (resultTransitionsSinceAd < RESULTS_BETWEEN_ADS || !timeCapSatisfied()) {
            onContinue()
            return
        }

        if (!adsInitialized) {
            initializeAndLoadIfNeeded()
            onContinue()
            return
        }

        val ad = interstitial
        if (ad == null) {
            load()
            onContinue()
            return
        }

        interstitial = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                // Only consume the frequency cap once Google confirms the ad is actually visible.
                resultTransitionsSinceAd = 0
                lastAdShownAtElapsedRealtime = SystemClock.elapsedRealtime()
            }

            override fun onAdDismissedFullScreenContent() {
                onContinue()
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                AppEvents.recordError(
                    IllegalStateException("Ad show failed [${adError.code}]: ${adError.message}"),
                    mapOf("placement" to "result_interstitial", "stage" to "show", "domain" to adError.domain)
                )
                onContinue()
            }

            override fun onAdImpression() {
                AppEvents.adImpression("result_interstitial")
            }
        }
        ad.show(activity)
    }

    private fun publishPrivacyOptionsRequirement() {
        onPrivacyOptionsRequirementChanged(isPrivacyOptionsRequired())
    }

    private fun initializeAndLoadIfNeeded() {
        if (!consentInformation.canRequestAds()) return
        if (adsInitialized) {
            load()
            return
        }
        adsInitialized = true
        MobileAds.initialize(appContext) { load() }
    }

    private fun timeCapSatisfied(): Boolean {
        if (lastAdShownAtElapsedRealtime == Long.MIN_VALUE) return true
        return SystemClock.elapsedRealtime() - lastAdShownAtElapsedRealtime >= MIN_MILLIS_BETWEEN_ADS
    }

    private fun load() {
        if (!adsInitialized || !consentInformation.canRequestAds() || interstitial != null) return
        InterstitialAd.load(
            appContext,
            BuildConfig.ADMOB_INTERSTITIAL_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                }

                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    AppEvents.recordError(
                        IllegalStateException("Ad load failed [${error.code}]: ${error.message}"),
                        mapOf("placement" to "result_interstitial", "stage" to "load", "domain" to error.domain)
                    )
                }
            }
        )
    }
}
