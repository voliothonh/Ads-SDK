package com.admob.ads.open

import com.admob.AdType
import com.admob.TAdCallback
import com.admob.ads.AdsSDK
import com.admob.ads.open.AdmobOpenResume.adUnitId
import com.admob.getActivityOnTop
import com.admob.logAdImpression
import com.admob.trackingAdValue
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

object AdmobOpen {

    private const val TAG = "AdmobOpenAds"

    internal fun load(
        adUnitId: String,
        callback: TAdCallback? = null,
        onAdLoadFailure : () -> Unit = {},
        onAdLoaded: (appOpenAd: AppOpenAd) -> Unit = {},
    ) {
        AppOpenAd.load(
            AdsSDK.app,
            adUnitId,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    AdsSDK.adCallback.onAdFailedToLoad(adUnitId, AdType.OpenApp, loadAdError)
                    callback?.onAdFailedToLoad(adUnitId, AdType.OpenApp, loadAdError)
                    onAdLoadFailure.invoke()
                }

                override fun onAdLoaded(appOpenAd: AppOpenAd) {
                    super.onAdLoaded(appOpenAd)
                    AdsSDK.adCallback.onAdLoaded(adUnitId, AdType.OpenApp)
                    callback?.onAdLoaded(adUnitId, AdType.OpenApp)
                    onAdLoaded.invoke(appOpenAd)
                    val adapter = appOpenAd.responseInfo.mediationAdapterClassName
                    appOpenAd.setOnPaidEventListener { adValue ->
                        trackingAdValue(adValue, adUnitId, "OpenApp", adapter)
                    }
                }
            }
        )
    }

    internal fun show(
        appOpenAd: AppOpenAd,
        callback: TAdCallback? = null
    ) {

        if (!AdsSDK.isEnableOpenAds){
            return
        }

        val activity = AdsSDK.getActivityOnTop() ?: return

        appOpenAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                AdsSDK.adCallback.onAdClicked(appOpenAd.adUnitId, AdType.OpenApp)
                callback?.onAdClicked(appOpenAd.adUnitId, AdType.OpenApp)
            }

            override fun onAdDismissedFullScreenContent() {
                AdsSDK.adCallback.onAdDismissedFullScreenContent(appOpenAd.adUnitId, AdType.OpenApp)
                callback?.onAdDismissedFullScreenContent(appOpenAd.adUnitId, AdType.OpenApp)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                AdsSDK.adCallback.onAdFailedToShowFullScreenContent(
                    appOpenAd.adUnitId,
                    AdType.OpenApp
                )
                callback?.onAdFailedToShowFullScreenContent(appOpenAd.adUnitId, AdType.OpenApp)
                runCatching { Throwable(adError.message) }
            }

            override fun onAdImpression() {
                AdsSDK.adCallback.onAdImpression(appOpenAd.adUnitId, AdType.OpenApp)
                callback?.onAdImpression(appOpenAd.adUnitId, AdType.OpenApp)
                logAdImpression(TAG)
            }

            override fun onAdShowedFullScreenContent() {
                AdsSDK.adCallback.onAdShowedFullScreenContent(appOpenAd.adUnitId, AdType.OpenApp)
                callback?.onAdShowedFullScreenContent(appOpenAd.adUnitId, AdType.OpenApp)
            }
        }

        appOpenAd.show(activity)
    }
}