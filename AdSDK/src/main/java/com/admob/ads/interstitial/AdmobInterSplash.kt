package com.admob.ads.interstitial

import android.os.CountDownTimer
import com.admob.AdType
import com.admob.TAdCallback
import com.admob.ads.AdsSDK
import com.admob.getAppCompatActivityOnTop
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
        adUnitId: String,
        timeout: Long,
        nextAction: () -> Unit
    ) {

        if (!AdsSDK.isEnableInter){
            nextAction.invoke()
            return
        }

        val callback = object : TAdCallback {
            override fun onAdFailedToLoad(adUnit: String, adType: AdType, error: LoadAdError) {
                super.onAdFailedToLoad(adUnit, adType, error)
                onNextActionWhenResume(nextAction)
            }

            override fun onAdFailedToShowFullScreenContent(adUnit: String, adType: AdType) {
                super.onAdFailedToShowFullScreenContent(adUnit, adType)
                onNextActionWhenResume(nextAction)
            }
        }

        AdmobInter.load(adUnitId, callback)

        timer?.cancel()
        timer = object : CountDownTimer(timeout, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                if (!AdsSDK.isEnableInter) {
                    nextAction.invoke()
                    return
                }

                if (AdmobInter.checkShowInterCondition(adUnitId, false)) {
                    timer?.cancel()
                    onNextActionWhenResume {
                        AdsSDK.getAppCompatActivityOnTop()?.waitActivityResumed {
                            AdmobInter.show(
                                adUnitId = adUnitId,
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
