package com.artiusid.sdk.ui.screens.face

import com.artiusid.sdk.models.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artiusid.sdk.services.FaceMeshDetectorService
import com.artiusid.sdk.models.FaceMeshResult
import com.artiusid.sdk.models.ProcessingStage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

class FaceVerificationViewModel(
    private val faceMeshDetectorService: FaceMeshDetectorService
) : ViewModel() {
    
    companion object {
        private const val TAG = "FaceVerificationViewModel"
    }

    // Directly expose the service's state flows - this is the key fix!
    val segmentStatus: StateFlow<List<Boolean>> = faceMeshDetectorService.segmentStatus
    val currentInstruction: StateFlow<String> = faceMeshDetectorService.currentInstruction
    val isProcessingComplete: StateFlow<Boolean> = faceMeshDetectorService.isProcessingComplete
    val isLoading: StateFlow<Boolean> = faceMeshDetectorService.isLoading
    val error: StateFlow<String?> = faceMeshDetectorService.error
    val faceResult: StateFlow<FaceMeshResult?> = faceMeshDetectorService.faceResult
    val calibrationCountdown: StateFlow<Int> = faceMeshDetectorService.calibrationCountdown
    
    // Additional computed properties
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()
    
    init {
        startFaceDetection()
    }
    
    private fun startFaceDetection() {
        viewModelScope.launch {
            try {
                // Start the face detection flow - service manages its own state
                faceMeshDetectorService.startFaceDetection().collect { result ->
                    Log.d(TAG, "Received face detection result: $result")
                    
                    // Update progress based on completed segments
                    val completedSegments = faceMeshDetectorService.segmentStatus.value.count { it }
                    _progress.value = completedSegments / 8f
                    
                    Log.d(TAG, "Updated UI state - Segments: $completedSegments/8, Progress: ${_progress.value}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in face detection: ${e.message}", e)
            }
        }
    }
    
    fun createImageAnalyzer() = faceMeshDetectorService.createImageAnalyzer()
    
    override fun onCleared() {
        super.onCleared()
        faceMeshDetectorService.stopFaceDetection()
    }
}