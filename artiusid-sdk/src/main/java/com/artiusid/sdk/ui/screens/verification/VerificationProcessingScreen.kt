package com.artiusid.sdk.ui.screens.verification

import com.artiusid.sdk.models.*

import com.artiusid.sdk.ui.components.*

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artiusid.sdk.R
import com.artiusid.sdk.ui.components.AppTopBar
import com.artiusid.sdk.ui.components.GradientBackground
import com.artiusid.sdk.ui.theme.Yellow900
import com.artiusid.sdk.utils.ImageStorage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.artiusid.sdk.ui.components.DocumentRecaptureNotificationView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationProcessingScreen(
    onNavigateToResults: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToPassportCapture: () -> Unit,
    onNavigateToStateIdFrontCapture: () -> Unit = {},
    onNavigateToStateIdBackCapture: () -> Unit = {},
    onNavigateToFailure: (VerificationFailureType, String) -> Unit = { _, _ -> },
    viewModel: VerificationProcessingViewModel = viewModel()
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
                    containerColor = Color.Red.copy(alpha = 0.9f)
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
                        Image(
                            painter = painterResource(id = R.drawable.img_crossplatform),
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
                            color = Yellow900,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = Yellow900
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
                        Image(
                            painter = painterResource(id = R.drawable.img_success),
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
                        // Error state
                        Image(
                            painter = painterResource(id = R.drawable.img_system_error),
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
                            onRetryClick = onNavigateToPassportCapture,
                            onCancelClick = onNavigateBack
                        )
                    }
                    
                    is VerificationProcessingUiState.StateIdFrontRecaptureRequired -> {
                        val state = uiState as VerificationProcessingUiState.StateIdFrontRecaptureRequired
                        DocumentRecaptureNotificationView(
                            recaptureType = state.recaptureType,
                            onRetryClick = onNavigateToStateIdFrontCapture,
                            onCancelClick = onNavigateBack
                        )
                    }
                    
                    is VerificationProcessingUiState.StateIdBackRecaptureRequired -> {
                        val state = uiState as VerificationProcessingUiState.StateIdBackRecaptureRequired
                        DocumentRecaptureNotificationView(
                            recaptureType = state.recaptureType,
                            onRetryClick = onNavigateToStateIdBackCapture,
                            onCancelClick = onNavigateBack
                        )
                    }
                    
                    is VerificationProcessingUiState.DocumentRecaptureRequired -> {
                        val state = uiState as VerificationProcessingUiState.DocumentRecaptureRequired
                        DocumentRecaptureNotificationView(
                            recaptureType = state.recaptureType,
                            onRetryClick = {
                                // Route to appropriate capture based on recapture type
                                when (state.recaptureType) {
                                    DocumentRecaptureType.PASSPORT_MRZ_ERROR,
                                    DocumentRecaptureType.PASSPORT_OCR_ERROR -> onNavigateToPassportCapture()
                                    
                                    DocumentRecaptureType.STATE_ID_FRONT_ERROR -> onNavigateToStateIdFrontCapture()
                                    
                                    DocumentRecaptureType.STATE_ID_BACK_ERROR,
                                    DocumentRecaptureType.STATE_ID_BARCODE_ERROR -> onNavigateToStateIdBackCapture()
                                    
                                    else -> onNavigateBack() // Generic fallback
                                }
                            },
                            onCancelClick = onNavigateBack
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
} 