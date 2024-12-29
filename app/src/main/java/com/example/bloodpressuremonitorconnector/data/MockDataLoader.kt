package com.example.bloodpressuremonitorconnector.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MockDataLoader {
    suspend fun loadMockDataIntoDatabase(
        context: Context,
        bpDao: BPDao,
        replace: Boolean = true
    ) {
        withContext(Dispatchers.IO) {
            try {
                val readings = context.assets.open("fake_bp_readings.csv").bufferedReader().useLines { lines ->
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

                if (replace) {
                    bpDao.replaceAllReadings(readings)
                } else {
                    bpDao.insertReadings(readings)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}