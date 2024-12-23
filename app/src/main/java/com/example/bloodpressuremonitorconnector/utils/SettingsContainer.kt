package com.example.bloodpressuremonitorconnector.utils

import android.content.Context

object SettingsContainer {

    private var settingsManager: SettingsManager? = null

    fun initialize(context: Context) {
        if (settingsManager == null) {
            settingsManager = SettingsManager(context.applicationContext)
        }
    }
    
    fun getSettingsManager(): SettingsManager {
        return settingsManager ?: throw IllegalStateException("SettingsManager not initialized")
    }
    

}