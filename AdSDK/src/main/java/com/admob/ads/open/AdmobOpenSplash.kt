package com.admob.ads.open

import android.os.Bundle
import android.os.CountDownTimer
import com.admob.AdType
import com.admob.TAdCallback
import com.admob.ads.AdsSDK
import com.admob.getAppCompatActivityOnTop
import com.admob.onNextActionWhenResume
import com.admob.waitActivityResumed
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

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
        onPaidValueListener: (Bundle) -> Unit,
        nextAction: () -> Unit
    ) {

        var isNextActionExecuted = false

        fun callNextAction() {
            if (isNextActionExecuted) {
                isNextActionExecuted = true
                return
            }
            onNextActionWhenResume(nextAction)
        }

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
                callNextAction()
            }

            override fun onAdFailedToShowFullScreenContent(adUnit: String, adType: AdType) {
                super.onAdFailedToShowFullScreenContent(adUnit, adType)
                timer?.cancel()
                callNextAction()
            }

            override fun onAdDismissedFullScreenContent(adUnit: String, adType: AdType) {
                super.onAdDismissedFullScreenContent(adUnit, adType)
                timer?.cancel()
                callNextAction()
            }

            override fun onPaidValueListener(bundle: Bundle) {
                super.onPaidValueListener(bundle)
                onPaidValueListener.invoke(bundle)
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
                callNextAction()
            }
        }.start()
    }

    /**
     * @param adUnitId: adUnitId
     * @param timeout: timeout to wait ad show
     * @param onAdLoaded: callback when adLoaded => for update UI or something
     * @param nextAction: callback for your work, general handle nextActivity or nextFragment
     */
    fun showWithFloor(
        adUnitIdH: String,
        adUnitIdM: String,
        adUnitIdL: String,
        timeout: Long,
        onAdLoaded: () -> Unit = {},
        onPaidValueListener: (Bundle) -> Unit,
        nextAction: () -> Unit
    ) {

        Firebase.analytics.logEvent("Splash_Ads_Floor", null)

        var isNextActionExecuted = false

        var currentAdUnitLoading = adUnitIdH

        var adOpenAd: AppOpenAd? = null

        fun callNextAction() {
            if (isNextActionExecuted) {
                isNextActionExecuted = true
                return
            }
            onNextActionWhenResume(nextAction)
        }

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


                currentAdUnitLoading = when (currentAdUnitLoading) {
                    adUnitIdH -> {
                        Firebase.analytics.logEvent("Splash_Ads_Floor_Medium", null)
                        adUnitIdM
                    }

                    adUnitIdM -> {
                        Firebase.analytics.logEvent("Splash_Ads_Floor_Low", null)
                        adUnitIdL
                    }

                    else -> {
                        ""
                    }
                }

                if (currentAdUnitLoading.isEmpty()) {
                    timer?.cancel()
                    callNextAction()
                } else {
                    AdmobOpen.load(
                        currentAdUnitLoading,
                        this,
                        onAdLoaded = { adOpenAd = it }
                    )
                }
            }

            override fun onAdFailedToShowFullScreenContent(adUnit: String, adType: AdType) {
                super.onAdFailedToShowFullScreenContent(adUnit, adType)
                timer?.cancel()
                callNextAction()
            }

            override fun onAdDismissedFullScreenContent(adUnit: String, adType: AdType) {
                super.onAdDismissedFullScreenContent(adUnit, adType)
                timer?.cancel()
                callNextAction()
            }

            override fun onPaidValueListener(bundle: Bundle) {
                super.onPaidValueListener(bundle)
                onPaidValueListener.invoke(bundle)
            }
        }

        Firebase.analytics.logEvent("Splash_Ads_Floor_High", null)

        AdmobOpen.load(
            currentAdUnitLoading,
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
                callNextAction()
            }
        }.start()
    }
}