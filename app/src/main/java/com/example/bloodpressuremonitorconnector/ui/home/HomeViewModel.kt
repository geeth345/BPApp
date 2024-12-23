package com.example.bloodpressuremonitorconnector.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bloodpressuremonitorconnector.BloodPressureMonitorApplication
import com.example.bloodpressuremonitorconnector.ui.setup.state.BleConnectionState
import com.example.bloodpressuremonitorconnector.utils.BleContainer
import com.example.bloodpressuremonitorconnector.utils.BleManager
import com.example.bloodpressuremonitorconnector.utils.SettingsContainer
import com.example.bloodpressuremonitorconnector.utils.SettingsManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val bleManager: BleManager,
    private val settingsManager: SettingsManager
) : ViewModel() {
    private val _deviceConnectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Initial)
    val deviceConnectionState: StateFlow<BleConnectionState> = _deviceConnectionState.asStateFlow()

    private val _debugModeState = MutableStateFlow<Boolean>(false)
    val debugModeState: StateFlow<Boolean> = _debugModeState.asStateFlow()


    // Collect relevant changes
    init {
        // settings
        viewModelScope.launch {
            settingsManager.debugMode.collect {
                _debugModeState.value = it
            }
        }
        // connection state
        viewModelScope.launch {
            bleManager.connectionState.collect {
                _deviceConnectionState.value = it
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BloodPressureMonitorApplication)
                HomeViewModel(
                    bleManager = BleContainer.getBleManager(),
                    settingsManager = SettingsContainer.getSettingsManager()
                )
            }
        }
    }
}