package com.artiusid.sdk.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.util.Log
import android.util.Size
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

/**
 * Enhanced camera service with advanced autofocus and document-specific optimizations
 */
class EnhancedCameraService(
    private val context: Context
) {
    companion object {
        private const val TAG = "EnhancedCameraService"
        private const val FOCUS_UPDATE_THROTTLE_MS = 500L
        private const val FOCUS_STABILITY_THRESHOLD = 3
    }
    
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var preview: Preview? = null
    private lateinit var cameraExecutor: ExecutorService
    
    // Focus management
    private var focusStabilityMonitor: FocusStabilityMonitor? = null
    private var lastFocusUpdateTime = System.currentTimeMillis()
    private var lastDocumentBounds: Rect? = null
    private var focusLockScheduled = false
    
    private val _isCameraReady = MutableStateFlow(false)
    val isCameraReady: StateFlow<Boolean> = _isCameraReady.asStateFlow()
    
    private val _capturedImage = MutableStateFlow<Bitmap?>(null)
    val capturedImage: StateFlow<Bitmap?> = _capturedImage.asStateFlow()
    
    private val _isFocusStable = MutableStateFlow(false)
    val isFocusStable: StateFlow<Boolean> = _isFocusStable.asStateFlow()
    
    private val _documentBounds = MutableStateFlow<Rect?>(null)
    val documentBounds: StateFlow<Rect?> = _documentBounds.asStateFlow()
    
    init {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }
    
    /**
     * Start enhanced camera with document-specific optimizations
     */
    suspend fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        targetResolution: Size = Size(1280, 720),
        imageAnalyzer: ImageAnalysis.Analyzer? = null
    ) {
        try {
            Log.d(TAG, "Starting enhanced camera with document optimizations...")
            
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProvider = cameraProviderFuture.get()
            
            // Enhanced preview configuration
            preview = Preview.Builder()
                .setTargetResolution(targetResolution)
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
            
            // Enhanced image capture configuration
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setTargetResolution(targetResolution)
                .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                .apply {
                    // Configure enhanced settings using Camera2 interop
                    val camera2Interop = Camera2Interop.Extender(this)
                    configureEnhancedImageCapture(camera2Interop)
                }
                .build()
            
            // Enhanced image analysis configuration
            this.imageAnalyzer = imageAnalyzer?.let { analyzer ->
                ImageAnalysis.Builder()
                    .setTargetResolution(targetResolution)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .apply {
                        val camera2Interop = Camera2Interop.Extender(this)
                        configureEnhancedAutofocus(camera2Interop)
                    }
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            processImageProxy(imageProxy, analyzer)
                        }
                    }
            }
            
            // Select back camera with enhanced configuration
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            // Unbind use cases before rebinding
            cameraProvider?.unbindAll()
            
            // Bind use cases to camera
            val useCases = mutableListOf<UseCase>().apply {
                preview?.let { add(it) }
                imageCapture?.let { add(it) }
                this@EnhancedCameraService.imageAnalyzer?.let { add(it) }
            }
            
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                *useCases.toTypedArray()
            )
            
            // Initialize enhanced focus monitoring
            initializeFocusMonitoring()
            
            _isCameraReady.value = true
            Log.d(TAG, "Enhanced camera started successfully")
            
        } catch (exc: Exception) {
            Log.e(TAG, "Enhanced camera startup failed", exc)
            _isCameraReady.value = false
        }
    }
    
    /**
     * Configure enhanced autofocus for document scanning
     */
    private fun configureEnhancedAutofocus(camera2Interop: Camera2Interop.Extender<*>) {
        try {
            // Set continuous autofocus mode optimized for documents
            camera2Interop.setCaptureRequestOption(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            
            // Enable optical image stabilization if available
            camera2Interop.setCaptureRequestOption(
                CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
                CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON
            )
            
            // Optimize exposure for documents
            camera2Interop.setCaptureRequestOption(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
            )
            
            Log.d(TAG, "Enhanced autofocus configuration applied")
            
        } catch (e: Exception) {
            Log.w(TAG, "Some enhanced autofocus features not available on this device", e)
        }
    }
    
    /**
     * Configure enhanced image capture settings
     */
    private fun configureEnhancedImageCapture(camera2Interop: Camera2Interop.Extender<*>) {
        try {
            // Set high quality JPEG compression
            camera2Interop.setCaptureRequestOption(
                CaptureRequest.JPEG_QUALITY,
                95.toByte()
            )
            
            // Enable noise reduction
            camera2Interop.setCaptureRequestOption(
                CaptureRequest.NOISE_REDUCTION_MODE,
                CaptureRequest.NOISE_REDUCTION_MODE_HIGH_QUALITY
            )
            
            // Set edge enhancement for document text
            camera2Interop.setCaptureRequestOption(
                CaptureRequest.EDGE_MODE,
                CaptureRequest.EDGE_MODE_HIGH_QUALITY
            )
            
            Log.d(TAG, "Enhanced image capture configuration applied")
            
        } catch (e: Exception) {
            Log.w(TAG, "Some enhanced image capture features not available", e)
        }
    }
    
    /**
     * Process image proxy with enhanced analysis
     */
    private fun processImageProxy(imageProxy: ImageProxy, analyzer: ImageAnalysis.Analyzer) {
        try {
            // Run the provided analyzer
            analyzer.analyze(imageProxy)
            
            // Additional processing for focus stability
            monitorFocusStability(imageProxy)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image proxy", e)
        } finally {
            imageProxy.close()
        }
    }
    
    /**
     * Monitor focus stability for optimal document capture timing
     */
    private fun monitorFocusStability(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        
        // Throttle focus updates to avoid excessive processing
        if (currentTime - lastFocusUpdateTime < FOCUS_UPDATE_THROTTLE_MS) {
            return
        }
        
        lastFocusUpdateTime = currentTime
        
        // Update focus stability monitor
        focusStabilityMonitor?.let { monitor ->
            val bitmap = imageProxy.toBitmap()
            val isStable = monitor.updateFocusStability(bitmap)
            _isFocusStable.value = isStable
            
            if (isStable && !focusLockScheduled) {
                scheduleFocusLock()
            }
        }
    }
    
    /**
     * Initialize focus stability monitoring
     */
    private fun initializeFocusMonitoring() {
        focusStabilityMonitor = FocusStabilityMonitor()
        Log.d(TAG, "Focus stability monitoring initialized")
    }
    
    /**
     * Schedule focus lock for optimal capture timing
     */
    private fun scheduleFocusLock() {
        focusLockScheduled = true
        
        // Lock focus after a short delay to ensure stability
        CoroutineScope(Dispatchers.Main).launch {
            delay(200)
            lockFocusForCapture()
            focusLockScheduled = false
        }
    }
    
    /**
     * Lock focus for optimal document capture
     */
    private fun lockFocusForCapture() {
        try {
            camera?.cameraControl?.let { cameraControl ->
                // Trigger autofocus
                val action = FocusMeteringAction.Builder(
                    SurfaceOrientedMeteringPointFactory(1.0f, 1.0f)
                        .createPoint(0.5f, 0.5f)
                ).build()
                
                cameraControl.startFocusAndMetering(action)
                Log.d(TAG, "Focus locked for document capture")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to lock focus", e)
        }
    }
    
    /**
     * Capture image with enhanced quality
     */
    fun captureImage(onImageCaptured: (Bitmap?) -> Unit) {
        val imageCapture = imageCapture ?: run {
            Log.e(TAG, "Image capture use case not initialized")
            onImageCaptured(null)
            return
        }
        
        Log.d(TAG, "Capturing enhanced image...")
        
        val outputFile = File(context.cacheDir, "enhanced_capture_${System.currentTimeMillis()}.jpg")
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
        
        imageCapture.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Enhanced image captured successfully")
                    
                    try {
                        val bitmap = android.graphics.BitmapFactory.decodeFile(outputFile.absolutePath)
                        _capturedImage.value = bitmap
                        onImageCaptured(bitmap)
                        
                        // Clean up temp file
                        outputFile.delete()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to load captured image", e)
                        onImageCaptured(null)
                    }
                }
                
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Enhanced image capture failed", exception)
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
        Log.d(TAG, "Flash mode set to: $flashMode")
    }
    
    /**
     * Stop camera
     */
    fun stopCamera() {
        Log.d(TAG, "Stopping enhanced camera...")
        cameraProvider?.unbindAll()
        _isCameraReady.value = false
    }
    
    /**
     * Release resources
     */
    fun release() {
        Log.d(TAG, "Releasing enhanced camera resources...")
        stopCamera()
        cameraExecutor.shutdown()
        focusStabilityMonitor = null
    }
}

