package com.admob.ads.nativead

import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.admob.AdType
import com.admob.TAdCallback
import com.google.android.gms.ads.LoadAdError

object AdmobNativeFloor {

    fun showNative(
        adUnitIdHigh: String,
        adUnitIdMedium: String,
        adUnitIdLow: String,
        adContainer: ViewGroup,
        @LayoutRes nativeContentLayoutId: Int,
        forceRefresh: Boolean = false,
        callback: TAdCallback? = null
    ) {

        var currAdUnitId = adUnitIdHigh

        fun showNative(adUnitId: String) {
            showNative(
                adContainer = adContainer,
                adUnitId = adUnitId,
                nativeContentLayoutId = nativeContentLayoutId,
                forceRefresh = forceRefresh,
                callback = callback,
                onLoadFailure = {
                    if (currAdUnitId == adUnitIdHigh) {
                        currAdUnitId = adUnitIdMedium
                    } else if (currAdUnitId == adUnitIdMedium) {
                        currAdUnitId = adUnitIdLow
                    }

                    showNative(currAdUnitId)

                }
            )
        }

        showNative(currAdUnitId)

    }

    private fun showNative(
        adContainer: ViewGroup,
        adUnitId: String,
        @LayoutRes nativeContentLayoutId: Int,
        forceRefresh: Boolean = false,
        callback: TAdCallback? = null,
        onLoadFailure: () -> Unit,
    ) {
        AdmobNative.show(
            adContainer,
            adUnitId,
            nativeContentLayoutId,
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