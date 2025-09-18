/*
 * File: CameraPreview.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.components

import android.content.Context
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.artiusid.sdk.presentation.screens.document.DocumentScanViewModel
import com.artiusid.sdk.utils.DocumentSide
import kotlinx.coroutines.delay
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    preview: Preview,
    modifier: Modifier = Modifier,
    viewModel: DocumentScanViewModel? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        modifier = modifier,
        update = { previewView ->
            preview.setSurfaceProvider(previewView.surfaceProvider)
        }
    )
}

@Composable
fun DocumentCameraPreview(
    modifier: Modifier = Modifier,
    viewModel: DocumentScanViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val documentSide by viewModel.documentSide.collectAsState()
    val isProcessingComplete by viewModel.isProcessingComplete.collectAsState()
    
    var preview by remember { mutableStateOf<Preview?>(null) }
    var imageAnalysis by remember { mutableStateOf<ImageAnalysis?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    DisposableEffect(lifecycleOwner) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        
        val previewInstance = Preview.Builder().build()
        val imageAnalysisInstance = ImageAnalysis.Builder()
            .setTargetResolution(android.util.Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        
        imageAnalysisInstance.setAnalyzer(cameraExecutor) { imageProxy ->
            val bitmap = imageProxy.toBitmap()
            if (bitmap != null) {
                android.util.Log.d("DocumentCameraPreview", "Processing image: ${bitmap.width}x${bitmap.height}")
                
                // Use appropriate method based on document side
                val currentSide = viewModel.documentSide.value
                when (currentSide) {
                    DocumentSide.FRONT -> {
                        viewModel.processDocumentImage(bitmap)
                    }
                    DocumentSide.BACK -> {
                        // Process every frame for continuous barcode detection
                        if (!isProcessingComplete) {
                            android.util.Log.d("DocumentCameraPreview", "Processing back scan frame for barcode detection")
                            viewModel.processBackScanImage(bitmap)
                        }
                    }
                }
            }
            imageProxy.close()
        }
        
        try {
            cameraProvider.unbindAll()
            
            val cameraInstance = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                previewInstance,
                imageAnalysisInstance
            )
            
            preview = previewInstance
            imageAnalysis = imageAnalysisInstance
            camera = cameraInstance
            
            android.util.Log.d("DocumentCameraPreview", "Camera setup completed successfully")
            
        } catch (e: Exception) {
            android.util.Log.e("DocumentCameraPreview", "Camera setup failed: ${e.message}", e)
        }
    }

    preview?.let { previewInstance ->
        CameraPreview(
            preview = previewInstance,
            modifier = modifier,
            viewModel = viewModel
        )
    }
}

private fun ImageProxy.toBitmap(): android.graphics.Bitmap? {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
} 