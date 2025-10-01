/*
 * File: FaceVerificationViewModel.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.face

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artiusid.sdk.services.FaceMeshDetectorService
import com.artiusid.sdk.services.FaceMeshResult
import com.artiusid.sdk.services.ProcessingStage
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

    // iOS-like state management
    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()
    
    private val _segmentStatus = MutableStateFlow(List(8) { false })
    val segmentStatus: StateFlow<List<Boolean>> = _segmentStatus.asStateFlow()
    
    private val _currentInstruction = MutableStateFlow("Position your face in the circle")
    val currentInstruction: StateFlow<String> = _currentInstruction.asStateFlow()
    
    private val _isProcessingComplete = MutableStateFlow(false)
    val isProcessingComplete: StateFlow<Boolean> = _isProcessingComplete.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _faceResult = MutableStateFlow<FaceMeshResult?>(null)
    val faceResult: StateFlow<FaceMeshResult?> = _faceResult.asStateFlow()
    
    init {
        startFaceDetection()
    }
    
    private fun startFaceDetection() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Start the face detection flow
                faceMeshDetectorService.startFaceDetection().collect { result ->
                    Log.d(TAG, "Received face detection result: $result")
                    
                    // Update face result
                    _faceResult.value = result
                    
                    // Update segment status from the service
                    _segmentStatus.value = faceMeshDetectorService.segmentStatus.value
                    
                    // Update current instruction from the service
                    _currentInstruction.value = faceMeshDetectorService.currentInstruction.value
                    
                    // Update processing complete status
                    _isProcessingComplete.value = faceMeshDetectorService.isProcessingComplete.value
                    
                    // Update error if any
                    _error.value = faceMeshDetectorService.error.value
                    
                    // Update progress based on completed segments
                    val completedSegments = faceMeshDetectorService.segmentStatus.value.count { it }
                    _progress.value = completedSegments / 8f
                    
                    Log.d(TAG, "Updated UI state - Segments: $completedSegments/8, Progress: ${_progress.value}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in face detection: ${e.message}", e)
                _error.value = "Face detection error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun createImageAnalyzer() = faceMeshDetectorService.createImageAnalyzer()
    
    override fun onCleared() {
        super.onCleared()
        faceMeshDetectorService.stopFaceDetection()
    }
}