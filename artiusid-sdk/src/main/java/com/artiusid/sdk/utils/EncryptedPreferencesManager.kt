/*
 * File: EncryptedPreferencesManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 * 
 * CRITICAL BUG FIX: SDK v1.2.38
 * Handles EncryptedSharedPreferences corruption and automatic recovery
 * 
 * Issue: When app clears certificate via clearCertificate(), the Android Keystore
 * master keys are deleted but encrypted data remains, causing AEADBadTagException
 * on all subsequent access attempts.
 * 
 * Solution: Detect AEADBadTagException and automatically clean up corrupted
 * encryption state, then recreate fresh EncryptedSharedPreferences.
 */

package com.artiusid.sdk.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.io.File
import java.security.KeyStore
import javax.crypto.AEADBadTagException

/**
 * Manages EncryptedSharedPreferences with automatic corruption detection and recovery.
 * 
 * This utility class handles the critical bug where EncryptedSharedPreferences becomes
 * corrupted after master keys are deleted from Android Keystore, causing permanent
 * AEADBadTagException errors that block certificate registration.
 * 
 * Key Features:
 * - Automatic detection of AEADBadTagException corruption
 * - Safe cleanup of corrupted encryption state
 * - Automatic recreation of fresh EncryptedSharedPreferences
 * - Comprehensive logging for debugging
 * - Production-ready error handling
 */
class EncryptedPreferencesManager private constructor() {
    
