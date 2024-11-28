package com.example.bloodpressuremonitorconnector.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloodpressuremonitorconnector.ui.setup.state.BleConnectionState
import com.example.bloodpressuremonitorconnector.utils.BleContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val belManager = BleContainer.getBleManager()

    private val _deviceConnectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Initial)
    val deviceConnectionState: StateFlow<BleConnectionState> = _deviceConnectionState.asStateFlow()

    init {
        viewModelScope.launch {
            belManager.connectionState.collect {
                _deviceConnectionState.value = it
            }
        }
    }


}