/*
 * File: EnhancedCameraPreview.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.view.PreviewView
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import com.artiusid.sdk.presentation.screens.document.DocumentScanViewModel
import com.artiusid.sdk.utils.DocumentSide
import com.artiusid.sdk.utils.EnhancedCameraManager
import kotlinx.coroutines.launch

@Composable
fun EnhancedCameraPreview(
    preview: Preview,
    modifier: Modifier = Modifier,
    onFocusStableChanged: ((Boolean) -> Unit)? = null
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
                // Enable touch to focus
                setOnTouchListener { _, event ->
                    // Handle manual focus tap if needed
                    false
                }
            }
        },
        modifier = modifier,
        update = { previewView ->
            preview.setSurfaceProvider(previewView.surfaceProvider)
        }
    )
}

@Composable
fun EnhancedDocumentCameraPreview(
    modifier: Modifier = Modifier,
    viewModel: DocumentScanViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val documentSide by viewModel.documentSide.collectAsState()
    val isProcessingComplete by viewModel.isProcessingComplete.collectAsState()
    val scope = rememberCoroutineScope()
    
    var enhancedCameraManager by remember { mutableStateOf<EnhancedCameraManager?>(null) }
    var preview by remember { mutableStateOf<Preview?>(null) }
    var isFocusStable by remember { mutableStateOf(false) }
    
    // Track document bounds for autofocus adjustment
    var lastDetectedDocumentBounds by remember { mutableStateOf<Rect?>(null) }
    var frameWidth by remember { mutableStateOf(1280) }
    var frameHeight by remember { mutableStateOf(720) }
    
    // Add scan delay state - delay frame analysis but show camera preview immediately
    var isScanDelayActive by remember { mutableStateOf(true) }
    val scanDelayMs = 2000L // 2 seconds
    
    // Reset scan delay when document side changes
    LaunchedEffect(documentSide) {
        isScanDelayActive = true
        android.util.Log.d("EnhancedCameraPreview", "Starting 2-second analysis delay for ${documentSide.name} scan (camera preview active)")
        
        // Wait 2 seconds before allowing frame analysis
        kotlinx.coroutines.delay(scanDelayMs)
        
        isScanDelayActive = false
        android.util.Log.d("EnhancedCameraPreview", "Analysis delay completed, starting frame processing for ${documentSide.name}")
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            enhancedCameraManager?.stopCamera()
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            val cameraManager = EnhancedCameraManager(context)
            enhancedCameraManager = cameraManager
            
            // Start camera with enhanced autofocus
            cameraManager.startCamera(
                cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
                targetResolution = android.util.Size(frameWidth, frameHeight)
            ) { previewProvider ->
                // Process the camera preview
                // Note: Simplified for compilation
            }
            
            // Set preview state
            // Note: Type conversion needed for Preview compatibility
        }
        
        scope.launch {
            // Set focus stability
            isFocusStable = enhancedCameraManager?.isFocusStable ?: true
            android.util.Log.d("EnhancedCameraPreview", "Focus stable: $isFocusStable")
        }
    }

    preview?.let { previewInstance ->
        EnhancedCameraPreview(
            preview = previewInstance,
            modifier = modifier,
            onFocusStableChanged = { stable ->
                isFocusStable = stable
            }
        )
    }
}

/**
 * Process camera frame with enhanced autofocus adjustment
 */
private fun processCameraFrame(
    bitmap: Bitmap,
    viewModel: DocumentScanViewModel,
    documentSide: DocumentSide,
    isProcessingComplete: Boolean,
    cameraManager: EnhancedCameraManager,
    isScanDelayActive: Boolean
) {
    // Skip frame analysis during the 2-second delay period (camera preview still active)
    if (isScanDelayActive) {
        android.util.Log.d("EnhancedCameraPreview", "Analysis delay active, skipping frame analysis (preview still running)")
        return
    }
    
    android.util.Log.d("EnhancedCameraPreview", "Processing image: ${bitmap.width}x${bitmap.height}")
    
    when (documentSide) {
        DocumentSide.FRONT -> {
            // Process document image and get document bounds for autofocus
            android.util.Log.d("EnhancedCameraPreview", "Processing front scan frame for document detection")
            val documentBounds = viewModel.processDocumentImageWithBounds(bitmap)
            
            // Adjust autofocus based on detected document
            documentBounds?.let { bounds ->
                cameraManager.adjustFocusForDocument(bounds, bitmap.width, bitmap.height)
            }
        }
        DocumentSide.BACK -> {
            // Process every frame for continuous barcode detection
            if (!isProcessingComplete) {
                android.util.Log.d("EnhancedCameraPreview", "Processing back scan frame for barcode detection")
                val documentBounds = viewModel.processBackScanImageWithBounds(bitmap)
                
                // Adjust autofocus for barcode detection
                documentBounds?.let { bounds ->
                    cameraManager.adjustFocusForDocument(bounds, bitmap.width, bitmap.height)
                }
            }
        }
    }
}