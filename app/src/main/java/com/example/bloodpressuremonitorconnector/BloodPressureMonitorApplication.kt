package com.example.bloodpressuremonitorconnector

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.example.bloodpressuremonitorconnector.utils.bluetooth.BleContainer
import com.example.bloodpressuremonitorconnector.utils.SettingsContainer
import com.example.bloodpressuremonitorconnector.utils.models.ModelsContainer

// BloodPressureMonitorApplication.kt
class BloodPressureMonitorApplication : Application() {

    private var runningActivities = 0

    private fun isActivityRunning(): Boolean {
        return runningActivities > 0
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("BloodPressureMonitorApplication", "onCreate")
        Log.d("BloodPressureMonitorApplication", "Initializing BleContainer")
        BleContainer.initialize(this)
        Log.d("BloodPressureMonitorApplication", "BleContainer initialized")
        Log.d("BloodPressureMonitorApplication", "Initialzing SettingsManager")
        SettingsContainer.initialize(this)
        Log.d("BloodPressureMonitorApplication", "SettingsManager initialised")
        Log.d("BloodPressureMonitorApplication", "Initialising ModelsManager")
        ModelsContainer.initialize(this)
        Log.d("BloodPressureMonitorApplication", "ModelsManager initialised")
        Log.d("BloodPressureMonitorApplication", "Initialisation complete")

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                runningActivities++
                Log.d("BloodPressureMonitorApplication", "Activity created: ${activity.javaClass.simpleName}")
            }

            override fun onActivityStarted(activity: Activity) {
                Log.d("BloodPressureMonitorApplication", "Activity started: ${activity.javaClass.simpleName}")
            }

            override fun onActivityResumed(activity: Activity) {
                Log.d("BloodPressureMonitorApplication", "Activity resumed: ${activity.javaClass.simpleName}")
            }

            override fun onActivityPaused(activity: Activity) {
                Log.d("BloodPressureMonitorApplication", "Activity paused: ${activity.javaClass.simpleName}")
            }

            override fun onActivityStopped(activity: Activity) {
                Log.d("BloodPressureMonitorApplication", "Activity stopped: ${activity.javaClass.simpleName}")
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                Log.d("BloodPressureMonitorApplication", "Activity saved instance state: ${activity.javaClass.simpleName}")
            }

            override fun onActivityDestroyed(activity: Activity) {
                runningActivities--
                Log.d("BloodPressureMonitorApplication", "Activity destroyed: ${activity.javaClass.simpleName}")
                if (!isActivityRunning()) {
                    Log.d("BloodPressureMonitorApplication", "No activities running, cleaning up")
                    BleContainer.cleanup()
                }
            }
        })

    }
}