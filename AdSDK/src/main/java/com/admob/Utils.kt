package com.admob

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
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
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdValue


val displayMetrics: DisplayMetrics get() = Resources.getSystem().displayMetrics


val screenWidth: Int get() = Resources.getSystem().displayMetrics.widthPixels


val screenHeight: Int get() = Resources.getSystem().displayMetrics.heightPixels


val adaptiveBannerSize: AdSize
    get() {
        val adWidth = (screenWidth / displayMetrics.density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(AdsSDK.app, adWidth)
    }


fun trackingAdValue(adValue: AdValue, adId: String, adType: String, mediationClazz: String?) {
    val value = (adValue.valueMicros / 1000000.0).toString()
    val getCurrencyCode = adValue.currencyCode
    val precisionType = (adValue.precisionType).toString()

    logParams("AdValue") {
        param("adId", adId)
        param("adType", adType)
        param("value", value)
        param("getCurrencyCode", getCurrencyCode)
        param("precisionType", precisionType)
        param("mediationClazz", mediationClazz ?: "")
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
    if (duration > 0){
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
    if (duration > 0){
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



fun AppCompatActivity?.runIfResuming(block : () -> Unit) {
    if (this?.lifecycle?.currentState == Lifecycle.State.RESUMED){
        block.invoke()
    }
}


fun ViewGroup.addLoadingView(){
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