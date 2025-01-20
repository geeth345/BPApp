package com.example.bloodpressuremonitorconnector.utils

import android.content.Context
import android.util.Log
import com.example.bloodpressuremonitorconnector.data.BPDao
import com.example.bloodpressuremonitorconnector.data.BPDatabase
import com.example.bloodpressuremonitorconnector.data.BPReading
import com.example.bloodpressuremonitorconnector.utils.models.ModelsContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Processes blood pressure samples and stores readings in the database.
 * This class is responsible for:
 * 1. Collecting samples from the BLE device
 * 2. Processing them in batches using the ML model
 * 3. Storing the results in the database
 * 4. Providing access to the latest reading
 */
class BPProcessor(
    private val context: Context,
    private val sampleInterval: Int = 10000,
    private val sampleArraySize: Int = 1000,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + Job())
) {
    private val modelsManager = ModelsContainer.getModelsManager()
    private val bpDao: BPDao = BPDatabase.getInstance(context).bpDao()

    private var samples = mutableListOf<Float>()
    private var sampleCount = 0

    private val _lastReading = MutableStateFlow<BPReading?>(null)
    val lastReading: StateFlow<BPReading?> = _lastReading.asStateFlow()

    fun processSample(sample: Float) {
        samples.add(sample)
        samples = samples.takeLast(sampleArraySize).toMutableList()
        sampleCount++

        if (sampleCount >= sampleInterval) {
            processAndStoreBPReading(samples)
            // Clear samples after processing
            samples.clear()
            sampleCount = 0
        }
    }

    fun batchProcessSample(sample: List<Float>) {
        samples.addAll(sample)
        samples = samples.takeLast(sampleArraySize).toMutableList()
        sampleCount += sample.size

        if (sampleCount >= sampleInterval) {
            processAndStoreBPReading(samples)
            // Clear samples after processing
            samples.clear()
            sampleCount = 0
        }
    }

    private fun processAndStoreBPReading(samplesToProcess: List<Float>) {
        if (samplesToProcess.isEmpty()) return

        coroutineScope.launch {
            try {
                val result = modelsManager.predictBP(samplesToProcess)
                val reading = BPReading(
                    timestamp = System.currentTimeMillis(),
                    systolic = result.systolic,
                    diastolic = result.diastolic
                )

                // Store in database
                bpDao.insertReading(reading)

                // Update last reading state
                _lastReading.value = reading

                Log.d("BPProcessor", "Stored new reading: $reading")
            } catch (e: Exception) {
                Log.e("BPProcessor", "Error processing BP reading", e)
            }
        }
    }

    companion object {
        @Volatile
        private var instance: BPProcessor? = null

        fun getInstance(context: Context): BPProcessor {
            return instance ?: synchronized(this) {
                instance ?: BPProcessor(context.applicationContext).also { instance = it }
            }
        }
    }
}