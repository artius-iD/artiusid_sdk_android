/*
 * File: ApprovalRequestScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sample

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay

/**
 * Extension function to find FragmentActivity from Context
 */
fun Context.findActivity(): FragmentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is FragmentActivity) return context
        context = context.baseContext
    }
    return null
}

/**
 * Sample App Approval Request Screen
 * Shows approval request with biometric authentication (Face ID/Fingerprint)
 * Matches the standalone app's ApprovalRequestScreen functionality
 */
@Composable
fun ApprovalRequestScreen(
    requestId: Int?,
    title: String,
    description: String,
    onApprove: () -> Unit,
    onDeny: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var authenticationState by remember { mutableStateOf(AuthenticationState.Authenticating) }
    var hasTriggeredBiometric by remember { mutableStateOf(false) }
    
    // Trigger biometric authentication when screen loads (only once)
    LaunchedEffect(requestId) {
        if (hasTriggeredBiometric) {
            android.util.Log.d("ApprovalRequestScreen", "⚠️ Biometric authentication already triggered, skipping")
            return@LaunchedEffect
        }
        
        android.util.Log.d("ApprovalRequestScreen", "🔐 Starting biometric authentication for approval request")
        android.util.Log.d("ApprovalRequestScreen", "📋 Request ID: $requestId")
        android.util.Log.d("ApprovalRequestScreen", "📋 Title: $title")
        android.util.Log.d("ApprovalRequestScreen", "📋 Description: $description")
        
        hasTriggeredBiometric = true
        
        // Delay slightly to let the UI state update
        delay(500)
        
        val activity = context.findActivity()
        
        if (activity is FragmentActivity) {
            android.util.Log.d("ApprovalRequestScreen", "✅ Found FragmentActivity - checking biometric availability")
            
            val biometricManager = BiometricManager.from(context)
            when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    android.util.Log.d("ApprovalRequestScreen", "✅ Biometric authentication available")
                    
                    val executor = ContextCompat.getMainExecutor(context)
                    val biometricPrompt = BiometricPrompt(activity, executor,
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                super.onAuthenticationError(errorCode, errString)
                                android.util.Log.e("ApprovalRequestScreen", "❌ Biometric authentication error: $errString")
                                authenticationState = AuthenticationState.Failed
                            }

                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                super.onAuthenticationSucceeded(result)
                                android.util.Log.d("ApprovalRequestScreen", "✅ Biometric authentication succeeded")
                                authenticationState = AuthenticationState.Authenticated
                            }

                            override fun onAuthenticationFailed() {
                                super.onAuthenticationFailed()
                                android.util.Log.e("ApprovalRequestScreen", "❌ Biometric authentication failed")
                                authenticationState = AuthenticationState.Failed
                            }
                        })

                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Approval Request Authentication")
                        .setSubtitle("Authenticate to proceed with approval request")
                        .setDescription("Use your fingerprint or face to authenticate")
                        .setNegativeButtonText("Cancel")
                        .build()

                    biometricPrompt.authenticate(promptInfo)
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    android.util.Log.w("ApprovalRequestScreen", "⚠️ No biometric hardware - proceeding without authentication")
                    authenticationState = AuthenticationState.Authenticated
                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    android.util.Log.w("ApprovalRequestScreen", "⚠️ Biometric hardware unavailable - proceeding without authentication")
                    authenticationState = AuthenticationState.Authenticated
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    android.util.Log.w("ApprovalRequestScreen", "⚠️ No biometrics enrolled - user needs to set up biometrics")
                    authenticationState = AuthenticationState.Failed
                }
                else -> {
                    android.util.Log.e("ApprovalRequestScreen", "❌ Biometric authentication not available")
                    authenticationState = AuthenticationState.Failed
                }
            }
        } else {
            android.util.Log.e("ApprovalRequestScreen", "❌ No FragmentActivity found - cannot trigger biometric authentication")
            authenticationState = AuthenticationState.Failed
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            // Approval Request Icon
            Icon(
                painter = painterResource(id = android.R.drawable.ic_dialog_info),
                contentDescription = "Approval Request",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
            )
            
            Spacer(modifier = Modifier.height(18.dp))
            
            // Description
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Authentication State UI
            when (authenticationState) {
                AuthenticationState.Authenticating -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Authenticating...",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Please authenticate using your fingerprint or face",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
                
                AuthenticationState.Authenticated -> {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_dialog_info),
                        contentDescription = "Authentication Successful",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Authentication Successful",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Show Approve/Deny buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Approve Button
                        Button(
                            onClick = onApprove,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "✅ Approve",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Deny Button
                        OutlinedButton(
                            onClick = onDeny,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.error),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                                containerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "❌ Deny",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                AuthenticationState.Failed -> {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_dialog_alert),
                        contentDescription = "Authentication Failed",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "Authentication Failed",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 30.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Authentication is required to proceed with the approval request.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 30.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Back Button
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Back to Home",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

enum class AuthenticationState {
    Authenticating,
    Authenticated,
    Failed
}
