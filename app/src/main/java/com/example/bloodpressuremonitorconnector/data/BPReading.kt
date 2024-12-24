package com.example.bloodpressuremonitorconnector.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bp_readings")
data class BPReading(
    @PrimaryKey
    val timestamp: Long,
    val systolic: Int,
    val diastolic: Int
)
