package com.brainzomatch.app.core.ads

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * AdMobFamiliesManager manages all ad requests under strict Google Play Families Policy guidelines.
 * All requests enforce COPPA child-directed treatment, non-personalized advertising, and G-rated content filtering.
 * File Path: app/src/main/java/com/brainzomatch/app/core/ads/AdMobFamiliesManager.kt
 */
object AdMobFamiliesManager {

    private const val TAG = "AdMobFamiliesManager"

    // Google Official Sample Test Ad Unit IDs (DO NOT USE PRODUCTION IDS IN TEST BUILDS):
    private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    // Production ad unit IDs configured prior to Play Store release
    val BANNER_AD_UNIT_ID: String = TEST_BANNER_AD_UNIT_ID
    val INTERSTITIAL_AD_UNIT_ID: String = TEST_INTERSTITIAL_AD_UNIT_ID

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading: Boolean = false
    private var lastInterstitialShownTimestamp: Long = 0L

    // Minimum cooldown time between interstitial ads (180 seconds / 3 minutes) to protect child focus
    private const val INTERSTITIAL_COOLDOWN_MILLIS = 180_000L

    /**
     * Initializes the Mobile Ads SDK with global Families Policy & COPPA compliance.
     * Call this in BrainzoApplication.onCreate().
     */
    fun initialize(context: Context) {
        val requestConfiguration = MobileAds.getRequestConfiguration()
            .toBuilder()
            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
            .setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE)
            .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
            .build()

        MobileAds.setRequestConfiguration(requestConfiguration)

        MobileAds.initialize(context) { initializationStatus ->
            val statusMap = initializationStatus.adapterStatusMap
            for ((adapterClass, status) in statusMap) {
                Log.d(TAG, "AdMob Adapter: $adapterClass, State: ${status.initializationState}")
            }
            preloadInterstitialAd(context)
        }
    }

    /**
     * Creates an AdRequest with strictly non-personalized ad extras.
     * Prevents behavioral targeting, tracking cookies, and remarketing for child safety.
     */
    fun buildChildSafeAdRequest(): AdRequest {
        val extras = Bundle().apply {
            putString("npa", "1") // Enforce Non-Personalized Ads flag
            putBoolean("is_designed_for_families", true)
        }

        return AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()
    }

    /**
     * Preloads an Interstitial Ad in the background.
     */
    fun preloadInterstitialAd(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) {
            return
        }

        isInterstitialLoading = true
        val adRequest = buildChildSafeAdRequest()

        InterstitialAd.load(
            context.applicationContext,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Child-Safe Interstitial Ad loaded successfully.")

                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            interstitialAd = null
                            lastInterstitialShownTimestamp = System.currentTimeMillis()
                            preloadInterstitialAd(context)
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            interstitialAd = null
                            isInterstitialLoading = false
                            Log.e(TAG, "Interstitial failed to show: ${adError.message}")
                            preloadInterstitialAd(context)
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Interstitial displayed safely between level transitions.")
                        }
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.w(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    /**
     * Displays an interstitial ad ONLY if:
     * 1. The user is not a VIP Gold subscriber.
     * 2. The level is complete (never during active live gameplay).
     * 3. The 3-minute child cooldown timer has elapsed.
     */
    fun showInterstitialBetweenLevels(
        activity: Activity,
        isVipSubscriber: Boolean,
        onAdClosedOrSkipped: () -> Unit
    ) {
        if (isVipSubscriber) {
            onAdClosedOrSkipped()
            return
        }

        val now = System.currentTimeMillis()
        val timeSinceLastAd = now - lastInterstitialShownTimestamp

        if (timeSinceLastAd < INTERSTITIAL_COOLDOWN_MILLIS) {
            Log.d(TAG, "Interstitial skipped: Cooldown active (${(INTERSTITIAL_COOLDOWN_MILLIS - timeSinceLastAd) / 1000}s remaining)")
            onAdClosedOrSkipped()
            return
        }

        if (interstitialAd != null) {
            interstitialAd?.show(activity)
        } else {
            preloadInterstitialAd(activity)
            onAdClosedOrSkipped()
        }
    }
}
