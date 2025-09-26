/*
 * File: AuthenticationProgressScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 * 
 * Matches iOS AuthenticationProgressView.swift exactly
 */

package com.artiusid.sdk.ui.screens.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.artiusid.sdk.ui.theme.AppColors
import com.artiusid.sdk.ui.theme.ColorManager
import com.artiusid.sdk.utils.VerificationStateManager
// TODO: Add actual API authentication service when ready
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Match iOS states exactly
enum class AuthState {
    PROCESSING,      // iOS: !failedAuth && !noBiometricFound
    FAILED,          // iOS: failedAuth && !noBiometricFound
    NO_BIOMETRIC,    // iOS: noBiometricFound
    SUCCESS          // iOS: AuthenticatedView (success screen)
}

@Composable
fun AuthenticationProgressScreen(
    onAuthenticationSuccess: () -> Unit,
    onAuthenticationFailure: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var authState by remember { mutableStateOf(AuthState.PROCESSING) }
    var isAccountActive by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // Match iOS: authenticate() function called on appear
        authenticateWithBiometrics(context as FragmentActivity) { biometricSuccess ->
            if (biometricSuccess) {
                scope.launch {
                    // Match iOS: after biometric success, call API
                    try {
                        // Simulate API call like iOS (for now just succeed after delay)
                        delay(2000) // Match iOS processing time
                        
                        // For now, always succeed to match iOS behavior
                        // TODO: Implement actual API authentication call
                        val apiSuccess = true
                        isAccountActive = apiSuccess
                        
                        if (apiSuccess) {
                            // Match iOS: delay before showing success screen
                            delay(1000)
                            authState = AuthState.SUCCESS
                        } else {
                            authState = AuthState.FAILED
                        }
                    } catch (e: Exception) {
                        authState = AuthState.FAILED
                    }
                }
            } else {
                authState = AuthState.FAILED
            }
        }
    }
    
    // Match iOS background gradient exactly
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = ColorManager.getGradientBrush()
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (authState) {
                AuthState.PROCESSING -> {
                    // Match iOS: ProgressView with secondary color
                    CircularProgressIndicator(
                        color = AppColors.secondary,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(83.dp))
                    
                    // Match iOS: "gen_processing" text
                    Text(
                        text = "Processing",
                        color = AppColors.textPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(26.dp))
                    
                    // Match iOS: "gen_doNotCloseApp" text
                    Text(
                        text = "Do not close app",
                        color = AppColors.textSecondary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                AuthState.NO_BIOMETRIC -> {
                    // Match iOS: "auth_noBiometrics" text
                    Text(
                        text = "No Biometrics",
                        color = AppColors.textPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(168.dp)
                    )
                }
                
                AuthState.FAILED -> {
                    // Match iOS: "auth_failed" text
                    Text(
                        text = "Authentication Failed",
                        color = AppColors.textPrimary,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(313.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Match iOS: GoNextButtonView with "button_backHome"
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.secondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            text = "Back Home",
                            color = AppColors.buttonTextSecondary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                AuthState.SUCCESS -> {
                    // Match iOS: Show AuthenticatedView (success screen)
                    val verificationStateManager = VerificationStateManager(context)
                    val accountFullName = verificationStateManager.getAccountFullName()
                    
                    AuthenticationSuccessScreen(
                        accountFullName = accountFullName,
                        onBackHome = {
                            onAuthenticationSuccess()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Match iOS authenticate() function exactly
 * Uses LAContext equivalent (BiometricPrompt) for device authentication
 */
private fun authenticateWithBiometrics(
    activity: FragmentActivity,
    onResult: (Boolean) -> Unit
) {
    val biometricManager = BiometricManager.from(activity)
    
    // Match iOS: canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics)
    when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            // Match iOS: evaluatePolicy with reason
            val biometricPrompt = BiometricPrompt(activity, 
                ContextCompat.getMainExecutor(activity),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        // Match iOS: success = true
                        onResult(true)
                    }
                    
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        // Match iOS: success = false, failedAuth = true
                        onResult(false)
                    }
                    
                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        // Match iOS: success = false, failedAuth = true
                        onResult(false)
                    }
                }
            )
            
            // Match iOS: localizedReason
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock Device Permission")
                .setSubtitle("Use your biometric to authenticate")
                .setNegativeButtonText("Cancel")
                .build()
                
            biometricPrompt.authenticate(promptInfo)
        }
        
        else -> {
            // Match iOS: else case - no biometric available
            onResult(false)
        }
    }
}
