package com.benkkstudio.beeconsentexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.benkkstudio.beeconsent.BeeConsent
import com.benkkstudio.beeconsent.BeeConsentCallback

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        BeeConsent.Builder(this)
            .debugMode(true)
            .enableLogging(true)
            .listener(object : BeeConsentCallback {
                override fun onRequested() {
                    // consent success requested
                }
            })
            .request()
    }
}