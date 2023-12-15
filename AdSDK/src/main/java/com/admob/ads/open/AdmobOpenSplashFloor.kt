package com.admob.ads.open

import android.app.Activity
import android.os.Bundle

object AdmobOpenSplashFloor {

    fun show(
        activity: Activity,
        listId: List<String>,
        onLoadFailAll: () -> Unit,
        onPairValue: (Bundle) -> Unit,
        nextAction: () -> Unit
    ) {

        val tempList = mutableListOf<String>()
        tempList.addAll(listId)

        fun loadAd() {
            val id = tempList.first()
            AdmobOpen.load(id, onAdLoaded = {
                it.show(activity)
            }, onAdLoadFailure = {
                if (tempList.isNotEmpty()){
                    loadAd()
                } else {
                    onLoadFailAll()
                }
            })


        }


    }


}