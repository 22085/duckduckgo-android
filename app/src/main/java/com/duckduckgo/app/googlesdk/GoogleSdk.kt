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
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import logcat.logcat

/**
 * Google Ads (AdMob) only for the duckduckgo-android fork.
 *
 * Everything here is fail-safe: AdMob initialization failure logs and skips
 * instead of crashing startup. AdMob runs on Google test IDs only.
 */
object GoogleSdk {

    /** Google test App ID. Replace with the formal AdMob App ID before a production release. */
    const val ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"

    /** Google test banner unit ID. */
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    /** Google test interstitial unit ID. */
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    fun initialize(context: Context) {
        try {
            MobileAds.initialize(context)
            logcat { "GoogleSdk: AdMob initialized (test App ID)" }
        } catch (e: Exception) {
            logcat { "GoogleSdk: AdMob initialization failed, ads disabled: ${e.message}" }
        }
    }

    /** Loads a test banner into [parent]. Returns the AdView, or null if loading failed. */
    fun loadTestBanner(activity: Activity, parent: ViewGroup): AdView? {
        return try {
            val adView = AdView(activity).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = TEST_BANNER_AD_UNIT_ID
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
    fun loadTestInterstitial(activity: Activity, onLoaded: (InterstitialAd?) -> Unit) {
        try {
            InterstitialAd.load(
                activity,
                TEST_INTERSTITIAL_AD_UNIT_ID,
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
}
