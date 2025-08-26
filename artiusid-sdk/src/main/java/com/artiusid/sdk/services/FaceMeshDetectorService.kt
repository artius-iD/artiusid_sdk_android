package com.artiusid.sdk.services

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import com.artiusid.sdk.config.LivenessConfig
import com.artiusid.sdk.models.HeadMovement
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for face mesh detection service
 */
interface FaceMeshDetectorService {
    
    /**
     * Current segment status (completed/incomplete)
     */
    val segmentStatus: StateFlow<List<Boolean>>
    
    /**
     * Current instruction for the user
     */
    val currentInstruction: StateFlow<String>
    
    /**
     * Whether processing is complete
     */
    val isProcessingComplete: StateFlow<Boolean>
    
    /**
     * Current error state
     */
    val error: StateFlow<String?>
    
    /**
     * Current face detection result
     */
    val faceResult: StateFlow<FaceMeshResult?>
    
    /**
     * Calibration countdown
     */
    val calibrationCountdown: StateFlow<Int>
    
    /**
     * Initialize the service with configuration
     */
    fun initialize(config: LivenessConfig?)
    
    /**
     * Create image analyzer for camera
     */
    fun createImageAnalyzer(): ImageAnalysis
    
    /**
     * Start face detection
     */
    fun startFaceDetection()
    
    /**
     * Stop face detection
     */
    fun stopFaceDetection()
    
    /**
     * Reset detection state
     */
    fun reset()
}

/**
 * Face mesh detection result
 */
data class FaceMeshResult(
    val hasFace: Boolean,
    val faceId: Int? = null,
    val headEulerAngleX: Float = 0f, // Pitch
    val headEulerAngleY: Float = 0f, // Yaw
    val headEulerAngleZ: Float = 0f, // Roll
    val leftEyeOpenProbability: Float? = null,
    val rightEyeOpenProbability: Float? = null,
    val smilingProbability: Float? = null,
    val faceBounds: android.graphics.RectF? = null,
    val distanceToFace: Float = 0f,
    val qualityScore: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)
