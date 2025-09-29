/*
 * File: ApprovalRequestScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.approval

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import com.artiusid.sdk.ui.components.ThemedImage
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.artiusid.sdk.R
import com.artiusid.sdk.data.model.AppNotificationState
import com.artiusid.sdk.presentation.components.FaceIdAnimation
import com.artiusid.sdk.ui.theme.AppColors
import com.artiusid.sdk.ui.theme.ColorManager
import com.artiusid.sdk.utils.BiometricAuthHelper
import com.artiusid.sdk.utils.BiometricStatus
import com.artiusid.sdk.utils.BiometricType

/**
 * Extension function to find FragmentActivity from Context
 */
fun Context.findActivity(): ComponentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    return null
}



/**
 * Matches iOS ApprovalRequestView.swift exactly
 * Shows approval request with biometric authentication
 */
@Composable
fun ApprovalRequestScreen(
    onNavigateToApprovalResponse: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ApprovalRequestViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val notificationTitle by AppNotificationState.notificationTitle.collectAsState()
    val notificationDescription by AppNotificationState.notificationDescription.collectAsState()
    
    // Track biometric retry attempts (max 3 attempts)
    var biometricAttempts by remember { mutableIntStateOf(0) }
    val maxBiometricAttempts = 3
    
    // Function to start biometric authentication with retry logic
    fun startBiometricAuthentication() {
        val activity = context.findActivity()
        
        if (activity is FragmentActivity) {
            android.util.Log.d("ApprovalRequestScreen", "✅ Found FragmentActivity - checking biometric availability (attempt ${biometricAttempts + 1}/$maxBiometricAttempts)")
            
            val biometricManager = BiometricManager.from(context)
            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    android.util.Log.d("ApprovalRequestScreen", "✅ Biometric authentication available - starting biometric prompt")
                    
                    val executor = ContextCompat.getMainExecutor(context)
                    val biometricPrompt = BiometricPrompt(activity, executor,
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                super.onAuthenticationError(errorCode, errString)
                                android.util.Log.e("ApprovalRequestScreen", "❌ Biometric authentication error: $errString (attempt ${biometricAttempts + 1}/$maxBiometricAttempts)")
                                
                                // Check if user cancelled or if it's a retryable error
                                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || 
                                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                                    // User cancelled - fail immediately
                                    viewModel.onBiometricAuthenticationFailed()
                                } else {
                                    // Other errors - retry if attempts remaining
                                    biometricAttempts++
                                    if (biometricAttempts < maxBiometricAttempts) {
                                        android.util.Log.d("ApprovalRequestScreen", "🔄 Retrying biometric authentication...")
                                        CoroutineScope(Dispatchers.Main).launch {
                                            delay(1000) // Brief delay before retry
                                            startBiometricAuthentication()
                                        }
                                    } else {
                                        android.util.Log.e("ApprovalRequestScreen", "❌ Max biometric attempts reached - failing")
                                        viewModel.onBiometricAuthenticationFailed()
                                    }
                                }
                            }

                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                super.onAuthenticationSucceeded(result)
                                android.util.Log.d("ApprovalRequestScreen", "✅ Biometric authentication succeeded on attempt ${biometricAttempts + 1}")
                                viewModel.onBiometricAuthenticationSuccess()
                            }

                            override fun onAuthenticationFailed() {
                                super.onAuthenticationFailed()
                                android.util.Log.w("ApprovalRequestScreen", "⚠️ Biometric authentication failed (attempt ${biometricAttempts + 1}/$maxBiometricAttempts)")
                                
                                // Increment attempts and retry if possible
                                biometricAttempts++
                                if (biometricAttempts < maxBiometricAttempts) {
                                    android.util.Log.d("ApprovalRequestScreen", "🔄 Retrying biometric authentication...")
                                    CoroutineScope(Dispatchers.Main).launch {
                                        delay(1000) // Brief delay before retry
                                        startBiometricAuthentication()
                                    }
                                } else {
                                    android.util.Log.e("ApprovalRequestScreen", "❌ Max biometric attempts reached - failing")
                                    viewModel.onBiometricAuthenticationFailed()
                                }
                            }
                        })

                    val attemptText = if (biometricAttempts > 0) " (Attempt ${biometricAttempts + 1}/$maxBiometricAttempts)" else ""
                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Approval Authentication$attemptText")
                        .setSubtitle("Authenticate to approve this request")
                        .setNegativeButtonText("Cancel")
                        .build()

                    biometricPrompt.authenticate(promptInfo)
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    android.util.Log.w("ApprovalRequestScreen", "⚠️ No biometric hardware - proceeding without")
                    viewModel.onBiometricAuthenticationSuccess()
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    android.util.Log.w("ApprovalRequestScreen", "⚠️ Biometric hardware unavailable - proceeding without")
                    viewModel.onBiometricAuthenticationSuccess()
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    android.util.Log.w("ApprovalRequestScreen", "⚠️ No biometrics enrolled - user needs to set up biometrics")
                    viewModel.onBiometricAuthenticationFailed()
                }
                else -> {
                    android.util.Log.e("ApprovalRequestScreen", "❌ Biometric authentication not possible")
                    viewModel.onBiometricAuthenticationFailed()
                }
            }
        } else {
            android.util.Log.e("ApprovalRequestScreen", "❌ No FragmentActivity found - cannot trigger biometric authentication")
            viewModel.onBiometricAuthenticationFailed()
        }
    }
    
    // Get notification data to trigger re-authentication on new requests
    val requestId by AppNotificationState.requestId.collectAsState()
    
    // Trigger authentication when screen loads or when a new request comes in (like iOS onAppear)
    LaunchedEffect(requestId) {
        android.util.Log.d("ApprovalRequestScreen", "🔄 New approval request detected (ID: $requestId) - resetting state")
        
        // Reset ViewModel state for new approval request
        viewModel.resetForNewRequest()
        
        // Reset biometric attempts counter for new request
        biometricAttempts = 0
        
        viewModel.authenticate(context)
        
        // Delay slightly to let the UI state update, then trigger biometric auth
        delay(500)
        
        // Start initial biometric authentication
        startBiometricAuthentication()
    }
    
    // Use theme-based background
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
            // Approval Request Image (like iOS approval_request image) - supports corporate image overrides
            ThemedImage(
                defaultResourceId = R.drawable.approval_reqeust, // Note: Using existing resource
                overrideKey = "approval_request_icon",
                contentDescription = "Approval Request",
                modifier = Modifier
                    .size(width = 353.dp, height = 254.dp)
                    .padding(bottom = 20.dp)
            )
            
            // Title from AppNotificationState (like iOS)
            Text(
                text = notificationTitle,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor(),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
            )
            
            Spacer(modifier = Modifier.height(18.dp))
            
            // Description from AppNotificationState (like iOS)
            Text(
                text = notificationDescription,
                style = MaterialTheme.typography.bodyLarge,
                color = com.artiusid.sdk.ui.theme.ThemedTextColors.getSecondaryTextColor(),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Conditional UI based on authentication state (like iOS)
            when (uiState) {
                is ApprovalRequestUiState.Authenticating -> {
                    // Show Face ID animation while authenticating
                    FaceIdAnimation(
                        modifier = Modifier.size(120.dp),
                        isScanning = true,
                        scanColor = AppColors.secondary,
                        faceColor = AppColors.textPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Authenticating...",
                        color = com.artiusid.sdk.ui.theme.ThemedTextColors.getPrimaryTextColor(),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Please authenticate to continue",
                        color = com.artiusid.sdk.ui.theme.ThemedTextColors.getSecondaryTextColor(),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    

                }
                
                is ApprovalRequestUiState.Authenticated -> {
                    // Show success Face ID animation first
                    FaceIdAnimation(
                        modifier = Modifier.size(80.dp),
                        isScanning = false,
                        scanColor = AppColors.success,
                        faceColor = AppColors.success
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Authentication Successful",
                        color = AppColors.success,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Show Approve/Deny buttons (like iOS when isButtonEnabled = true)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Approve Button (like iOS approveRequest button)
                        Button(
                            onClick = { onNavigateToApprovalResponse("yes") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(59.dp),
                            colors = com.artiusid.sdk.ui.theme.AppButtonDefaults.primaryButtonColors(),
                            shape = RoundedCornerShape(12.58.dp)
                        ) {
                            Text(
                                text = "Approve",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Deny Button (like iOS denyRequest button, isSecondary = true)
                        OutlinedButton(
                            onClick = { onNavigateToApprovalResponse("no") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(59.dp),
                            colors = com.artiusid.sdk.ui.theme.AppButtonDefaults.outlinedButtonColors(),
                            shape = RoundedCornerShape(12.58.dp)
                        ) {
                            Text(
                                text = "Deny",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                is ApprovalRequestUiState.AuthenticationFailed -> {
                    // Show failed Face ID animation
                    FaceIdAnimation(
                        modifier = Modifier.size(100.dp),
                        isScanning = false,
                        scanColor = AppColors.error,
                        faceColor = AppColors.error.copy(alpha = 0.8f)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Show failure message (like iOS failedAuth or !isAccountActive)
                    Text(
                        text = "Face ID Authentication Failed",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 30.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Authentication required to proceed with approval request.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = com.artiusid.sdk.ui.theme.ThemedTextColors.getSecondaryTextColor(),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 30.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Back Home Button (like iOS goBack button)
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(59.dp),
                        colors = com.artiusid.sdk.ui.theme.AppButtonDefaults.primaryButtonColors(),
                        shape = RoundedCornerShape(12.58.dp)
                    ) {
                        Text(
                            text = "Back Home",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                is ApprovalRequestUiState.AccountInactive -> {
                    // Show account inactive message
                    Text(
                        text = "Account is not active for approval requests.",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 30.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(59.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.secondary
                        ),
                        shape = RoundedCornerShape(12.58.dp)
                    ) {
                        Text(
                            text = "Back Home",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.buttonTextPrimary
                        )
                    }
                }
            }
        }
    }
} 