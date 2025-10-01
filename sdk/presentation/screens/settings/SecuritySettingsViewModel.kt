/*
 * File: SecuritySettingsViewModel.kt
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

data class SecuritySettingsUiState(
    val biometricEnabled: Boolean = false,
    val twoFactorEnabled: Boolean = false,
    val autoLockEnabled: Boolean = false,
    val sessionTimeoutEnabled: Boolean = false
)

class SecuritySettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SecuritySettingsUiState())
    val uiState: StateFlow<SecuritySettingsUiState> = _uiState.asStateFlow()

    fun setBiometric(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(biometricEnabled = enabled)
            // TODO: Save to preferences or backend
        }
    }

    fun setTwoFactor(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(twoFactorEnabled = enabled)
            // TODO: Save to preferences or backend
        }
    }

    fun setAutoLock(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(autoLockEnabled = enabled)
            // TODO: Save to preferences or backend
        }
    }

    fun setSessionTimeout(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(sessionTimeoutEnabled = enabled)
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