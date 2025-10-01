/*
 * File: FaceScanScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.face

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import com.artiusid.sdk.ui.components.ThemedImage
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artiusid.sdk.R
import com.artiusid.sdk.services.FaceMeshDetectorServiceImpl
import com.artiusid.sdk.services.ProcessingStage
import com.artiusid.sdk.ui.components.CustomInfoButton

@Composable
fun FaceScanScreen(
    onNavigateToVerification: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Create ViewModel with proper factory
    val viewModel: FaceVerificationViewModel = viewModel {
        FaceVerificationViewModel(FaceMeshDetectorServiceImpl(context))
    }
    
    // State from ViewModel
    val segmentStatus by viewModel.segmentStatus.collectAsState()
    val currentInstruction by viewModel.currentInstruction.collectAsState()
    val isProcessingComplete by viewModel.isProcessingComplete.collectAsState()
    val error by viewModel.error.collectAsState()
    val faceResult by viewModel.faceResult.collectAsState()
    
    // Camera permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )
    
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }
    
    // Navigate when processing is complete, but only once
    var hasNavigated by remember { mutableStateOf(false) }
    LaunchedEffect(isProcessingComplete) {
        if (isProcessingComplete && !hasNavigated) {
            hasNavigated = true
            Log.d("FaceScanScreen", "Processing complete, showing checkmark and navigating to verification")
            // Show checkmark for 1 second before navigating
            kotlinx.coroutines.delay(1000)
            onNavigateToVerification()
        }
    }
    
    // Add logging for state changes
    LaunchedEffect(faceResult) {
        android.util.Log.d("FaceScanScreen", "[LIVENESS] State changed: ${faceResult?.processingStage}, Segments: ${segmentStatus}, Blink: ${faceResult?.blinkDetected}")
    }
    // Add logging for segment filling
    if (faceResult?.processingStage == ProcessingStage.GUIDED_MESH_CAPTURE) {
        android.util.Log.d("FaceScanScreen", "[LIVENESS] Segment status: ${segmentStatus}")
    }
    // Add logging for blink detection
    if (faceResult?.processingStage == ProcessingStage.BLINK_DETECTION) {
        android.util.Log.d("FaceScanScreen", "[LIVENESS] Blink detection active. Blink detected: ${faceResult?.blinkDetected}")
    }
    // Add logging for completion
    if (faceResult?.processingStage == ProcessingStage.COMPLETED) {
        android.util.Log.d("FaceScanScreen", "[LIVENESS] Liveness test completed!")
    }
    
    // iOS-like ZStack layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
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
                        val imageAnalyzer = viewModel.createImageAnalyzer()
                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                            .build()
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalyzer
                            )
                            preview.setSurfaceProvider(previewView.surfaceProvider)
                        } catch (e: Exception) {
                            Log.e("FaceScanScreen", "Camera binding failed", e)
                        }
                    }, ContextCompat.getMainExecutor(context))
                }
            )
            // Face overlay image (iOS: Image("face_overlay") with 0.5 opacity, 360x360)
            if (faceResult?.processingStage == ProcessingStage.INITIAL_INSTRUCTIONS) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ThemedImage(
                        defaultResourceId = R.drawable.face_overlay,
                        overrideKey = "face_overlay",
                        contentDescription = "Face Overlay",
                        modifier = Modifier
                            .size(450.dp)
                            .alpha(0.5f)
                    )
                }
            }
            // Segmented progress circle (no overlays/text)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ProgressCircleView(
                    segmentStatus = segmentStatus,
                    modifier = Modifier.size(450.dp)
                )
            }
            // Orange instruction bar at the bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                CustomInfoButton(
                    buttonLabel = currentInstruction.ifBlank { "Move your head to fill all segments, then blink" },
                    isSecondary = false,
                    modifier = Modifier
                        .padding(horizontal = 15.dp)
                )
            }
            // Success indicator (iOS-like checkmark)
            if (faceResult?.processingStage == ProcessingStage.COMPLETED && !hasNavigated) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.Green, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓",
                            color = Color.White,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            // Error overlay
            error?.let { errorMessage ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = com.artiusid.sdk.ui.theme.ThemedStatusColors.getErrorColor().copy(alpha = 0.9f)
                        )
                    ) {
                        Text(
                            text = errorMessage,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Camera permission required",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
} 