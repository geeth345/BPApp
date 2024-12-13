package com.example.bloodpressuremonitorconnector

import android.app.Application
import android.util.Log
import com.example.bloodpressuremonitorconnector.utils.BleContainer
import com.example.bloodpressuremonitorconnector.utils.SettingsManager

// BloodPressureMonitorApplication.kt
class BloodPressureMonitorApplication : Application() {

    lateinit var settingsManager: SettingsManager
        private set

    override fun onCreate() {
        super.onCreate()
        Log.d("BloodPressureMonitorApplication", "onCreate")
        Log.d("BloodPressureMonitorApplication", "Initializing BleContainer")
        BleContainer.initialize(this)
        Log.d("BloodPressureMonitorApplication", "BleContainer initialized")
        Log.d("BloodPressureMonitorApplication", "Initialsiing SettingsManager")
        settingsManager = SettingsManager(this)
    }
}