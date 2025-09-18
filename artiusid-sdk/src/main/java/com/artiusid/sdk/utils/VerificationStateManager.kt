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
        isAccountActive: Boolean = true
    ) {
        try {
            encryptedPrefs.edit().apply {
                putString(KEY_ACCOUNT_NUMBER, accountNumber)
                putBoolean(KEY_IS_ACCOUNT_ACTIVE, isAccountActive)
                putLong(KEY_VERIFIED_TIMESTAMP, System.currentTimeMillis())
                accountFullName?.let { putString(KEY_ACCOUNT_FULL_NAME, it) }
                apply()
            }
            Log.d(TAG, "✅ Stored verification success in keychain: accountNumber=$accountNumber, isActive=$isAccountActive")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to store verification success in keychain", e)
        }
    }
    
    // Check if user is verified (has account number like iOS keychain check)
    fun isVerified(): Boolean {
        return try {
            val accountNumber = encryptedPrefs.getString(KEY_ACCOUNT_NUMBER, null)
            val isActive = encryptedPrefs.getBoolean(KEY_IS_ACCOUNT_ACTIVE, false)
            !accountNumber.isNullOrEmpty() && isActive
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check verification status", e)
            false
        }
    }
    
    // Get stored account number (like iOS keychain["verification"])
    fun getAccountNumber(): String? {
        return try {
            encryptedPrefs.getString(KEY_ACCOUNT_NUMBER, null)
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
    fun clearVerificationData() {
        try {
            encryptedPrefs.edit().apply {
                remove(KEY_ACCOUNT_NUMBER)
                remove(KEY_ACCOUNT_FULL_NAME)
                remove(KEY_IS_ACCOUNT_ACTIVE)
                remove(KEY_VERIFIED_TIMESTAMP)
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