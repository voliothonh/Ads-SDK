package com.admob.ads.banner

import android.os.Bundle
import android.view.ViewGroup
import com.admob.AdType
import com.admob.TAdCallback
import com.google.android.gms.ads.LoadAdError

object AdmobBannerFloor {
    fun showAdaptive(
        adUnitIdHigh: String,
        adUnitIdMedium: String,
        adUnitIdLow: String,
        adContainer: ViewGroup,
        forceRefresh: Boolean = false,
        callback: TAdCallback? = null
    ) {
        var currAdUnitId = adUnitIdHigh

        fun showAdaptiveBanner(adUnitId : String){
            showAdaptiveBanner(
                adUnitId = adUnitId,
                adContainer = adContainer,
                forceRefresh = forceRefresh,
                callback = callback,
                onLoadFailure = {
                    if (currAdUnitId == adUnitIdHigh) {
                        currAdUnitId = adUnitIdMedium
                    }

                    if (currAdUnitId == adUnitIdMedium) {
                        currAdUnitId = adUnitIdLow
                    }

                    showAdaptiveBanner(currAdUnitId)
                },
            )
        }

        showAdaptiveBanner(currAdUnitId)

    }

    private fun showAdaptiveBanner(
        adUnitId: String,
        adContainer: ViewGroup,
        forceRefresh: Boolean = false,
        callback: TAdCallback? = null,
        onLoadFailure: () -> Unit,
    ) {
        AdmobBanner.showAdaptive(
            adContainer,
            adUnitId,
            forceRefresh,
            callback = object : TAdCallback {
                override fun onAdClicked(adUnit: String, adType: AdType) {
                    callback?.onAdClicked(adUnit, adType)
                }

                override fun onAdClosed(adUnit: String, adType: AdType) {
                    callback?.onAdClosed(adUnit, adType)
                }

                override fun onAdDismissedFullScreenContent(adUnit: String, adType: AdType) {
                    callback?.onAdDismissedFullScreenContent(adUnit, adType)
                }

                override fun onAdShowedFullScreenContent(adUnit: String, adType: AdType) {
                    callback?.onAdShowedFullScreenContent(adUnit, adType)
                }

                override fun onAdFailedToShowFullScreenContent(adUnit: String, adType: AdType) {
                    callback?.onAdFailedToShowFullScreenContent(adUnit, adType)
                }

                override fun onAdFailedToLoad(adUnit: String, adType: AdType, error: LoadAdError) {
                    callback?.onAdFailedToLoad(adUnit, adType, error)
                    onLoadFailure.invoke()
                }

                override fun onAdImpression(adUnit: String, adType: AdType) {
                    callback?.onAdImpression(adUnit, adType)
                }

                override fun onAdLoaded(adUnit: String, adType: AdType) {
                    callback?.onAdLoaded(adUnit, adType)
                }

                override fun onAdOpened(adUnit: String, adType: AdType) {
                    callback?.onAdOpened(adUnit, adType)
                }

                override fun onAdSwipeGestureClicked(adUnit: String, adType: AdType) {
                    callback?.onAdSwipeGestureClicked(adUnit, adType)
                }

                override fun onPaidValueListener(bundle: Bundle) {
                    callback?.onPaidValueListener(bundle)
                }
            }
        )
    }
}