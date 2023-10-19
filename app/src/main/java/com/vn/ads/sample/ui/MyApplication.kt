package com.vn.ads.sample.ui

import android.app.Application
import android.os.Process
import android.util.Log

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.e("ThoNH","MyApplication onCreate: ${Process.myPid()}")

    }

}