/*
 * File: SettingsRepositoryImpl.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.artiusid.sdk.domain.repository.Settings
import com.artiusid.sdk.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepositoryImpl(
    private val context: Context
) : SettingsRepository {
    private object PreferencesKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val LOCATION_ENABLED = booleanPreferencesKey("location_enabled")
        val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
        val CRASH_REPORTING_ENABLED = booleanPreferencesKey("crash_reporting_enabled")
        val AUTO_UPDATE_ENABLED = booleanPreferencesKey("auto_update_enabled")
        val DATA_SAVER_ENABLED = booleanPreferencesKey("data_saver_enabled")
        val HIGH_CONTRAST_ENABLED = booleanPreferencesKey("high_contrast_enabled")
        val LARGE_TEXT_ENABLED = booleanPreferencesKey("large_text_enabled")
        val BOLD_TEXT_ENABLED = booleanPreferencesKey("bold_text_enabled")
        val SCREEN_READER_ENABLED = booleanPreferencesKey("screen_reader_enabled")
        val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val AUTO_LOCK_ENABLED = booleanPreferencesKey("auto_lock_enabled")
        val AUTO_LOCK_TIMEOUT = intPreferencesKey("auto_lock_timeout")
        val LANGUAGE = stringPreferencesKey("language")
    }

    override suspend fun getSettings(): Settings {
        return context.dataStore.data.map { preferences ->
            Settings(
                isDarkMode = preferences[PreferencesKeys.DARK_MODE] ?: false,
                isBiometricEnabled = preferences[PreferencesKeys.BIOMETRIC_ENABLED] ?: false,
                isNotificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
                isLocationEnabled = preferences[PreferencesKeys.LOCATION_ENABLED] ?: true,
                isAnalyticsEnabled = preferences[PreferencesKeys.ANALYTICS_ENABLED] ?: true,
                isCrashReportingEnabled = preferences[PreferencesKeys.CRASH_REPORTING_ENABLED] ?: true,
                isAutoUpdateEnabled = preferences[PreferencesKeys.AUTO_UPDATE_ENABLED] ?: true,
                isDataSaverEnabled = preferences[PreferencesKeys.DATA_SAVER_ENABLED] ?: false,
                isHighContrastEnabled = preferences[PreferencesKeys.HIGH_CONTRAST_ENABLED] ?: false,
                isLargeTextEnabled = preferences[PreferencesKeys.LARGE_TEXT_ENABLED] ?: false,
                isBoldTextEnabled = preferences[PreferencesKeys.BOLD_TEXT_ENABLED] ?: false,
                isScreenReaderEnabled = preferences[PreferencesKeys.SCREEN_READER_ENABLED] ?: false,
                isHapticFeedbackEnabled = preferences[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED] ?: true,
                isSoundEnabled = preferences[PreferencesKeys.SOUND_ENABLED] ?: true,
                isVibrationEnabled = preferences[PreferencesKeys.VIBRATION_ENABLED] ?: true,
                isAutoLockEnabled = preferences[PreferencesKeys.AUTO_LOCK_ENABLED] ?: false,
                autoLockTimeout = preferences[PreferencesKeys.AUTO_LOCK_TIMEOUT] ?: 5,
                language = preferences[PreferencesKeys.LANGUAGE] ?: "English"
            )
        }.first()
    }

    override suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = enabled
        }
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_ENABLED] = enabled
        }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    override suspend fun setLocationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOCATION_ENABLED] = enabled
        }
    }

    override suspend fun setAnalyticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANALYTICS_ENABLED] = enabled
        }
    }

    override suspend fun setCrashReportingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CRASH_REPORTING_ENABLED] = enabled
        }
    }

    override suspend fun setAutoUpdateEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_UPDATE_ENABLED] = enabled
        }
    }

    override suspend fun setDataSaverEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DATA_SAVER_ENABLED] = enabled
        }
    }

    override suspend fun setHighContrastEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIGH_CONTRAST_ENABLED] = enabled
        }
    }

    override suspend fun setLargeTextEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LARGE_TEXT_ENABLED] = enabled
        }
    }

    override suspend fun setBoldTextEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BOLD_TEXT_ENABLED] = enabled
        }
    }

    override suspend fun setScreenReaderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SCREEN_READER_ENABLED] = enabled
        }
    }

    override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED] = enabled
        }
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.VIBRATION_ENABLED] = enabled
        }
    }

    override suspend fun setAutoLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_LOCK_ENABLED] = enabled
        }
    }

    override suspend fun setAutoLockTimeout(timeout: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_LOCK_TIMEOUT] = timeout
        }
    }

    override suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language
        }
    }

    override suspend fun resetSettings() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    override suspend fun logout() {
        // TODO: Implement logout logic
        // This might involve clearing preferences, tokens, etc.
    }
} 