package com.admob

import android.os.Bundle
import com.google.android.gms.ads.LoadAdError
import com.google.gson.annotations.SerializedName

interface TAdCallback {
    fun onAdStartLoading(adUnit: String, adType: AdType) {}
    fun onAdClicked(adUnit: String, adType: AdType) {}
    fun onAdClosed(adUnit: String, adType: AdType) {}
    fun onAdDismissedFullScreenContent(adUnit: String, adType: AdType) {}
    fun onAdShowedFullScreenContent(adUnit: String, adType: AdType) {}
    fun onAdFailedToShowFullScreenContent(error : String, adUnit: String, adType: AdType) {}
    fun onAdFailedToLoad(adUnit: String, adType: AdType, error: LoadAdError) {}
    fun onAdImpression(adUnit: String, adType: AdType) {}
    fun onAdLoaded(adUnit: String, adType: AdType) {}
    fun onAdOpened(adUnit: String, adType: AdType) {}
    fun onAdSwipeGestureClicked(adUnit: String, adType: AdType) {}
    fun onPaidValueListener(bundle : Bundle) {}
    fun onSetInterFloorId(){} /*When application resume, some device init all member variable, pls call set InterFloor.setId*/
    fun onDisable(){}
}

enum class AdType {
    OpenApp, Inter, Banner, Native, Rewarded
}

data class AdData(
    val adUnitId: String,
    val adType: String,
    val adSpace: String,
)

class AdsChild {
    @SerializedName("spaceName")
    var spaceName:String = "null"

    @SerializedName("adsType")
    var adsType:String = "null"

    @SerializedName("id")
    var adsId:String = "null"

    @SerializedName("isEnable")
    var isEnable : String = "enable"

    override fun toString(): String {
        return "AdsChild spaceName='$spaceName', adsType='$adsType', adsId='$adsId' ,$isEnable )"
    }
}

class Ads {
    @SerializedName("listAds")
    var listAdsChild:ArrayList<AdsChild> = ArrayList()
    override fun toString(): String {
        return "Ads(listAdsChild=$listAdsChild)"
    }

}

fun AdsChild.isEnable() = this.isEnable == "enable"