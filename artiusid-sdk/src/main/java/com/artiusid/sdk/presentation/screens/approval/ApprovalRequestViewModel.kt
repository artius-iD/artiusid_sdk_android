/*
 * File: ApprovalRequestViewModel.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.approval

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.collectAsState
import com.artiusid.sdk.data.api.ApiService
import com.artiusid.sdk.utils.VerificationStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ApprovalRequestUiState {
    object Authenticating : ApprovalRequestUiState()
    object Authenticated : ApprovalRequestUiState()
    object AuthenticationFailed : ApprovalRequestUiState()
    object AccountInactive : ApprovalRequestUiState()
}

/**
 * Matches iOS ApprovalRequestView authentication logic
 */
@HiltViewModel
class ApprovalRequestViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
    
    companion object {
        private const val TAG = "ApprovalRequestViewModel"
    }
    
    private val _uiState = MutableStateFlow<ApprovalRequestUiState>(ApprovalRequestUiState.Authenticating)
    val uiState: StateFlow<ApprovalRequestUiState> = _uiState.asStateFlow()
    
    /**
     * Reset the ViewModel state for a new approval request
     * This ensures each approval request starts fresh
     */
    fun resetForNewRequest() {
        Log.d(TAG, "🔄 Resetting ViewModel state for new approval request")
        _uiState.value = ApprovalRequestUiState.Authenticating
    }
    
    fun authenticate(context: Context) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting approval request authentication")
                
                // Check if account is verified and active (like iOS isAccountActive check)
                val verificationStateManager = VerificationStateManager(context)
                val isVerified = verificationStateManager.isVerified()
                val isAccountActive = verificationStateManager.isAccountActive()
                
                if (!isVerified || !isAccountActive) {
                    Log.w(TAG, "Account not verified or inactive: verified=$isVerified, active=$isAccountActive")
                    _uiState.value = ApprovalRequestUiState.AccountInactive
                    return@launch
                }
                
                // First authenticate the account via API (like iOS task block)
                Log.d(TAG, "Performing account authentication...")
                // For demo purposes, we'll skip the full authentication API call
                // and proceed directly to biometric authentication
                // In a full implementation, you'd call the authentication API here
                
                // Check if Face ID authentication is available (strong biometrics only)
                val biometricManager = BiometricManager.from(context)
                when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                    BiometricManager.BIOMETRIC_SUCCESS -> {
                        Log.d(TAG, "Face ID authentication available")
                        // CRITICAL: Don't set Authenticated yet! Wait for actual Face ID success
                        // iOS waits for Face ID success before enabling buttons
                        // Stay in Authenticating state until Face ID auth completes
                    }
                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                        Log.w(TAG, "No Face ID hardware available - proceeding without biometric")
                        // Allow authentication without Face ID if hardware not available
                        _uiState.value = ApprovalRequestUiState.Authenticated
                    }
                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                        Log.w(TAG, "Face ID hardware unavailable - proceeding without biometric")
                        _uiState.value = ApprovalRequestUiState.Authenticated
                    }
                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                        Log.w(TAG, "No Face ID enrolled - user needs to set up Face ID in device settings")
                        _uiState.value = ApprovalRequestUiState.AuthenticationFailed
                    }
                    else -> {
                        Log.e(TAG, "Face ID authentication not possible")
                        _uiState.value = ApprovalRequestUiState.AuthenticationFailed
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Authentication error", e)
                _uiState.value = ApprovalRequestUiState.AuthenticationFailed
            }
        }
    }
    
    /**
     * Called when biometric authentication succeeds (like iOS isUnlocked = true, isButtonEnabled = true)
     */
    fun onBiometricAuthenticationSuccess() {
        Log.d(TAG, "🔓 Biometric authentication SUCCEEDED - changing state to Authenticated")
        Log.d(TAG, "Current state: ${_uiState.value}, New state: Authenticated")
        _uiState.value = ApprovalRequestUiState.Authenticated
        Log.d(TAG, "State change completed - approval buttons should now be visible")
    }
    
    /**
     * Called when biometric authentication fails (like iOS isUnlocked = false, failedAuth = true)
     */
    fun onBiometricAuthenticationFailed() {
        Log.w(TAG, "🔒 Biometric authentication FAILED - changing state to AuthenticationFailed")
        Log.w(TAG, "Current state: ${_uiState.value}, New state: AuthenticationFailed")
        _uiState.value = ApprovalRequestUiState.AuthenticationFailed
        Log.w(TAG, "State change completed - showing failure message")
    }
} 