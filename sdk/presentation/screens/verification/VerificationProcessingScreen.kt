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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationProcessingScreen(
    onNavigateToResults: () -> Unit,
    onNavigateBack: () -> Unit,
    onError: ((String) -> Unit)? = null,
    onNavigateToPassportCapture: () -> Unit,
    onNavigateToStateIdFrontCapture: () -> Unit = {},
    onNavigateToStateIdBackCapture: () -> Unit = {},
    onNavigateToFailure: (com.artiusid.sdk.data.model.VerificationFailureType, String) -> Unit = { _, _ -> },
    viewModel: VerificationProcessingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val verificationResultData by viewModel.verificationResultData.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    var localError by remember { mutableStateOf<String?>(null) }

    // Start verification when the screen is first displayed
    LaunchedEffect(Unit) {
        Log.d("VerificationProcessingScreen", "=== UI: LaunchedEffect triggered, starting verification ===")
        val capturedImages = ImageStorage.getCapturedImages()
        val missing = mutableListOf<String>()
        
        // Validate based on document type (passport vs ID) - matching iOS logic
        if (capturedImages.passportImage != null) {
            // Passport flow: only requires passport + face
            if (capturedImages.faceImage == null) missing.add("face")
            Log.d("VerificationProcessingScreen", "PASSPORT validation - Image presence: passport=${capturedImages.passportImage != null}, face=${capturedImages.faceImage != null}")
            Log.d("VerificationProcessingScreen", "PASSPORT validation - Image sizes: passport=${capturedImages.passportImage?.width}x${capturedImages.passportImage?.height}, face=${capturedImages.faceImage?.width}x${capturedImages.faceImage?.height}")
        } else {
            // ID flow: requires front + back + face
            if (capturedImages.frontImage == null) missing.add("front")
            if (capturedImages.backImage == null) missing.add("back")
            if (capturedImages.faceImage == null) missing.add("face")
            Log.d("VerificationProcessingScreen", "ID validation - Image presence: front=${capturedImages.frontImage != null}, back=${capturedImages.backImage != null}, face=${capturedImages.faceImage != null}")
            Log.d("VerificationProcessingScreen", "ID validation - Image sizes: front=${capturedImages.frontImage?.width}x${capturedImages.frontImage?.height}, back=${capturedImages.backImage?.width}x${capturedImages.backImage?.height}, face=${capturedImages.faceImage?.width}x${capturedImages.faceImage?.height}")
        }
        
        if (missing.isNotEmpty()) {
            Log.e("VerificationProcessingScreen", "Cannot start verification, missing images: ${missing.joinToString()}")
            localError = "Missing required images: ${missing.joinToString()}. Please complete all steps."
            return@LaunchedEffect
        }
        if (capturedImages.passportImage != null) {
            viewModel.startVerification(
                frontImageBitmap = null,
                backImageBitmap = null,
                faceImageBitmap = capturedImages.faceImage,
                passportImageBitmap = capturedImages.passportImage,
                context = context
            )
        } else {
            viewModel.startVerification(
                frontImageBitmap = capturedImages.frontImage,
                backImageBitmap = capturedImages.backImage,
                faceImageBitmap = capturedImages.faceImage,
                context = context
            )
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
                        // Processing animation placeholder
                        ThemedImage(
                            defaultResourceId = R.drawable.img_crossplatform,
                            overrideKey = "cross_platform_image",
                            contentDescription = "Processing",
                            modifier = Modifier
                                .size(200.dp)
                                .padding(vertical = 40.dp)
                        )

                        Text(
                            text = "Verification in Progress",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )

                        Text(
                            text = currentStep,
                            style = MaterialTheme.typography.titleMedium,
                            color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor(),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()
                        )

                        Text(
                            text = "Please do not close the application while we process your request. This can take up to a minute to process.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 20.dp)
                        )
                    }
                    
                    VerificationProcessingUiState.Success -> {
                        // Processing complete
                        ThemedImage(
                            defaultResourceId = R.drawable.img_success,
                            overrideKey = "success_icon",
                            contentDescription = "Processing Complete",
                            modifier = Modifier
                                .size(200.dp)
                                .padding(vertical = 40.dp)
                        )

                        Text(
                            text = "Processing Complete",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )

                        Text(
                            text = "Redirecting to results...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        
                        // Navigate to results after a short delay
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(1000)
                            onNavigateToResults()
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