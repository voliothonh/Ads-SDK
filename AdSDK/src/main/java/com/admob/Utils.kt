package com.admob

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.NavHostFragment
import com.admob.ads.AdsSDK
import com.admob.ads.databinding.AdLoadingViewBinding
import com.admob.ads.open.AdmobOpenResume
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.ResponseInfo
import java.util.Locale


val displayMetrics: DisplayMetrics get() = Resources.getSystem().displayMetrics


val screenWidth: Int get() = Resources.getSystem().displayMetrics.widthPixels


val screenHeight: Int get() = Resources.getSystem().displayMetrics.heightPixels


val adaptiveBannerSize: AdSize
    get() {
        val adWidth = (screenWidth / displayMetrics.density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(AdsSDK.app, adWidth)
    }

fun logAdError(adError: String, adId: String, adType: AdType, loadingTime: Long) {

    val adFormat = when (adType) {
        AdType.OpenApp -> {
            if (adId == AdmobOpenResume.adUnitId) {
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

    logParams("ad_error") {
        param("ad_unit_id", adId)
        param("ad_format", adFormat)
        param("loading_time", "$loadingTime")
        param("error_reason", adError.replace(" ", "_").take(100))
        param("country_code", Locale.getDefault().country)
        param("network_type", getNetwork())
    }
}

fun getPaidTrackingBundle(
    adValue: AdValue,
    adId: String,
    adType: String,
    responseInfo: ResponseInfo?
): Bundle {
    return Bundle().apply {

        val loadingTime = AdsSDK.getAdLoadingTime(adId)

        putString("ad_unit_id", adId)
        putString("ad_type", adType)
        putString("revenue_micros", "${adValue.valueMicros}")
        putString("currency_code", adValue.currencyCode)
        putString("precision_type", "${adValue.precisionType}")
        putString("loading_time", "$loadingTime")
        putString("country_code", Locale.getDefault().country)
        putString("network_type", getNetwork())
        val adapterResponseInfo = responseInfo?.loadedAdapterResponseInfo

        adapterResponseInfo?.let {
            putString("ad_source_id", it.adSourceId)
            putString("ad_source_name", it.adSourceName)
        }
    }
}

fun Activity.waitingResume(block: () -> Unit) {
    if (this is AppCompatActivity) {
        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
            block.invoke()
            return
        }

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                lifecycle.removeObserver(this)
                delay(1500) { block.invoke() }
            }
        })
        return
    }
    block.invoke()
}

fun Activity.waitingResumeNoDelay(block: () -> Unit) {
    if (this is AppCompatActivity) {
        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
            delay(0) { block.invoke() }
            return
        }

        lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                lifecycle.removeObserver(this)
            }

//            override fun onPause(owner: LifecycleOwner) {
//                super.onPause(owner)
//                lifecycle.removeObserver(this)
//                lifecycle.addObserver(this)
//            }

            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                lifecycle.removeObserver(this)
                delay(0) {
                    block.invoke()
                }
            }
        })
        return
    }
//    block.invoke()
}

fun Activity.avoidShowWhenResume(block: () -> Unit) {
    if (this is AppCompatActivity) {
        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
            block.invoke()
            return
        }
    }
}

fun delay(duration: Int, block: () -> Unit) {
    if (duration > 0) {
        safeRun {
            Handler()
                .postDelayed(
                    { block.invoke() },
                    duration.toLong()
                )
        }
        return
    }
    block.invoke()
}

fun delay(duration: Long, block: () -> Unit) {
    if (duration > 0) {
        safeRun {
            Handler()
                .postDelayed(
                    { block.invoke() },
                    duration
                )
        }
        return
    }
    block.invoke()
}


fun safeRun(block: () -> Unit) {
    try {
        block.invoke()
    } catch (throwable: Throwable) {
        throwable.printStackTrace()
    }
}


fun Context.isInternetConnected(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetwork != null && cm.getNetworkCapabilities(cm.activeNetwork) != null
}


