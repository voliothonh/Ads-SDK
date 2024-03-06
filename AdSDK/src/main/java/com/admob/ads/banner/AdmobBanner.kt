package com.admob.ads.banner

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.admob.AdType
import com.admob.TAdCallback
import com.admob.adaptiveBannerSize
import com.admob.addLoadingView
import com.admob.ads.AdsSDK
import com.admob.ads.AdsSDK.isEnableBanner
import com.admob.getPaidTrackingBundle
import com.admob.isNetworkAvailable
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.firebase.crashlytics.FirebaseCrashlytics

object AdmobBanner {

    private const val TAG = "AdmobBanner"

    private val banners = mutableMapOf<String, AdView?>()

    /**
     * @param adContainer: ViewGroup contain this Ad
     * @param adUnitId AdId
     * @param forceRefresh always load new ad then fill to ViewGroup
     * @param callback callback
     */
    fun showAdaptive(
        adContainer: ViewGroup,
        adUnitId: String,
        forceRefresh: Boolean = false,
        callback: TAdCallback? = null
    ) = show(
        null,
        adContainer,
        adUnitId,
        BannerAdSize.BannerAdaptive,
        forceRefresh,
        callback,
    )


    /**
     * @param adContainer: ViewGroup contain this Ad
     * @param adUnitId AdId
     * @param forceRefresh always load new ad then fill to ViewGroup
     * @param callback callback
     */
    fun show300x250(
        adContainer: ViewGroup,
        adUnitId: String,
        forceRefresh: Boolean = false,
        callback: TAdCallback? = null
    ) = show(
        null,
        adContainer,
        adUnitId,
        BannerAdSize.Banner300x250,
        forceRefresh,
        callback,
    )

    /**
     * Each position show be Unique AdUnitID
     * @param adContainer: ViewGroup contain this Ad
     * @param adUnitId AdId
     * @param showOnBottom: Show on Top or Bottom
     * @param forceRefresh always load new ad then fill to ViewGroup
     * @param callback callback
     */
    fun showCollapsible(
        lifecycle: Lifecycle? = null,
        adContainer: ViewGroup,
        adUnitId: String,
        showOnBottom: Boolean = true,
        forceRefresh: Boolean = false,
        callback: TAdCallback? = null
    ) = show(
        lifecycle,
        adContainer,
        adUnitId,
        if (showOnBottom) BannerAdSize.BannerCollapsibleBottom else BannerAdSize.BannerCollapsibleTop,
        forceRefresh,
        callback,
    )

    private fun show(
        lifecycle: Lifecycle? = null,
        adContainer: ViewGroup,
        adUnitId: String,
        bannerType: BannerAdSize,
        forceRefresh: Boolean = false,
        callback: TAdCallback? = null
    ) {

        if (!isEnableBanner) {
            adContainer.removeAllViews()
            adContainer.isVisible = false
            return
        }


        val adSize = getAdSize(bannerType)
        addLoadingLayout(adContainer, adSize)

        if (!adContainer.context.isNetworkAvailable()) {
            return
        }

        val adView = banners[adUnitId]


        if (adView == null || forceRefresh) {

            val context =
                if (bannerType == BannerAdSize.BannerCollapsibleBottom || bannerType == BannerAdSize.BannerCollapsibleTop)
                    adContainer.context
                else
                    AdsSDK.app

            AdView(context).let {
                it.adUnitId = adUnitId
                it.setAdSize(adSize)
                it.setAdCallback(it, bannerType, callback) {
                    addExistBanner(lifecycle, adContainer, it)
                }
                it.loadAd(getAdRequest(bannerType))

                AdsSDK.adCallback.onAdStartLoading(adUnitId, AdType.Banner)
                callback?.onAdStartLoading(adUnitId, AdType.Banner)
            }
        }

        if (adView != null) {
            addExistBanner(lifecycle, adContainer, adView)
            adView.setAdCallback(adView, bannerType, callback) {
                addExistBanner(lifecycle, adContainer, adView)
            }
            return
        }

    }


