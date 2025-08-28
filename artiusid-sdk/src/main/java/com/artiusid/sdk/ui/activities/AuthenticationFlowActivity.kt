package com.artiusid.sdk.ui.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.artiusid.sdk.sdk.ArtiusIDSDK
import com.artiusid.sdk.sdk.managers.SDKConfigManager
import com.artiusid.sdk.sdk.managers.AnalyticsManager
import com.artiusid.sdk.sdk.models.*
import com.artiusid.sdk.sdk.network.BiometricData
import com.artiusid.sdk.sdk.ui.theme.SDKThemeProvider
import com.artiusid.sdk.sdk.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Complete authentication flow activity with SDK-managed UI
 * 
 * This activity handles the entire authentication process:
 * 1. Biometric authentication (face, fingerprint)
 * 2. Device binding verification
 * 3. Secure token exchange with API
 * 4. Return results to host application
 */
class AuthenticationFlowActivity : BaseSDKActivity() {
    
    private var currentStep by mutableStateOf("Initializing Authentication...")
    private var progress by mutableFloatStateOf(0.0f)
    private var isProcessing by mutableStateOf(false)
    private var showError by mutableStateOf(false)
    private var errorMessage by mutableStateOf("")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        android.util.Log.d("AuthenticationFlowActivity", "Starting authentication flow")
        
        setContent {
            SDKThemeProvider.ArtiusSDKTheme {
                AuthenticationFlowContent()
            }
        }
        
        // Start authentication process
        startAuthenticationProcess()
    }
    
    @Composable
    private fun AuthenticationFlowContent() {
        val config = SDKConfigManager.getConfig()
        
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // SDK Logo
                SDKLogo(
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                // Title
                Text(
                    text = "Authentication",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Current step
                Text(
                    text = currentStep,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                // Progress indicator
                if (isProcessing) {
                    SDKProgressIndicator(
                        progress = if (progress > 0) progress else null,
                        modifier = Modifier
                            .size(64.dp)
                            .padding(bottom = 32.dp)
                    )
                }
                
                // Error message
                if (showError) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (showError) {
                        SDKButton(
                            onClick = { retryAuthentication() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Retry")
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    
                    SDKButton(
                        onClick = { cancelAuthentication() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
    
    private fun startAuthenticationProcess() {
        lifecycleScope.launch {
            try {
                isProcessing = true
                showError = false
                
                // Step 1: Initialize biometric authentication
                currentStep = "Initializing biometric authentication..."
                progress = 0.1f
                delay(1000)
                
                // Step 2: Check biometric availability
                currentStep = "Checking biometric availability..."
                progress = 0.2f
                delay(1000)
                
                val biometricAvailable = checkBiometricAvailability()
                if (!biometricAvailable) {
                    throw Exception("Biometric authentication not available on this device")
                }
                
                // Step 3: Perform biometric authentication
                currentStep = "Performing biometric authentication..."
                progress = 0.4f
                delay(2000)
                
                val biometricData = performBiometricAuthentication()
                
                // Step 4: Device binding verification
                currentStep = "Verifying device binding..."
                progress = 0.6f
                delay(1500)
                
                val deviceBindingValid = verifyDeviceBinding()
                if (!deviceBindingValid) {
                    throw Exception("Device binding verification failed")
                }
                
                // Step 5: Secure token exchange
                currentStep = "Exchanging secure tokens..."
                progress = 0.8f
                delay(1000)
                
                val apiClient = SDKConfigManager.getApiClient()
                val authResult = apiClient.submitAuthentication(biometricData)
                
                // Step 6: Complete authentication
                currentStep = "Authentication completed successfully!"
                progress = 1.0f
                delay(1000)
                
                if (authResult.success && authResult.token != null) {
                    // Track success
                    AnalyticsManager.trackAuthenticationCompleted(true)
                    
                    // Return success result
                    val result = AuthenticationResult(
                        success = true,
                        token = authResult.token,
                        expiresAt = authResult.expiresAt,
                        sessionId = authResult.sessionId ?: "auth-${System.currentTimeMillis()}"
                    )
                    
                    ArtiusIDSDK.authenticationCallback?.onAuthenticationComplete(result)
                    finishWithSuccess()
                    
                } else {
                    throw Exception(authResult.error ?: "Authentication failed")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("AuthenticationFlowActivity", "Authentication failed", e)
                
                // Track failure
                AnalyticsManager.trackAuthenticationCompleted(false)
                
                // Show error
                isProcessing = false
                showError = true
                errorMessage = e.message ?: "Authentication failed"
                currentStep = "Authentication failed"
                
                // Notify callback
                ArtiusIDSDK.authenticationCallback?.onAuthenticationError(
                    SDKError(
                        code = SDKErrorCode.UNKNOWN_ERROR,
                        message = e.message ?: "Authentication failed",
                        cause = e
                    )
                )
            }
        }
    }
    
    private suspend fun checkBiometricAvailability(): Boolean {
        // Simulate biometric availability check
        delay(500)
        
        // In production, check for:
        // - Fingerprint sensor availability
        // - Face recognition capability
        // - Device biometric enrollment
        
        return true // Simulate availability
    }
    
    private suspend fun performBiometricAuthentication(): BiometricData {
        // Simulate biometric authentication
        delay(2000)
        
        // In production, this would:
        // 1. Prompt for fingerprint/face recognition
        // 2. Capture biometric data
        // 3. Perform local verification
        // 4. Generate secure biometric template
        
        return BiometricData(
            faceData = "simulated_face_template_${System.currentTimeMillis()}",
            fingerprintData = "simulated_fingerprint_template_${System.currentTimeMillis()}",
            deviceBiometric = true,
            confidence = 0.95f
        )
    }
    
    private suspend fun verifyDeviceBinding(): Boolean {
        // Simulate device binding verification
        delay(1000)
        
        // In production, this would:
        // 1. Check device registration status
        // 2. Verify device certificate
        // 3. Validate device integrity
        // 4. Check for device tampering
        
        return true // Simulate success
    }
    
    private fun retryAuthentication() {
        android.util.Log.d("AuthenticationFlowActivity", "Retrying authentication")
        startAuthenticationProcess()
    }
    
    private fun cancelAuthentication() {
        android.util.Log.d("AuthenticationFlowActivity", "Authentication cancelled by user")
        ArtiusIDSDK.authenticationCallback?.onAuthenticationCancelled()
        finishAsCancelled()
    }
    
    @Composable
    override fun Content() {
        // This is required by BaseSDKActivity but we handle content in onCreate
        AuthenticationFlowContent()
    }
}
