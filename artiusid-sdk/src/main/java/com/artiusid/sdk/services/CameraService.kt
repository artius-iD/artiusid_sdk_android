package com.artiusid.sdk.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Camera service for document capture using CameraX
 */
class CameraService(private val context: Context) {
    
    companion object {
        private const val TAG = "CameraService"
    }
    
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var preview: Preview? = null
    private var camera: Camera? = null
    
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    
    private val _isCameraReady = MutableStateFlow(false)
    val isCameraReady: StateFlow<Boolean> = _isCameraReady.asStateFlow()
    
    private val _capturedImage = MutableStateFlow<Bitmap?>(null)
    val capturedImage: StateFlow<Bitmap?> = _capturedImage.asStateFlow()
    
    /**
     * Start camera with preview
     */
    suspend fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        imageAnalyzer: ImageAnalysis.Analyzer? = null
    ) {
        try {
            Log.d(TAG, "Starting camera...")
            
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProvider = cameraProviderFuture.get()
            
            // Preview use case
            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
            
            // Image capture use case
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()
            
            // Image analysis use case (optional)
            this.imageAnalyzer = imageAnalyzer?.let { analyzer ->
                ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor, analyzer)
                    }
            }
            
            // Select back camera as default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            // Unbind use cases before rebinding
            cameraProvider?.unbindAll()
            
            // Bind use cases to camera
            val useCases = mutableListOf<UseCase>().apply {
                preview?.let { add(it) }
                imageCapture?.let { add(it) }
                this@CameraService.imageAnalyzer?.let { add(it) }
            }
            
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                *useCases.toTypedArray()
            )
            
            _isCameraReady.value = true
            Log.d(TAG, "Camera started successfully")
            
        } catch (exc: Exception) {
            Log.e(TAG, "Camera startup failed", exc)
            _isCameraReady.value = false
        }
    }
    
    /**
     * Capture image
     */
    fun captureImage(onImageCaptured: (Bitmap?) -> Unit) {
        val imageCapture = imageCapture ?: run {
            Log.e(TAG, "Image capture use case not initialized")
            onImageCaptured(null)
            return
        }
        
        Log.d(TAG, "Capturing image...")
        
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
            context.cacheDir.resolve("temp_capture_${System.currentTimeMillis()}.jpg")
        ).build()
        
        imageCapture.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Image captured successfully: ${output.savedUri}")
                    
                    // Load the captured image as bitmap
                    try {
                        val bitmap = BitmapFactory.decodeFile(output.savedUri?.path)
                        _capturedImage.value = bitmap
                        onImageCaptured(bitmap)
                        
                        // Clean up temp file
                        output.savedUri?.path?.let { path ->
                            context.cacheDir.resolve(path).delete()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to load captured image", e)
                        onImageCaptured(null)
                    }
                }
                
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Image capture failed", exception)
                    onImageCaptured(null)
                }
            }
        )
    }
    
    /**
     * Enable/disable flash
     */
    fun setFlashMode(flashMode: Int) {
        imageCapture?.flashMode = flashMode
    }
    
    /**
     * Stop camera
     */
    fun stopCamera() {
        Log.d(TAG, "Stopping camera...")
        cameraProvider?.unbindAll()
        _isCameraReady.value = false
    }
    
    /**
     * Release resources
     */
    fun release() {
        Log.d(TAG, "Releasing camera resources...")
        stopCamera()
        cameraExecutor.shutdown()
    }
}

/**
 * Extension function to convert ImageProxy to Bitmap
 */
fun ImageProxy.toBitmap(): Bitmap {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
