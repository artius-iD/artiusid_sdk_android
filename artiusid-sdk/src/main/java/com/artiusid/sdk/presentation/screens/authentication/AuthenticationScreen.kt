/*
 * File: AuthenticationScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.authentication

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
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
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.artiusid.sdk.R
import com.artiusid.sdk.presentation.components.AppTopBar
import com.artiusid.sdk.ui.theme.GradientBackground
import com.artiusid.sdk.ui.components.ThemedImage
import com.artiusid.sdk.ui.theme.Yellow900
import com.artiusid.sdk.ui.theme.LocalSDKTheme
import com.artiusid.sdk.ui.theme.AppColors
import com.artiusid.sdk.ui.screens.auth.AuthenticationSuccessScreen
import com.artiusid.sdk.utils.VerificationStateManager
import kotlinx.coroutines.delay

@Composable
fun AuthenticationScreen(
    onNavigateToApproval: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthenticationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    
    // Start authentication when screen loads
    LaunchedEffect(Unit) {
        viewModel.startAuthentication(context)
    }
    
    // Handle biometric authentication (like iOS LAContext)
    LaunchedEffect(uiState) {
        if (uiState is AuthenticationUiState.BiometricRequired) {
            val activity = context as? FragmentActivity
            if (activity != null) {
                val biometricManager = BiometricManager.from(context)
                when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
                    BiometricManager.BIOMETRIC_SUCCESS -> {
                        // Create BiometricPrompt like iOS LAContext
                        val executor = ContextCompat.getMainExecutor(context)
                        val biometricPrompt = BiometricPrompt(activity, executor,
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    android.util.Log.d("AuthenticationScreen", "✅ Biometric authentication succeeded")
                                    viewModel.onBiometricSuccess()
                                }
                                
                                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                    android.util.Log.e("AuthenticationScreen", "❌ Biometric error: $errorCode - $errString")
                                    viewModel.onBiometricFailure()
                                }
                                
                                override fun onAuthenticationFailed() {
                                    android.util.Log.w("AuthenticationScreen", "⚠️ Biometric authentication failed")
                                    viewModel.onBiometricFailure()
                                }
                            }
                        )
                        
                        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Unlock Device Permission")
                            .setSubtitle("Use your biometric to authenticate")
                            .setNegativeButtonText("Cancel")
                            .build()
                        
                        biometricPrompt.authenticate(promptInfo)
                    }
                    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                        // ✅ FIX: Biometric not enrolled - show error instead of auto-success
                        android.util.Log.e("AuthenticationScreen", "❌ Biometric not enrolled on device")
                        viewModel.onBiometricFailure()
                    }
                    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                        // ✅ FIX: No biometric hardware - show error instead of auto-success
                        android.util.Log.e("AuthenticationScreen", "❌ No biometric hardware available")
                        viewModel.onBiometricFailure()
                    }
                    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                        // ✅ FIX: Biometric hardware unavailable - show error
                        android.util.Log.e("AuthenticationScreen", "❌ Biometric hardware unavailable")
                        viewModel.onBiometricFailure()
                    }
                    BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                        // ✅ FIX: Security update required
                        android.util.Log.e("AuthenticationScreen", "❌ Biometric requires security update")
                        viewModel.onBiometricFailure()
                    }
                    BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                        // ✅ FIX: Biometric unsupported
                        android.util.Log.e("AuthenticationScreen", "❌ Biometric authentication unsupported")
                        viewModel.onBiometricFailure()
                    }
                    BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                        // ✅ FIX: Unknown status
                        android.util.Log.e("AuthenticationScreen", "❌ Biometric status unknown")
                        viewModel.onBiometricFailure()
                    }
                    else -> {
                        // ✅ FIX: Any other case - fail instead of auto-success
                        android.util.Log.e("AuthenticationScreen", "❌ Biometric authentication not available")
                        viewModel.onBiometricFailure()
                    }
                }
            } else {
                // ✅ FIX: Not a FragmentActivity - fail instead of auto-success
                android.util.Log.e("AuthenticationScreen", "❌ Activity is not FragmentActivity")
                viewModel.onBiometricFailure()
            }
        }
    }
    
    // Success screen is full-screen, others need scaffold
    if (uiState is AuthenticationUiState.Success) {
        // Full-screen success view
        val verificationStateManager = VerificationStateManager(context)
        val accountFullName = verificationStateManager.getAccountFullName()
        
        AuthenticationSuccessScreen(
            accountFullName = accountFullName,
            onBackHome = {
                onNavigateToApproval()
            }
        )
    } else {
        // All other states use Scaffold with top bar
        GradientBackground {
            Scaffold(
                topBar = {
                    AppTopBar(
                        title = "Authentication",
                        onBackClick = onNavigateBack
                    )
                },
                containerColor = Color.Transparent
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (uiState) {
                        is AuthenticationUiState.Processing -> {
                            // Simple processing animation - matches iOS
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
                        
                        is AuthenticationUiState.BiometricRequired -> {
                            // Biometric prompt state - show waiting for biometric
                            val themeConfig = LocalSDKTheme.current
                            ThemedImage(
                                defaultResourceId = R.drawable.img_artiusid_ios,
                                overrideKey = "brand_image",
                                contentDescription = themeConfig.brandName,
                                modifier = Modifier
                                    .size(120.dp)
                                    .padding(vertical = 20.dp)
                            )
                            
                            Text(
                                text = "Biometric Authentication Required",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.secondary, // Use theme color
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Please use your fingerprint or face to complete authentication",
                                style = MaterialTheme.typography.bodyLarge,
                                color = AppColors.textSecondary, // Use theme color
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        is AuthenticationUiState.Error -> {
                            // Error state
                            ThemedImage(
                                defaultResourceId = R.drawable.img_failed,
                                overrideKey = "failed_icon",
                                contentDescription = "Authentication Failed",
                                modifier = Modifier
                                    .size(200.dp)
                                    .padding(vertical = 40.dp)
                            )

                            Text(
                                text = "Authentication Failed",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = com.artiusid.sdk.ui.theme.ThemedStatusColors.getErrorColor(), // Keep error color
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 20.dp)
                            )

                            Text(
                                text = (uiState as AuthenticationUiState.Error).message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = AppColors.textSecondary, // Use theme color
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 32.dp)
                            )
                            
                            // Retry button
                            Button(
                                onClick = { viewModel.startAuthentication(context) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.secondary, // Use theme color
                                    contentColor = AppColors.buttonTextSecondary // Use theme button text color
                                ),
                                shape = RoundedCornerShape(25.dp)
                            ) {
                                Text(
                                    text = "Try Again",
                                    color = AppColors.buttonTextSecondary, // Use theme color
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        else -> { /* Success handled above */ }
                    }
                }
            }
        }
    }
} 