package com.example.bloodpressuremonitorconnector.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bloodpressuremonitorconnector.BloodPressureMonitorApplication
import com.example.bloodpressuremonitorconnector.utils.SettingsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val debugMode: Boolean = false,
    val measurementInterval: Int = 30,
    val notificationsEnabled: Boolean = true
)

class SettingsViewModel(
    private val settingsManager: SettingsManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Collect all settings and update UI state
        viewModelScope.launch {
            combine(
                settingsManager.debugMode,
                settingsManager.measurementInterval,
                settingsManager.notificationsEnabled,
            ) { debugMode, interval, notifications ->
                SettingsUiState(
                    debugMode = debugMode,
                    measurementInterval = interval,
                    notificationsEnabled = notifications
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setDebugMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setDebugMode(enabled)
        }
    }

    fun setMeasurementInterval(interval: Int) {
        viewModelScope.launch {
            settingsManager.setMeasurementInterval(interval)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setNotificationsEnabled(enabled)
        }
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BloodPressureMonitorApplication)
                SettingsViewModel(
                    settingsManager = application.settingsManager
                )
            }
        }
    }
}

