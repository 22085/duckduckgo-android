/*
 * Copyright (c) 2026 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.app.googlesdk

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import logcat.logcat

/**
 * Google Ads (AdMob) only for the duckduckgo-android fork.
 *
 * All six AdMob formats are supported, on the fork's formal AdMob IDs: banner,
 * interstitial, rewarded, rewarded interstitial, native, and app open.
 * Everything here is fail-safe: AdMob initialization failure logs and skips
 * instead of crashing startup, and every load reports failure through its
 * callback instead of throwing.
 */
object GoogleSdk {

    /** Formal AdMob App ID for the duckduckgo-android fork. */
    const val ADMOB_APP_ID = "ca-app-pub-1386469545698637~4946467586"

    /** Formal banner unit ID. */
    const val BANNER_AD_UNIT_ID = "ca-app-pub-1386469545698637/1935105522"

    /** Formal interstitial unit ID. */
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-1386469545698637/9622023856"

    /** Formal rewarded unit ID. */
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-1386469545698637/6398103873"

    /** Formal rewarded interstitial unit ID. */
    const val REWARDED_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-1386469545698637/2937196007"

    /** Formal native unit ID. */
    const val NATIVE_AD_UNIT_ID = "ca-app-pub-1386469545698637/3771940532"

    /** Formal app open unit ID. */
    const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-1386469545698637/3731402926"

    fun initialize(context: Context) {
        try {
            MobileAds.initialize(context)
            logcat { "GoogleSdk: AdMob initialized (test App ID)" }
        } catch (e: Exception) {
            logcat { "GoogleSdk: AdMob initialization failed, ads disabled: ${e.message}" }
        }
    }