fun AppCompatActivity?.runIfResuming(block: () -> Unit) {
    if (this?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
        block.invoke()
    }
}


fun ViewGroup.addLoadingView() {
    val view = AdLoadingViewBinding
        .inflate(LayoutInflater.from(context), null, false)
        .root

    removeAllViews()
    addView(view)
    view.requestLayout()
}


/**
 * Trả về liệu fragment này có phải là NavigationHostFragment hay không
 */
private fun Fragment.isNavHostFragment(): Boolean {
    return this is NavHostFragment && childFragmentManager.fragments.size == 1
}


fun AdsSDK.getActivityOnTop(): Activity? {
    return activities.findLast { !it.isFinishing }
}

fun AdsSDK.getAppCompatActivityOnTop(): AppCompatActivity? {
    return activities.findLast { it is AppCompatActivity && !it.isFinishing } as? AppCompatActivity?
}


/**
 * Trả về Clazz đang hiển thị trên màn hình
 * Nếu AppCompat == null => Return null
 * Nếu Có NavHostFragment => Trả ra fragment đầu tiên trong Host
 * Nếu là appCompatActivity => Trả ra clazz của activity đó
 */
fun AdsSDK.getClazzOnTop(): Class<*>? {
    val activity = AdsSDK.getAppCompatActivityOnTop() ?: return null

    activity
        .supportFragmentManager
        .fragments
        .forEach {
            if (it.isNavHostFragment()) {
                return it.childFragmentManager.fragments[0].javaClass
            }
        }

    return activity.javaClass
}


fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    val network: Network? = connectivityManager!!.activeNetwork
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
    return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

fun AppCompatActivity.waitActivityResumed(onResumed: () -> Unit) {
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            lifecycle.removeObserver(this)
            onResumed.invoke()
        }
    })
}


fun AppCompatActivity.waitActivityStop(onStopped: () -> Unit) {
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            lifecycle.removeObserver(this)
            onStopped.invoke()
        }
    })
}


fun AppCompatActivity.waitActivityDestroy(onDestroy: () -> Unit) {
    lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            lifecycle.removeObserver(this)
            onDestroy.invoke()
        }
    })
}


fun onNextActionWhenResume(nextAction: () -> Unit) {
    AdsSDK.getAppCompatActivityOnTop()?.waitActivityResumed {
        nextAction.invoke()
    }
}


fun adLogger(
    adType: AdType,
    adUnitId: String,
    message: String
) {
    Log.i("AdsSDK.ThoNH.[$adType]", "[$adUnitId] => $message")
}


@SuppressLint("MissingPermission")
fun getNetwork(): String {
    val connectivityManager =
        AdsSDK.app.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    val nw = connectivityManager.activeNetwork ?: return "-"
    val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return "-"
    when {
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return "WIFI"
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return "ETHERNET"
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
            val tm = AdsSDK.app.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            when (tm.dataNetworkType) {
                TelephonyManager.NETWORK_TYPE_GPRS,
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_CDMA,
                TelephonyManager.NETWORK_TYPE_1xRTT,
                TelephonyManager.NETWORK_TYPE_IDEN,
                TelephonyManager.NETWORK_TYPE_GSM -> return "2G"

                TelephonyManager.NETWORK_TYPE_UMTS,
                TelephonyManager.NETWORK_TYPE_EVDO_0,
                TelephonyManager.NETWORK_TYPE_EVDO_A,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_EVDO_B,
                TelephonyManager.NETWORK_TYPE_EHRPD,
                TelephonyManager.NETWORK_TYPE_HSPAP,
                TelephonyManager.NETWORK_TYPE_TD_SCDMA -> return "3G"

                TelephonyManager.NETWORK_TYPE_LTE,
                TelephonyManager.NETWORK_TYPE_IWLAN, 19 -> return "4G"

                TelephonyManager.NETWORK_TYPE_NR -> return "5G"
                else -> return "_"
            }
        }

        else -> return "?"
    }
}