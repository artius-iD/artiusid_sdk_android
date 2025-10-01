/*
 * Author: Todd Bryant
 * Company: artius.iD
 * Enhanced FaceScanScreen with iOS-like segmented circle animation
 */

package com.artiusid.sdk.ui.screens.face

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import com.artiusid.sdk.ui.components.ThemedImage
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artiusid.sdk.services.FaceMeshResult
import com.artiusid.sdk.services.ProcessingStage
import com.artiusid.sdk.services.Point
import com.artiusid.sdk.ui.components.CustomBackButton
import com.artiusid.sdk.ui.components.CustomInfoButton
import com.artiusid.sdk.ui.theme.*
import com.artiusid.sdk.presentation.screens.face.FaceVerificationViewModelFactory
import com.artiusid.sdk.services.FaceMeshDetectorServiceImpl
import com.artiusid.sdk.presentation.screens.face.FaceVerificationViewModel
import com.artiusid.sdk.R
import kotlinx.coroutines.delay
import kotlin.math.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@Composable
fun FaceScanScreen(
    onNavigateToDocumentScan: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Create ViewModel with proper factory
    val faceMeshDetectorService = remember { FaceMeshDetectorServiceImpl(context) }
    val viewModel: FaceVerificationViewModel = viewModel(
        factory = FaceVerificationViewModelFactory(faceMeshDetectorService)
    )
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    // Camera permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Camera setup state
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    
    // Initialize camera provider
    LaunchedEffect(Unit) {
        cameraProvider = try {
            cameraProviderFuture.get()
        } catch (e: Exception) {
            android.util.Log.e("FaceScanScreen", "Failed to get camera provider: ${e.message}", e)
            null
        }
    }
    
    // Setup camera when permission is granted and preview view is available
    LaunchedEffect(hasCameraPermission, previewView, cameraProvider) {
        if (hasCameraPermission && previewView != null && cameraProvider != null) {
            val preview = Preview.Builder()
                .setTargetRotation(previewView!!.display.rotation)
                .build()
            
            // Set the surface provider
            preview.setSurfaceProvider(previewView!!.surfaceProvider)
            
            // Unbind any existing use cases
            cameraProvider!!.unbindAll()
            
            // Bind the preview use case to the lifecycle
            cameraProvider!!.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview
            )
            
            // Add image analyzer if needed
            try {
                val imageAnalyzer = viewModel.createImageAnalyzer()
                cameraProvider!!.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                android.util.Log.w("FaceScanScreen", "Image analyzer binding failed: ${e.message}")
                // Continue without image analyzer for now
            }
        }
    }
    
    // Request permission on first launch
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    // iOS-like state management
    val progress by viewModel.progress.collectAsState()
    val segmentStatus by viewModel.segmentStatus.collectAsState()
    val currentInstruction by viewModel.currentInstruction.collectAsState()
    val isProcessingComplete by viewModel.isProcessingComplete.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val faceResult by viewModel.faceResult.collectAsState()
    
    // iOS-like processing stage
    var processingStage by remember { mutableStateOf(ProcessingStage.INITIAL_INSTRUCTIONS) }
    var alignmentDirection by remember { mutableStateOf("") }
    var hintText by remember { mutableStateOf("") }
    
    // Update processing stage from face result
    LaunchedEffect(faceResult) {
        faceResult?.let { result ->
            processingStage = result.processingStage
            alignmentDirection = result.alignmentDirection
            hintText = result.hintText
        }
    }
    
    // Navigate to next screen when complete
    LaunchedEffect(isProcessingComplete) {
        if (isProcessingComplete) {
            delay(3000) // iOS-like 3 second delay
            onNavigateToDocumentScan()
        }
    }
    
    // iOS-like background gradient
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = com.artiusid.sdk.ui.theme.ColorManager.getGradientBrush()
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Custom back button
            CustomBackButton(
                onBackClick = onNavigateBack,
                navTitle = "Face Scan"
            )
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Main content area - iOS-like circular layout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(horizontal = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                // Camera preview with iOS-like circular mask
                if (hasCameraPermission) {
                    AndroidView(
                        factory = { context ->
                            PreviewView(context).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }
                        },
                        modifier = Modifier
                            .size(450.dp)
                            .clip(CircleShape),
                        update = { view ->
                            previewView = view
                        }
                    )
                } else {
                    // Camera permission not granted - show placeholder
                    Box(
                        modifier = Modifier
                            .size(450.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.camera_permission_required),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.camera_permission_description),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor()
                                )
                            ) {
                                Text(stringResource(R.string.grant_permission))
                            }
                        }
                    }
                }
                // Face overlay and positioning animations (iOS-like)
                if (faceResult?.processingStage == ProcessingStage.INITIAL_INSTRUCTIONS) {
                    // Face outline overlay to guide positioning (like standalone app)
                    ThemedImage(
                        defaultResourceId = R.drawable.face_overlay,
                        overrideKey = "face_overlay",
                        contentDescription = "Face Outline Guide",
                        modifier = Modifier
                            .size(450.dp)
                            .alpha(0.3f)
                    )
                    
                    // Show positioning guidance GIF based on face detection results
                    faceResult?.let { result ->
                        if (result.alignmentDirection.isNotEmpty()) {
                            // Show specific positioning guidance GIF - smaller size to fit within face outline
                            com.artiusid.sdk.presentation.components.FacePositioningAnimationView(
                                direction = result.alignmentDirection,
                                modifier = Modifier
                                    .size(300.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                } else {
                    // Show progress circle for segment capture
                    ProgressCircleView(
                        segmentStatus = segmentStatus.ifEmpty { List(8) { false } },
                        modifier = Modifier.size(450.dp)
                    )
                }
            }
            
            // Orange instruction bar - always visible below the circle
            Spacer(modifier = Modifier.height(16.dp))
            CustomInfoButton(
                buttonLabel = currentInstruction.ifBlank { stringResource(R.string.face_turn_head_slowly) },
                isSecondary = false,
                modifier = Modifier
                    .padding(horizontal = 15.dp)
                    .fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun FacePositioningOverlay(
    direction: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // iOS-like animated GIF overlay would be here
        // For now, using placeholder animations
        when (direction) {
            "Phone Up" -> {
                // Animated phone up indicator
                AnimatedDirectionIndicator(
                    direction = "up",
                    color = Color.White
                )
            }
            "Phone Down" -> {
                // Animated phone down indicator
                AnimatedDirectionIndicator(
                    direction = "down",
                    color = Color.White
                )
            }
            "Face Up" -> {
                // Animated face up indicator
                AnimatedDirectionIndicator(
                    direction = "up",
                    color = Color.White
                )
            }
            "Face Down" -> {
                // Animated face down indicator
                AnimatedDirectionIndicator(
                    direction = "down",
                    color = Color.White
                )
            }
            else -> {
                // No direction - empty overlay
            }
        }
    }
}

@Composable
fun AnimatedDirectionIndicator(
    direction: String,
    color: Color
) {
    var isVisible by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(750)
            isVisible = !isVisible
        }
    }
    
    Canvas(
        modifier = Modifier.size(150.dp)
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val arrowSize = 50f
        
        when (direction) {
            "up" -> {
                // Draw up arrow
                drawLine(
                    color = color.copy(alpha = if (isVisible) 0.8f else 0.0f),
                    start = Offset(center.x, center.y + arrowSize),
                    end = Offset(center.x, center.y - arrowSize),
                    strokeWidth = 8f
                )
                drawLine(
                    color = color.copy(alpha = if (isVisible) 0.8f else 0.0f),
                    start = Offset(center.x - arrowSize/2, center.y - arrowSize/2),
                    end = Offset(center.x, center.y - arrowSize),
                    strokeWidth = 8f
                )
                drawLine(
                    color = color.copy(alpha = if (isVisible) 0.8f else 0.0f),
                    start = Offset(center.x + arrowSize/2, center.y - arrowSize/2),
                    end = Offset(center.x, center.y - arrowSize),
                    strokeWidth = 8f
                )
            }
            "down" -> {
                // Draw down arrow
                drawLine(
                    color = color.copy(alpha = if (isVisible) 0.8f else 0.0f),
                    start = Offset(center.x, center.y - arrowSize),
                    end = Offset(center.x, center.y + arrowSize),
                    strokeWidth = 8f
                )
                drawLine(
                    color = color.copy(alpha = if (isVisible) 0.8f else 0.0f),
                    start = Offset(center.x - arrowSize/2, center.y + arrowSize/2),
                    end = Offset(center.x, center.y + arrowSize),
                    strokeWidth = 8f
                )
                drawLine(
                    color = color.copy(alpha = if (isVisible) 0.8f else 0.0f),
                    start = Offset(center.x + arrowSize/2, center.y + arrowSize/2),
                    end = Offset(center.x, center.y + arrowSize),
                    strokeWidth = 8f
                )
            }
        }
    }
}

@Composable
fun ProgressCircleView(
    segmentStatus: List<Boolean>,
    modifier: Modifier = Modifier
) {
    // Animation state
    var animationRotation by remember { mutableStateOf(0f) }
    val completedCount = segmentStatus.count { it }
    val isCompleted = completedCount == 8
    
    // Animate rotation for completion indicator
    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            while (true) {
                animationRotation += 360f
                delay(1000)
            }
        }
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val outerRadius = kotlin.math.min(size.width, size.height) / 2
            val segmentCircleRadius = outerRadius * 0.95f  // Position segments closer to the edge of the camera circle
            val strokeWidth = segmentCircleRadius * 0.08f  // Slightly thinner stroke for better proportion
            
            // Background circle segments (iOS-like background)
            val segmentAngle = 360f / 8f
            val segmentLength = 0.8f // 80% of segment length with gap
            
            for (i in 0 until 8) {
                val startAngle = i * segmentAngle - 90f - 22.5f // Start from top, iOS rotation offset
                val sweepAngle = segmentAngle * segmentLength
                
                // Background segment - removed to eliminate unwanted grey circle
                
                // Progress segment with enhanced colors and animation (iOS-like)
                val segmentColor = getSegmentColor(i, segmentStatus)
                val isCurrentSegment = segmentStatus.getOrNull(i) == true
                val scale = if (isCurrentSegment) 1.1f else 1.0f
                
                drawArc(
                    color = segmentColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidth * scale,
                        cap = StrokeCap.Round
                    ),
                    size = Size(segmentCircleRadius * 2 * scale, segmentCircleRadius * 2 * scale),
                    topLeft = Offset(
                        center.x - segmentCircleRadius * scale, 
                        center.y - segmentCircleRadius * scale
                    )
                )
            }
        }
        
        // Completion indicator (iOS-like checkmark animation)
        if (isCompleted) {
            Canvas(
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer {
                        rotationZ = animationRotation
                        scaleX = 1.0f + 0.1f * sin(animationRotation * PI.toFloat() / 180f)
                        scaleY = 1.0f + 0.1f * sin(animationRotation * PI.toFloat() / 180f)
                    }
            ) {
                val checkmarkSize = size.minDimension * 0.6f
                val strokeWidth = checkmarkSize * 0.1f
                val center = Offset(size.width / 2, size.height / 2)
                
                // Draw checkmark
                val path = Path().apply {
                    moveTo(center.x - checkmarkSize * 0.25f, center.y)
                    lineTo(center.x - checkmarkSize * 0.1f, center.y + checkmarkSize * 0.15f)
                    lineTo(center.x + checkmarkSize * 0.25f, center.y - checkmarkSize * 0.15f)
                }
                
                drawPath(
                    path = path,
                    color = Color.Green,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    )
                )
            }
        }
        
        // Progress text with completion count (iOS-like)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.offset(y = 120.dp) // Position below the circle
        ) {
            Text(
                text = "$completedCount/8 segments",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            // Removed "Now blink!" text - only show head turning instruction
            Text(
                text = stringResource(R.string.face_turn_head_slowly),
                color = AppColors.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun getSegmentColor(index: Int, segmentStatus: List<Boolean>): Color {
    return if (segmentStatus.getOrNull(index) == true) {
        Color.Green // Use green for completed segments
    } else {
        // All incomplete segments should be red (no orange highlighting)
        Color.Red.copy(alpha = 0.6f) // Use red for incomplete segments
    }
} 