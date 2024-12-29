package com.example.bloodpressuremonitorconnector.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bloodpressuremonitorconnector.BloodPressureMonitorApplication
import com.example.bloodpressuremonitorconnector.utils.SettingsContainer
import com.example.bloodpressuremonitorconnector.utils.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val name: String = "John Doe",
    val email: String = "john.doe@example.com",
    val phone: String = "000 0000 0000",
    val medicalPractice: String = "University Health Service",
    val doctorName: String = "Dr. Meredith Grey",
    val heightCm: Int = 175,
    val weightKg: Int = 70,
    val birthDate: String = "1980-01-01",
    val isEditing: Boolean = false
)

class ProfileViewModel(
    private val settingsManager: SettingsManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun toggleEditMode() {
        _uiState.value = _uiState.value.copy(isEditing = !_uiState.value.isEditing)
    }

    fun updateProfile(
        name: String? = null,
        email: String? = null,
        heightCm: Int? = null,
        weightKg: Int? = null
    ) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            name = name ?: currentState.name,
            email = email ?: currentState.email,
            heightCm = heightCm ?: currentState.heightCm,
            weightKg = weightKg ?: currentState.weightKg,
            isEditing = false
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ProfileViewModel(
                    settingsManager = SettingsContainer.getSettingsManager()
                )
            }
        }
    }
}