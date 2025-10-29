/*
 * File: VerificationStateManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import android.util.Log

class VerificationStateManager(private val context: Context) {
    
    companion object {
        private const val TAG = "VerificationStateManager"
        private const val PREFS_NAME = "verification_state_prefs"
        private const val KEY_ACCOUNT_NUMBER = "verification"
        private const val KEY_ACCOUNT_FULL_NAME = "accountFullName"
        private const val KEY_IS_ACCOUNT_ACTIVE = "isAccountActive"
        private const val KEY_VERIFIED_TIMESTAMP = "verifiedTimestamp"
        
        // 🚨 CRITICAL: Environment-specific keys for credential isolation
        private const val KEY_CURRENT_ENVIRONMENT = "currentEnvironment"
        private const val KEY_ENVIRONMENT_PREFIX = "env_"
    }
    
    /**
     * 🚨 CRITICAL: Get current environment from stored credentials
     * This determines which environment the user is verified in
     */
    fun getCurrentEnvironmentFromCredentials(): String? {
        return try {
            // First check if there's a stored current environment
            val storedEnv = encryptedPrefs.getString(KEY_CURRENT_ENVIRONMENT, null)
            if (!storedEnv.isNullOrEmpty()) {
                android.util.Log.d(TAG, "🚨 Found stored current environment: $storedEnv")
                return storedEnv
            }
            
            // If no stored environment, check which environments have credentials
            val environments = listOf("Sandbox", "Development", "Staging")
            for (env in environments) {
                val envAccountNumber = encryptedPrefs.getString("${KEY_ENVIRONMENT_PREFIX}${env}_${KEY_ACCOUNT_NUMBER}", null)
                if (!envAccountNumber.isNullOrEmpty()) {
                    android.util.Log.d(TAG, "🚨 Auto-detected environment from credentials: $env")
                    // Store this as current environment for future use
                    encryptedPrefs.edit().putString(KEY_CURRENT_ENVIRONMENT, env).apply()
                    return env
                }
            }
            
            android.util.Log.d(TAG, "🚨 No environment credentials found")
            return null
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Failed to get current environment from credentials", e)
            null
        }
    }
    
    /**
     * 🚨 CRITICAL: Set current environment for credential storage
     */
    fun setCurrentEnvironment(environment: String) {
        try {
            encryptedPrefs.edit().putString(KEY_CURRENT_ENVIRONMENT, environment).apply()
            android.util.Log.d(TAG, "🚨 Set current environment: $environment")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "❌ Failed to set current environment", e)
        }
    }
    
    /**
     * 🚨 CRITICAL: Get environment-specific key for credential storage
     */
    private fun getEnvironmentKey(baseKey: String, environment: String? = null): String {
        val env = environment ?: getCurrentEnvironmentFromCredentials() ?: "Sandbox"
        return "${KEY_ENVIRONMENT_PREFIX}${env}_${baseKey}"
    }
    
    private val encryptedPrefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    // Store verification success data like iOS keychain
    fun storeVerificationSuccess(
        accountNumber: String,
        accountFullName: String? = null,
        isAccountActive: Boolean = true,
        environment: String? = null
    ) {
        try {
            // 🚨 CRITICAL: Store with environment-specific keys
            val currentEnv = environment ?: getCurrentEnvironmentFromCredentials() ?: "Sandbox"
            
            encryptedPrefs.edit().apply {
                // Store with environment-specific keys
                putString(getEnvironmentKey(KEY_ACCOUNT_NUMBER, currentEnv), accountNumber)
                putBoolean(getEnvironmentKey(KEY_IS_ACCOUNT_ACTIVE, currentEnv), isAccountActive)
                putLong(getEnvironmentKey(KEY_VERIFIED_TIMESTAMP, currentEnv), System.currentTimeMillis())
                accountFullName?.let { putString(getEnvironmentKey(KEY_ACCOUNT_FULL_NAME, currentEnv), it) }
                
                // Also store legacy keys for backward compatibility
                putString(KEY_ACCOUNT_NUMBER, accountNumber)
                putBoolean(KEY_IS_ACCOUNT_ACTIVE, isAccountActive)
                putLong(KEY_VERIFIED_TIMESTAMP, System.currentTimeMillis())
                accountFullName?.let { putString(KEY_ACCOUNT_FULL_NAME, it) }
                
                // Set current environment
                putString(KEY_CURRENT_ENVIRONMENT, currentEnv)
                apply()
            }
            Log.d(TAG, "✅ Stored verification success in keychain for environment $currentEnv: accountNumber=$accountNumber, isActive=$isAccountActive")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to store verification success in keychain", e)
        }
    }
    
    // Check if user is verified (has account number like iOS keychain check)
    fun isVerified(environment: String? = null): Boolean {
        return try {
            // 🚨 CRITICAL: Check environment-specific verification first
            val currentEnv = environment ?: getCurrentEnvironmentFromCredentials()
            if (currentEnv != null) {
                val envAccountNumber = encryptedPrefs.getString(getEnvironmentKey(KEY_ACCOUNT_NUMBER, currentEnv), null)
                val envIsActive = encryptedPrefs.getBoolean(getEnvironmentKey(KEY_IS_ACCOUNT_ACTIVE, currentEnv), false)
                if (!envAccountNumber.isNullOrEmpty() && envIsActive) {
                    android.util.Log.d(TAG, "✅ User verified in environment: $currentEnv")
                    return true
                }
            }
            
            // Fallback to legacy keys for backward compatibility
            val accountNumber = encryptedPrefs.getString(KEY_ACCOUNT_NUMBER, null)
            val isActive = encryptedPrefs.getBoolean(KEY_IS_ACCOUNT_ACTIVE, false)
            val result = !accountNumber.isNullOrEmpty() && isActive
            if (result) {
                android.util.Log.d(TAG, "✅ User verified (legacy storage)")
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check verification status", e)
            false
        }
    }
    
    // Get stored account number (like iOS keychain["verification"])
    fun getAccountNumber(environment: String? = null): String? {
        return try {
            // 🚨 CRITICAL: Get environment-specific account number first
            val currentEnv = environment ?: getCurrentEnvironmentFromCredentials()
            if (currentEnv != null) {
                val envAccountNumber = encryptedPrefs.getString(getEnvironmentKey(KEY_ACCOUNT_NUMBER, currentEnv), null)
                if (!envAccountNumber.isNullOrEmpty()) {
                    android.util.Log.d(TAG, "✅ Retrieved account number for environment $currentEnv: $envAccountNumber")
                    return envAccountNumber
                }
            }
            
            // Fallback to legacy key for backward compatibility
            val legacyAccountNumber = encryptedPrefs.getString(KEY_ACCOUNT_NUMBER, null)
            if (!legacyAccountNumber.isNullOrEmpty()) {
                android.util.Log.d(TAG, "✅ Retrieved account number (legacy storage): $legacyAccountNumber")
            }
            legacyAccountNumber
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get account number", e)
            null
        }
    }
    
    // Get stored account full name (like iOS AppStorage accountFullName)
    fun getAccountFullName(): String? {
        return try {
            encryptedPrefs.getString(KEY_ACCOUNT_FULL_NAME, null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get account full name", e)
            null
        }
    }
    
    // Set account full name (like iOS AppStorage)
    fun setAccountFullName(fullName: String) {
        try {
            encryptedPrefs.edit().putString(KEY_ACCOUNT_FULL_NAME, fullName).apply()
            Log.d(TAG, "Stored account full name: $fullName")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store account full name", e)
        }
    }
    
    // Check if account is active
    fun isAccountActive(): Boolean {
        return try {
            encryptedPrefs.getBoolean(KEY_IS_ACCOUNT_ACTIVE, false)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check account active status", e)
            false
        }
    }
    
    // Clear verification data (like iOS keychain["verification"] = nil)
    fun clearVerificationData(environment: String? = null) {
        try {
            encryptedPrefs.edit().apply {
                if (environment != null) {
                    // Clear specific environment data
                    remove(getEnvironmentKey(KEY_ACCOUNT_NUMBER, environment))
                    remove(getEnvironmentKey(KEY_ACCOUNT_FULL_NAME, environment))
                    remove(getEnvironmentKey(KEY_IS_ACCOUNT_ACTIVE, environment))
                    remove(getEnvironmentKey(KEY_VERIFIED_TIMESTAMP, environment))
                    android.util.Log.d(TAG, "🚨 Cleared verification data for environment: $environment")
                } else {
                    // Clear all environment data
                    val environments = listOf("Sandbox", "Development", "Staging")
                    for (env in environments) {
                        remove(getEnvironmentKey(KEY_ACCOUNT_NUMBER, env))
                        remove(getEnvironmentKey(KEY_ACCOUNT_FULL_NAME, env))
                        remove(getEnvironmentKey(KEY_IS_ACCOUNT_ACTIVE, env))
                        remove(getEnvironmentKey(KEY_VERIFIED_TIMESTAMP, env))
                    }
                    // Also clear legacy keys
                    remove(KEY_ACCOUNT_NUMBER)
                    remove(KEY_ACCOUNT_FULL_NAME)
                    remove(KEY_IS_ACCOUNT_ACTIVE)
                    remove(KEY_VERIFIED_TIMESTAMP)
                    remove(KEY_CURRENT_ENVIRONMENT)
                    android.util.Log.d(TAG, "🚨 Cleared ALL verification data for all environments")
                }
                apply()
            }
            Log.d(TAG, "Cleared verification data")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear verification data", e)
        }
    }
    
    // Get verification timestamp
    fun getVerificationTimestamp(): Long {
        return try {
            encryptedPrefs.getLong(KEY_VERIFIED_TIMESTAMP, 0L)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get verification timestamp", e)
            0L
        }
    }
}