package com.admob

import android.os.Bundle
import com.google.android.gms.ads.LoadAdError

interface TAdCallback {
    fun onAdStartLoading(adUnit: String, adType: AdType) {}
    fun onAdClicked(adUnit: String, adType: AdType) {}
    fun onAdClosed(adUnit: String, adType: AdType) {}
    fun onAdDismissedFullScreenContent(adUnit: String, adType: AdType) {}
    fun onAdShowedFullScreenContent(adUnit: String, adType: AdType) {}
    fun onAdFailedToShowFullScreenContent(error : String, adUnit: String, adType: AdType) {}
    fun onAdFailedToLoad(adUnit: String, adType: AdType, error: LoadAdError) {}
    fun onAdImpression(adUnit: String, adType: AdType) {}
    fun onAdLoaded(adUnit: String, adType: AdType) {}
    fun onAdOpened(adUnit: String, adType: AdType) {}
    fun onAdSwipeGestureClicked(adUnit: String, adType: AdType) {}
    fun onPaidValueListener(bundle : Bundle) {}
    fun onSetInterFloorId(){} /*When application resume, some device init all member variable, pls call set InterFloor.setId*/
}

enum class AdType {
    OpenApp, Inter, Banner, Native, Rewarded
}