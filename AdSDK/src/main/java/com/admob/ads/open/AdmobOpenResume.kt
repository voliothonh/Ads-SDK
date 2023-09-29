package com.admob.ads.open

import com.admob.AdType
import com.admob.TAdCallback
import com.admob.ads.AdsSDK
import com.admob.getActivityOnTop
import com.admob.getClazzOnTop
import com.admob.ui.dialogs.DialogBackgroundOpenApp
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.appopen.AppOpenAd

object AdmobOpenResume {

    internal lateinit var adUnitId: String

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
        val adActivityOnTop = AdsSDK.getActivityOnTop() is AdActivity
        val containClazzOnTop = AdsSDK.clazzIgnoreAdResume.contains(AdsSDK.getClazzOnTop())
        if (clazzOnTop == null || containClazzOnTop || adActivityOnTop) {
            return
        }

        appOpenAd?.let { appOpenAd ->
            val dialog = DialogBackgroundOpenApp(AdsSDK.getActivityOnTop()!!)
            if (!dialog.isShowing) {
                dialog.show()
                AdmobOpen.show(
                    appOpenAd,
                    callback = object : TAdCallback {

                        override fun onAdImpression(adUnit: String, adType: AdType) {
                            super.onAdImpression(adUnit, adType)
                            isAppOpenAdShowing = true
                        }

                        override fun onAdFailedToShowFullScreenContent(
                            adUnit: String,
                            adType: AdType
                        ) {
                            super.onAdFailedToShowFullScreenContent(adUnit, adType)
                            isAppOpenAdShowing = false
                            AdmobOpenResume.appOpenAd = null
                            isAppOpenAdLoading = false
                            if (dialog.isShowing) {
                                dialog.dismiss()
                            }
                        }

                        override fun onAdDismissedFullScreenContent(
                            adUnit: String,
                            adType: AdType
                        ) {
                            super.onAdDismissedFullScreenContent(adUnit, adType)
                            isAppOpenAdShowing = false
                            AdmobOpenResume.appOpenAd = null
                            isAppOpenAdLoading = false

                            if (dialog.isShowing) {
                                dialog.dismiss()
                            }

                            load(adUnitId, callback = mCallback)
                        }
                    }
                )
            }
        }
    }
}