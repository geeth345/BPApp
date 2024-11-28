package com.example.bloodpressuremonitorconnector

import android.app.Application
import android.util.Log
import com.example.bloodpressuremonitorconnector.utils.BleContainer

// BloodPressureMonitorApplication.kt
class BloodPressureMonitorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("BloodPressureMonitorApplication", "onCreate")
        Log.d("BloodPressureMonitorApplication", "Initializing BleContainer")
        BleContainer.initialize(this)
    }
}