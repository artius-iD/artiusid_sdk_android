package com.artiusid.sdk.ui.screens.authentication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artiusid.sdk.models.AuthenticationRequest
import com.artiusid.sdk.utils.FirebaseTokenManager
import com.artiusid.sdk.utils.VerificationStateManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthenticationUiState {
    object Processing : AuthenticationUiState()
    object Success : AuthenticationUiState()
    object BiometricRequired : AuthenticationUiState()
    data class Error(val message: String) : AuthenticationUiState()
}

class AuthenticationViewModel : ViewModel() {

    companion object {
        private const val TAG = "AuthenticationViewModel"
    }

    private val _uiState = MutableStateFlow<AuthenticationUiState>(AuthenticationUiState.Processing)
    val uiState: StateFlow<AuthenticationUiState> = _uiState.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _currentStep = MutableStateFlow("")
    val currentStep: StateFlow<String> = _currentStep.asStateFlow()

    // Note: In a real implementation, these would be injected with proper context
    private val firebaseTokenManager = null // FirebaseTokenManager requires context
    private val verificationStateManager = null // VerificationStateManager requires context

    init {
        startAuthentication()
    }

    fun startAuthentication() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting authentication process")
                
                _progress.value = 0.1f
                _currentStep.value = "Initializing authentication..."
                delay(500)

                _progress.value = 0.3f
                _currentStep.value = "Getting Firebase token..."
                
                // Simulate getting Firebase token since manager is null
                val firebaseToken = "simulated_firebase_token_${System.currentTimeMillis()}"
                Log.d(TAG, "Firebase token simulated: ${firebaseToken.take(20)}...")
                delay(500)

                _progress.value = 0.5f
                _currentStep.value = "Preparing authentication request..."
                
                // Create authentication request
                val sessionId = "auth_${System.currentTimeMillis()}"
                val authRequest = AuthenticationRequest(
                    username = "demo_user",
                    password = "demo_password",
                    biometricData = null,
                    deviceId = "sdk_device_${System.currentTimeMillis()}"
                )
                
                Log.d(TAG, "Authentication request prepared")
                
                _progress.value = 0.9f
                _currentStep.value = "Processing authentication..."
                
                // Simulate authentication API call
                delay(1000)
                val response = com.artiusid.sdk.data.models.AuthenticationResponse(
                    success = true,
                    message = "Authentication successful",
                    token = "auth_token_${System.currentTimeMillis()}",
                    userId = "demo_user",
                    sessionId = sessionId
                )
                
                Log.d(TAG, "Authentication response: $response")
                
                // Parse response
                if (response.success) {
                    _progress.value = 1.0f
                    _currentStep.value = "Account verified - requesting biometric authentication..."
                    delay(500)
                    
                    Log.d(TAG, "Account verified - requesting biometric authentication")
                    _uiState.value = AuthenticationUiState.BiometricRequired
                } else {
                    Log.w(TAG, "Authentication failed")
                    // verificationStateManager?.clearVerificationData() // Commented out since manager is null
                    _uiState.value = AuthenticationUiState.Error("Authentication failed")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Authentication error", e)
                // verificationStateManager?.clearVerificationData() // Commented out since manager is null
                _uiState.value = AuthenticationUiState.Error("Authentication failed: ${e.message}")
            }
        }
    }

    fun completeBiometricAuthentication() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Completing biometric authentication")
                _currentStep.value = "Completing authentication..."
                delay(1000)
                
                _uiState.value = AuthenticationUiState.Success
                Log.d(TAG, "Authentication completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Biometric authentication error", e)
                _uiState.value = AuthenticationUiState.Error("Biometric authentication failed: ${e.message}")
            }
        }
    }

    fun retry() {
        _uiState.value = AuthenticationUiState.Processing
        _progress.value = 0f
        _currentStep.value = ""
        startAuthentication()
    }
    
    fun onBiometricSuccess() {
        Log.d(TAG, "Biometric authentication successful")
        _progress.value = 1.0f
        _currentStep.value = "Authentication complete!"
        _uiState.value = AuthenticationUiState.Success
    }
    
    fun onBiometricFailure() {
        Log.d(TAG, "Biometric authentication failed")
        _currentStep.value = "Biometric authentication failed. Please try again."
        _uiState.value = AuthenticationUiState.Error("Biometric authentication failed")
    }
    
}