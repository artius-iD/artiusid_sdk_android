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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    
    // Trigger authentication when screen loads (like iOS onAppear)
    LaunchedEffect(Unit) {
        viewModel.authenticate(context)
        
        // Delay slightly to let the UI state update, then trigger biometric auth
        kotlinx.coroutines.delay(500)
        
        // Use BiometricAuthHelper for cleaner Face ID authentication
        val activity = context.findActivity()
        
        if (activity is FragmentActivity) {
            android.util.Log.d("ApprovalRequestScreen", "✅ Found FragmentActivity - checking Face ID availability")
            
            when (BiometricAuthHelper.getBiometricStatus(context)) {
                BiometricStatus.Available -> {
                    android.util.Log.d("ApprovalRequestScreen", "✅ Face ID available - starting authentication")
                    
                    BiometricAuthHelper.authenticateFaceIdOnly(
                        activity = activity,
                        onSuccess = {
                            android.util.Log.d("ApprovalRequestScreen", "✅ Face ID authentication succeeded")
                            viewModel.onBiometricAuthenticationSuccess()
                        },
                        onError = { error ->
                            android.util.Log.e("ApprovalRequestScreen", "❌ Face ID authentication failed: $error")
                            viewModel.onBiometricAuthenticationFailed()
                        },
                        onUserCancel = {
                            android.util.Log.d("ApprovalRequestScreen", "⚠️ User canceled Face ID authentication")
                            viewModel.onBiometricAuthenticationFailed()
                        }
                    )
                }
                BiometricStatus.NoHardware -> {
                    android.util.Log.w("ApprovalRequestScreen", "⚠️ No Face ID hardware - proceeding without")
                    viewModel.onBiometricAuthenticationSuccess()
                }
                BiometricStatus.HardwareUnavailable -> {
                    android.util.Log.w("ApprovalRequestScreen", "⚠️ Face ID hardware unavailable - proceeding without")
                    viewModel.onBiometricAuthenticationSuccess()
                }
                BiometricStatus.NoneEnrolled -> {
                    android.util.Log.w("ApprovalRequestScreen", "⚠️ No Face ID enrolled - user needs to set up Face ID")
                    viewModel.onBiometricAuthenticationFailed()
                }
                else -> {
                    android.util.Log.e("ApprovalRequestScreen", "❌ Face ID not available")
                    viewModel.onBiometricAuthenticationFailed()
                }
            }
        } else {
            android.util.Log.e("ApprovalRequestScreen", "❌ No FragmentActivity found - cannot trigger Face ID")
            viewModel.onBiometricAuthenticationFailed()
        }
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
            // Approval Request Image (like iOS approval_request image)
            Image(
                painter = painterResource(id = R.drawable.approval_reqeust), // Note: Using existing resource
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
                color = AppColors.textPrimary,
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
                        text = "Authenticating with Face ID...",
                        color = AppColors.textPrimary,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Please look at your device camera",
                        color = AppColors.textPrimary.copy(alpha = 0.7f),
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.secondary
                            ),
                            shape = RoundedCornerShape(12.58.dp)
                        ) {
                            Text(
                                text = "Approve",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.buttonTextPrimary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Deny Button (like iOS denyRequest button, isSecondary = true)
                        OutlinedButton(
                            onClick = { onNavigateToApprovalResponse("no") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(59.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, AppColors.secondary),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AppColors.secondary,
                                containerColor = AppColors.surface
                            ),
                            shape = RoundedCornerShape(12.58.dp)
                        ) {
                            Text(
                                text = "Deny",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.secondary
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
                        color = AppColors.textPrimary.copy(alpha = 0.8f),
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