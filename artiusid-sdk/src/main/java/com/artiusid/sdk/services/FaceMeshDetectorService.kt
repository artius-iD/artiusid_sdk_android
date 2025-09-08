package com.artiusid.sdk.services

import androidx.camera.core.ImageAnalysis
import com.artiusid.sdk.models.FaceMeshResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Face mesh detection service interface - EXACT STANDALONE MATCH
 */
interface FaceMeshDetectorService {
    
    // StateFlow properties
    val faceResult: StateFlow<FaceMeshResult?>
    val segmentStatus: StateFlow<List<Boolean>>
    val currentInstruction: StateFlow<String>
    val isProcessingComplete: StateFlow<Boolean>
    val isLoading: StateFlow<Boolean>
    val error: StateFlow<String?>
    val calibrationCountdown: StateFlow<Int>
    
    // Methods
    fun startFaceDetection(): Flow<FaceMeshResult>
    fun stopFaceDetection()
    fun createImageAnalyzer(): ImageAnalysis
}