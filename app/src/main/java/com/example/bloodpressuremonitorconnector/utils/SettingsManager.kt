package com.example.bloodpressuremonitorconnector.utils

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsManager (
    private val context: Context,
) {
    companion object {
        private val Context.dataStore by preferencesDataStore(name = "settings")

        // defining the keys for the data store, what to show in settings
        private val DEBUG_MODE = booleanPreferencesKey("debug_mode")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val MEASUREMENT_INTERVAL = intPreferencesKey("measurement_interval")
        private val NAME = stringPreferencesKey("name")
        private val EMAIL = stringPreferencesKey("email")
        private val HEIGHT = intPreferencesKey("height")
        private val WEIGHT = intPreferencesKey("weight")
    }

    // debug mode setting
    val debugMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
            preferences[DEBUG_MODE] ?: false
    }

    suspend fun setDebugMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DEBUG_MODE] = enabled
        }
    }

    // notifications setting
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED] ?: true
    }
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    // measurement interval setting
    val measurementInterval: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[MEASUREMENT_INTERVAL] ?: 30
    }
    suspend fun setMeasurementInterval(interval: Int) {
        context.dataStore.edit { preferences ->
            preferences[MEASUREMENT_INTERVAL] = interval
        }
    }

    // Profile Information (name, email, height, weight) - set on profile screen

    // Name
    val name: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[NAME] ?: "John Doe"
    }
    suspend fun setName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[NAME] = name
        }
    }

    // Email
    val email: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[EMAIL] ?: "email@example.com" }
    suspend fun setEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[EMAIL] = email
        }
    }

    // Height
    val height: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[HEIGHT] ?: 175
    }
    suspend fun setHeight(height: Int) {
        context.dataStore.edit { preferences ->
            preferences[HEIGHT] = height
        }
    }

    // Weight
    val weight: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[WEIGHT] ?: 70
    }
    suspend fun setWeight(weight: Int) {
        context.dataStore.edit { preferences ->
            preferences[WEIGHT] = weight
        }
    }




}