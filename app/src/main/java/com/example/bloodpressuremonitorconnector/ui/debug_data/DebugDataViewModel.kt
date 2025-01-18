package com.example.bloodpressuremonitorconnector.ui.debug_data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloodpressuremonitorconnector.utils.bluetooth.state.BleConnectionState
import com.example.bloodpressuremonitorconnector.utils.bluetooth.BleContainer
import com.example.bloodpressuremonitorconnector.utils.models.BPResult
import com.example.bloodpressuremonitorconnector.utils.models.ModelsContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DataPoint(
    val value: Float,
    val timestamp: Long = System.currentTimeMillis()
)

data class DataScreenState(
    val connectionState: BleConnectionState = BleConnectionState.Initial,
    val dataPoints: List<DataPoint> = emptyList(),
    val isRecording: Boolean = false,
    val maxValue: Float = Float.MIN_VALUE,
    val minValue: Float = Float.MAX_VALUE,
    val predictionSystolic: Int = 0,
    val predictionDiastolic: Int = 0
)

class DebugDataViewModel : ViewModel() {
    private val bleManager = BleContainer.getBleManager()
    private val modelsManager = ModelsContainer.getModelsManager()
    private val maxDataPoints = 2000 // Keep last 1000 points for display

    private val _uiState = MutableStateFlow(DataScreenState())
    val uiState: StateFlow<DataScreenState> = _uiState.asStateFlow()


    init {
        viewModelScope.launch {
            bleManager.connectionState.collect { connectionState ->
                _uiState.value = _uiState.value.copy(connectionState = connectionState)
            }
        }

        viewModelScope.launch {
            bleManager.sensorData.collect { sensorDataPoint ->
                addDataPoint(sensorDataPoint)
            }
        }
    }

    fun startRecording() {
        _uiState.value = _uiState.value.copy(isRecording = true)
        // TODO: Implement start recording logic with BleManager
    }

    fun stopRecording() {
        _uiState.value = _uiState.value.copy(isRecording = false)
        // TODO: Implement stop recording logic with BleManager
    }

    fun clearData() {
        _uiState.value = _uiState.value.copy(
            dataPoints = emptyList(),
            maxValue = Float.MIN_VALUE,
            minValue = Float.MAX_VALUE
        )
    }

    // Add a new data point
    fun addDataPoint(value: Float) {
        if (!_uiState.value.isRecording) return
        val currentPoints = _uiState.value.dataPoints.toMutableList()
        if (currentPoints.size >= maxDataPoints) {
            currentPoints.removeFirst()
        }

        currentPoints.add(DataPoint(value))

        // Update min/max values
        val currentMax = maxOf(_uiState.value.maxValue, value)
        val currentMin = minOf(_uiState.value.minValue, value)

        val prediction = getPrediction()

        _uiState.value = _uiState.value.copy(
            dataPoints = currentPoints,
            maxValue = currentMax,
            minValue = currentMin,
            predictionSystolic = prediction.systolic,
            predictionDiastolic = prediction.diastolic
        )
    }

    fun getPrediction(): BPResult {
        val data = _uiState.value.dataPoints.map { it.value }
        val result = modelsManager.predictBP(data)
        return result
    }

//    // Add multiple data points at once (for bulk updates)
//    fun addDataPoints(values: List<Float>) {
//        val currentPoints = _uiState.value.dataPoints.toMutableList()
//        val newPoints = values.map { DataPoint(it) }
//
//        // Keep only the last maxDataPoints
//        val combinedPoints = (currentPoints + newPoints).takeLast(maxDataPoints)
//
//        // Update min/max values
//        val newMax = maxOf(_uiState.value.maxValue, values.maxOrNull() ?: Float.MIN_VALUE)
//        val newMin = minOf(_uiState.value.minValue, values.minOrNull() ?: Float.MAX_VALUE)
//
//        _uiState.value = _uiState.value.copy(
//            dataPoints = combinedPoints,
//            maxValue = newMax,
//            minValue = newMin
//        )
//    }
}