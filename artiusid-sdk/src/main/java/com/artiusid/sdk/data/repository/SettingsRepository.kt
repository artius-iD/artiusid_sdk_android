/*
 * File: SettingsRepository.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.artiusid.sdk.data.api.ApiService
import com.artiusid.sdk.utils.SendApprovalRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsRepository(
    private val context: Context,
    private val apiService: ApiService? = null
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    
    companion object {
        private const val TAG = "SettingsRepository"
    }

    // --- Environment ---
    fun getEnvironment(): String = prefs.getString("environment", "Staging") ?: "Staging"
    fun setEnvironment(env: String) = prefs.edit().putString("environment", env).apply()

    // --- Toggles ---
    fun getBoolean(key: String, default: Boolean = false) = prefs.getBoolean(key, default)
    fun setBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()

    // --- Log Level ---
    fun getLogLevel(): String = prefs.getString("logLevel", "Debug") ?: "Debug"
    fun setLogLevel(level: String) = prefs.edit().putString("logLevel", level).apply()

    // --- Account Number ---
    fun getAccountNumber(): String = prefs.getString("accountNumber", "") ?: ""
    fun setAccountNumber(value: String) = prefs.edit().putString("accountNumber", value).apply()

    // --- Certificate Management (placeholder) ---
    suspend fun clearAndRegenerateCertificate(): Boolean = withContext(Dispatchers.IO) {
        // TODO: Implement actual certificate logic
        Thread.sleep(1000)
        true
    }

    // --- Log Management ---
    fun getLogs(): List<String> = LogManager.getLogs()
    fun clearLogs() = LogManager.clearLogs()
    fun addLog(message: String, level: LogManager.LogLevel = LogManager.LogLevel.INFO, source: String = "App") = 
        LogManager.addLog(message, level, source)

    // --- Approval Request (real implementation like iOS) ---
    suspend fun sendApprovalRequest(): Triple<Boolean, String, Int?> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (apiService == null) {
                Log.e(TAG, "ApiService not available for approval request")
                LogManager.logError("Error: ApiService not configured", TAG)
                Triple(false, "Service not available", null)
            } else {
                val sendApprovalRequest = SendApprovalRequest(apiService, context)
                val (success, requestId) = sendApprovalRequest.send()
                
                if (success) {
                    val message = "Approval request sent successfully."
                    LogManager.logInfo(message, TAG)
                    Log.d(TAG, message)
                    Triple(true, message, requestId)
                } else {
                    val message = "Failed to send approval request."
                    LogManager.logError(message, TAG)
                    Log.e(TAG, message)
                    Triple(false, message, null)
                }
            }
        } catch (e: Exception) {
            val message = "Approval request error: ${e.localizedMessage}"
            LogManager.logError(message, TAG)
            Log.e(TAG, "Approval request exception", e)
            Triple(false, message, null)
        }
    }
} 