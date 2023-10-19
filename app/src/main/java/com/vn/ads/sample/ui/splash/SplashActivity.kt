package com.vn.ads.sample.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.admob.delay
import com.vn.ads.sample.R
import com.vn.ads.sample.ui.main.MainActivity2

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        delay(2000){
            startActivity(Intent(this, MainActivity2::class.java))
        }
    }
}