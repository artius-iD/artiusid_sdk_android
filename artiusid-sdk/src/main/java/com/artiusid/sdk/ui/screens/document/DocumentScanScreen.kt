package com.artiusid.sdk.ui.screens.document

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.artiusid.sdk.ui.components.CustomBackButton
import com.artiusid.sdk.ui.components.CustomInfoButton
import com.artiusid.sdk.utils.DocumentSide
import com.artiusid.sdk.R
import com.artiusid.sdk.ui.theme.*


/**
 * DocumentScanScreen - EXACT STANDALONE APPLICATION IMPLEMENTATION
 * This matches the standalone app's document scanning screen exactly
 */
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DocumentScanScreen(
    onDocumentScanComplete: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToFrontScan: (() -> Unit)? = null, // New callback for returning to front scan on comparison failure
    documentSide: DocumentSide = DocumentSide.FRONT,
    viewModel: DocumentScanViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val validationMessage by viewModel.validationMessage.collectAsState()
    val isProcessingComplete by viewModel.isProcessingComplete.collectAsState()
    val currentDocumentSide by viewModel.documentSide.collectAsState()

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Set document side when screen is created - EXACT STANDALONE IMPLEMENTATION
    LaunchedEffect(documentSide) {
        viewModel.setDocumentSide(documentSide)
    }

    LaunchedEffect(Unit) {
        if (cameraPermissionState.status == com.google.accompanist.permissions.PermissionStatus.Granted) {
            // Camera will be started by the DocumentCameraPreview
        }
    }

    // Handle completion navigation - EXACT STANDALONE IMPLEMENTATION
    LaunchedEffect(isProcessingComplete) {
        android.util.Log.d("DocumentScanScreen", "isProcessingComplete changed to: $isProcessingComplete")
        if (isProcessingComplete) {
            android.util.Log.d("DocumentScanScreen", "Processing complete, navigating to next screen")
            kotlinx.coroutines.delay(1000) // Small delay to show success message
            onDocumentScanComplete()
        }
    }
    
    // Also watch for UI state changes - EXACT STANDALONE IMPLEMENTATION
    LaunchedEffect(uiState) {
        android.util.Log.d("DocumentScanScreen", "UI state changed to: $uiState")
        if (uiState is DocumentScanUiState.Success || uiState is DocumentScanUiState.BarcodeDetected) {
            android.util.Log.d("DocumentScanScreen", "Success state detected, should trigger completion")
        }
    }

    // Watch for comparison failure that requires front image retake - EXACT STANDALONE IMPLEMENTATION
    LaunchedEffect(uiState) {
        if (uiState is DocumentScanUiState.ComparisonFailed && onNavigateToFrontScan != null) {
            android.util.Log.d("DocumentScanScreen", "Comparison failed, navigating back to front scan")
            kotlinx.coroutines.delay(2000) // Show error message briefly
            onNavigateToFrontScan()
        }
    }

    // EXACT STANDALONE IMPLEMENTATION - GradientBackground with Scaffold
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E), // Bluegray900
                        Color(0xFF16213E)  // Gray900
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Custom back button - EXACT STANDALONE IMPLEMENTATION
            CustomBackButton(
                onBackClick = onNavigateBack,
                navTitle = when (currentDocumentSide) {
                    DocumentSide.FRONT -> "Front ID Scan"
                    DocumentSide.BACK -> "Back ID Scan"
                }
            )
            
            // Main content area - EXACT STANDALONE IMPLEMENTATION
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    cameraPermissionState.status != com.google.accompanist.permissions.PermissionStatus.Granted -> {
                        // Camera permission not granted - EXACT STANDALONE IMPLEMENTATION
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Camera permission is required for document scanning",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { cameraPermissionState.launchPermissionRequest() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF6B35)
                                )
                            ) {
                                Text("Grant Permission")
                            }
                        }
                    }
                    else -> {
                        // Enhanced Camera Preview with Advanced Autofocus - EXACT STANDALONE IMPLEMENTATION
                        AndroidView(
                            factory = { ctx ->
                                PreviewView(ctx).apply {
                                    this.scaleType = PreviewView.ScaleType.FILL_CENTER
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            update = { previewView ->
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build()
                                    
                                    val cameraSelector = CameraSelector.Builder()
                                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                                        .build()
                                    
                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            cameraSelector,
                                            preview
                                        )
                                        preview.setSurfaceProvider(previewView.surfaceProvider)
                                    } catch (e: Exception) {
                                        android.util.Log.e("DocumentScanScreen", "Camera binding failed", e)
                                    }
                                }, ContextCompat.getMainExecutor(context))
                            }
                        )

                        // Graphic Overlay for ID Alignment (fill viewport) - EXACT STANDALONE IMPLEMENTATION
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 60.dp)
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

                        // Validation Message (rotated to landscape) - EXACT STANDALONE IMPLEMENTATION
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    end = 48.dp,
                                    top = 32.dp,
                                    bottom = 32.dp
                                )
                        ) {
                            CustomInfoButton(
                                buttonLabel = validationMessage,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .rotate(90f)
                                    .width(140.dp)
                            )
                        }

                        // Main Instruction Text (rotated to landscape) - EXACT STANDALONE IMPLEMENTATION
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    end = 48.dp,
                                    bottom = 48.dp
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
                                    .width(110.dp)
                            )
                        }

                        // Additional instruction for back scan (rotated to landscape) - EXACT STANDALONE IMPLEMENTATION
                        if (currentDocumentSide == DocumentSide.BACK) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(end = 48.dp, top = 48.dp)
                            ) {
                                CustomInfoButton(
                                    buttonLabel = "Scan barcode on back",
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .rotate(90f)
                                        .width(80.dp)
                                )
                            }
                        }

                        // Success overlay - EXACT STANDALONE IMPLEMENTATION
                        if (uiState is DocumentScanUiState.Success || uiState is DocumentScanUiState.BarcodeDetected) {
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
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "✓",
                                            color = Color.White,
                                            fontSize = 48.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = when (currentDocumentSide) {
                                                DocumentSide.FRONT -> "Front ID captured successfully!"
                                                DocumentSide.BACK -> "Back ID captured successfully!"
                                            },
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = when (currentDocumentSide) {
                                                DocumentSide.FRONT -> "Proceeding to back scan..."
                                                DocumentSide.BACK -> "Document verification complete!"
                                            },
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // Error overlay - EXACT STANDALONE IMPLEMENTATION
                        if (uiState is DocumentScanUiState.ComparisonFailed) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Red.copy(alpha = 0.3f))
                            ) {
                                Card(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(32.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Red.copy(alpha = 0.9f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "⚠",
                                            color = Color.White,
                                            fontSize = 48.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Document comparison failed",
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Please retake the front image",
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 14.sp,
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
}