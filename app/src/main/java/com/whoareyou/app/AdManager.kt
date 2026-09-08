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
    private val context: Context,
    private val onPrivacyOptionsRequirementChanged: (Boolean) -> Unit = {}
) {
    companion object {
        private const val RESULTS_BETWEEN_ADS = 3
        private const val MIN_MILLIS_BETWEEN_ADS = 7 * 60 * 1000L
    }

    private val consentInformation = UserMessagingPlatform.getConsentInformation(context)
    private var interstitial: InterstitialAd? = null
    private var resultTransitionsSinceAd = 0
    private var lastAdShownAtElapsedRealtime = Long.MIN_VALUE
    private var adsInitialized = false

    fun start() = start(context as? Activity)

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
                    tryInitializeAds()
                }
                // Cached consent can already make ad requests eligible while the form check completes.
                tryInitializeAds()
            },
            {
                // Cached consent may still allow requests if the network update fails.
                publishPrivacyOptionsRequirement()
                tryInitializeAds()
            }
        )
    }

    fun showPrivacyOptions(activity: Activity?) {
        if (activity == null || !isPrivacyOptionsRequired()) return
        UserMessagingPlatform.showPrivacyOptionsForm(activity) {
            publishPrivacyOptionsRequirement()
            tryInitializeAds()
        }
    }

    fun isPrivacyOptionsRequired(): Boolean =
        consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

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

        interstitial = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                // Only consume the frequency cap once Google confirms the ad is actually visible.
                resultTransitionsSinceAd = 0
                lastAdShownAtElapsedRealtime = SystemClock.elapsedRealtime()
            }

            override fun onAdDismissedFullScreenContent() {
                load()
                onContinue()
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                AppEvents.recordError(
                    IllegalStateException("Ad show failed [${adError.code}]: ${adError.message}"),
                    mapOf("placement" to "result_interstitial", "stage" to "show", "domain" to adError.domain)
                )
                load()
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