    private fun addExistBanner(
        lifecycle: Lifecycle?,
        adContainer: ViewGroup,
        bannerView: AdView
    ) {
        adContainer.removeAllViews()
        if (bannerView.parent is ViewGroup && bannerView.parent != null) {
            (bannerView.parent as ViewGroup).removeAllViews()
        }
        adContainer.addView(bannerView)

        lifecycle?.addObserver(object : DefaultLifecycleObserver {

            override fun onStop(owner: LifecycleOwner) {
                super.onStop(owner)
                Log.e("DucLH----","onStop")
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                bannerView.destroy()
                Log.e("DucLH----","onDestroy")
                lifecycle.removeObserver(this)
                adContainer.removeAllViews()
                bannerView.removeAllViews()
            }
        })
    }

    private fun addLoadingLayout(adContainer: ViewGroup, adSize: AdSize) {
        val lp = adContainer.layoutParams
        lp.width = adSize.getWidthInPixels(adContainer.context)
        lp.height = adSize.getHeightInPixels(adContainer.context)
        adContainer.layoutParams = lp
        adContainer.requestLayout()
        adContainer.addLoadingView()
    }

    private fun getAdSize(bannerAdSize: BannerAdSize): AdSize {
        return when (bannerAdSize) {
            BannerAdSize.BannerAdaptive -> adaptiveBannerSize
            BannerAdSize.BannerCollapsibleTop -> adaptiveBannerSize
            BannerAdSize.BannerCollapsibleBottom -> adaptiveBannerSize
            BannerAdSize.Banner300x250 -> AdSize.MEDIUM_RECTANGLE
        }
    }

    private fun getAdRequest(bannerAdSize: BannerAdSize): AdRequest {
        val adRequestBuilder = AdRequest.Builder()
        val extras = Bundle()

        if (bannerAdSize == BannerAdSize.BannerCollapsibleTop) {
            extras.putString("collapsible", "top")
            adRequestBuilder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        }

        if (bannerAdSize == BannerAdSize.BannerCollapsibleBottom) {
            extras.putString("collapsible", "bottom")
            adRequestBuilder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        }

        return adRequestBuilder.build()
    }

    private fun AdView.setAdCallback(
        adView: AdView,
        bannerType: BannerAdSize,
        tAdCallback: TAdCallback?,
        onAdLoaded: () -> Unit
    ) {
        adListener = object : AdListener() {
            override fun onAdClicked() {
                AdsSDK.adCallback.onAdClicked(adUnitId, AdType.Banner)
                tAdCallback?.onAdClicked(adUnitId, AdType.Banner)
            }

            override fun onAdClosed() {
                AdsSDK.adCallback.onAdClosed(adUnitId, AdType.Banner)
                tAdCallback?.onAdClosed(adUnitId, AdType.Banner)
            }

            override fun onAdFailedToLoad(var1: LoadAdError) {
                banners[adView.adUnitId] = null
                AdsSDK.adCallback.onAdFailedToLoad(adUnitId, AdType.Banner, var1)
                tAdCallback?.onAdFailedToLoad(adUnitId, AdType.Banner, var1)
                runCatching { Throwable(var1.message) }
            }

            override fun onAdImpression() {
                AdsSDK.adCallback.onAdImpression(adUnitId, AdType.Banner)
                tAdCallback?.onAdImpression(adUnitId, AdType.Banner)
            }

            override fun onAdLoaded() {
                AdsSDK.adCallback.onAdLoaded(adUnitId, AdType.Banner)
                tAdCallback?.onAdLoaded(adUnitId, AdType.Banner)
                adView.setOnPaidEventListener { adValue ->
                    val bundle =
                        getPaidTrackingBundle(adValue, adUnitId, "Banner", adView.responseInfo)
                    AdsSDK.adCallback.onPaidValueListener(bundle)
                    tAdCallback?.onPaidValueListener(bundle)
                }
                banners[adView.adUnitId]?.let {
                    if (bannerType == BannerAdSize.BannerCollapsibleTop || bannerType == BannerAdSize.BannerCollapsibleBottom) {
//                        it.destroy()
                    }
                }
                banners[adView.adUnitId] = adView

                onAdLoaded.invoke()
            }
        }
    }


    fun setEnableBanner(isEnable: Boolean) {
        if (!isEnable) {
            try {
                banners.forEach { (_, adView) ->
                    val viewGroup = adView?.parent as? ViewGroup
                    adView?.destroy()
                    viewGroup?.removeAllViews()
                    viewGroup?.isVisible = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }
}