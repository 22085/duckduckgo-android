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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging
import logcat.logcat

/**
 * Google SDKs (AdMob, Firebase, Google Sign-In) for the duckduckgo-android fork.
 *
 * Everything here is fail-safe: any SDK that is not configured yet logs and skips
 * instead of crashing startup. AdMob runs on Google test IDs only; Firebase does
 * nothing until a real google-services.json is added; Sign-In needs a real OAuth
 * client ID for this package before it can return an account.
 */
object GoogleSdk {

    /** Google test App ID. Replace with the formal AdMob App ID before a production release. */
    const val ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"

    /** Google test banner unit ID. */
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    /** Google test interstitial unit ID. */
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    fun initialize(context: Context) {
        initializeAdMob(context)
        initializeFirebase(context)
    }

    private fun initializeAdMob(context: Context) {
        try {
            MobileAds.initialize(context)
            logcat { "GoogleSdk: AdMob initialized (test App ID)" }
        } catch (e: Exception) {
            logcat { "GoogleSdk: AdMob initialization failed, ads disabled: ${e.message}" }
        }
    }

    private fun initializeFirebase(context: Context) {
        if (!isFirebaseConfigured()) {
            logcat { "GoogleSdk: Firebase not configured, skipping. Add a real google-services.json to enable it." }
            return
        }
        try {
            FirebaseAnalytics.getInstance(context)
            FirebaseCrashlytics.getInstance()
            FirebaseMessaging.getInstance()
            logcat { "GoogleSdk: Firebase initialized" }
        } catch (e: Exception) {
            logcat { "GoogleSdk: Firebase initialization failed: ${e.message}" }
        }
    }

    fun isFirebaseConfigured(): Boolean {
        return try {
            FirebaseApp.getInstance()
            true
        } catch (e: IllegalStateException) {
            false
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

    /**
     * Builds a Google Sign-In client requesting the account email. Returns null when
     * Sign-In cannot be built. Real sign-in also needs an OAuth client ID registered
     * for this package in the user's Google Cloud project, otherwise sign-in fails
     * with an API error at runtime.
     */
    fun buildSignInClient(context: Context): GoogleSignInClient? {
        return try {
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
            GoogleSignIn.getClient(context, options)
        } catch (e: Exception) {
            logcat { "GoogleSdk: Google Sign-In client unavailable: ${e.message}" }
            null
        }
    }
}
