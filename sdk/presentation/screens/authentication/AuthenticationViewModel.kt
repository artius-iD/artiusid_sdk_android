/*
 * File: AuthenticationViewModel.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.authentication

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artiusid.sdk.data.model.AuthenticationRequest
import com.artiusid.sdk.utils.FirebaseTokenManager
import com.artiusid.sdk.utils.VerificationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthenticationUiState {
    object Processing : AuthenticationUiState()
    object Success : AuthenticationUiState()
    object BiometricRequired : AuthenticationUiState()
    data class Error(val message: String) : AuthenticationUiState()
}

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val apiService: com.artiusid.sdk.data.api.ApiService
) : ViewModel() {

    companion object {
        private const val TAG = "AuthenticationViewModel"
    }

    private val _uiState = MutableStateFlow<AuthenticationUiState>(AuthenticationUiState.Processing)
    val uiState: StateFlow<AuthenticationUiState> = _uiState.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _currentStep = MutableStateFlow("Initializing...")
    val currentStep: StateFlow<String> = _currentStep.asStateFlow()

    fun startAuthentication(context: Context) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "=== AUTHENTICATION FLOW STARTED ===")
                _uiState.value = AuthenticationUiState.Processing
                _progress.value = 0.1f
                _currentStep.value = "Checking verification status..."
                
                // Check verification state like iOS keychain check
                val verificationStateManager = VerificationStateManager(context)
                val accountNumber = verificationStateManager.getAccountNumber()
                
                if (accountNumber.isNullOrEmpty()) {
                    Log.e(TAG, "No account number found - user not verified")
                    _uiState.value = AuthenticationUiState.Error("Please complete verification first")
                    return@launch
                }
                
                _progress.value = 0.3f
                _currentStep.value = "Retrieving device information..."
                delay(500)
                
                // Get device info like iOS
                val deviceId = android.provider.Settings.Secure.getString(
                    context.contentResolver,
                    android.provider.Settings.Secure.ANDROID_ID
                ) ?: "unknown"
                
                val deviceModel = "${Build.MODEL}; Android: ${Build.VERSION.RELEASE}"
                
                _progress.value = 0.5f
                _currentStep.value = "Getting Firebase token..."
                delay(500)
                
                // Get FCM token like iOS does (continue even if unavailable)
                val firebaseTokenManager = FirebaseTokenManager.getInstance()
                var fcmToken: String? = null
                
                try {
                    fcmToken = firebaseTokenManager?.getFCMToken()
                    if (!fcmToken.isNullOrEmpty()) {
                        Log.d(TAG, "Using FCM token for authentication")
                    } else {
                        Log.w(TAG, "No FCM token available, continuing without it (like iOS)")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to get FCM token, continuing without it: ${e.message}")
                    fcmToken = null
                }
                
                _progress.value = 0.7f
                _currentStep.value = "Authenticating with server..."
                delay(500)
                
                // Create authentication request exactly like iOS (body only contains deviceId + deviceModel)
                val authRequest = AuthenticationRequest(
                    deviceId = deviceId,
                    deviceModel = deviceModel
                )
                
                Log.d(TAG, "Authentication request: clientId=1, accountNumber=$accountNumber")
                
                _progress.value = 0.9f
                _currentStep.value = "Processing authentication..."
                
                // Call authentication API with query parameters (matching iOS exactly)
                val response = apiService.authenticate(
                    clientId = 1, // AppConstants.clientId
                    clientGroupId = 1, // AppConstants.clientGroupId
                    accountNumber = accountNumber,
                    request = authRequest
                )
                
                Log.d(TAG, "Authentication response: $response")
                
                // Parse response like iOS AuthenticationResponse.swift
                if (response.authenticationData.statusCode == 200) {
                    // Parse payload to check AccountActive like iOS
                    val payload = response.authenticationData.payload
                    
                    if (payload.isNotEmpty()) {
                        try {
                            // iOS parses as JSON array: [[String: Any]]
                            val jsonArray = org.json.JSONArray(payload as String)
                            var isAccountActive = false
                            
                            for (i in 0 until jsonArray.length()) {
                                val item = jsonArray.getJSONObject(i)
                                if (item.has("AccountActive")) {
                                    val accountActive = item.getInt("AccountActive")
                                    isAccountActive = accountActive > 0
                                    Log.d(TAG, "AccountActive status: $accountActive")
                                    break
                                }
                            }
                            
                            if (isAccountActive) {
                                _progress.value = 1.0f
                                _currentStep.value = "Account verified - requesting biometric authentication..."
                                delay(500)
                                
                                Log.d(TAG, "Account verified - requesting biometric authentication (like iOS)")
                                _uiState.value = AuthenticationUiState.BiometricRequired
                            } else {
                                Log.w(TAG, "Authentication failed - account not active")
                                verificationStateManager.clearVerificationData()
                                _uiState.value = AuthenticationUiState.Error("Account is not active")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing authentication payload", e)
                            _uiState.value = AuthenticationUiState.Error("Failed to parse authentication response")
                        }
                    } else {
                        Log.e(TAG, "Empty authentication payload")
                        _uiState.value = AuthenticationUiState.Error("Invalid authentication response")
                    }
                } else {
                    Log.w(TAG, "Authentication failed with status: ${response.authenticationData.statusCode}")
                    verificationStateManager.clearVerificationData()
                    _uiState.value = AuthenticationUiState.Error("Authentication failed: ${response.authenticationData.message}")
                }
                
                Log.d(TAG, "=== AUTHENTICATION FLOW ENDED ===")
                
            } catch (e: Exception) {
                Log.e(TAG, "Authentication error", e)
                _uiState.value = AuthenticationUiState.Error("Authentication failed: ${e.message}")
            }
        }
    }
    
    fun onBiometricSuccess() {
        Log.d(TAG, "Biometric authentication successful (like iOS)")
        _uiState.value = AuthenticationUiState.Success
        Log.d(TAG, "=== AUTHENTICATION COMPLETE ===")
    }
    
    fun onBiometricFailure() {
        Log.w(TAG, "Biometric authentication failed (like iOS)")
        _uiState.value = AuthenticationUiState.Error("Biometric authentication failed")
    }
} 