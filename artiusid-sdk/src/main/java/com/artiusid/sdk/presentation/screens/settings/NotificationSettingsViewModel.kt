/*
 * File: NotificationSettingsViewModel.kt
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

data class NotificationSettingsUiState(
    val pushNotificationsEnabled: Boolean = false,
    val emailNotificationsEnabled: Boolean = false,
    val smsNotificationsEnabled: Boolean = false,
    val documentVerificationNotifications: Boolean = false,
    val securityAlerts: Boolean = false,
    val systemUpdates: Boolean = false,
    val notificationSound: Boolean = false
)

class NotificationSettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationSettingsUiState())
    val uiState: StateFlow<NotificationSettingsUiState> = _uiState.asStateFlow()

    fun togglePushNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(pushNotificationsEnabled = enabled)
            // TODO: Save to preferences or backend
        }
    }

    fun toggleEmailNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(emailNotificationsEnabled = enabled)
            // TODO: Save to preferences or backend
        }
    }

    fun toggleSmsNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(smsNotificationsEnabled = enabled)
            // TODO: Save to preferences or backend
        }
    }

    fun toggleDocumentVerificationNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(documentVerificationNotifications = enabled)
            // TODO: Save to preferences or backend
        }
    }

    fun setDocumentVerificationNotifications(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(documentVerificationNotifications = enabled)
            // TODO: Save to preferences or backend
        }
    }

    fun setSecurityAlerts(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(securityAlerts = enabled)
            // TODO: Save to preferences or backend
        }
    }

    fun setSystemUpdates(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(systemUpdates = enabled)
            // TODO: Save to preferences or backend
        }
    }

    fun setNotificationSound(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(notificationSound = enabled)
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