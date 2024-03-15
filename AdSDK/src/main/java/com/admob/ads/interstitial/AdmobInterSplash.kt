package com.admob.ads.interstitial

import android.os.CountDownTimer
import com.admob.AdType
import com.admob.TAdCallback
import com.admob.ads.AdsSDK
import com.admob.getAppCompatActivityOnTop
import com.admob.isEnable
import com.admob.isNetworkAvailable
import com.admob.onNextActionWhenResume
import com.admob.waitActivityResumed
import com.google.android.gms.ads.LoadAdError


object AdmobInterSplash {

    private var timer: CountDownTimer? = null

    /**
     * @param adUnitId: adUnit
     * @param timeout: timeout to wait ad show
     * @param nextAction
     */
    fun show(
        space: String,
        timeout: Long,
        nextAction: () -> Unit
    ) {

        val adChild = AdsSDK.getAdChild(space)

        if (adChild == null){
            nextAction.invoke()
            return
        }

        if (!AdsSDK.isEnableInter || AdsSDK.isPremium || (adChild.adsType != "inter_splash") || !AdsSDK.app.isNetworkAvailable() || !adChild.isEnable()) {
            nextAction.invoke()
            return
        }

        val callback = object : TAdCallback {
            override fun onAdFailedToLoad(adUnit: String, adType: AdType, error: LoadAdError) {
                super.onAdFailedToLoad(adUnit, adType, error)
                timer?.cancel()
                onNextActionWhenResume(nextAction)
            }

            override fun onAdFailedToShowFullScreenContent(
                error: String,
                adUnit: String,
                adType: AdType
            ) {
                super.onAdFailedToShowFullScreenContent(error, adUnit, adType)
                timer?.cancel()
                onNextActionWhenResume(nextAction)
            }
        }

        AdmobInter.load(adChild.adsId, callback)

        timer?.cancel()
        timer = object : CountDownTimer(timeout, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                if (!AdsSDK.isEnableInter) {
                    timer?.cancel()
                    nextAction.invoke()
                    return
                }

                if (AdmobInter.checkShowInterCondition(adChild.spaceName, false)) {
                    timer?.cancel()
                    onNextActionWhenResume {
                        AdsSDK.getAppCompatActivityOnTop()?.waitActivityResumed {
                            AdmobInter.show(
                                space = space,
                                showLoadingInter = false,
                                forceShow = true,
                                loadAfterDismiss = false,
                                loadIfNotAvailable = false,
                                callback = callback,
                                nextAction = nextAction
                            )
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
