package com.example.bloodpressuremonitorconnector

import android.app.Application
import android.util.Log
import com.example.bloodpressuremonitorconnector.utils.BleContainer
import com.example.bloodpressuremonitorconnector.utils.SettingsContainer

// BloodPressureMonitorApplication.kt
class BloodPressureMonitorApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("BloodPressureMonitorApplication", "onCreate")
        Log.d("BloodPressureMonitorApplication", "Initializing BleContainer")
        BleContainer.initialize(this)
        Log.d("BloodPressureMonitorApplication", "BleContainer initialized")
        Log.d("BloodPressureMonitorApplication", "Initialzing SettingsManager")
        SettingsContainer.initialize(this)
        Log.d("BloodPressureMonitorApplication", "SettingsManager initialised")
        Log.d("BloodPressureMonitorApplication", "Initialisation complete")

    }
}