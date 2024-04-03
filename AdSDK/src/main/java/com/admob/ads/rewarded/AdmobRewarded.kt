package com.admob.ads.rewarded

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.admob.AdFormat
import com.admob.AdType
import com.admob.TAdCallback
import com.admob.ads.AdsSDK
import com.admob.getPaidTrackingBundle
import com.admob.isEnable
import com.admob.isNetworkAvailable
import com.admob.ui.dialogs.DialogShowLoadingAds
import com.admob.waitActivityResumed
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback


object AdmobRewarded {

    /**
     * @param activity: Show on this activity
     * @param adUnitId: adUnitId
     * @param callBack
     * @param onUserEarnedReward
     * @param onFailureUserNotEarn
     */
    fun show(
        activity: AppCompatActivity,
        space: String,
        isShowDefaultLoadingDialog: Boolean = true,
        callBack: TAdCallback? = null,
        onFailureUserNotEarn: () -> Unit = {},
        onUserEarnedReward: () -> Unit
    ) {

        val adChild = AdsSDK.getAdChild(space) ?: return
        val  adUnitId = adChild.adsId

        if (!AdsSDK.isEnableRewarded){
            onUserEarnedReward.invoke()
            return
        }

        if (!AdsSDK.app.isNetworkAvailable() || AdsSDK.isPremium  || (adChild.adsType != AdFormat.Reward) || !AdsSDK.app.isNetworkAvailable() || !adChild.isEnable()) {
            onUserEarnedReward.invoke()
            return
        }


        var dialog : DialogShowLoadingAds? = null

        if (isShowDefaultLoadingDialog){
            dialog = DialogShowLoadingAds(activity).apply { show() }
        }


        AdsSDK.adCallback.onAdStartLoading(adUnitId, AdType.Rewarded)
        callBack?.onAdStartLoading(adUnitId, AdType.Native)

        RewardedAd.load(
            AdsSDK.app,
            adUnitId,
            AdsSDK.defaultAdRequest(),
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("ThoNH-1","onAdFailedToLoad")
                    super.onAdFailedToLoad(error)
                    AdsSDK.adCallback.onAdFailedToLoad(adUnitId, AdType.Rewarded, error)
                    callBack?.onAdFailedToLoad(adUnitId, AdType.Rewarded, error)
                    onFailureUserNotEarn.invoke()
                    dialog?.dismiss()
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    Log.e("ThoNH-1","onAdLoaded")
                    super.onAdLoaded(rewardedAd)
                    AdsSDK.adCallback.onAdLoaded(adUnitId, AdType.Rewarded)
                    callBack?.onAdLoaded(adUnitId, AdType.Rewarded)

                    rewardedAd.setOnPaidEventListener { adValue ->
                        val bundle = getPaidTrackingBundle(adValue, adUnitId, "Rewarded", rewardedAd.responseInfo)
                        AdsSDK.adCallback.onPaidValueListener(bundle)
                        callBack?.onPaidValueListener(bundle)
                    }

                    rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdClicked() {
                            Log.e("ThoNH-1","onAdClicked")
                            super.onAdClicked()
                            AdsSDK.adCallback.onAdClicked(adUnitId, AdType.Rewarded)
                            callBack?.onAdClicked(adUnitId, AdType.Rewarded)
                        }

                        override fun onAdDismissedFullScreenContent() {
                            Log.e("ThoNH-1","onAdDismissedFullScreenContent")
                            super.onAdDismissedFullScreenContent()
                            AdsSDK.adCallback.onAdDismissedFullScreenContent(adUnitId, AdType.Rewarded)
                            callBack?.onAdDismissedFullScreenContent(adUnitId, AdType.Rewarded)
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            Log.e("ThoNH-1","onAdFailedToShowFullScreenContent")
                            super.onAdFailedToShowFullScreenContent(error)
                            AdsSDK.adCallback.onAdFailedToShowFullScreenContent(adUnitId,  error.message,AdType.Rewarded)
                            callBack?.onAdFailedToShowFullScreenContent(adUnitId,  error.message, AdType.Rewarded)
                            onFailureUserNotEarn.invoke()
                        }

                        override fun onAdImpression() {
                            Log.e("ThoNH-1","onAdImpression")
                            super.onAdImpression()
                            AdsSDK.adCallback.onAdImpression(adUnitId, AdType.Rewarded)
                            callBack?.onAdImpression(adUnitId, AdType.Rewarded)
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.e("ThoNH-1","onAdShowedFullScreenContent")
                            super.onAdShowedFullScreenContent()
                            AdsSDK.adCallback.onAdShowedFullScreenContent(adUnitId, AdType.Rewarded)
                            callBack?.onAdShowedFullScreenContent(adUnitId, AdType.Rewarded)
                        }
                    }

                    activity.waitActivityResumed  {
                        dialog?.dismiss()
                        rewardedAd.show(activity) { _ ->
                            Log.e("ThoNH-1","onUserEarnedReward")
                            onUserEarnedReward.invoke()
                        }
                    }
                }
            }
        )
    }

}