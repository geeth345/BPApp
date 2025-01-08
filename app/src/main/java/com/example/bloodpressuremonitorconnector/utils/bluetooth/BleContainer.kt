package com.example.bloodpressuremonitorconnector.utils.bluetooth

import android.bluetooth.BluetoothManager
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

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
        bleManager?.cleanup()
        bleManager = null
    }
}