package com.example.bloodpressuremonitorconnector.utils.bluetooth

import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob


/*
    Singleton container for BleManager
    This class is responsible for initializing and providing the BleManager instance to the app
    It also provides a cleanup method to release resources when the app is closed
*/

object BleContainer {
    private var bleManager: BleManager? = null

    fun initialize(context: Context) {
        if (bleManager == null) {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
            bleManager = BleManager(
                context = context.applicationContext, // Use application context
                bluetoothAdapter = bluetoothManager.adapter,
                coroutineScope = coroutineScope
            )
        }
    }

    fun getBleManager(): BleManager {
        return bleManager ?: throw IllegalStateException("BleManager not initialized")
    }

    fun cleanup() {
        Log.d("BleContainer", "Cleaning up BleManager")
        bleManager?.cleanup()
        bleManager = null
    }
}