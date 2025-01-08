package com.example.bloodpressuremonitorconnector.utils.bluetooth.state

sealed class BleConnectionState {
    object Initial : BleConnectionState()
    object BluetoothOff : BleConnectionState()
    object DeviceOff : BleConnectionState()
    object Scanning : BleConnectionState()
    data class DeviceFound(val deviceId: String) : BleConnectionState()
    object Connected : BleConnectionState()
    data class Error(val message: String) : BleConnectionState()
}