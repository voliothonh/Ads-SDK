package com.admob.cmp

import android.app.Activity
import android.content.Context
import androidx.preference.PreferenceManager
import com.admob.ads.AdsSDK
import com.admob.ui.dialogs.DialogShowLoadingFormConsent
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object GDPRUtils {

    var isUserConsent = true
        private set

    val canShowAd: Boolean
        get() = canShowAds(AdsSDK.app)


    fun showCMP(activity: Activity) {
        val loading = DialogShowLoadingFormConsent(activity)
        loading.show()
        UserMessagingPlatform.loadConsentForm(activity, {
            it.show(activity) {
                loading.dismiss()
            }
        }, {
            loading.dismiss()
        })
    }


    fun showCMP(activity: Activity, isTesting: Boolean = false, onDone: () -> Unit) {
        val loading = DialogShowLoadingFormConsent(activity)
        loading.show()
        if (!isGDPR(activity.application)) {
            isUserConsent = true
            onDone.invoke()
            loading.dismiss()
            return
        }

        val debugSettings = ConsentDebugSettings.Builder(activity)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .addTestDeviceHashedId(Utils.getDeviceID(activity)).build()

        val params = ConsentRequestParameters.Builder().setTagForUnderAgeOfConsent(false)
            .setConsentDebugSettings(if (isTesting) debugSettings else null).build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        consentInformation.requestConsentInfoUpdate(activity, params, {

            val canRequestAds = canShowAds(activity)

            if (canRequestAds) {
                isUserConsent = true
                onDone.invoke()
                loading.dismiss()
            } else {
                UserMessagingPlatform.loadConsentForm(activity, {
                    it.show(activity) {
                        if (canShowAd) {
                            isUserConsent = true
                            onDone.invoke()
                            loading.dismiss()
                        } else {
                            isUserConsent = false
                            onDone.invoke()
                            loading.dismiss()
                        }
                    }
                }, {
                    onDone.invoke()
                    loading.dismiss()
                })
            }
        }, { _ ->
            onDone.invoke()
            loading.dismiss()
        })
    }


    fun isGDPR(applicationContext: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val gdpr = prefs.getInt("IABTCF_gdprApplies", 1)
        return gdpr == 1
    }

    private fun canShowAds(applicationContext: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        //https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/TCFv2/IAB%20Tech%20Lab%20-%20CMP%20API%20v2.md#in-app-details
        //https://support.google.com/admob/answer/9760862?hl=en&ref_topic=9756841

        val purposeConsent = prefs.getString("IABTCF_PurposeConsents", "") ?: ""
        val vendorConsent = prefs.getString("IABTCF_VendorConsents", "") ?: ""
        val vendorLI = prefs.getString("IABTCF_VendorLegitimateInterests", "") ?: ""
        val purposeLI = prefs.getString("IABTCF_PurposeLegitimateInterests", "") ?: ""

        val googleId = 755
        val hasGoogleVendorConsent = hasAttribute(vendorConsent, index = googleId)
        val hasGoogleVendorLI = hasAttribute(vendorLI, index = googleId)

        // Minimum required for at least non-personalized ads
        return hasConsentFor(
            listOf(1), purposeConsent, hasGoogleVendorConsent
        ) && hasConsentOrLegitimateInterestFor(
            listOf(2, 7, 9, 10),
            purposeConsent,
            purposeLI,
            hasGoogleVendorConsent,
            hasGoogleVendorLI
        )

    }

    // Check if a binary string has a "1" at position "index" (1-based)
    private fun hasAttribute(input: String, index: Int): Boolean {
        return input.length >= index && input[index - 1] == '1'
    }

    // Check if consent is given for a list of purposes
    private fun hasConsentFor(
        purposes: List<Int>, purposeConsent: String, hasVendorConsent: Boolean
    ): Boolean {
        return purposes.all { p -> hasAttribute(purposeConsent, p) } && hasVendorConsent
    }

    // Check if a vendor either has consent or legitimate interest for a list of purposes
    private fun hasConsentOrLegitimateInterestFor(
        purposes: List<Int>,
        purposeConsent: String,
        purposeLI: String,
        hasVendorConsent: Boolean,
        hasVendorLI: Boolean
    ): Boolean {
        return purposes.all { p ->
            (hasAttribute(purposeLI, p) && hasVendorLI) || (hasAttribute(
                purposeConsent, p
            ) && hasVendorConsent)
        }
    }
}