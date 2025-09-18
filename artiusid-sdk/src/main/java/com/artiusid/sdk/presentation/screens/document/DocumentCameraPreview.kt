/*
 * File: DocumentCameraPreview.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.document

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.artiusid.sdk.ui.theme.GradientBackground
import com.artiusid.sdk.utils.DocumentSide
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong

@Composable
fun DocumentCameraPreview(
    modifier: Modifier = Modifier,
    viewModel: DocumentScanViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var detectedFaces by remember { mutableStateOf<List<Rect>>(emptyList()) }
    var isCameraBound by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var latestBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var currentZoom by remember { mutableStateOf(1.0f) }
    var frameCount by remember { mutableStateOf(0) }
    var validFrameCount by remember { mutableStateOf(0) }
    
    val processingMutex = Mutex()
    val lastProcessedTime = AtomicLong(0)
    val minFrameIntervalMs = 100L // 10 FPS
    
    DisposableEffect(lifecycleOwner) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
    
    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    this.scaleType = PreviewView.ScaleType.FILL_CENTER
                    previewView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Camera controls
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(16.dp)
        ) {
            Text(
                text = "Camera Status",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Frames: $frameCount",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Valid: $validFrameCount",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Zoom: ${String.format("%.1f", currentZoom)}x",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Zoom controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val newZoom = (currentZoom + 0.2f).coerceAtMost(2.0f)
                        currentZoom = newZoom
                        camera?.cameraControl?.setZoomRatio(newZoom)
                        Log.d("DocumentCameraPreview", "Zoom set to: $newZoom")
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Text("+")
                }
                
                Button(
                    onClick = {
                        val newZoom = (currentZoom - 0.2f).coerceAtLeast(1.0f)
                        currentZoom = newZoom
                        camera?.cameraControl?.setZoomRatio(newZoom)
                        Log.d("DocumentCameraPreview", "Zoom set to: $newZoom")
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Text("-")
                }
            }
        }
        
        // Capture button
        Button(
            onClick = {
                latestBitmap?.let { bitmap ->
                    Log.d("DocumentCameraPreview", "Capturing image with zoom: ${currentZoom}x")
                    Log.d("DocumentCameraPreview", "Image size: ${bitmap.width}x${bitmap.height}")
                    // For manual capture, use the existing onCapture callback
                    // This is mainly for front scan or manual capture scenarios
                } ?: run {
                    Log.w("DocumentCameraPreview", "No bitmap available for capture")
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text("Capture")
        }
    }
    
    LaunchedEffect(previewView) {
        if (previewView != null && !isCameraBound) {
            try {
                val cameraProvider = cameraProviderFuture.get()
                
                // Use back camera for document scanning
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                Log.d("DocumentCameraPreview", "Using back camera for document scanning")
                
                // Configure preview with basic settings
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView?.surfaceProvider)
                    }
                
                // Configure image analysis with basic settings
                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastProcessedTime.get() < minFrameIntervalMs) {
                                imageProxy.close()
                                return@setAnalyzer
                            }
                            lastProcessedTime.set(currentTime)

                            // Process frame in background without blocking UI
                            CoroutineScope(Dispatchers.Default).launch {
                                processingMutex.withLock {
                                    try {
                                        frameCount++
                                        
                                        // Process every frame for continuous barcode detection
                                        Log.d("DocumentCameraPreview", "Processing frame $frameCount: ${imageProxy.width}x${imageProxy.height}")
                                        
                                        // Convert to bitmap with proper aspect ratio
                                        val bitmap = imageProxy.toBitmap()
                                        if (bitmap != null && !isBitmapBlack(bitmap)) {
                                            validFrameCount++
                                            Log.d("DocumentCameraPreview", "Valid frame $validFrameCount received")
                                            
                                            // Store the latest bitmap for capture
                                            latestBitmap = bitmap
                                            
                                            // CRITICAL FIX: Pass every valid frame directly to the ViewModel for barcode detection
                                            // This ensures the processed image matches exactly what the user sees in the preview
                                            // Run barcode detection in background without blocking UI
                                            val currentDocumentSide = viewModel.documentSide.value
                                            if (currentDocumentSide == DocumentSide.BACK) {
                                                Log.d("DocumentCameraPreview", "Passing frame $validFrameCount to barcode detection pipeline")
                                                // Launch barcode detection in background
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    viewModel.processBackScanImage(bitmap)
                                                }
                                            } else if (currentDocumentSide == DocumentSide.FRONT) {
                                                Log.d("DocumentCameraPreview", "Passing frame $validFrameCount to front scan pipeline")
                                                // Launch front scan in background
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    viewModel.processDocumentImage(bitmap)
                                                }
                                            }
                                            
                                            // For now, use empty face list
                                            val faces = emptyList<Rect>()
                                            detectedFaces = faces
                                        } else {
                                            Log.w("DocumentCameraPreview", "Received black/empty frame, skipping")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("DocumentCameraPreview", "Error in image analysis: ${e.message}", e)
                                    }
                                }
                            }
                        }
                    }
                
                cameraProvider.unbindAll()
                val boundCamera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
                
                camera = boundCamera
                isCameraBound = true
                
                // Configure camera settings for optimal document scanning
                boundCamera.cameraControl.apply {
                    setZoomRatio(1.0f)
                }
                
                // Add camera state observer
                boundCamera.cameraInfo.cameraState.observe(lifecycleOwner) { state ->
                    when (state.type) {
                        androidx.camera.core.CameraState.Type.OPENING -> {
                            Log.d("DocumentCameraPreview", "Camera is opening...")
                        }
                        androidx.camera.core.CameraState.Type.OPEN -> {
                            Log.d("DocumentCameraPreview", "Camera is open and active")
                        }
                        androidx.camera.core.CameraState.Type.CLOSING -> {
                            Log.d("DocumentCameraPreview", "Camera is closing...")
                        }
                        androidx.camera.core.CameraState.Type.CLOSED -> {
                            Log.d("DocumentCameraPreview", "Camera is closed")
                            isCameraBound = false
                        }
                        androidx.camera.core.CameraState.Type.PENDING_OPEN -> {
                            Log.d("DocumentCameraPreview", "Camera is pending open")
                        }
                    }
                }
                
                // Add lifecycle observer to maintain camera activity
                lifecycleOwner.lifecycle.addObserver(object : androidx.lifecycle.LifecycleObserver {
                    @androidx.lifecycle.OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_PAUSE)
                    fun onPause() {
                        Log.d("DocumentCameraPreview", "Lifecycle paused, keeping camera active")
                    }
                    
                    @androidx.lifecycle.OnLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_DESTROY)
                    fun onDestroy() {
                        Log.d("DocumentCameraPreview", "Lifecycle destroyed, unbinding camera")
                        cameraProvider.unbindAll()
                        isCameraBound = false
                    }
                })
                
                Log.d("DocumentCameraPreview", "Camera bound successfully - frames will be passed directly to barcode detection")
                
            } catch (e: Exception) {
                Log.e("DocumentCameraPreview", "Error setting up camera", e)
                isCameraBound = false
            }
        }
    }
}

private fun ImageProxy.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = android.graphics.YuvImage(nv21, android.graphics.ImageFormat.NV21, width, height, null)
    val out = java.io.ByteArrayOutputStream()
    yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 100, out)
    val imageBytes = out.toByteArray()
    val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    return ensure16x9AspectRatio(bitmap)
}

private fun ensure16x9AspectRatio(bitmap: Bitmap): Bitmap {
    val targetAspectRatio = 16.0f / 9.0f // 1.778
    val currentAspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
    
    Log.d("DocumentCameraPreview", "Current aspect ratio: $currentAspectRatio, target: $targetAspectRatio")
    
    if (kotlin.math.abs(currentAspectRatio - targetAspectRatio) < 0.1f) {
        // Already close to 16:9, use as is
        Log.d("DocumentCameraPreview", "Bitmap already has correct 16:9 aspect ratio: ${bitmap.width}x${bitmap.height}")
        return bitmap
    } else {
        // Need to correct the aspect ratio - use cropping for better quality
        Log.d("DocumentCameraPreview", "Correcting aspect ratio from $currentAspectRatio to $targetAspectRatio")
        
        val targetWidth: Int
        val targetHeight: Int
        val cropX: Int
        val cropY: Int
        val cropWidth: Int
        val cropHeight: Int
        
        if (bitmap.width > bitmap.height) {
            // Landscape image - crop width to get 16:9
            targetHeight = bitmap.height
            targetWidth = (bitmap.height * targetAspectRatio).toInt()
            cropX = (bitmap.width - targetWidth) / 2
            cropY = 0
            cropWidth = targetWidth
            cropHeight = bitmap.height
        } else {
            // Portrait or square image - crop height to get 16:9
            targetWidth = bitmap.width
            targetHeight = (bitmap.width / targetAspectRatio).toInt()
            cropX = 0
            cropY = (bitmap.height - targetHeight) / 2
            cropWidth = bitmap.width
            cropHeight = targetHeight
        }
        
        // Crop the bitmap to get 16:9 aspect ratio
        val croppedBitmap = android.graphics.Bitmap.createBitmap(bitmap, cropX, cropY, cropWidth, cropHeight)
        
        val correctedAspectRatio = croppedBitmap.width.toFloat() / croppedBitmap.height.toFloat()
        Log.d("DocumentCameraPreview", "Cropped bitmap: ${croppedBitmap.width}x${croppedBitmap.height}")
        Log.d("DocumentCameraPreview", "Cropped bitmap aspect ratio: $correctedAspectRatio")
        
        return croppedBitmap
    }
}

private fun ExecutorService.shutdown() {
    try {
        this.shutdown()
    } catch (e: Exception) {
        Log.e("DocumentCameraPreview", "Error shutting down executor", e)
    }
}

/**
 * Check if a bitmap is mostly black or empty
 */