/**
 * Focus stability monitor for optimal document capture timing
 */
private class FocusStabilityMonitor {
    private val focusHistory = mutableListOf<Float>()
    private val maxHistorySize = 10
    private val stabilityThreshold = 0.1f
    
    fun updateFocusStability(bitmap: Bitmap): Boolean {
        val focusScore = calculateFocusScore(bitmap)
        focusHistory.add(focusScore)
        
        if (focusHistory.size > maxHistorySize) {
            focusHistory.removeAt(0)
        }
        
        return if (focusHistory.size >= 5) {
            val variance = calculateVariance(focusHistory)
            variance < stabilityThreshold
        } else {
            false
        }
    }
    
    private fun calculateFocusScore(bitmap: Bitmap): Float {
        // Simple Laplacian variance for focus measurement
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        var variance = 0.0
        var count = 0
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val center = pixels[y * width + x] and 0xFF
                val laplacian = abs(
                    4 * center -
                    (pixels[(y - 1) * width + x] and 0xFF) -
                    (pixels[(y + 1) * width + x] and 0xFF) -
                    (pixels[y * width + (x - 1)] and 0xFF) -
                    (pixels[y * width + (x + 1)] and 0xFF)
                )
                variance += laplacian * laplacian
                count++
            }
        }
        
        return (variance / count).toFloat()
    }
    
    private fun calculateVariance(values: List<Float>): Float {
        if (values.isEmpty()) return Float.MAX_VALUE
        
        val mean = values.average().toFloat()
        val variance = values.map { (it - mean) * (it - mean) }.average().toFloat()
        return variance
    }
}
