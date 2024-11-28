package com.example.bloodpressuremonitorconnector.ui.setup

import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bloodpressuremonitorconnector.utils.BleManager
import com.example.bloodpressuremonitorconnector.utils.BlePermissionsManager
import com.example.bloodpressuremonitorconnector.ui.setup.state.BleConnectionState
import com.example.bloodpressuremonitorconnector.ui.setup.state.BlePermissionState
import com.example.bloodpressuremonitorconnector.utils.BleContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow

import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class BleSetupViewModel(
    private val bleManager: BleManager,
    private val permissionsManager: BlePermissionsManager
) : ViewModel() {
    private val _permissionState = MutableStateFlow<BlePermissionState>(BlePermissionState.RequiresPermissions(emptyList()))
    val permissionState = _permissionState.asStateFlow()

    private val _connectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Initial)
    val connectionState = _connectionState.asStateFlow()

    init {
        // Collect permission state changes
        viewModelScope.launch {
            permissionsManager.permissionState.collect {
                _permissionState.value = it
            }
        }

        // Collect connection state changes
        viewModelScope.launch {
            bleManager.connectionState.collect {
                _connectionState.value = it
            }
        }

        checkPermissions()
    }

    init {
        checkPermissions()
    }

    fun checkPermissions() {
        permissionsManager.checkPermissions()
    }

    fun handlePermissionResult(permissions: Map<String, Boolean>) {
        permissionsManager.handlePermissionResult(permissions)
    }

    fun startSetup() {
        bleManager.startSetup()
    }

    override fun onCleared() {
        // not clearing up the BleManager here, as it should be shared across the app
        //bleManager.cleanup()
        super.onCleared()
    }
}

class BleSetupViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BleSetupViewModel::class.java)) {
            val permissionsManager = BlePermissionsManager(context)
            return BleSetupViewModel(
                bleManager = BleContainer.getBleManager(),
                permissionsManager = permissionsManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}