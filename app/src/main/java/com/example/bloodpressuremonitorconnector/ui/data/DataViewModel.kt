package com.example.bloodpressuremonitorconnector.ui.data

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bloodpressuremonitorconnector.data.BPDataRepository
import com.example.bloodpressuremonitorconnector.data.BPReading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class DataViewModel : ViewModel() {
    private val _readings = MutableStateFlow<List<BPReading>>(emptyList())
    val readings: StateFlow<List<BPReading>> = _readings.asStateFlow()

    fun loadData(context: Context) {
        viewModelScope.launch {
            _readings.value = BPDataRepository(context).loadMockData()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(DataViewModel::class.java)) {
                    return DataViewModel() as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }


}