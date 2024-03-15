package com.admob.ads.open

import com.admob.AdType
import com.admob.TAdCallback
import com.admob.ads.AdsSDK
import com.admob.getActivityOnTop
import com.admob.getPaidTrackingBundle
import com.admob.isEnable
import com.admob.isNetworkAvailable
import com.admob.logAdImpression
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

object AdmobOpen {

    private const val TAG = "AdmobOpenAds"

    internal fun load(
        space: String,
        callback: TAdCallback? = null,
        onAdLoadFailure : () -> Unit = {},
        onAdLoaded: (appOpenAd: AppOpenAd) -> Unit = {},
    ) {
        val adChild = AdsSDK.getAdChild(space)
        if (adChild == null){
            onAdLoadFailure.invoke()
            return
        }

        if (!AdsSDK.app.isNetworkAvailable() || AdsSDK.isPremium  || (adChild.adsType != "open_app") || !adChild.isEnable()) {
            onAdLoadFailure.invoke()
            return
        }



        AdsSDK.adCallback.onAdStartLoading(adChild.adsId, AdType.OpenApp)
        callback?.onAdStartLoading(adChild.adsId, AdType.OpenApp)

        AppOpenAd.load(
            AdsSDK.app,
            adChild.adsId,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    AdsSDK.adCallback.onAdFailedToLoad(adChild.adsId, AdType.OpenApp, loadAdError)
                    callback?.onAdFailedToLoad(adChild.adsId, AdType.OpenApp, loadAdError)
                    onAdLoadFailure.invoke()
                }

                override fun onAdLoaded(appOpenAd: AppOpenAd) {
                    super.onAdLoaded(appOpenAd)
                    AdsSDK.adCallback.onAdLoaded(adChild.adsId, AdType.OpenApp)
                    callback?.onAdLoaded(adChild.adsId, AdType.OpenApp)
                    onAdLoaded.invoke(appOpenAd)
                    appOpenAd.setOnPaidEventListener { adValue ->
                        val bundle = getPaidTrackingBundle(adValue, adChild.adsId, "OpenApp", appOpenAd.responseInfo)
                        AdsSDK.adCallback.onPaidValueListener(bundle)
                        callback?.onPaidValueListener(bundle)
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
                    adError.message,
                    appOpenAd.adUnitId,
                    AdType.OpenApp
                )
                callback?.onAdFailedToShowFullScreenContent(appOpenAd.adUnitId, adError.message, AdType.OpenApp)
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