    /** Loads a test banner into [parent]. Returns the AdView, or null if loading failed. */
    fun loadBanner(activity: Activity, parent: ViewGroup): AdView? {
        return try {
            val adView = AdView(activity).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = BANNER_AD_UNIT_ID
            }
            parent.addView(adView)
            adView.loadAd(AdRequest.Builder().build())
            adView
        } catch (e: Exception) {
            logcat { "GoogleSdk: test banner failed: ${e.message}" }
            null
        }
    }

    /** Loads a test interstitial. [onLoaded] receives the ad, or null if loading failed. */
    fun loadInterstitial(activity: Activity, onLoaded: (InterstitialAd?) -> Unit) {
        try {
            InterstitialAd.load(
                activity,
                INTERSTITIAL_AD_UNIT_ID,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        onLoaded(ad)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        logcat { "GoogleSdk: test interstitial failed to load: ${error.message}" }
                        onLoaded(null)
                    }
                },
            )
        } catch (e: Exception) {
            logcat { "GoogleSdk: test interstitial failed: ${e.message}" }
            onLoaded(null)
        }
    }

    /** Loads a test rewarded ad. [onLoaded] receives the ad, or null if loading failed. */
    fun loadRewarded(activity: Activity, onLoaded: (RewardedAd?) -> Unit) {
        try {
            RewardedAd.load(
                activity,
                REWARDED_AD_UNIT_ID,
                AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        onLoaded(ad)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        logcat { "GoogleSdk: test rewarded failed to load: ${error.message}" }
                        onLoaded(null)
                    }
                },
            )
        } catch (e: Exception) {
            logcat { "GoogleSdk: test rewarded failed: ${e.message}" }
            onLoaded(null)
        }
    }

    /** Shows a loaded rewarded ad. [onRewarded] fires when the user earns the reward. */
    fun showRewarded(activity: Activity, ad: RewardedAd, onRewarded: (rewardType: String, rewardAmount: Int) -> Unit) {
        try {
            ad.show(activity, OnUserEarnedRewardListener { reward ->
                onRewarded(reward.type, reward.amount)
            })
        } catch (e: Exception) {
            logcat { "GoogleSdk: test rewarded failed to show: ${e.message}" }
        }
    }

    /** Loads a test rewarded interstitial ad. [onLoaded] receives the ad, or null if loading failed. */
    fun loadRewardedInterstitial(activity: Activity, onLoaded: (RewardedInterstitialAd?) -> Unit) {
        try {
            RewardedInterstitialAd.load(
                activity,
                REWARDED_INTERSTITIAL_AD_UNIT_ID,
                AdRequest.Builder().build(),
                object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedInterstitialAd) {
                        onLoaded(ad)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        logcat { "GoogleSdk: test rewarded interstitial failed to load: ${error.message}" }
                        onLoaded(null)
                    }
                },
            )
        } catch (e: Exception) {
            logcat { "GoogleSdk: test rewarded interstitial failed: ${e.message}" }
            onLoaded(null)
        }
    }

    /** Shows a loaded rewarded interstitial ad. [onRewarded] fires when the user earns the reward. */
    fun showRewardedInterstitial(activity: Activity, ad: RewardedInterstitialAd, onRewarded: (rewardType: String, rewardAmount: Int) -> Unit) {
        try {
            ad.show(activity, OnUserEarnedRewardListener { reward ->
                onRewarded(reward.type, reward.amount)
            })
        } catch (e: Exception) {
            logcat { "GoogleSdk: test rewarded interstitial failed to show: ${e.message}" }
        }
    }

    /** Loads a test native ad. [onLoaded] receives the ad, or null if loading failed. */
    fun loadNative(activity: Activity, onLoaded: (NativeAd?) -> Unit) {
        try {
            AdLoader.Builder(activity, NATIVE_AD_UNIT_ID)
                .forNativeAd { ad -> onLoaded(ad) }
                .withNativeAdOptions(NativeAdOptions.Builder().build())
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        logcat { "GoogleSdk: test native failed to load: ${error.message}" }
                        onLoaded(null)
                    }
                })
                .build()
                .loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            logcat { "GoogleSdk: test native failed: ${e.message}" }
            onLoaded(null)
        }
    }

    /**
     * Binds a loaded native ad into a [NativeAdView]. The view must already carry
     * the headline, body, call-to-action, icon, and media asset views.
     */
    fun bindNativeAd(nativeAd: NativeAd, adView: NativeAdView) {
        try {
            adView.headlineView?.let { (it as? android.widget.TextView)?.text = nativeAd.headline }
            adView.bodyView?.let { (it as? android.widget.TextView)?.text = nativeAd.body }
            adView.callToActionView?.let { (it as? android.widget.TextView)?.text = nativeAd.callToAction }
            adView.iconView?.let { (it as? android.widget.ImageView)?.setImageDrawable(nativeAd.icon?.drawable) }
            adView.mediaView?.let { mediaView ->
                nativeAd.mediaContent?.let { mediaView.mediaContent = it }
            }
            adView.setNativeAd(nativeAd)
        } catch (e: Exception) {
            logcat { "GoogleSdk: binding native ad failed: ${e.message}" }
        }
    }

    /** Loads a test app open ad. [onLoaded] receives the ad, or null if loading failed. */
    fun loadAppOpen(activity: Activity, onLoaded: (AppOpenAd?) -> Unit) {
        try {
            // play-services-ads 25.x has no orientation parameter on load();
            // the 4-argument overload is the only one.
            AppOpenAd.load(
                activity,
                APP_OPEN_AD_UNIT_ID,
                AdRequest.Builder().build(),
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        onLoaded(ad)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        logcat { "GoogleSdk: test app open failed to load: ${error.message}" }
                        onLoaded(null)
                    }
                },
            )
        } catch (e: Exception) {
            logcat { "GoogleSdk: test app open failed: ${e.message}" }
            onLoaded(null)
        }
    }

    /** Shows a loaded app open ad. */
    fun showAppOpen(activity: Activity, ad: AppOpenAd) {
        try {
            ad.show(activity)
        } catch (e: Exception) {
            logcat { "GoogleSdk: test app open failed to show: ${e.message}" }
        }
    }
}