private fun isBitmapBlack(bitmap: Bitmap): Boolean {
    try {
        // Sample pixels from the center of the image
        val centerX = bitmap.width / 2
        val centerY = bitmap.height / 2
        val sampleSize = 10
        
        var blackPixelCount = 0
        var totalPixels = 0
        
        for (x in (centerX - sampleSize)..(centerX + sampleSize) step 2) {
            for (y in (centerY - sampleSize)..(centerY + sampleSize) step 2) {
                if (x >= 0 && x < bitmap.width && y >= 0 && y < bitmap.height) {
                    val pixel = bitmap.getPixel(x, y)
                    val red = android.graphics.Color.red(pixel)
                    val green = android.graphics.Color.green(pixel)
                    val blue = android.graphics.Color.blue(pixel)
                    
                    // Consider pixel black if all RGB values are very low
                    if (red < 10 && green < 10 && blue < 10) {
                        blackPixelCount++
                    }
                    totalPixels++
                }
            }
        }
        
        val blackPercentage = if (totalPixels > 0) blackPixelCount.toFloat() / totalPixels else 0f
        Log.d("DocumentCameraPreview", "Black pixel percentage: ${blackPercentage * 100}%")
        
        // Consider bitmap black if more than 80% of sampled pixels are black
        return blackPercentage > 0.8f
    } catch (e: Exception) {
        Log.e("DocumentCameraPreview", "Error checking if bitmap is black", e)
        return false
    }
} 