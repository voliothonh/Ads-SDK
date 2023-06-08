package com.admob.ads.open

import android.os.CountDownTimer
import com.admob.AdType
import com.admob.TAdCallback
import com.admob.ads.AdsSDK
import com.admob.ads.interstitial.AdmobInterSplash
import com.admob.getAppCompatActivityOnTop
import com.admob.onNextActionWhenResume
import com.admob.waitActivityResumed
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

object AdmobOpenSplash {

    private var timer: CountDownTimer? = null

    /**
     * @param adUnitId: adUnitId
     * @param timeout: timeout to wait ad show
     * @param onAdLoaded: callback when adLoaded => for update UI or something
     * @param nextAction: callback for your work, general handle nextActivity or nextFragment
     */
    fun show(
        adUnitId: String,
        timeout: Long,
        onAdLoaded: () -> Unit = {},
        nextAction: () -> Unit
    ) {

        if (!AdsSDK.isEnableOpenAds) {
            onAdLoaded.invoke()
            nextAction.invoke()
            return
        }

        val callback = object : TAdCallback {

            override fun onAdLoaded(adUnit: String, adType: AdType) {
                super.onAdLoaded(adUnit, adType)
                onAdLoaded.invoke()
            }

            override fun onAdFailedToLoad(adUnit: String, adType: AdType, error: LoadAdError) {
                super.onAdFailedToLoad(adUnit, adType, error)
                timer?.cancel()
                onNextActionWhenResume(nextAction)
            }

            override fun onAdFailedToShowFullScreenContent(adUnit: String, adType: AdType) {
                super.onAdFailedToShowFullScreenContent(adUnit, adType)
                timer?.cancel()
                onNextActionWhenResume(nextAction)
            }

            override fun onAdDismissedFullScreenContent(adUnit: String, adType: AdType) {
                super.onAdDismissedFullScreenContent(adUnit, adType)
                timer?.cancel()
                onNextActionWhenResume(nextAction)
            }
        }

        var adOpenAd: AppOpenAd? = null

        AdmobOpen.load(
            adUnitId,
            callback,
            onAdLoaded = { adOpenAd = it }
        )

        timer?.cancel()
        timer = object : CountDownTimer(timeout, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                if (!AdsSDK.isEnableOpenAds) {
                    timer?.cancel()
                    onAdLoaded.invoke()
                    nextAction.invoke()
                    return
                }

                adOpenAd?.let { appOpenAd ->
                    timer?.cancel()
                    onNextActionWhenResume {
                        AdsSDK.getAppCompatActivityOnTop()?.waitActivityResumed {
                            AdmobOpen.show(appOpenAd, callback)
                        }
                    }
                }
            }

            override fun onFinish() {
                timer?.cancel()
                onNextActionWhenResume(nextAction)
            }
        }.start()
    }
}