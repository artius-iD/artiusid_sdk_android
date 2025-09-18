/*
 * File: SettingsRepository.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.domain.repository

data class Settings(
    val isDarkMode: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val isNotificationsEnabled: Boolean = true,
    val isLocationEnabled: Boolean = true,
    val isAnalyticsEnabled: Boolean = true,
    val isCrashReportingEnabled: Boolean = true,
    val isAutoUpdateEnabled: Boolean = true,
    val isDataSaverEnabled: Boolean = false,
    val isHighContrastEnabled: Boolean = false,
    val isLargeTextEnabled: Boolean = false,
    val isBoldTextEnabled: Boolean = false,
    val isScreenReaderEnabled: Boolean = false,
    val isHapticFeedbackEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true,
    val isAutoLockEnabled: Boolean = false,
    val autoLockTimeout: Int = 5,
    val isRememberLastScreenEnabled: Boolean = true,
    val isRememberLastDocumentEnabled: Boolean = true,
    val isRememberLastFaceEnabled: Boolean = true,
    val isRememberLastEnrollmentEnabled: Boolean = true,
    val isRememberLastSettingsEnabled: Boolean = true,
    val isRememberLastHelpEnabled: Boolean = true,
    val isRememberLastAboutEnabled: Boolean = true,
    val isRememberLastPrivacyEnabled: Boolean = true,
    val isRememberLastTermsEnabled: Boolean = true,
    val isRememberLastContactEnabled: Boolean = true,
    val isRememberLastFeedbackEnabled: Boolean = true,
    val isRememberLastSupportEnabled: Boolean = true,
    val isRememberLastProfileEnabled: Boolean = true,
    val isRememberLastSecurityEnabled: Boolean = true,
    val isRememberLastNotificationsEnabled: Boolean = true,
    val isRememberLastPreferencesEnabled: Boolean = true,
    val isRememberLastSupportEnabled2: Boolean = true,
    val isRememberLastSignOutEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val language: String = "English"
)

interface SettingsRepository {
    suspend fun getSettings(): Settings
    suspend fun setDarkMode(enabled: Boolean)
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setLocationEnabled(enabled: Boolean)
    suspend fun setAnalyticsEnabled(enabled: Boolean)
    suspend fun setCrashReportingEnabled(enabled: Boolean)
    suspend fun setAutoUpdateEnabled(enabled: Boolean)
    suspend fun setDataSaverEnabled(enabled: Boolean)
    suspend fun setHighContrastEnabled(enabled: Boolean)
    suspend fun setLargeTextEnabled(enabled: Boolean)
    suspend fun setBoldTextEnabled(enabled: Boolean)
    suspend fun setScreenReaderEnabled(enabled: Boolean)
    suspend fun setHapticFeedbackEnabled(enabled: Boolean)
    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setVibrationEnabled(enabled: Boolean)
    suspend fun setAutoLockEnabled(enabled: Boolean)
    suspend fun setAutoLockTimeout(timeout: Int)
    suspend fun setLanguage(language: String)
    suspend fun resetSettings()
    suspend fun logout()
} 