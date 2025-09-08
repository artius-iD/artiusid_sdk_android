package com.artiusid.sdk.ui.screens.document

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.artiusid.sdk.R
// Using fully qualified name to avoid conflicts
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artiusid.sdk.ui.components.CustomInfoButton
import com.artiusid.sdk.ui.theme.*
import com.artiusid.sdk.models.PassportScanningState
import com.artiusid.sdk.models.PassportMRZData
import com.artiusid.sdk.utils.passport.PassportTextAnalyzer

/**
 * PassportScanScreen - EXACT STANDALONE APPLICATION IMPLEMENTATION
 * This matches the standalone app's passport scanning screen exactly
 */
@Composable
fun PassportScanScreen(
    onPassportScanComplete: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: PassportScanViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Observe ViewModel state - EXACT STANDALONE IMPLEMENTATION
    val uiState by viewModel.uiState.collectAsState()
    val passportData by viewModel.passportData.collectAsState()
    val recognizedText by viewModel.recognizedText.collectAsState()
    
    // Handle success navigation with delay (like iOS) - EXACT STANDALONE IMPLEMENTATION
    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            kotlinx.coroutines.delay(2000) // Show success for 2 seconds
            onPassportScanComplete()
        }
    }
    
    // Camera permission handling - EXACT STANDALONE IMPLEMENTATION
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
    
    // EXACT STANDALONE IMPLEMENTATION - Black background with overlays
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            // Camera preview with MRZ analysis - EXACT STANDALONE IMPLEMENTATION
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onMRZDetected = { mrzData, bitmap ->
                    viewModel.onMRZDetected(mrzData, bitmap)
                },
                onTextRecognized = { textLines ->
                    viewModel.onTextRecognized(textLines)
                }
            )
            
            // Passport overlay image approach (like iOS and State ID implementation) - EXACT STANDALONE IMPLEMENTATION
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Use passport_overlay image like iOS ScanPassportView.swift line 49-54
                Image(
                    painter = painterResource(id = R.drawable.passport_overlay),
                    contentDescription = "Passport overlay",
                    modifier = Modifier
                        .size(width = getRelativeWidth(351f).dp, height = getRelativeHeight(510f).dp)
                        .alpha(0.4f),
                    contentScale = ContentScale.Fit
                )
            }
            
            // Instructions and status using CustomInfoButton (landscape orientation, centered on left side) - EXACT STANDALONE IMPLEMENTATION
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = getRelativeWidth(48f).dp
                    )
            ) {
                val statusText = when (uiState.scanningState) {
                    PassportScanningState.NOT_STARTED -> "HOLD PASSPORT HORIZONTALLY"
                    PassportScanningState.SCANNING -> "SCANNING MRZ ON LEFT SIDE..."
                    PassportScanningState.MRZ_DETECTED -> "MRZ DETECTED - HOLD STEADY"
                    PassportScanningState.VALIDATING -> "VALIDATING MRZ DATA..."
                    PassportScanningState.COMPLETED -> "✓ PASSPORT SCAN COMPLETE"
                    PassportScanningState.FAILED -> "SCAN FAILED - TRY AGAIN"
                    PassportScanningState.IDLE -> "POSITION PASSPORT IN FRAME"
                    PassportScanningState.PROCESSING_MRZ -> "PROCESSING MRZ DATA..."
                    PassportScanningState.MRZ_COMPLETE -> "MRZ PROCESSING COMPLETE"
                    PassportScanningState.READY_FOR_NFC -> "READY FOR NFC SCAN"
                    PassportScanningState.ERROR -> "ERROR - PLEASE TRY AGAIN"
                }
                
                CustomInfoButton(
                    buttonLabel = statusText,
                    isSecondary = false,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .rotate(90f)
                        .width(getRelativeWidth(140f).dp)
                )
            }
            
            // Success overlay (similar to DocumentScanScreen) - EXACT STANDALONE IMPLEMENTATION
            if (uiState.isComplete) {
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
                                text = "Passport captured successfully!",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Proceeding to NFC chip scan...",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            
            // Back button - EXACT STANDALONE IMPLEMENTATION
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        } else {
            // Permission request - EXACT STANDALONE IMPLEMENTATION
            PermissionRequest(
                onRequestPermission = {
                    launcher.launch(Manifest.permission.CAMERA)
                }
            )
        }
    }
}

/**
 * CameraPreview - EXACT STANDALONE APPLICATION IMPLEMENTATION
 * This handles the camera preview and MRZ detection
 */
@Composable
private fun CameraPreview(
    modifier: Modifier = Modifier,
    onMRZDetected: (com.artiusid.sdk.data.models.passport.PassportMRZData, android.graphics.Bitmap) -> Unit,
    onTextRecognized: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var textAnalyzer by remember { mutableStateOf<PassportTextAnalyzer?>(null) }
    
    // Initialize text analyzer - EXACT STANDALONE IMPLEMENTATION
    LaunchedEffect(Unit) {
        Log.d("PassportScan", "Initializing PassportTextAnalyzer")
        textAnalyzer = PassportTextAnalyzer(
            onMRZDetected = onMRZDetected,
            onTextRecognized = onTextRecognized,
            onPassportCaptured = { bitmap ->
                Log.d("PassportScan", "📸 Passport captured: ${bitmap.width}x${bitmap.height}")
                // TODO: Store or process captured passport image
            }
        )
        Log.d("PassportScan", "PassportTextAnalyzer initialized")
    }
    
    // Cleanup analyzer on dispose - EXACT STANDALONE IMPLEMENTATION
    DisposableEffect(Unit) {
        onDispose {
            textAnalyzer?.cleanup()
        }
    }
    
    LaunchedEffect(Unit) {
        val provider = ProcessCameraProvider.getInstance(context).get()
        cameraProvider = provider
    }
    
    val previewView = remember { 
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    // Bind camera only once when components are ready - EXACT STANDALONE IMPLEMENTATION
    LaunchedEffect(cameraProvider, textAnalyzer) {
        cameraProvider?.let { provider ->
            textAnalyzer?.let { analyzer ->
                try {
                    val preview = Preview.Builder()
                        .setTargetResolution(Size(1920, 1080)) // High resolution 16:9 to match preview
                        .build()
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                    
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(1920, 1080)) // High resolution 16:9 for better OCR/MRZ
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    
                    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), analyzer)
                    
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    
                    Log.d("PassportScan", "Camera bound successfully")
                } catch (e: Exception) {
                    Log.e("PassportScan", "Camera binding error: ${e.message}", e)
                }
            }
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

/**
 * PermissionRequest - EXACT STANDALONE APPLICATION IMPLEMENTATION
 */
@Composable
private fun PermissionRequest(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Camera Permission Required",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "This app needs camera access to scan your passport",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRequestPermission,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF58220) // Yellow900
            )
        ) {
            Text("Grant Permission")
        }
    }
}

/**
 * Helper functions to match iOS relative dimensions - EXACT STANDALONE IMPLEMENTATION
 */
@Composable
fun getRelativeWidth(value: Float): Float {
    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    return (displayMetrics.widthPixels * value) / 375.0f
}

@Composable  
fun getRelativeHeight(value: Float): Float {
    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    return (displayMetrics.heightPixels * value) / 812.0f
}

