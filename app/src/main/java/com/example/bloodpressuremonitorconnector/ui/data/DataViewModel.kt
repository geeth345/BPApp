package com.example.bloodpressuremonitorconnector.ui.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bloodpressuremonitorconnector.BloodPressureMonitorApplication
import com.example.bloodpressuremonitorconnector.data.BPDao
import com.example.bloodpressuremonitorconnector.data.BPDatabase
import com.example.bloodpressuremonitorconnector.data.BPReading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class DataViewModel(
    private val bpDao: BPDao
) : ViewModel() {
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _readings = MutableStateFlow<List<BPReading>>(emptyList())
    val readings: StateFlow<List<BPReading>> = _readings.asStateFlow()

    private val _dayReadings = MutableStateFlow<List<BPReading>>(emptyList())
    val dayReadings: StateFlow<List<BPReading>> = _dayReadings.asStateFlow()

    private val _weekReadings = MutableStateFlow<List<BPReading>>(emptyList())
    val weekReadings: StateFlow<List<BPReading>> = _weekReadings.asStateFlow()

    private val _yearReadings = MutableStateFlow<List<BPReading>>(emptyList())
    val yearReadings: StateFlow<List<BPReading>> = _yearReadings.asStateFlow()

    init {
        loadData()
    }

    object TimeConstants {
        const val MILLIS_PER_HOUR = 60L * 60 * 1000
        const val MILLIS_PER_DAY = 24L * MILLIS_PER_HOUR
        const val MILLIS_PER_WEEK = 7L * MILLIS_PER_DAY
    }

    fun loadData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val currentTime = System.currentTimeMillis()

                // Hourly averages for past day
                val dayStartTime = currentTime - TimeConstants.MILLIS_PER_DAY
                _dayReadings.value = getAverageIntervalReadings(
                    dayStartTime,
                    currentTime,
                    TimeConstants.MILLIS_PER_HOUR
                )

                // Daily averages for past week
                val weekStartTime = currentTime - (7 * TimeConstants.MILLIS_PER_DAY)
                _weekReadings.value = getAverageIntervalReadings(
                    weekStartTime,
                    currentTime,
                    TimeConstants.MILLIS_PER_DAY
                )

                // Weekly averages for 365 days
                val monthStartTime = currentTime - (30 * TimeConstants.MILLIS_PER_DAY)
                _yearReadings.value = getAverageIntervalReadings(
                    monthStartTime,
                    currentTime,
                    TimeConstants.MILLIS_PER_WEEK
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun getAverageIntervalReadings(startTime: Long, endTime: Long, interval: Long): List<BPReading> {
        val readings = bpDao.getReadingsInRange(startTime, endTime)
        return readings.groupBy { it.timestamp / interval }
            .mapValues { (_, readings) ->
                val systolic = readings.map { it.systolic }.average().toInt()
                val diastolic = readings.map { it.diastolic }.average().toInt()
                BPReading(
                    timestamp = readings.first().timestamp,
                    systolic = systolic,
                    diastolic = diastolic
                )
            }.values.toList()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BloodPressureMonitorApplication)
                val database = BPDatabase.getInstance(application.applicationContext)
                DataViewModel(
                    bpDao = database.bpDao()
                )
            }
        }
    }
}