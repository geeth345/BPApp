package com.example.bloodpressuremonitorconnector.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bloodpressuremonitorconnector.BloodPressureMonitorApplication
import com.example.bloodpressuremonitorconnector.data.BPDao
import com.example.bloodpressuremonitorconnector.data.BPDatabase
import com.example.bloodpressuremonitorconnector.data.BPReading
import com.example.bloodpressuremonitorconnector.ui.setup.state.BleConnectionState
import com.example.bloodpressuremonitorconnector.utils.BleContainer
import com.example.bloodpressuremonitorconnector.utils.BleManager
import com.example.bloodpressuremonitorconnector.utils.SettingsContainer
import com.example.bloodpressuremonitorconnector.utils.SettingsManager
import com.example.bloodpressuremonitorconnector.data.MockDataLoader
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val application: BloodPressureMonitorApplication,
    private val bleManager: BleManager,
    private val settingsManager: SettingsManager,
    private val bpDao: BPDao
) : ViewModel() {
    private val _deviceConnectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Initial)
    val deviceConnectionState: StateFlow<BleConnectionState> = _deviceConnectionState.asStateFlow()

    private val _debugModeState = MutableStateFlow<Boolean>(false)
    val debugModeState: StateFlow<Boolean> = _debugModeState.asStateFlow()

    private val _latestReading = MutableStateFlow<BPReading?>(null)
    val latestReading: StateFlow<BPReading?> = _latestReading.asStateFlow()


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
        // get the latest reading from db
        viewModelScope.launch {
            fetchLatestReading()
        }
    }

    private suspend fun fetchLatestReading() {
        try {
            _latestReading.value = bpDao.getLatestReading()
        } catch (e: Exception) {
            // set to null if any issue
            _latestReading.value = null
        }
    }

    fun loadMockData() {
        viewModelScope.launch {
            MockDataLoader.loadMockDataIntoDatabase(
                context = application.applicationContext,
                bpDao = bpDao,
                replace = true
            )
            fetchLatestReading()
        }
    }


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BloodPressureMonitorApplication)
                val database = BPDatabase.getInstance(application.applicationContext)
                HomeViewModel(
                    application = application,
                    bleManager = BleContainer.getBleManager(),
                    settingsManager = SettingsContainer.getSettingsManager(),
                    bpDao = database.bpDao()
                )
            }
        }
    }
}