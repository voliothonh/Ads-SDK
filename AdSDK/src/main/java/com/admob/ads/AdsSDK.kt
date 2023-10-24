package com.admob.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.admob.ActivityActivityLifecycleCallbacks
import com.admob.AdType
import com.admob.TAdCallback
import com.admob.adLogger
import com.admob.ads.banner.AdmobBanner
import com.admob.ads.interstitial.AdmobInterResume
import com.admob.ads.nativead.AdmobNative
import com.admob.ads.open.AdmobOpenResume
import com.admob.delay
import com.admob.logAdClicked
import com.admob.logAdError
import com.admob.logParams
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.adrevenue.AppsFlyerAdRevenue
import com.appsflyer.api.PurchaseClient
import com.appsflyer.api.Store
import com.facebook.ads.AudienceNetworkAds
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds

object AdsSDK {

    internal lateinit var app: Application

    var isEnableBanner = true
        private set

    var isEnableNative = true
        private set

    var isEnableInter = true
        private set

    var isEnableOpenAds = true
        private set

    var isEnableRewarded = true
        private set

    private var autoLogPaidValueTrackingInSdk = false


    private var outsideAdCallback: TAdCallback? = null


    private var preventShowResumeAd = false


    internal var isEnableAppsflyer = false

    /**
     * Mark loading time
     *  [String] => AdUnitId
     *  [Long] => Start time loading
     *  [Long] => End time loading
     */
    private var adUnitLoadingTime = mutableMapOf<String, Pair<Long, Long>?>()

    val adCallback: TAdCallback = object : TAdCallback {

        override fun onAdStartLoading(adUnit: String, adType: AdType) {
            super.onAdStartLoading(adUnit, adType)
            adLogger(adType, adUnit, "onAdStartLoading")

            // Đánh dấu thời gian bắt đầu load quảng cáo
            markAdStartLoading(adUnit)
        }

        override fun onAdClicked(adUnit: String, adType: AdType) {
            super.onAdClicked(adUnit, adType)
            outsideAdCallback?.onAdClicked(adUnit, adType)
            adLogger(adType, adUnit, "onAdClicked")
            logAdClicked(adType, adUnit)
        }

        override fun onAdClosed(adUnit: String, adType: AdType) {
            super.onAdClosed(adUnit, adType)
            outsideAdCallback?.onAdClosed(adUnit, adType)
            adLogger(adType, adUnit, "onAdClosed")

            // Reset đánh dấu quảng cáo
            clearMarkLoadingTime(adUnit)
        }

        override fun onAdDismissedFullScreenContent(adUnit: String, adType: AdType) {
            super.onAdDismissedFullScreenContent(adUnit, adType)
            outsideAdCallback?.onAdDismissedFullScreenContent(adUnit, adType)
            adLogger(adType, adUnit, "onAdDismissedFullScreenContent")

            // Reset đánh dấu quảng cáo
            clearMarkLoadingTime(adUnit)
        }

        override fun onAdShowedFullScreenContent(adUnit: String, adType: AdType) {
            super.onAdShowedFullScreenContent(adUnit, adType)
            outsideAdCallback?.onAdShowedFullScreenContent(adUnit, adType)
            adLogger(adType, adUnit, "onAdShowedFullScreenContent")
        }

        override fun onAdFailedToShowFullScreenContent(error: String, adUnit: String, adType: AdType) {
            super.onAdFailedToShowFullScreenContent(error, adUnit, adType)
            outsideAdCallback?.onAdFailedToShowFullScreenContent(error, adUnit, adType)
            adLogger(adType, adUnit, "onAdFailedToShowFullScreenContent")

            markAdEndLoading(adUnit)
            val loadingTime = getAdLoadingTime(adUnit)
            logAdError(
                error,
                adUnit,
                adType,
                loadingTime
            )

            clearMarkLoadingTime(adUnit)
        }

        override fun onAdFailedToLoad(adUnit: String, adType: AdType, error: LoadAdError) {
            super.onAdFailedToLoad(adUnit, adType, error)
            outsideAdCallback?.onAdFailedToLoad(adUnit, adType, error)
            adLogger(adType, adUnit, "onAdFailedToLoad(${error.code} - ${error.message})")


            // Đánh dấu thời gian bắt đầu load xong quảng cáo (load bị lỗi)
            markAdEndLoading(adUnit)
            val loadingTime = getAdLoadingTime(adUnit)
            logAdError(
                error.message,
                adUnit,
                adType,
                loadingTime
            )

            clearMarkLoadingTime(adUnit)
        }

        override fun onAdImpression(adUnit: String, adType: AdType) {
            super.onAdImpression(adUnit, adType)
            outsideAdCallback?.onAdImpression(adUnit, adType)
            adLogger(adType, adUnit, "onAdImpression")
        }

        override fun onAdLoaded(adUnit: String, adType: AdType) {
            super.onAdLoaded(adUnit, adType)
            outsideAdCallback?.onAdLoaded(adUnit, adType)
            adLogger(adType, adUnit, "onAdLoaded")


            // Đánh dấu thời gian bắt đầu load xong quảng cáo (load bị lỗi)
            markAdEndLoading(adUnit)
        }

        override fun onAdOpened(adUnit: String, adType: AdType) {
            super.onAdOpened(adUnit, adType)
            outsideAdCallback?.onAdOpened(adUnit, adType)
            adLogger(adType, adUnit, "onAdOpened")
        }

        override fun onAdSwipeGestureClicked(adUnit: String, adType: AdType) {
            super.onAdSwipeGestureClicked(adUnit, adType)
            outsideAdCallback?.onAdSwipeGestureClicked(adUnit, adType)
            adLogger(adType, adUnit, "onAdSwipeGestureClicked")
        }

        override fun onPaidValueListener(bundle: Bundle) {
            super.onPaidValueListener(bundle)
            outsideAdCallback?.onPaidValueListener(bundle)

            if (autoLogPaidValueTrackingInSdk) {
                logParams("AdValue") {
                    bundle.keySet().forEach { key ->
                        val value = bundle.getString(key)
                        if (!value.isNullOrBlank()) {
                            param(key, value)
                        }
                    }
                }
            }
        }
    }

