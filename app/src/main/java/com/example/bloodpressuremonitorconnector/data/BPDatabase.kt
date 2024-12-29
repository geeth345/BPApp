package com.example.bloodpressuremonitorconnector.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase

@Database(entities = [BPReading::class], version = 1)
abstract class BPDatabase : RoomDatabase() {
    abstract fun bpDao(): BPDao

    // we ensure only one instance of the database is created. i.e. singleton pattern
    companion object {
        @Volatile
        private var INSTANCE: BPDatabase? = null

        fun getInstance(context: Context): BPDatabase {
            if (INSTANCE == null) {
                synchronized(BPDatabase::class) {
                    INSTANCE = databaseBuilder(
                        context.applicationContext,
                        BPDatabase::class.java,
                        "bp_database"
                    ).build()
                }
            }
            return INSTANCE!!
        }

    }

}