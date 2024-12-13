package com.example.bloodpressuremonitorconnector.db

// BPReading.kt
data class BPReading(
    val timestamp: Long,
    val systolic: Int,
    val diastolic: Int
)
