package com.example.bloodpressuremonitorconnector.utils

import android.content.Context
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log
import com.example.bloodpressuremonitorconnector.ui.setup.state.BleConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class BleManager(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
    private val coroutineScope: CoroutineScope
) {
    private val _connectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Initial)
    val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private val _sensorData = MutableStateFlow<Float>(0.0F)
    val sensorData: SharedFlow<Float> = _sensorData.asSharedFlow()


    private var bluetoothGatt: BluetoothGatt? = null
    private var isScanning = false
    private var isRetrying = false
    private var retryCount = 0
    private val MAX_RETRY_ATTEMPTS = 3


    object BleConstants {
        const val SERVICE_UUID = "a5c298c0-a235-4a32-a4e9-5b42f6bd50e5"
        const val SENSOR_CHAR_UUID = "a5c298c1-a235-4a32-a4e9-5b42f6bd50e5"
        const val DEVICE_NAME_PREFIX = "Group12"
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            try {
                if (result.device.name?.contains(BleConstants.DEVICE_NAME_PREFIX) == true) {
                    stopScan()
                    _connectionState.value = BleConnectionState.DeviceFound(result.device.address)
                    connectToDevice(result.device)
                }
            }
            catch (e: SecurityException) {
                _connectionState.value = BleConnectionState.Error("Permission denied")
            }

        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.d("BleManager", "Connection state changed - status: $status, newState: $newState")

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d("BleManager", "Connected to GATT server")
                    try {
                        // Request a higher connection priority for more stable connection
                        gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                        // Discover services immediately after connection
                        gatt.discoverServices()
                    } catch (e: SecurityException) {
                        Log.e("BleManager", "Security exception requesting connection priority", e)
                        _connectionState.value = BleConnectionState.Error("Permission denied")
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    when (status) {
                        19 -> { // GATT_CONN_TERMINATE_PEER_USER
                            if (retryCount < MAX_RETRY_ATTEMPTS && !isRetrying) {
                                Log.d("BleManager", "Device disconnected, retrying... Attempt ${retryCount + 1}")
                                retryConnection(gatt.device)
                            } else {
                                Log.d("BleManager", "Max retry attempts reached or already retrying")
                                _connectionState.value = BleConnectionState.Error("Device disconnected")
                            }
                        }
                        else -> {
                            Log.d("BleManager", "Disconnected with status: $status")
                            _connectionState.value = BleConnectionState.Error("Device disconnected (status: $status)")
                        }
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BleManager", "Services discovered successfully")
                // Reset retry count on successful service discovery
                retryCount = 0
                isRetrying = false
                setupGattServices(gatt)
            } else {
                Log.e("BleManager", "Service discovery failed with status: $status")
                _connectionState.value = BleConnectionState.Error("Service discovery failed")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            if (characteristic.uuid.toString().equals(BleConstants.SENSOR_CHAR_UUID, ignoreCase = true)) {
                // Convert byte array to Int16 array, since board sends 16-bit samples
                val samples = value.toShortArray()
                samples.forEach { sample ->
                    val voltage = (sample.toFloat())
                    coroutineScope.launch {
                        _sensorData.emit(voltage)
                    }
                }
                Log.d("BleManager", "Received ${samples.size} samples")
            }
        }
    }

    private fun ByteArray.toShortArray(): ShortArray {
        val shorts = ShortArray(size / 2)
        for (i in shorts.indices) {
            shorts[i] = ((this[i * 2 + 1].toInt() shl 8) or (this[i * 2].toInt() and 0xFF)).toShort()
        }
        return shorts
    }

    private fun setupGattServices(gatt: BluetoothGatt) {
        try {
            val service = gatt.services.find { it.uuid.toString().equals(BleConstants.SERVICE_UUID, ignoreCase = true) }
            if (service != null) {
                Log.d("BleManager", "Found main service: ${service.uuid}")

                // Sensor characteristic from constants
                val sensorCharacteristic = service.characteristics.find {
                    it.uuid.toString().equals(BleConstants.SENSOR_CHAR_UUID, ignoreCase = true)
                }

                if (sensorCharacteristic != null) {
                    enableNotifications(gatt, sensorCharacteristic)
                    _connectionState.value = BleConnectionState.Connected
                } else {
                    Log.e("BleManager", "Sensor characteristic not found")
                    _connectionState.value = BleConnectionState.Error("Sensor characteristic not found")
                }
            } else {
                Log.e("BleManager", "Required service not found")
                _connectionState.value = BleConnectionState.Error("Required service not found")
            }
        } catch (e: SecurityException) {
            Log.e("BleManager", "Security exception during service setup", e)
            _connectionState.value = BleConnectionState.Error("Permission denied")
        }
    }

    private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        try {
            gatt.setCharacteristicNotification(characteristic, true)
            // Enable notifications on the remote device
            characteristic.descriptors.firstOrNull()?.let { descriptor ->
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
            }
        } catch (e: SecurityException) {
            Log.e("BleManager", "Security exception enabling notifications", e)
        }
    }

    private fun retryConnection(device: BluetoothDevice) {
        isRetrying = true
        retryCount++
        coroutineScope.launch {
            delay(1000) // Wait a second before retrying
            try {
                bluetoothGatt?.close()
                bluetoothGatt = device.connectGatt(
                    context,
                    false,
                    gattCallback,
                    BluetoothDevice.TRANSPORT_LE
                )
            } catch (e: SecurityException) {
                Log.e("BleManager", "Security exception during retry", e)
                _connectionState.value = BleConnectionState.Error("Permission denied")
            } finally {
                isRetrying = false
            }
        }
    }


    fun startSetup() {
        if (!bluetoothAdapter.isEnabled) {
            _connectionState.value = BleConnectionState.BluetoothOff
            return
        }
        startScan()
    }

    private fun startScan() {
        if (isScanning) return
        isScanning = true
        _connectionState.value = BleConnectionState.Scanning
        try {
            bluetoothAdapter.bluetoothLeScanner?.startScan(scanCallback)
        } catch (e: SecurityException) {
            _connectionState.value = BleConnectionState.Error("Permission denied")
        }
        coroutineScope.launch {
            delay(10000)
            if (isScanning) {
                stopScan()
                _connectionState.value = BleConnectionState.DeviceOff
            }
        }
    }

    private fun stopScan() {
        if (!isScanning) return
        isScanning = false
        try {
            bluetoothAdapter.bluetoothLeScanner?.stopScan(scanCallback)
        } catch (e: SecurityException) {
            _connectionState.value = BleConnectionState.Error("Permission denied")
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        try {
            bluetoothGatt = device.connectGatt(context, false, gattCallback)
        } catch (e: SecurityException) {
            _connectionState.value = BleConnectionState.Error("Permission denied")
        }
    }

    fun cleanup() {
        try {
            isScanning = false
            bluetoothAdapter.bluetoothLeScanner?.stopScan(scanCallback)
            bluetoothGatt?.let { gatt ->
                gatt.disconnect()
                gatt.close()
            }
            bluetoothGatt = null
        } catch (e: SecurityException) {
            Log.e("BleManager", "Security exception during cleanup", e)
        }
    }
}