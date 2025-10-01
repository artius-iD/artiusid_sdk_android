/*
 * File: EnvironmentManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.artiusid.sdk.services.APIManager
import com.artiusid.sdk.util.DeviceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EnvironmentManager(private val context: Context) {
    companion object {
        private const val TAG = "EnvironmentManager"
        private const val PREFS_NAME = "environment_prefs"
        private const val KEY_ENVIRONMENT = "current_environment"
        private const val DEFAULT_ENVIRONMENT = "stage"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Get current environment
    fun getCurrentEnvironment(): String {
        return prefs.getString(KEY_ENVIRONMENT, DEFAULT_ENVIRONMENT) ?: DEFAULT_ENVIRONMENT
    }

    // Set current environment
    fun setCurrentEnvironment(environment: String) {
        Log.d(TAG, "Setting environment to: $environment")
        prefs.edit().putString(KEY_ENVIRONMENT, environment).apply()
    }

    // Call this after setCurrentEnvironment to clear and reload certificate for new environment
    fun handleEnvironmentChange() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiManager = APIManager(context)
                apiManager.clearAndReloadIdentity()
                val deviceId = DeviceUtils.getDeviceId(context)
                val certUrl = buildEndpointURL(ServiceType.LOAD_CERTIFICATE, getCurrentEnvironment())
                val subject = "CN=$deviceId, O=ArtiusID, C=US"
                Log.d(TAG, "Re-registering certificate for new environment: $certUrl")
                apiManager.ensureCertificate(deviceId, certUrl)
                Log.i(TAG, "Certificate re-registration for new environment completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error during certificate re-registration for environment change", e)
            }
        }
    }

    // Build endpoint URL exactly like iOS
    fun buildEndpointURL(serviceType: ServiceType, environment: String): String {
        return when (serviceType) {
            ServiceType.LOAD_CERTIFICATE -> "https://service-registration.$environment.artiusid.dev"
            ServiceType.VERIFICATION -> "https://service-mobile.$environment.artiusid.dev"
        }
    }
}

enum class ServiceType {
    LOAD_CERTIFICATE,
    VERIFICATION
} 