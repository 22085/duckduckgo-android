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

import android.os.Bundle
import android.view.View
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.browser.databinding.ActivityGoogleAdsTestBinding
import com.duckduckgo.codegen.annotations.ContributeToActivityStarter
import com.duckduckgo.codegen.annotations.InjectWith
import com.duckduckgo.common.ui.DuckDuckGoActivity
import com.duckduckgo.common.ui.viewbinding.viewBinding
import com.duckduckgo.di.scopes.ActivityScope
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.nativead.NativeAd

/**
 * Test screen for all six AdMob formats, reachable from Settings.
 * Every format runs on the fork's formal AdMob IDs; nothing here is wired into
 * the browser UI.
 */
@InjectWith(ActivityScope::class)
@ContributeToActivityStarter(GoogleAdsTestNoParams::class)
class GoogleAdsTestActivity : DuckDuckGoActivity() {

    private val binding: ActivityGoogleAdsTestBinding by viewBinding()

    private var bannerAdView: AdView? = null
    private var nativeAd: NativeAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            googleAdsTestShowBannerButton.setOnClickListener { loadBanner() }
            googleAdsTestShowInterstitialButton.setOnClickListener { loadInterstitial() }
            googleAdsTestShowRewardedButton.setOnClickListener { loadRewarded() }
            googleAdsTestShowRewardedInterstitialButton.setOnClickListener { loadRewardedInterstitial() }
            googleAdsTestShowNativeButton.setOnClickListener { loadNative() }
            googleAdsTestShowAppOpenButton.setOnClickListener { loadAppOpen() }
        }
    }

    private fun loadBanner() {
        setStatus(getString(R.string.googleAdsTestStatusLoading, getString(R.string.googleAdsTestFormatBanner)))
        bannerAdView?.destroy()
        bannerAdView = null
        binding.googleAdsTestBannerContainer.removeAllViews()
        val adView = GoogleSdk.loadBanner(this, binding.googleAdsTestBannerContainer)
        if (adView == null) {
            setStatus(getString(R.string.googleAdsTestStatusFailed, getString(R.string.googleAdsTestFormatBanner)))
        } else {
            bannerAdView = adView
            setStatus(getString(R.string.googleAdsTestStatusLoaded, getString(R.string.googleAdsTestFormatBanner)))
        }
    }

    private fun loadInterstitial() {
        setStatus(getString(R.string.googleAdsTestStatusLoading, getString(R.string.googleAdsTestFormatInterstitial)))
        GoogleSdk.loadInterstitial(this) { ad ->
            if (ad == null) {
                setStatus(getString(R.string.googleAdsTestStatusFailed, getString(R.string.googleAdsTestFormatInterstitial)))
            } else {
                setStatus(getString(R.string.googleAdsTestStatusLoaded, getString(R.string.googleAdsTestFormatInterstitial)))
                ad.show(this)
            }
        }
    }

    private fun loadRewarded() {
        setStatus(getString(R.string.googleAdsTestStatusLoading, getString(R.string.googleAdsTestFormatRewarded)))
        GoogleSdk.loadRewarded(this) { ad ->
            if (ad == null) {
                setStatus(getString(R.string.googleAdsTestStatusFailed, getString(R.string.googleAdsTestFormatRewarded)))
            } else {
                setStatus(getString(R.string.googleAdsTestStatusLoaded, getString(R.string.googleAdsTestFormatRewarded)))
                GoogleSdk.showRewarded(this, ad) { type, amount ->
                    setStatus(getString(R.string.googleAdsTestStatusRewarded, type, amount))
                }
            }
        }
    }

    private fun loadRewardedInterstitial() {
        setStatus(getString(R.string.googleAdsTestStatusLoading, getString(R.string.googleAdsTestFormatRewardedInterstitial)))
        GoogleSdk.loadRewardedInterstitial(this) { ad ->
            if (ad == null) {
                setStatus(getString(R.string.googleAdsTestStatusFailed, getString(R.string.googleAdsTestFormatRewardedInterstitial)))
            } else {
                setStatus(getString(R.string.googleAdsTestStatusLoaded, getString(R.string.googleAdsTestFormatRewardedInterstitial)))
                GoogleSdk.showRewardedInterstitial(this, ad) { type, amount ->
                    setStatus(getString(R.string.googleAdsTestStatusRewarded, type, amount))
                }
            }
        }
    }

    private fun loadNative() {
        setStatus(getString(R.string.googleAdsTestStatusLoading, getString(R.string.googleAdsTestFormatNative)))
        GoogleSdk.loadNative(this) { ad ->
            if (ad == null) {
                setStatus(getString(R.string.googleAdsTestStatusFailed, getString(R.string.googleAdsTestFormatNative)))
            } else {
                nativeAd?.destroy()
                nativeAd = ad
                with(binding) {
                    googleAdsTestNativeAdView.headlineView = googleAdsTestNativeHeadline
                    googleAdsTestNativeAdView.bodyView = googleAdsTestNativeBody
                    googleAdsTestNativeAdView.callToActionView = googleAdsTestNativeCallToAction
                    googleAdsTestNativeAdView.iconView = googleAdsTestNativeIcon
                    googleAdsTestNativeAdView.mediaView = googleAdsTestNativeMedia
                    GoogleSdk.bindNativeAd(ad, googleAdsTestNativeAdView)
                    googleAdsTestNativeAdView.visibility = View.VISIBLE
                }
                setStatus(getString(R.string.googleAdsTestStatusLoaded, getString(R.string.googleAdsTestFormatNative)))
            }
        }
    }

    private fun loadAppOpen() {
        setStatus(getString(R.string.googleAdsTestStatusLoading, getString(R.string.googleAdsTestFormatAppOpen)))
        GoogleSdk.loadAppOpen(this) { ad ->
            if (ad == null) {
                setStatus(getString(R.string.googleAdsTestStatusFailed, getString(R.string.googleAdsTestFormatAppOpen)))
            } else {
                setStatus(getString(R.string.googleAdsTestStatusLoaded, getString(R.string.googleAdsTestFormatAppOpen)))
                GoogleSdk.showAppOpen(this, ad)
            }
        }
    }

    private fun setStatus(text: String) {
        binding.googleAdsTestStatus.text = text
    }

    override fun onDestroy() {
        bannerAdView?.destroy()
        nativeAd?.destroy()
        super.onDestroy()
    }
}
