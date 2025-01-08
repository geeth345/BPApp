package com.example.bloodpressuremonitorconnector.utils.bluetooth

import com.example.bloodpressuremonitorconnector.utils.bluetooth.state.BlePermissionState
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.bluetooth.BluetoothManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class BlePermissionsManager(private val context: Context) {
    companion object {
        val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION  // Add this for better compatibility
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION  // Add this for better compatibility
            )
        }
    }

    private val _permissionState = MutableStateFlow<BlePermissionState>(
        BlePermissionState.RequiresPermissions(REQUIRED_PERMISSIONS.toList())
    )
    val permissionState: StateFlow<BlePermissionState> = _permissionState.asStateFlow()

    fun checkPermissions() {
        val bluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _permissionState.value = BlePermissionState.RequiresBluetooth
            return
        }

        val missingPermissions = REQUIRED_PERMISSIONS.filter {
            Log.d("BlePermissions", "Checking permission $it: ${
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }")
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        when {
            missingPermissions.isEmpty() -> {
                Log.d("BlePermissions", "All permissions granted")
                _permissionState.value = BlePermissionState.AllGranted
            }
            else -> {
                Log.d("BlePermissions", "Missing permissions: $missingPermissions")
                _permissionState.value = BlePermissionState.RequiresPermissions(missingPermissions)
            }
        }
    }

    fun handlePermissionResult(permissions: Map<String, Boolean>) {
        Log.d("BlePermissions", "Handling permission results: $permissions")
        // Check if we have all required permissions, not just the ones that were just granted
        checkPermissions()
    }
}