    val activities = mutableSetOf<Activity>()

    val clazzIgnoreAdResume = mutableListOf<Class<*>>()

    private val applicationStateObserver = object : DefaultLifecycleObserver {

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            if (preventShowResumeAd) {
                preventShowResumeAd = false
                return
            }
            AdmobInterResume.onInterAppResume()
            AdmobOpenResume.onOpenAdAppResume()
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
        }
    }

    private val activityLifecycleCallbacks = object : ActivityActivityLifecycleCallbacks() {
        override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
            super.onActivityCreated(activity, bundle)
            activities.add(activity)
        }

        override fun onActivityResumed(activity: Activity) {
            super.onActivityResumed(activity)
            activities.add(activity)
        }

        override fun onActivityDestroyed(activity: Activity) {
            super.onActivityDestroyed(activity)
            activities.remove(activity)
        }
    }

    fun init(application: Application): AdsSDK {
        app = application
        ProcessLifecycleOwner.get().lifecycle.addObserver(applicationStateObserver)
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks)

        delay(1000) {
            MobileAds.initialize(application)
            AudienceNetworkAds.initialize(application)
        }

        return this
    }

    fun enableAppsflyer(appsflyerId: String): AdsSDK {
        isEnableAppsflyer = true
        val afRevenueBuilder = AppsFlyerAdRevenue.Builder(app)
        AppsFlyerAdRevenue.initialize(afRevenueBuilder.build())
        AppsFlyerLib.getInstance().init(
            appsflyerId,
            object : AppsFlyerConversionListener {
                override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {
                    Log.i("AdsSDK", "AppsFlyer ==> onConversionDataSuccess($p0)")
                }

                override fun onConversionDataFail(p0: String?) {
                    Log.i("AdsSDK", "AppsFlyer ==> onConversionDataFail($p0)")
                }

                override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                    Log.i("AdsSDK", "AppsFlyer ==> onAppOpenAttribution($p0)")
                }

                override fun onAttributionFailure(p0: String?) {
                    Log.i("AdsSDK", "AppsFlyer ==> onAttributionFailure($p0")
                }
            },
            app
        )

        AppsFlyerLib.getInstance().setCollectAndroidID(true)
        AppsFlyerLib.getInstance().setCollectIMEI(true)
        AppsFlyerLib.getInstance().setCollectOaid(true)
        AppsFlyerLib.getInstance().start(app)

        val builder = PurchaseClient.Builder(app, Store.GOOGLE)
        val afPurchaseClient = builder.build()
        afPurchaseClient.startObservingTransactions()
        builder.logSubscriptions(true)
        builder.autoLogInApps(true)


        if (BuildConfig.DEBUG) {
            AppsFlyerLib.getInstance().setDebugLog(true)
        }
        return this
    }

    fun setAdCallback(callback: TAdCallback): AdsSDK {
        outsideAdCallback = callback
        return this
    }

    fun setIgnoreAdResume(vararg clazz: Class<*>): AdsSDK {
        clazzIgnoreAdResume.clear()
        clazzIgnoreAdResume.add(AdActivity::class.java)
        clazzIgnoreAdResume.addAll(clazz)
        return this
    }

    fun preventShowResumeAdNextTime() {
        preventShowResumeAd = true
    }


    fun setEnableBanner(isEnable: Boolean) {
        isEnableBanner = isEnable
        AdmobBanner.setEnableBanner(isEnable)
    }

    fun setEnableNative(isEnable: Boolean) {
        isEnableNative = isEnable
        AdmobNative.setEnableNative(isEnable)
    }

    fun setEnableInter(isEnable: Boolean) {
        isEnableInter = isEnable
    }

    fun setEnableOpenAds(isEnable: Boolean) {
        isEnableOpenAds = isEnable
    }

    fun setEnableRewarded(isEnable: Boolean) {
        isEnableRewarded = isEnable
    }

    fun setAutoTrackingPaidValueInSdk(useInSDK: Boolean) {
        autoLogPaidValueTrackingInSdk = useInSDK
    }

    internal fun defaultAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    private fun markAdStartLoading(adUnitId: String){
        adUnitLoadingTime[adUnitId] = Pair(SystemClock.elapsedRealtime(), 0)
    }

    private fun markAdEndLoading(adUnitId: String){
        val pairStartEndTime = adUnitLoadingTime[adUnitId]
        pairStartEndTime ?: return
        val startAdLoadingTime = pairStartEndTime.first
        adUnitLoadingTime[adUnitId] = Pair(startAdLoadingTime, SystemClock.elapsedRealtime())
    }

    internal fun getAdLoadingTime(adUnitId: String): Long {
        val pairStartEndTime = adUnitLoadingTime[adUnitId]
        pairStartEndTime ?: return -1

        val startAdLoadingTime = pairStartEndTime.first
        val endAdLoadingTime = pairStartEndTime.second

        if (startAdLoadingTime  <=0 || endAdLoadingTime <= 0){
            return -1
        }

        if (endAdLoadingTime - startAdLoadingTime <=0 ){
            return -1
        }

        return endAdLoadingTime - startAdLoadingTime
    }

    internal fun clearMarkLoadingTime(adUnitId : String){
        adUnitLoadingTime.remove(adUnitId)
    }

}



