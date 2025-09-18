/*
 * File: DocumentScanScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.document

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.artiusid.sdk.presentation.components.DocumentCameraPreview
import com.artiusid.sdk.presentation.components.EnhancedDocumentCameraPreview
import com.artiusid.sdk.presentation.components.AppTopBar
import com.artiusid.sdk.ui.theme.GradientBackground
import com.artiusid.sdk.utils.DocumentSide
import com.artiusid.sdk.R
import com.artiusid.sdk.presentation.screens.document.DocumentType
import com.artiusid.sdk.ui.theme.*
import com.artiusid.sdk.ui.theme.getRelativeWidthDp
import com.artiusid.sdk.ui.theme.getRelativeHeightDp
import com.artiusid.sdk.ui.theme.CustomInfoButton

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DocumentScanScreen(
    onDocumentScanComplete: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToFrontScan: (() -> Unit)? = null, // New callback for returning to front scan on comparison failure
    documentSide: DocumentSide = DocumentSide.FRONT,
    viewModel: DocumentScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val validationMessage by viewModel.validationMessage.collectAsState()
    val isProcessingComplete by viewModel.isProcessingComplete.collectAsState()
    val currentDocumentSide by viewModel.documentSide.collectAsState()

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Set document side when screen is created
    LaunchedEffect(documentSide) {
        viewModel.setDocumentSide(documentSide)
    }

    LaunchedEffect(Unit) {
        if (cameraPermissionState.status == com.google.accompanist.permissions.PermissionStatus.Granted) {
            // Camera will be started by the DocumentCameraPreview
        }
    }

    LaunchedEffect(isProcessingComplete) {
        android.util.Log.d("DocumentScanScreen", "isProcessingComplete changed to: $isProcessingComplete")
        if (isProcessingComplete) {
            android.util.Log.d("DocumentScanScreen", "Processing complete, navigating to next screen")
            kotlinx.coroutines.delay(1000) // Small delay to show success message
            onDocumentScanComplete()
        }
    }
    
    // Also watch for UI state changes
    LaunchedEffect(uiState) {
        android.util.Log.d("DocumentScanScreen", "UI state changed to: $uiState")
        if (uiState is DocumentScanUiState.Success || uiState is DocumentScanUiState.BarcodeDetected) {
            android.util.Log.d("DocumentScanScreen", "Success state detected, should trigger completion")
        }
    }

    // Watch for comparison failure that requires front image retake
    LaunchedEffect(uiState) {
        if (uiState is DocumentScanUiState.ComparisonFailed && onNavigateToFrontScan != null) {
            android.util.Log.d("DocumentScanScreen", "Comparison failed, navigating back to front scan")
            kotlinx.coroutines.delay(2000) // Show error message briefly
            onNavigateToFrontScan()
        }
    }

    GradientBackground {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = when (currentDocumentSide) {
                        DocumentSide.FRONT -> "Front ID Scan"
                        DocumentSide.BACK -> "Back ID Scan"
                    },
                    onBackClick = onNavigateBack
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    cameraPermissionState.status != com.google.accompanist.permissions.PermissionStatus.Granted -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Camera permission is required for document scanning")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                                Text("Grant Permission")
                            }
                        }
                    }
                    else -> {
                        // Camera Preview with OCR and Image Capture
                        DocumentCameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            viewModel = viewModel
                        )

                        // Graphic Overlay for ID Alignment (fill viewport)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = getRelativeHeightDp(60.0f))
                        ) {
                            Image(
                                painter = painterResource(
                                    id = when (currentDocumentSide) {
                                        DocumentSide.FRONT -> R.drawable.state_id_front_overlay
                                        DocumentSide.BACK -> R.drawable.state_id_back_overlay
                                    }
                                ),
                                contentDescription = "ID Alignment Overlay",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .align(Alignment.Center)
                                    .alpha(0.4f)
                            )
                        }

                        // Validation Message (rotated to landscape)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    end = getRelativeWidthDp(48.0f),
                                    top = getRelativeHeightDp(32.0f),
                                    bottom = getRelativeHeightDp(32.0f)
                                )
                        ) {
                            CustomInfoButton(
                                buttonLabel = validationMessage,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .rotate(90f)
                                    .width(getRelativeWidthDp(140.0f))
                            )
                        }

                        // Main Instruction Text (rotated to landscape)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    end = getRelativeWidthDp(48.0f),
                                    bottom = getRelativeHeightDp(48.0f)
                                )
                        ) {
                            CustomInfoButton(
                                buttonLabel = when (currentDocumentSide) {
                                    DocumentSide.FRONT -> "Position your ID card in the frame"
                                    DocumentSide.BACK -> "Position the back of your ID card in the frame"
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .rotate(90f)
                                    .width(getRelativeWidthDp(110.0f))
                            )
                        }

                        // Additional instruction for back scan (rotated to landscape)
                        if (currentDocumentSide == DocumentSide.BACK) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(end = 48.dp, top = 48.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .rotate(90f)
                                        .width(90.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Black.copy(alpha = 0.7f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Scan PDF417 barcode",
                                        modifier = Modifier
                                            .padding(8.dp),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Additional instruction for front scan (rotated to landscape)
                        if (currentDocumentSide == DocumentSide.FRONT) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .rotate(90f)
                                        .width(120.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Black.copy(alpha = 0.7f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Ensure face is visible",
                                        modifier = Modifier
                                            .padding(8.dp),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Processing indicator when scanning
                        if (uiState is DocumentScanUiState.Success || 
                            uiState is DocumentScanUiState.BarcodeDetected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Green.copy(alpha = 0.3f))
                            ) {
                                Card(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(32.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Green.copy(alpha = 0.9f)
                                    )
                                ) {
                                    Text(
                                        text = when (currentDocumentSide) {
                                            DocumentSide.FRONT -> "Front ID captured successfully!"
                                            DocumentSide.BACK -> "Back ID captured successfully!"
                                        },
                                        modifier = Modifier.padding(24.dp),
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 