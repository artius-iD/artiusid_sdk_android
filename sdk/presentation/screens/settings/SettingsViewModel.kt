/*
 * File: SettingsViewModel.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.artiusid.sdk.data.api.ApiService
import com.artiusid.sdk.data.repository.LogManager
import com.artiusid.sdk.data.repository.SettingsRepository
import com.artiusid.sdk.util.DeviceUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val apiService: ApiService
) : AndroidViewModel(application) {
    private val repo = SettingsRepository(application.applicationContext, apiService)
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        loadSettings()
        
        // Add some sample logs to demonstrate functionality (like iOS)
        repo.addLog("Settings screen initialized", LogManager.LogLevel.INFO, "SettingsViewModel")
        repo.addLog("Configuration loaded successfully", LogManager.LogLevel.DEBUG, "SettingsViewModel")
    }

    private fun loadSettings() {
        val env = repo.getEnvironment()
        val devMode = repo.getBoolean("isDeveloperMode")
        val demoMode = repo.getBoolean("isDemoMode")
        val s3 = repo.getBoolean("enableS3")
        val overlays = repo.getBoolean("displayImageOverlays")
        val docOutline = repo.getBoolean("displayDocumentOutline")
        val targetOutline = repo.getBoolean("displayTargetObjectOutline")
        val logLevel = repo.getLogLevel()
        val accountNumber = repo.getAccountNumber()
        val deviceId = DeviceUtils.getDeviceId(getApplication())
        val logs = repo.getLogs()
        _uiState.value = _uiState.value.copy(
            environment = env,
            isDeveloperMode = devMode,
            isDemoMode = demoMode,
            enableS3 = s3,
            displayImageOverlays = overlays,
            displayDocumentOutline = docOutline,
            displayTargetObjectOutline = targetOutline,
            logLevel = logLevel,
            accountNumber = accountNumber,
            deviceId = deviceId,
            logs = logs,
            logCount = logs.size
        )
    }

    fun setEnvironment(env: String) {
        _uiState.value = _uiState.value.copy(environment = env, isCertClearing = true)
        repo.setEnvironment(env)
        viewModelScope.launch {
            val cleared = repo.clearAndRegenerateCertificate()
            _uiState.value = _uiState.value.copy(isCertCleared = cleared, isCertClearing = false)
        }
    }

    fun setDeveloperMode(enabled: Boolean) {
        repo.setBoolean("isDeveloperMode", enabled)
        _uiState.value = _uiState.value.copy(isDeveloperMode = enabled)
    }

    fun setDemoMode(enabled: Boolean) {
        repo.setBoolean("isDemoMode", enabled)
        _uiState.value = _uiState.value.copy(isDemoMode = enabled)
    }

    fun setEnableS3(enabled: Boolean) {
        repo.setBoolean("enableS3", enabled)
        _uiState.value = _uiState.value.copy(enableS3 = enabled)
    }

    fun setDisplayImageOverlays(enabled: Boolean) {
        repo.setBoolean("displayImageOverlays", enabled)
        _uiState.value = _uiState.value.copy(displayImageOverlays = enabled)
    }

    fun setDisplayDocumentOutline(enabled: Boolean) {
        repo.setBoolean("displayDocumentOutline", enabled)
        _uiState.value = _uiState.value.copy(displayDocumentOutline = enabled)
    }

    fun setDisplayTargetObjectOutline(enabled: Boolean) {
        repo.setBoolean("displayTargetObjectOutline", enabled)
        _uiState.value = _uiState.value.copy(displayTargetObjectOutline = enabled)
    }

    fun setLogLevel(level: String) {
        repo.setLogLevel(level)
        val desc = when (level) {
            "Debug" -> "Records all log messages: Debug, Info, Warning, and Error"
            "Info" -> "Records Info, Warning, and Error messages"
            "Warning" -> "Records only Warning and Error messages"
            "Error" -> "Records only Error messages"
            else -> ""
        }
        _uiState.value = _uiState.value.copy(logLevel = level, logLevelDescription = desc)
    }

    fun clearLogs() {
        repo.clearLogs()
        _uiState.value = _uiState.value.copy(logs = emptyList(), logCount = 0)
    }

    fun refreshLogs() {
        val logs = repo.getLogs()
        _uiState.value = _uiState.value.copy(logs = logs, logCount = logs.size)
    }

} 