    companion object {
        private const val TAG = "EncryptedPrefsManager"
        
        /**
         * Create or recover EncryptedSharedPreferences with automatic corruption handling.
         * 
         * This method will:
         * 1. Try to create EncryptedSharedPreferences normally
         * 2. If AEADBadTagException occurs, detect corruption and clean up
         * 3. Delete corrupted SharedPreferences XML file
         * 4. Delete corrupted master key from Android Keystore
         * 5. Recreate fresh EncryptedSharedPreferences
         * 
         * @param context Application context
         * @param prefsName Name of the SharedPreferences file
         * @return Working EncryptedSharedPreferences instance
         * @throws Exception if recovery fails after cleanup
         */
        fun createOrRecover(
            context: Context,
            prefsName: String
        ): SharedPreferences {
            return try {
                // Try normal creation first
                createEncryptedPrefs(context, prefsName)
            } catch (e: AEADBadTagException) {
                Log.w(TAG, "🚨 ========================================")
                Log.w(TAG, "🚨 CORRUPTION DETECTED: AEADBadTagException")
                Log.w(TAG, "🚨 EncryptedSharedPreferences corrupted for: $prefsName")
                Log.w(TAG, "🚨 Initiating automatic recovery...")
                Log.w(TAG, "🚨 ========================================")
                
                // Attempt to recover from corruption
                recoverFromCorruption(context, prefsName, e)
                
            } catch (e: Exception) {
                // Check if the root cause is AEADBadTagException
                val rootCause = findAEADBadTagException(e)
                if (rootCause != null) {
                    Log.w(TAG, "🚨 ========================================")
                    Log.w(TAG, "🚨 CORRUPTION DETECTED: AEADBadTagException (nested)")
                    Log.w(TAG, "🚨 EncryptedSharedPreferences corrupted for: $prefsName")
                    Log.w(TAG, "🚨 Root cause: ${rootCause.message}")
                    Log.w(TAG, "🚨 Initiating automatic recovery...")
                    Log.w(TAG, "🚨 ========================================")
                    
                    // Attempt to recover from corruption
                    recoverFromCorruption(context, prefsName, rootCause)
                } else {
                    // Different error, rethrow
                    Log.e(TAG, "❌ Failed to create EncryptedSharedPreferences for $prefsName", e)
                    throw e
                }
            }
        }
        
        /**
         * Create EncryptedSharedPreferences using standard configuration
         */
        private fun createEncryptedPrefs(
            context: Context,
            prefsName: String
        ): SharedPreferences {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            return EncryptedSharedPreferences.create(
                prefsName,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        
        /**
         * Recover from EncryptedSharedPreferences corruption by cleaning up and recreating
         */
        private fun recoverFromCorruption(
            context: Context,
            prefsName: String,
            originalException: AEADBadTagException
        ): SharedPreferences {
            try {
                Log.i(TAG, "🔧 Starting corruption recovery for: $prefsName")
                
                // Step 1: Delete corrupted SharedPreferences XML file
                val success1 = deleteCorruptedPrefsFile(context, prefsName)
                
                // Step 2: Delete corrupted master key from Android Keystore
                val success2 = deleteCorruptedMasterKey()
                
                Log.i(TAG, "🔧 Cleanup results - Prefs file: $success1, Master key: $success2")
                
                // Step 3: Recreate fresh EncryptedSharedPreferences
                Log.i(TAG, "🔧 Recreating fresh EncryptedSharedPreferences...")
                val recoveredPrefs = createEncryptedPrefs(context, prefsName)
                
                Log.i(TAG, "✅ ========================================")
                Log.i(TAG, "✅ RECOVERY SUCCESSFUL!")
                Log.i(TAG, "✅ EncryptedSharedPreferences recreated for: $prefsName")
                Log.i(TAG, "✅ Certificate registration should now work")
                Log.i(TAG, "✅ ========================================")
                
                return recoveredPrefs
                
            } catch (recoveryError: Exception) {
                Log.e(TAG, "❌ ========================================")
                Log.e(TAG, "❌ RECOVERY FAILED!")
                Log.e(TAG, "❌ Could not recover EncryptedSharedPreferences for: $prefsName")
                Log.e(TAG, "❌ Original error: ${originalException.message}")
                Log.e(TAG, "❌ Recovery error: ${recoveryError.message}")
                Log.e(TAG, "❌ ========================================")
                
                // Rethrow the recovery error with context
                throw Exception(
                    "Failed to recover from EncryptedSharedPreferences corruption for $prefsName. " +
                    "Original: ${originalException.message}, Recovery: ${recoveryError.message}",
                    recoveryError
                )
            }
        }
        
        /**
         * Delete the corrupted SharedPreferences XML file
         */
        private fun deleteCorruptedPrefsFile(context: Context, prefsName: String): Boolean {
            return try {
                val prefsFile = File(context.applicationInfo.dataDir, "shared_prefs/${prefsName}.xml")
                if (prefsFile.exists()) {
                    val deleted = prefsFile.delete()
                    if (deleted) {
                        Log.i(TAG, "🗑️ Deleted corrupted prefs file: ${prefsFile.absolutePath}")
                    } else {
                        Log.w(TAG, "⚠️ Failed to delete corrupted prefs file: ${prefsFile.absolutePath}")
                    }
                    deleted
                } else {
                    Log.d(TAG, "📁 Corrupted prefs file does not exist: ${prefsFile.absolutePath}")
                    true // Consider success if file doesn't exist
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception deleting corrupted prefs file", e)
                false
            }
        }
        
        /**
         * Delete the corrupted master key from Android Keystore
         */
        private fun deleteCorruptedMasterKey(): Boolean {
            return try {
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)
                
                val masterKeyAlias = "_androidx_security_master_key_"
                if (keyStore.containsAlias(masterKeyAlias)) {
                    keyStore.deleteEntry(masterKeyAlias)
                    Log.i(TAG, "🔑 Deleted corrupted master key: $masterKeyAlias")
                    true
                } else {
                    Log.d(TAG, "🔑 Corrupted master key does not exist: $masterKeyAlias")
                    true // Consider success if key doesn't exist
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception deleting corrupted master key", e)
                false
            }
        }
        
        /**
         * Recursively search for AEADBadTagException in the exception chain
         */
        private fun findAEADBadTagException(throwable: Throwable?): AEADBadTagException? {
            if (throwable == null) return null
            if (throwable is AEADBadTagException) return throwable
            
            // Check the cause chain
            return findAEADBadTagException(throwable.cause)
        }
        
        /**
         * Utility method to safely get a string from EncryptedSharedPreferences
         * with automatic corruption recovery.
         * 
         * @param context Application context
         * @param prefsName SharedPreferences name
         * @param key Key to retrieve
         * @param defaultValue Default value if key not found
         * @return String value or default
         */
        fun safeGetString(
            context: Context,
            prefsName: String,
            key: String,
            defaultValue: String? = null
        ): String? {
            return try {
                val prefs = createOrRecover(context, prefsName)
                prefs.getString(key, defaultValue)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to safely get string for key: $key from $prefsName", e)
                defaultValue
            }
        }
        
        /**
         * Utility method to safely put a string to EncryptedSharedPreferences
         * with automatic corruption recovery.
         * 
         * @param context Application context
         * @param prefsName SharedPreferences name
         * @param key Key to store
         * @param value Value to store
         * @return true if successful, false otherwise
         */
        fun safePutString(
            context: Context,
            prefsName: String,
            key: String,
            value: String?
        ): Boolean {
            return try {
                val prefs = createOrRecover(context, prefsName)
                prefs.edit().putString(key, value).apply()
                true
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to safely put string for key: $key to $prefsName", e)
                false
            }
        }
        
        /**
         * Utility method to safely remove a key from EncryptedSharedPreferences
         * with automatic corruption recovery.
         * 
         * @param context Application context
         * @param prefsName SharedPreferences name
         * @param key Key to remove
         * @return true if successful, false otherwise
         */
        fun safeRemove(
            context: Context,
            prefsName: String,
            key: String
        ): Boolean {
            return try {
                val prefs = createOrRecover(context, prefsName)
                prefs.edit().remove(key).apply()
                true
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to safely remove key: $key from $prefsName", e)
                false
            }
        }
    }
}
