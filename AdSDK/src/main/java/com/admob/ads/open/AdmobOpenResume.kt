package com.admob.ads.open

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.admob.AdType
import com.admob.TAdCallback
import com.admob.ads.AdsSDK
import com.admob.delay
import com.admob.getActivityOnTop
import com.admob.getClazzOnTop
import com.admob.topActivityIsAd
import com.admob.waitingResumeNoDelay
import com.google.android.gms.ads.appopen.AppOpenAd

object AdmobOpenResume {

    internal lateinit var adUnitId: String

    fun isAdUnitIdInit() = ::adUnitId.isInitialized

    private var appOpenAd: AppOpenAd? = null
    private var mCallback: TAdCallback? = null
    private var isAppOpenAdShowing = false
    private var isAppOpenAdLoading = false

    fun load(id: String, callback: TAdCallback? = null) {
        adUnitId = id
        mCallback = callback
        isAppOpenAdLoading = true
        AdmobOpen.load(
            adUnitId,
            mCallback,
            onAdLoadFailure = {
                isAppOpenAdLoading = false

            }, onAdLoaded = {
                isAppOpenAdLoading = false
                appOpenAd = it
            }
        )
    }

    internal fun onOpenAdAppResume() {

        if (!AdsSDK.isEnableOpenAds) {
            return
        }

        if (isAppOpenAdShowing) {
            return
        }

        if (!AdmobOpenResume::adUnitId.isInitialized) {
            return
        }

        if (appOpenAd == null && !isAppOpenAdLoading) {
            AdmobOpen.load(
                adUnitId,
                onAdLoadFailure = {
                    isAppOpenAdLoading = false
                    appOpenAd = null
                },
                onAdLoaded = {
                    isAppOpenAdLoading = false
                    appOpenAd = it
                },
                callback = mCallback
            )
            return
        }

        val activity = AdsSDK.getActivityOnTop()

        activity ?: return

        val clazzOnTop = AdsSDK.getClazzOnTop()
        val adActivityOnTop = AdsSDK.topActivityIsAd()
        val containClazzOnTop = AdsSDK.clazzIgnoreAdResume.contains(AdsSDK.getClazzOnTop())
        if (clazzOnTop == null || containClazzOnTop || adActivityOnTop) {
            return
        }

        appOpenAd?.let { appOpenAd ->
            activity.waitingResumeNoDelay {
                AdmobOpen.show(
                    appOpenAd,
                    callback = object : TAdCallback {

                        override fun onAdImpression(adUnit: String, adType: AdType) {
                            super.onAdImpression(adUnit, adType)
                            isAppOpenAdShowing = true
                        }

                        override fun onAdFailedToShowFullScreenContent(
                            error: String,
                            adUnit: String,
                            adType: AdType
                        ) {
                            super.onAdFailedToShowFullScreenContent(error, adUnit, adType)
                            isAppOpenAdShowing = false
                            AdmobOpenResume.appOpenAd = null
                            isAppOpenAdLoading = false

                        }

                        override fun onAdDismissedFullScreenContent(
                            adUnit: String,
                            adType: AdType
                        ) {
                            super.onAdDismissedFullScreenContent(adUnit, adType)
                            isAppOpenAdShowing = false
                            AdmobOpenResume.appOpenAd = null
                            isAppOpenAdLoading = false
                            load(adUnitId, callback = mCallback)
                        }
                    }
                )
            }
        }
    }
}