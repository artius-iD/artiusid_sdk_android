/*
 * File: VerificationProcessingScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.verification

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.artiusid.sdk.R
import com.artiusid.sdk.presentation.components.AppTopBar
import com.artiusid.sdk.ui.theme.GradientBackground
import com.artiusid.sdk.ui.theme.Yellow900
import com.artiusid.sdk.utils.ImageStorage
import com.artiusid.sdk.ui.components.ThemedImage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.presentation.components.DocumentRecaptureNotificationView
import com.artiusid.sdk.models.VerificationResult

@OptIn(ExperimentalMaterial3Api::class)
// ✅ CRITICAL FIX v1.2.47: Global screen guard to prevent multiple screen instances
private object ScreenGuard {
    @Volatile
    private var hasScreenTriggered = false
    
    fun tryTrigger(): Boolean {
        synchronized(this) {
            if (hasScreenTriggered) {
                return false
            }
            hasScreenTriggered = true
            return true
        }
    }
    
    fun reset() {
        synchronized(this) {
            hasScreenTriggered = false
        }
    }
}

@Composable
fun VerificationProcessingScreen(
    onNavigateToResults: () -> Unit,
    onNavigateBack: () -> Unit,
    onError: ((String) -> Unit)? = null,
    onCompleteWithRecapture: (VerificationResult) -> Unit = {},
    onNavigateToPassportCapture: () -> Unit,
    onNavigateToStateIdFrontCapture: () -> Unit = {},
    onNavigateToStateIdBackCapture: () -> Unit = {},
    onNavigateToFailure: (com.artiusid.sdk.data.model.VerificationFailureType, String) -> Unit = { _, _ -> },
    viewModel: VerificationProcessingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var localError by remember { mutableStateOf<String?>(null) }
    
    // 🚨 CRITICAL FIX: Enhanced state change logging to debug UI stuck issue
    LaunchedEffect(uiState) {
        Log.d("VerifProcessVM", "🔄 ========================================")
        Log.d("VerifProcessVM", "🔄 UI: STATE CHANGE DETECTED")
        Log.d("VerifProcessVM", "🔄 UI: New state: $uiState")
        Log.d("VerifProcessVM", "🔄 UI: State type: ${uiState.javaClass.simpleName}")
        Log.d("VerifProcessVM", "🔄 UI: Is Success? ${uiState is VerificationProcessingUiState.Success}")
        Log.d("VerifProcessVM", "🔄 UI: Is Processing? ${uiState is VerificationProcessingUiState.Processing}")
        Log.d("VerifProcessVM", "🔄 UI: Timestamp: ${System.currentTimeMillis()}")
        Log.d("VerifProcessVM", "🔄 ========================================")
    }
    
    // iOS parity: when recapture is required, return VerificationResult(requiresRecapture=true) to host and finish
    LaunchedEffect(uiState) {
        val recaptureType = when (uiState) {
            is VerificationProcessingUiState.PassportRecaptureRequired -> (uiState as VerificationProcessingUiState.PassportRecaptureRequired).recaptureType
            is VerificationProcessingUiState.StateIdFrontRecaptureRequired -> (uiState as VerificationProcessingUiState.StateIdFrontRecaptureRequired).recaptureType
            is VerificationProcessingUiState.StateIdBackRecaptureRequired -> (uiState as VerificationProcessingUiState.StateIdBackRecaptureRequired).recaptureType
            is VerificationProcessingUiState.DocumentRecaptureRequired -> (uiState as VerificationProcessingUiState.DocumentRecaptureRequired).recaptureType
            else -> null
        }
        if (recaptureType != null) {
            Log.d("VerifProcessVM", "📤 iOS parity: completing with recapture result type=${recaptureType.title}")
            onCompleteWithRecapture(
                VerificationResult(
                    success = false,
                    verificationId = "",
                    confidence = 0f,
                    documentType = null,
                    extractedData = emptyMap(),
                    processingTime = 0L,
                    sessionId = "",
                    rawResponse = null,
                    errorMessage = recaptureType.message,
                    requiresRecapture = true,
                    recaptureType = recaptureType
                )
            )
        }
    }
    
    // ARCHITECTURAL FIX: No more guards needed - verification is triggered once by ViewModel
    
    // SDK v1.2.39 CRITICAL FIX: Enhanced cleanup on screen disposal
    DisposableEffect(Unit) {
        // Log when the screen is composed
        Log.d("VerifProcessVM", "🔵 UI: VerificationProcessingScreen composed")
        Log.d("VerifProcessVM", "🔵 UI: Guard state: ${VerificationGuard.getDebugState()}")
        
        onDispose {
            Log.d("VerifProcessVM", "🔵 ========================================")
            Log.d("VerifProcessVM", "🔵 UI: Screen disposed - performing cleanup")
            Log.d("VerifProcessVM", "🔵 UI: Previous guard state: ${VerificationGuard.getDebugState()}")
            
            // Note: UI guard is now managed globally by VerificationGuard
            Log.d("VerifProcessVM", "🔵 UI: Screen disposed - VerificationGuard will handle cleanup")
            
            // CRITICAL: Reset singleton guard to prevent stuck states
            VerificationGuard.resetVerification()
            Log.d("VerifProcessVM", "🔵 UI: Singleton VerificationGuard reset")
            Log.d("VerifProcessVM", "🔵 UI: New guard state: ${VerificationGuard.getDebugState()}")
            Log.d("VerifProcessVM", "🔵 ========================================")
        }
    }

    // ✅ CRITICAL FIX v1.2.47: Use global screen guard to prevent multiple screen instances
    // This ensures verification is triggered only once across ALL screen/ViewModel instances
    LaunchedEffect(Unit) {
        Log.d("VerifProcessVM", "🎯 ========================================")
        Log.d("VerifProcessVM", "🎯 UI: Screen instance created - ViewModel ${viewModel.hashCode()}")
        
        if (ScreenGuard.tryTrigger()) {
            Log.d("VerifProcessVM", "🎯 UI: FIRST SCREEN INSTANCE - triggering verification")
            Log.d("VerifProcessVM", "🎯 UI: All subsequent screen instances will be ignored")
            Log.d("VerifProcessVM", "🎯 ========================================")
            
            // Only the first screen instance triggers verification
            viewModel.triggerVerificationStart(context)
        } else {
            Log.d("VerifProcessVM", "🎯 UI: DUPLICATE SCREEN INSTANCE - ignoring trigger")
            Log.d("VerifProcessVM", "🎯 UI: Verification already triggered by another screen instance")
            Log.d("VerifProcessVM", "🎯 ========================================")
        }
    }

    // Add error UI if localError is set
    if (localError != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = com.artiusid.sdk.ui.theme.ThemedStatusColors.getErrorColor().copy(alpha = 0.9f)
                )
            ) {
                Text(
                    text = localError!!,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        return
    }

    GradientBackground {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = "Verification Processing",
                    onBackClick = onNavigateBack
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (uiState) {
                    VerificationProcessingUiState.Processing -> {
                        Log.d("VerifProcessVM", "🔄 UI: Rendering PROCESSING state - SIMPLIFIED")
                        
                        // Add spacer to push content down from top
                        Spacer(modifier = Modifier.height(80.dp))
                        
                        // Simple spinning wheel
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(120.dp)
                                .padding(bottom = 40.dp), // Space between circle and text
                            color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor(),
                            strokeWidth = 8.dp
                        )

                        Text(
                            text = "Processing...",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )

                        Text(
                            text = "Please wait, this could take up to a minute.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 40.dp)
                        )
                    }
                    
                    VerificationProcessingUiState.Success -> {
                        Log.d("VerifProcessVM", "🔄 UI: Rendering SUCCESS state - SIMPLIFIED")
                        
                        // Simple success icon
                        ThemedImage(
                            defaultResourceId = R.drawable.img_success,
                            overrideKey = "success_icon",
                            contentDescription = "Success",
                            modifier = Modifier
                                .size(120.dp)
                                .padding(vertical = 60.dp)
                        )

                        Text(
                            text = "Verification Complete!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                        
                        // Navigate to results immediately when success state is reached
                        LaunchedEffect(Unit) {
                            Log.d("VerifProcessVM", "🔄 UI: SUCCESS - navigating to results immediately")
                            com.artiusid.sdk.presentation.screens.document.NfcStateManager.resetNFCState()
                            // Play success sound
                            val soundManager = com.artiusid.sdk.utils.CameraSoundManager(context)
                            soundManager.playSuccessSound()
                            // Small delay to show success message
                            kotlinx.coroutines.delay(500)
                            Log.d("VerifProcessVM", "🔄 UI: Calling onNavigateToResults()")
                            onNavigateToResults()
                            
                            // Reset screen guard AFTER navigation
                            ScreenGuard.reset()
                            Log.d("VerifProcessVM", "🔄 UI: Screen guard reset - ready for next verification")
                            
                            // Cleanup sound manager
                            soundManager.cleanup()
                        }
                    }
                    
                    is VerificationProcessingUiState.Error -> {
                        // Error state - navigate back to previous screen
                        ThemedImage(
                            defaultResourceId = R.drawable.img_system_error,
                            overrideKey = "system_error_icon",
                            contentDescription = "Error",
                            modifier = Modifier
                                .size(200.dp)
                                .padding(vertical = 40.dp)
                        )

                        Text(
                            text = "Verification Failed",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )

                        Text(
                            text = (uiState as VerificationProcessingUiState.Error).message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                        
                        // Navigate back on error
                        LaunchedEffect(Unit) {
                            com.artiusid.sdk.presentation.screens.document.NfcStateManager.resetNFCState()
                            kotlinx.coroutines.delay(1000)
                            onNavigateBack()
                        }
                    }
                    
                    is VerificationProcessingUiState.ConnectionError -> {
                        // Connection error - should return to sample app via SDK callback
                        ThemedImage(
                            defaultResourceId = R.drawable.img_system_error,
                            overrideKey = "system_error_icon",
                            contentDescription = "Connection Error",
                            modifier = Modifier
                                .size(200.dp)
                                .padding(vertical = 40.dp)
                        )

                        Text(
                            text = "Connection Failed",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )

                        Text(
                            text = (uiState as VerificationProcessingUiState.ConnectionError).message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                        
                        // Return to sample app via SDK error callback
                        LaunchedEffect(Unit) {
                            com.artiusid.sdk.presentation.screens.document.NfcStateManager.resetNFCState()
                            kotlinx.coroutines.delay(2000)
                            if (onError != null) {
                                // Call SDK error callback to return to sample app
                                onError("Connection failed: ${(uiState as VerificationProcessingUiState.ConnectionError).message}")
                            } else {
                                // Fallback to navigation if no error callback
                                onNavigateBack()
                            }
                        }
                    }
                    
                    is VerificationProcessingUiState.Failure -> {
                        // Navigate to failure screen
                        val state = uiState as VerificationProcessingUiState.Failure
                        LaunchedEffect(Unit) {
                            com.artiusid.sdk.presentation.screens.document.NfcStateManager.resetNFCState()
                            onNavigateToFailure(state.failureType, state.errorReason)
                        }
                    }
                    
                    is VerificationProcessingUiState.PassportRecaptureRequired -> {
                        val state = uiState as VerificationProcessingUiState.PassportRecaptureRequired
                        DocumentRecaptureNotificationView(
                            recaptureType = state.recaptureType,
                            onRecaptureAction = onNavigateToPassportCapture,
                            onCancel = onNavigateBack
                        )
                    }
                    
                    is VerificationProcessingUiState.StateIdFrontRecaptureRequired -> {
                        val state = uiState as VerificationProcessingUiState.StateIdFrontRecaptureRequired
                        DocumentRecaptureNotificationView(
                            recaptureType = state.recaptureType,
                            onRecaptureAction = onNavigateToStateIdFrontCapture,
                            onCancel = onNavigateBack
                        )
                    }
                    
                    is VerificationProcessingUiState.StateIdBackRecaptureRequired -> {
                        val state = uiState as VerificationProcessingUiState.StateIdBackRecaptureRequired
                        DocumentRecaptureNotificationView(
                            recaptureType = state.recaptureType,
                            onRecaptureAction = onNavigateToStateIdBackCapture,
                            onCancel = onNavigateBack
                        )
                    }
                    
                    is VerificationProcessingUiState.DocumentRecaptureRequired -> {
                        val state = uiState as VerificationProcessingUiState.DocumentRecaptureRequired
                        DocumentRecaptureNotificationView(
                            recaptureType = state.recaptureType,
                            onRecaptureAction = {
                                // Route to appropriate capture based on recapture type
                                when (state.recaptureType) {
                                    com.artiusid.sdk.data.model.DocumentRecaptureType.PASSPORT_MRZ_ERROR,
                                    com.artiusid.sdk.data.model.DocumentRecaptureType.PASSPORT_OCR_ERROR -> onNavigateToPassportCapture()
                                    
                                    com.artiusid.sdk.data.model.DocumentRecaptureType.STATE_ID_FRONT_ERROR -> onNavigateToStateIdFrontCapture()
                                    
                                    com.artiusid.sdk.data.model.DocumentRecaptureType.STATE_ID_BACK_ERROR,
                                    com.artiusid.sdk.data.model.DocumentRecaptureType.STATE_ID_BARCODE_ERROR -> onNavigateToStateIdBackCapture()
                                    
                                    else -> onNavigateBack() // Generic fallback
                                }
                            },
                            onCancel = onNavigateBack
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
} 