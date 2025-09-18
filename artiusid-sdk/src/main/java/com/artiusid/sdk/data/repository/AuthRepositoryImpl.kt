/*
 * File: AuthRepositoryImpl.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.repository

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.artiusid.sdk.data.api.ApiService
import com.artiusid.sdk.data.model.AuthenticationRequest
import com.artiusid.sdk.domain.repository.AuthRepository
import com.artiusid.sdk.util.DeviceUtils
import com.artiusid.sdk.utils.FirebaseTokenManager
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val context: Context
) : AuthRepository {
    
    companion object {
        private const val TAG = "AuthRepositoryImpl"
        private const val PREF_NAME = "auth_prefs"
        private const val VERIFICATION_KEY = "verification"
    }

    override suspend fun login(email: String, password: String): Boolean {
        // TODO: Implement actual login logic
        // For now, just simulate a network delay and return true
        kotlinx.coroutines.delay(1000)
        return true
    }

    override suspend fun logout() {
        // TODO: Implement actual logout logic
        // For now, just simulate a network delay
        kotlinx.coroutines.delay(1000)
    }

    override fun isUserLoggedIn(): Boolean {
        // TODO: Implement actual login check
        // For now, just return false
        return false
    }

    override suspend fun authenticate(): Boolean {
        return try {
            val deviceId = DeviceUtils.getDeviceId(context)
            val deviceModel = "${Build.MODEL}; Android: ${Build.VERSION.RELEASE}"
            
            // Get account number from secure storage (similar to iOS keychain)
            val accountNumber = getAccountNumber() ?: "DEFAULT"
            
            // Get FCM token using FirebaseTokenManager
            val tokenManager = FirebaseTokenManager.getInstance()
            val fcmToken = tokenManager?.getFCMTokenAsync()
            
            Log.d(TAG, "Authenticating with deviceId: $deviceId, deviceModel: $deviceModel, fcmToken: $fcmToken")
            
            val request = AuthenticationRequest(
                deviceId = deviceId,
                deviceModel = deviceModel
            )
            
            val response = apiService.authenticate(
                clientId = 1, // Match iOS AppConstants.clientId
                clientGroupId = 1, // Match iOS AppConstants.clientGroupId
                accountNumber = accountNumber,
                request = request
            )
            
            // Check status code similar to iOS
            if (response.authenticationData.statusCode == 200) {
                Log.i(TAG, "Authentication successful")
                true
            } else {
                Log.i(TAG, "Authentication failed with status: ${response.authenticationData.statusCode}")
                // Clear verification on failure (similar to iOS)
                clearAccountNumber()
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Authentication error: ${e.message}", e)
            false
        }
    }
    
    private fun getAccountNumber(): String? {
        return try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val prefs = EncryptedSharedPreferences.create(
                PREF_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            val accountNumber = prefs.getString(VERIFICATION_KEY, null)
            if (accountNumber.isNullOrEmpty()) null else accountNumber
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get account number: ${e.message}", e)
            null
        }
    }
    
    private fun clearAccountNumber() {
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val prefs = EncryptedSharedPreferences.create(
                PREF_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            prefs.edit().remove(VERIFICATION_KEY).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear account number: ${e.message}", e)
        }
    }
} 