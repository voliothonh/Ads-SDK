package com.admob

import android.util.Log
import com.admob.ads.AdsSDK
import com.admob.ads.open.AdmobOpenResume
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.ParametersBuilder
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase


private val tracker get() = Firebase.analytics


// Todo bốc ra ngoài app, ko để trong module
fun logAdClicked(adType: AdType, adID: String? = null) {
    logParams("ad_click_custom") {

        val clazz = AdsSDK.getClazzOnTop()

        if (clazz != null){
            runCatching { param("screen", clazz::class.java.simpleName) }

            val adFormat = when (adType) {
                AdType.OpenApp -> {
                    if (adID != null && adID == AdmobOpenResume.adUnitId) {
                        "ad_open_ads_resume"
                    } else {
                        "ad_open_ads"
                    }
                }
                AdType.Inter -> "ad_interstitial"
                AdType.Banner -> "ad_banner"
                AdType.Native -> "ad_native"
                AdType.Rewarded -> "ad_rewarded"
            }

            runCatching { param("ad_format", adFormat) }
        }

    }
}

fun logAdImpression(adTag: String) {
    AdsSDK.getClazzOnTop()?.let {
        logParams(adTag + "_impression") {
            param("screen", "$it")
        }
    }
}

fun logEvent(evenName: String) {
    val result = evenName.trim().replace("-", "_")

    Log.e("Tracking", "logEvent: $evenName")

    tracker.logEvent(result, null)
}

fun logScreen(screenName: String) {
    val result = screenName.trim().replace("-", "_")

    Log.e("Tracking", "logScreen: $screenName")

    tracker.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
        param(FirebaseAnalytics.Param.SCREEN_NAME, result)
    }
}

fun logParams(eventName: String, block: ParametersBuilder.() -> Unit) {

    Log.e("Tracking", "logParams: $eventName")

    runCatching {
        val result = eventName.trim().replace("-", "_")
        tracker.logEvent(result) {
            block()

            this.bundle.keySet().forEach {
                Log.e("Tracking", "param: [$it = ${this.bundle.get(it)}]")
            }
        }
    }
}
