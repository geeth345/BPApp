package com.example.bloodpressuremonitorconnector.utils.models

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object ModelsContainer {
    private var modelsManager: ModelsManager? = null

    fun initialize(context: Context) {
        if (modelsManager == null) {
            modelsManager = ModelsManager(
                context = context.applicationContext,)
        }
    }

    fun getModelsManager(): ModelsManager {
        return modelsManager ?: throw IllegalStateException("ModelsManager not initialized")
    }

    fun cleanup() {
        Log.d("ModelsContainer", "Cleaning up ModelsManager")
        modelsManager?.cleanup()
        modelsManager = null
    }


}