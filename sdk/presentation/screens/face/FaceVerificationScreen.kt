/*
 * File: FaceVerificationScreen.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.face

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.artiusid.sdk.presentation.components.*
import com.artiusid.sdk.ui.theme.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.artiusid.sdk.ui.theme.GradientBackground
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.artiusid.sdk.presentation.components.LoadingIndicator
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState

private const val TAG = "FaceVerification"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun FaceVerificationScreen(
    onNavigateBack: () -> Unit,
    onVerificationComplete: (Boolean) -> Unit,
    viewModel: FaceVerificationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isInitializing by remember { mutableStateOf(true) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    val faceDetectionManager = remember {
        FaceDetectionManager(context).apply {
            lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_DESTROY) {
                    // Clean up resources
                }
            })
        }
    }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    
    LaunchedEffect(Unit) {
        try {
            cameraProvider = cameraProviderFuture.get()
        } catch (e: Exception) {
            cameraError = "Failed to get camera provider: ${e.message}"
            Log.e(TAG, cameraError!!, e)
        }
        
        faceDetectionManager.waitForInitialization {
            isInitializing = false
        }
    }

    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(
                    ContextCompat.getMainExecutor(context),
                    ImageAnalysis.Analyzer { imageProxy ->
                        try {
                            val bitmap = imageProxy.toBitmap()
                            val faces = faceDetectionManager.detectFaces(bitmap)
                            
                            if (faces.isNotEmpty()) {
                                // Face detected - the ViewModel now handles this automatically
                                // No need to call processDetectedFaces as it's handled by the face detection service
                            }
                            
                            imageProxy.close()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing camera frame", e)
                            imageProxy.close()
                        }
                    }
                )
            }
    }
    
    GradientBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                isInitializing -> {
                    LoadingIndicator(
                        modifier = Modifier.fillMaxSize()
                    )
                }
                cameraPermissionState.status is com.google.accompanist.permissions.PermissionStatus.Denied -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Camera permission is required for face verification",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                            Text("Grant Permission")
                        }
                    }
                }
                else -> {
                    FaceCameraPreview(
                        onCapture = { bitmap ->
                            // TODO: Handle face verification
                            onVerificationComplete(true)
                        },
                        onFaceDetected = { faces ->
                            // TODO: Handle face detection
                        },
                        onError = { error ->
                            // TODO: Handle error
                            Log.e(TAG, "Face verification error: $error")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FaceCameraPreview(
    onCapture: (Bitmap) -> Unit,
    onFaceDetected: (List<Face>) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }

    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
    }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // Initialize face detector with offline mode
    val faceDetector = remember {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.15f)
                .enableTracking()
                .build()
        )
    }

    LaunchedEffect(previewView) {
        try {
            val cameraProvider = cameraProviderFuture.get()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            cameraProvider.unbindAll()
            
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                imageCapture
            )

            // Start face detection
            imageCapture.takePicture(
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        scope.launch {
                            try {
                                isProcessing = true
                                val bitmap = image.toBitmap()
                                val inputImage = InputImage.fromBitmap(bitmap, image.imageInfo.rotationDegrees)
                                
                                val faces = faceDetector.process(inputImage).await()
                                
                                if (faces.isEmpty()) {
                                    onError("No face detected")
                                } else if (faces.size > 1) {
                                    onError("Multiple faces detected")
                                } else {
                                    val face = faces[0]
                                    // Validate face position and quality - using stricter thresholds for straight-forward detection
                                    if (face.headEulerAngleX > 8 || face.headEulerAngleX < -8 ||
                                        face.headEulerAngleY > 8 || face.headEulerAngleY < -8
                                    ) {
                                        onError("Please look straight at the camera")
                                    } else if (face.leftEyeOpenProbability != null && face.rightEyeOpenProbability != null &&
                                        (face.leftEyeOpenProbability!! < 0.5f || face.rightEyeOpenProbability!! < 0.5f)
                                    ) {
                                        onError("Please keep your eyes open")
                                    } else {
                                        onCapture(bitmap)
                                    }
                                }
                            } catch (e: Exception) {
                                onError("Face detection failed: ${e.message}")
                            } finally {
                                isProcessing = false
                                image.close()
                            }
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        onError("Failed to capture image: ${exception.message}")
                        isProcessing = false
                    }
                }
            )
        } catch (e: Exception) {
            onError("Failed to start camera: ${e.message}")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        FaceScanFrame(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            isProcessing = isProcessing
        )

        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = com.artiusid.sdk.ui.theme.ThemedButtonColors.getPrimaryButtonColor(),
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
fun FaceDetectionOverlay(
    modifier: Modifier = Modifier
) {
    // TODO: Implement face detection overlay with rotation tracking and blink detection
    Box(modifier = modifier) {
        // Face detection visualization will be added here
    }
}

private fun createImageFile(context: android.content.Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "FACE_${timeStamp}_",
        ".jpg",
        storageDir
    )
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

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
} 