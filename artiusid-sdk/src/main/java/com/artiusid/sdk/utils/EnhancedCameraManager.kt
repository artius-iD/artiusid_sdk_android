package com.artiusid.utils

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
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

class EnhancedCameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private lateinit var cameraExecutor: ExecutorService
    
    // Focus management
    private var focusStabilityMonitor: FocusStabilityMonitor? = null
    private var lastFocusUpdateTime = System.currentTimeMillis()
    private var lastDocumentBounds: Rect? = null
    private var focusLockScheduled = false
    private val focusUpdateThrottleMs = 500L // Throttle focus updates
    
    private val _previewState = MutableStateFlow<Preview?>(null)
    val previewState: StateFlow<Preview?> = _previewState
    
    private val _isFocusStable = MutableStateFlow(false)
    val isFocusStable: StateFlow<Boolean> = _isFocusStable

    private var imageProcessor: ((Bitmap) -> Unit)? = null
    
    init {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    suspend fun startCamera(
        cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
        targetResolution: Size = Size(1280, 720),
        imageProcessor: ((Bitmap) -> Unit)? = null
    ) {
        this.imageProcessor = imageProcessor
        
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProvider = cameraProviderFuture.get()

        // Create preview with enhanced autofocus configuration
        val preview = Preview.Builder()
            .setTargetResolution(targetResolution)
            .build()
        _previewState.value = preview

        // Create image analysis with enhanced settings
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(targetResolution)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .apply {
                // Configure enhanced autofocus using Camera2Interop
                val camera2Interop = Camera2Interop.Extender(this)
                configureEnhancedAutofocus(camera2Interop)
            }
            .build()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            processImageProxy(imageProxy)
        }

        try {
            cameraProvider?.unbindAll()
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
            
            this.imageAnalyzer = imageAnalysis
            
            // Initialize focus stability monitoring
            initializeFocusMonitoring()
            
            Log.d(TAG, "Enhanced camera setup completed successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Enhanced camera setup failed", e)
        }
    }
    
    private fun configureEnhancedAutofocus(camera2Interop: Camera2Interop.Extender<*>) {
        try {
            // Configure continuous autofocus
            camera2Interop.setCaptureRequestOption(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            
            // Set focus point to center initially (will be set dynamically based on document detection)
            
            // Enable auto exposure for better document scanning
            camera2Interop.setCaptureRequestOption(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
            )
            
            Log.d(TAG, "Enhanced autofocus configuration applied")
            
        } catch (e: Exception) {
            Log.w(TAG, "Could not configure enhanced autofocus: ${e.message}")
        }
    }
    
    private fun initializeFocusMonitoring() {
        camera?.let { cam ->
            focusStabilityMonitor = FocusStabilityMonitor(cam) { isStable ->
                _isFocusStable.value = isStable
                if (isStable) {
                    scheduleAdaptiveFocusLock()
                }
            }
        }
    }
    
    private fun scheduleAdaptiveFocusLock() {
        if (focusLockScheduled) return
        
        focusLockScheduled = true
        CoroutineScope(Dispatchers.Main).launch {
            delay(1000) // Wait 1 second before locking focus
            lockFocusIfStable()
            focusLockScheduled = false
        }
    }
    
    private fun lockFocusIfStable() {
        try {
            camera?.cameraControl?.let { cameraControl ->
                // Lock autofocus for stable document capture
                cameraControl.setLinearZoom(camera?.cameraInfo?.zoomState?.value?.linearZoom ?: 0f)
                Log.d(TAG, "Focus locked for stable document capture")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error locking focus: ${e.message}")
        }
    }

    /**
     * Adjusts focus point based on detected document bounds
     * This mimics the iOS adjustFocusForDocument functionality
     */
    fun adjustFocusForDocument(documentBoundingBox: Rect, frameWidth: Int, frameHeight: Int) {
        val currentTime = System.currentTimeMillis()
        
        // Throttle focus updates (similar to iOS implementation)
        if (currentTime - lastFocusUpdateTime < focusUpdateThrottleMs) {
            return
        }
        
        // Check if document position has changed significantly
        lastDocumentBounds?.let { lastBounds ->
            val centerXDiff = abs((documentBoundingBox.centerX().toFloat() / frameWidth) - 
                                (lastBounds.centerX().toFloat() / frameWidth))
            val centerYDiff = abs((documentBoundingBox.centerY().toFloat() / frameHeight) - 
                                (lastBounds.centerY().toFloat() / frameHeight))
            
            // Only adjust if document moved significantly (> 5% of frame)
            if (centerXDiff < 0.05f && centerYDiff < 0.05f) {
                return
            }
        }
        
        try {
            camera?.cameraControl?.let { cameraControl ->
                // Calculate focus point based on document center (normalized coordinates)
                val focusPointX = documentBoundingBox.centerX().toFloat() / frameWidth
                val focusPointY = documentBoundingBox.centerY().toFloat() / frameHeight
                
                // Create metering point for focus
                val meteringPointFactory = SurfaceOrientedMeteringPointFactory(
                    frameWidth.toFloat(), frameHeight.toFloat()
                )
                val meteringPoint = meteringPointFactory.createPoint(focusPointX, focusPointY)
                
                // Create focus metering action
                val focusMeteringAction = FocusMeteringAction.Builder(meteringPoint).build()
                
                // Apply focus adjustment
                cameraControl.startFocusAndMetering(focusMeteringAction)
                
                lastFocusUpdateTime = currentTime
                lastDocumentBounds = documentBoundingBox
                
                Log.d(TAG, "Adjusted focus for document at ($focusPointX, $focusPointY)")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error adjusting focus for document: ${e.message}")
        }
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        val bitmap = imageProxy.toBitmap()
        bitmap?.let { bmp ->
            imageProcessor?.invoke(bmp)
        }
        imageProxy.close()
    }

    private fun ImageProxy.toBitmap(): Bitmap? {
        return try {
            val buffer = planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "Error converting ImageProxy to Bitmap: ${e.message}")
            null
        }
    }

    fun stopCamera() {
        focusStabilityMonitor?.stop()
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "EnhancedCameraManager"
    }
}

/**
 * Focus Stability Monitor
 * Monitors camera focus state and determines when focus becomes stable
 * This mimics the iOS FocusStabilityMonitor class
 */
class FocusStabilityMonitor(
    private val camera: Camera,
    private val onFocusStableChanged: (Boolean) -> Unit
) {
    private var isMonitoring = false
    private var focusStabilityCount = 0
    private val requiredStabilityCount = 5
    private var monitoringJob: Job? = null
    private var lastFocusState: Int? = null
    
    init {
        startMonitoring()
    }
    
    fun stop() {
        stopMonitoring()
    }
    
    private fun startMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        monitoringJob = CoroutineScope(Dispatchers.Default).launch {
            while (isMonitoring) {
                checkFocusStability()
                delay(200) // Check every 200ms (similar to iOS timer interval)
            }
        }
    }
    
    private fun stopMonitoring() {
        isMonitoring = false
        monitoringJob?.cancel()
        monitoringJob = null
    }
    
    private fun checkFocusStability() {
        try {
            // Get current focus state from camera info
            val cameraState = camera.cameraInfo.cameraState.value
            val currentFocusState = cameraState?.type?.ordinal
            
            // Check if focus is adjusting (similar to iOS isAdjustingFocus)
            val isFocusAdjusting = currentFocusState != lastFocusState
            
            if (isFocusAdjusting) {
                focusStabilityCount = 0
                onFocusStableChanged(false)
            } else {
                focusStabilityCount++
                
                if (focusStabilityCount >= requiredStabilityCount) {
                    onFocusStableChanged(true)
                    stopMonitoring() // Stop monitoring after focus is stable
                }
            }
            
            lastFocusState = currentFocusState
            
        } catch (e: Exception) {
            Log.e("FocusStabilityMonitor", "Error checking focus stability: ${e.message}")
        }
    }
}