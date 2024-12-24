package com.example.bloodpressuremonitorconnector.data

import android.content.Context

class BPDataRepository(
    private val context: Context
) {
    /*
    Class to handle the data operations for the blood pressure readings, and also the mock
    data when in debug mode. Essentially a bridge between the dao and the rest of the
    application.
     */
    fun loadMockData(): List<BPReading> {
        return try {
            context.assets.open("fake_bp_readings.csv").bufferedReader().useLines { lines ->
                lines.drop(1) // Skip header
                    .map { line ->
                        val parts = line.split(",")
                        BPReading(
                            timestamp = parts[0].toLong(),
                            systolic = parts[1].toInt(),
                            diastolic = parts[2].toInt()
                        )
                    }.toList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}