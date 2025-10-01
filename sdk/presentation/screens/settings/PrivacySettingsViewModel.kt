/*
 * File: PrivacySettingsViewModel.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PrivacySettingsUiState(
    val dataCollectionEnabled: Boolean = false,
    val locationServicesEnabled: Boolean = false,
    val analyticsEnabled: Boolean = false,
    val personalizedAdsEnabled: Boolean = false
)

class PrivacySettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PrivacySettingsUiState())
    val uiState: StateFlow<PrivacySettingsUiState> = _uiState.asStateFlow()

    fun setDataCollection(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(dataCollectionEnabled = enabled)
            // TODO: Save to preferences or backend
        }
    }

    fun setLocationServices(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(locationServicesEnabled = enabled)
            // TODO: Save to preferences or backend
        }
    }

    fun setAnalytics(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(analyticsEnabled = enabled)
            // TODO: Save to preferences or backend
        }
    }

    fun setPersonalizedAds(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(personalizedAdsEnabled = enabled)
            // TODO: Save to preferences or backend
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            // TODO: Save all settings to preferences or backend
            // This is where you would implement the actual saving logic
            // For now, we'll just keep the state in memory
        }
    }
} 