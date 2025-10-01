/*
 * File: FaceCameraPreview.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.face

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun FaceCameraPreview(
    onCapture: (Bitmap) -> Unit,
    onFaceDetected: (List<Rect>) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var imageAnalyzer by remember { mutableStateOf<ImageAnalysis?>(null) }
    var cameraExecutor by remember { mutableStateOf<ExecutorService?>(null) }
    var detectedFaces by remember { mutableStateOf<List<Rect>>(emptyList()) }
    var isCameraBound by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        cameraExecutor = Executors.newSingleThreadExecutor()
        onDispose {
            cameraExecutor?.shutdown()
        }
    }

    LaunchedEffect(previewView) {
        // Only bind camera once when previewView is available and camera is not already bound
        if (previewView != null && !isCameraBound) {
            val cameraProvider = cameraProviderFuture.get()
            try {
                cameraProvider.unbindAll()
                
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView?.surfaceProvider)
                }

                imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor!!) { imageProxy ->
                            val bitmap = imageProxy.toBitmap()
                            if (bitmap != null) {
                                val faces = FaceDetectionManager(context).detectFaces(bitmap)
                                detectedFaces = faces
                                onFaceDetected(faces)
                            }
                            imageProxy.close()
                        }
                    }

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageAnalyzer
                )
                
                isCameraBound = true
            } catch (e: Exception) {
                e.printStackTrace()
                isCameraBound = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also {
                    previewView = it
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Draw face detection rectangles
        val faceColor = com.artiusid.sdk.ui.theme.ThemedFaceDetection.getAlignedColor()
        Canvas(modifier = Modifier.fillMaxSize()) {
            detectedFaces.forEach { face ->
                val width = face.width().toFloat()
                val height = face.height().toFloat()
                drawRect(
                    color = faceColor,
                    topLeft = Offset(face.left.toFloat(), face.top.toFloat()),
                    size = Size(width, height),
                    style = Stroke(width = 2f)
                )
            }
        }

        // Capture button
        Button(
            onClick = {
                previewView?.bitmap?.let { bitmap ->
                    onCapture(bitmap)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text("Capture")
        }
    }
}

private fun ImageProxy.toBitmap(): Bitmap? {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
} 