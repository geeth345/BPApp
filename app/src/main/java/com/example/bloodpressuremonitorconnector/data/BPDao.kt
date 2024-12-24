package com.example.bloodpressuremonitorconnector.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface BPDao {
    /*
    Class to handle the data store for the blood pressure readings, implemented using Room.
     */
    // Get specific reading by timestamp
    @Query("SELECT * FROM bp_readings WHERE timestamp = :timestamp")
    suspend fun getReading(timestamp: Long): BPReading?

    // Get latest reading
    @Query("SELECT * FROM bp_readings ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestReading(): BPReading?

    // Get readings between timestamps (inclusive)
    @Query("SELECT * FROM bp_readings WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp ASC")
    suspend fun getReadingsInRange(startTime: Long, endTime: Long): List<BPReading>

    // Insert single reading
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: BPReading)

    // Insert multiple readings
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadings(readings: List<BPReading>)

    // Delete all readings
    @Query("DELETE FROM bp_readings")
    suspend fun deleteAllReadings()

    // Transaction to replace all data
    @Transaction
    suspend fun replaceAllReadings(readings: List<BPReading>) {
        deleteAllReadings()
        insertReadings(readings)
    }
}