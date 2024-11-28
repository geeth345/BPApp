package com.example.bloodpressuremonitorconnector.ui.setup.state


// Represents the current state of permissions
sealed class BlePermissionState {
    object AllGranted : BlePermissionState()
    data class RequiresPermissions(val permissions: List<String>) : BlePermissionState()
    object RequiresBluetooth : BlePermissionState()
    data class ShowRationale(val permissions: List<String>) : BlePermissionState()
}
