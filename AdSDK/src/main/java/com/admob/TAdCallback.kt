package com.admob

import android.os.Bundle
import com.google.android.gms.ads.LoadAdError

interface TAdCallback {
    fun onAdClicked(adUnit: String, adType: AdType) {}
    fun onAdClosed(adUnit: String, adType: AdType) {}
    fun onAdDismissedFullScreenContent(adUnit: String, adType: AdType) {}
    fun onAdShowedFullScreenContent(adUnit: String, adType: AdType) {}
    fun onAdFailedToShowFullScreenContent(adUnit: String, adType: AdType) {}
    fun onAdFailedToLoad(adUnit: String, adType: AdType, error: LoadAdError) {}
    fun onAdImpression(adUnit: String, adType: AdType) {}
    fun onAdLoaded(adUnit: String, adType: AdType) {}
    fun onAdOpened(adUnit: String, adType: AdType) {}
    fun onAdSwipeGestureClicked(adUnit: String, adType: AdType) {}
    fun onPaidValueListener(bundle : Bundle) {}
}

enum class AdType {
    OpenApp, Inter, Banner, Native, Rewarded
}