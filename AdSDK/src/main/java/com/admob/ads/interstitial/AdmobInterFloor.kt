package com.admob.ads.interstitial

import android.os.Bundle
import android.widget.Toast
import com.admob.AdType
import com.admob.TAdCallback
import com.admob.ads.AdsSDK
import com.admob.ads.BuildConfig
import com.google.android.gms.ads.LoadAdError
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object AdmobInterFloor {

    private lateinit var adUnitIdHigh: String
    private lateinit var adUnitIdMedium: String
    private lateinit var adUnitIdLow: String
    private lateinit var currAdUnitLoad: String


    /**
     * Call this function onCreate of Application class
     * If want to disable inter, call AdsSDK.disableInter()
     */
    fun setAdUnitId(
        adUnitIdHigh: String,
        adUnitIdMedium: String,
        adUnitIdLow: String,
    ) {
        this.adUnitIdHigh = adUnitIdHigh
        this.adUnitIdMedium = adUnitIdMedium
        this.adUnitIdLow = adUnitIdLow
        this.currAdUnitLoad = adUnitIdHigh
    }

    fun load() {
        if (::adUnitIdHigh.isInitialized) {
            loadInterFloor(adUnitIdHigh)
        } else {
            AdsSDK.adCallback.onSetInterFloorId()
            if (BuildConfig.DEBUG) {
                Toast.makeText(
                    AdsSDK.app,
                    "Please call setAdUnitId in Application class.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadInterFloor(adUnitId: String) {
        if (!::adUnitIdHigh.isInitialized) {
            AdsSDK.adCallback.onSetInterFloorId()
            return
        }
        currAdUnitLoad = adUnitId
        AdmobInter.load(
            currAdUnitLoad,
            object : TAdCallback {
                override fun onAdFailedToLoad(adUnit: String, adType: AdType, error: LoadAdError) {
                    super.onAdFailedToLoad(adUnit, adType, error)
                    val nextAdUnit = if (currAdUnitLoad == adUnitIdHigh) {
                        Firebase.analytics.logEvent("Inter_Ads_Floor_Medium", null)
                        adUnitIdMedium
                    } else if (currAdUnitLoad == adUnitIdMedium) {
                        Firebase.analytics.logEvent("Inter_Ads_Floor_Low", null)
                        adUnitIdLow
                    } else {
                        ""
                    }

                    if (nextAdUnit.isNotEmpty()) {
                        loadInterFloor(nextAdUnit)
                    }
                }
            }
        )
    }

    fun show(
        showLoadingInter: Boolean = true,
        forceShow: Boolean = false,
        nextActionBeforeDismiss: Boolean = true,
        nextActionBeforeDismissDelayTime: Long = 0,
        callback: TAdCallback? = null,
        nextAction: () -> Unit
    ) {

        if (!::adUnitIdHigh.isInitialized) {
            AdsSDK.adCallback.onSetInterFloorId()
            return
        }

        AdmobInter.show(
            currAdUnitLoad,
            showLoadingInter,
            forceShow,
            nextActionBeforeDismiss,
            nextActionBeforeDismissDelayTime,
            loadAfterDismiss = false,
            loadIfNotAvailable = false,
            nextAction = nextAction,
            callback = object : TAdCallback {
                override fun onAdClicked(adUnit: String, adType: AdType) {
                    super.onAdClicked(adUnit, adType)
                    callback?.onAdClicked(adUnit, adType)
                }

                override fun onAdClosed(adUnit: String, adType: AdType) {
                    super.onAdClosed(adUnit, adType)
                    callback?.onAdClosed(adUnit, adType)
                }

                override fun onAdDismissedFullScreenContent(adUnit: String, adType: AdType) {
                    super.onAdDismissedFullScreenContent(adUnit, adType)
                    callback?.onAdDismissedFullScreenContent(adUnit, adType)
                    load()
                }

                override fun onAdShowedFullScreenContent(adUnit: String, adType: AdType) {
                    super.onAdShowedFullScreenContent(adUnit, adType)
                    callback?.onAdShowedFullScreenContent(adUnit, adType)
                }

                override fun onAdFailedToShowFullScreenContent(error : String, adUnit: String, adType: AdType) {
                    super.onAdFailedToShowFullScreenContent(error, adUnit, adType)
                    callback?.onAdFailedToShowFullScreenContent(error, adUnit, adType)
                    load()
                }

                override fun onAdFailedToLoad(adUnit: String, adType: AdType, error: LoadAdError) {
                    super.onAdFailedToLoad(adUnit, adType, error)
                    callback?.onAdFailedToLoad(adUnit, adType, error)
                    load()
                }

                override fun onAdImpression(adUnit: String, adType: AdType) {
                    super.onAdImpression(adUnit, adType)
                    callback?.onAdImpression(adUnit, adType)
                }

                override fun onAdLoaded(adUnit: String, adType: AdType) {
                    super.onAdLoaded(adUnit, adType)
                    callback?.onAdLoaded(adUnit, adType)
                }

                override fun onAdOpened(adUnit: String, adType: AdType) {
                    super.onAdOpened(adUnit, adType)
                    callback?.onAdOpened(adUnit, adType)
                }

                override fun onAdSwipeGestureClicked(adUnit: String, adType: AdType) {
                    super.onAdSwipeGestureClicked(adUnit, adType)
                    callback?.onAdSwipeGestureClicked(adUnit, adType)
                }

                override fun onPaidValueListener(bundle: Bundle) {
                    super.onPaidValueListener(bundle)
                    callback?.onPaidValueListener(bundle)
                }
            }
        )
    }
}

