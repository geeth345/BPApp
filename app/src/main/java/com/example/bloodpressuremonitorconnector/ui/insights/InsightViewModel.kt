package com.example.bloodpressuremonitorconnector.ui.insights

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
import com.example.bloodpressuremonitorconnector.utils.models.ModelsContainer
import com.example.bloodpressuremonitorconnector.utils.models.ModelsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

data class BPInsight(
    val title: String,
    val description: String,
    val severity: InsightSeverity,
    val actionNeeded: Boolean = false
)

enum class InsightSeverity {
    LOW, MEDIUM, HIGH
}

data class InsightsUiState(
    val cvdRiskScore: Int = 0, // define risk as a percentage
    val insights: List<BPInsight> = emptyList(),
    val averageSystolic: Int = 0,
    val averageDiastolic: Int = 0,
    val stressScore: Int = 0,
    val isLoading: Boolean = true
)

class InsightsViewModel(
    private val bpDao: BPDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    private val modelsManager: ModelsManager = ModelsContainer.getModelsManager()

    init {
        loadInsights()
    }

    private fun loadInsights() {
        viewModelScope.launch {
            try {
                val readings = bpDao.getAllReadings()
                if (readings.isNotEmpty()) {
                    analyzeReadings(readings)
                }
            } catch (e: Exception) {
                // just log the error
                Log.e("InsightsViewModel", "Error loading insights", e)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun analyzeReadings(readings: List<BPReading>) {
        val avgSystolic = readings.map { it.systolic }.average().toInt()
        val avgDiastolic = readings.map { it.diastolic }.average().toInt()

        // cvd risk model
        val cvdRisk = calculateCvdRisk(avgSystolic, avgDiastolic)

        // stress model??? making this up
        val stressScore = calculateStressScore(readings)

        // get insights
        val insights = generateInsights(readings, avgSystolic, avgDiastolic)

        _uiState.value = _uiState.value.copy(
            cvdRiskScore = cvdRisk,
            insights = insights,
            averageSystolic = avgSystolic,
            averageDiastolic = avgDiastolic,
            stressScore = stressScore
        )
    }

    private fun calculateCvdRisk(avgSystolic: Int, avgDiastolic: Int): Int {
        // placeholder risk calculation, eventually gonna use a python implementation of some ML
        // model to get these values
        val risk = modelsManager.predictRisk(listOf(avgSystolic), listOf(avgDiastolic))

        return risk

    }

    private fun calculateStressScore(readings: List<BPReading>): Int {
        // Placeholder stress calculation based on BP variability
        if (readings.size < 2) return 0

        val variability = readings.zipWithNext().map {
            abs(it.first.systolic - it.second.systolic)
        }.average()

        return (variability * 2).toInt().coerceIn(0, 100) // enforce 0-100 range
    }

    // generate a list of insights based on the readings
    private fun generateInsights(
        readings: List<BPReading>,
        avgSystolic: Int,
        avgDiastolic: Int
    ): List<BPInsight> {
        val insights = mutableListOf<BPInsight>()

        // BP Classification insight
        val bpClassification = when {
            avgSystolic < 120 && avgDiastolic < 80 -> BPInsight(
                "Blood Pressure Status",
                "Your blood pressure appears to be healthy.",
                InsightSeverity.LOW
            )
            avgSystolic < 140 && avgDiastolic < 90 -> BPInsight(
                "Blood Pressure Status",
                "Your blood pressure is slightly above normal. While this isn't cause for " +
                        "immediate concern, consider discussing this with your GP at your next check-up.",
                InsightSeverity.MEDIUM
            )
            else -> BPInsight(
                "Blood Pressure Status",
                "Your blood pressure readings appear higher than normal, we suggest it " +
                        "would be beneficial to schedule a check-up with your GP to discuss your " +
                        "cardiovascular health.",
                InsightSeverity.HIGH,
                actionNeeded = true
            )
        }
        insights.add(bpClassification)

        // BP Variability insight
        if (readings.size >= 2) {
            val variability = readings.zipWithNext().map {
                abs(it.first.systolic - it.second.systolic)
            }.average()

            if (variability > 20) {
                insights.add(BPInsight(
                    "Blood Pressure Variability",
                    "We've noticed some variation in your blood pressure readings, " +
                            "which could indicate certain conditions. We recommend discussing" +
                            "this with your GP.",
                    InsightSeverity.MEDIUM
                ))
            }
        }

        // time based
        insights.add(BPInsight(
            "Morning Surge",
            "Everyone's blood pressure varies during the day. It tends to be highest in " +
                    "the morning and lowest at night, but a bigger surge in the morning could be a " +
                    "risk factor. We recommend talking to your GP about this.",
            InsightSeverity.LOW
        ))

        return insights
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BloodPressureMonitorApplication)
                val database = BPDatabase.getInstance(application.applicationContext)
                InsightsViewModel(
                    bpDao = database.bpDao()
                )
            }
        }
    }
}