/*
 * File: FaceViewModel.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.face

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artiusid.sdk.services.FaceMeshDetectorService
import com.artiusid.sdk.services.FaceMeshResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

class FaceViewModel(
    private val faceMeshDetectorService: FaceMeshDetectorService
) : ViewModel() {
    
    companion object {
        private const val TAG = "FaceViewModel"
    }

    private val _uiState = MutableStateFlow(FaceUiState())
    val uiState: StateFlow<FaceUiState> = _uiState.asStateFlow()

    private val _faceResult = MutableStateFlow<FaceMeshResult?>(null)
    val faceResult: StateFlow<FaceMeshResult?> = _faceResult.asStateFlow()

    init {
        Log.d(TAG, "FaceViewModel initialized")
        observeFaceDetection()
    }

    private fun observeFaceDetection() {
        viewModelScope.launch {
            faceMeshDetectorService.faceResult.collect { result ->
                Log.d(TAG, "Received face result: $result")
                _faceResult.value = result
                
                result?.let { faceResult ->
                    updateUiState(faceResult)
                }
            }
        }
    }

    private fun updateUiState(result: FaceMeshResult) {
        val newState = _uiState.value.copy(
            hasFace = result.hasFace,
            confidence = result.confidence,
            error = result.error,
            isProcessing = false
        )
        _uiState.value = newState
        Log.d(TAG, "Updated UI state: $newState")
    }

    fun startFaceDetection() {
        Log.d(TAG, "Starting face detection")
        _uiState.value = _uiState.value.copy(isProcessing = true)
        viewModelScope.launch {
            faceMeshDetectorService.startFaceDetection().collect { result ->
                _faceResult.value = result
                updateUiState(result)
            }
        }
    }

    fun stopFaceDetection() {
        Log.d(TAG, "Stopping face detection")
        faceMeshDetectorService.stopFaceDetection()
        _uiState.value = _uiState.value.copy(isProcessing = false)
    }

    fun updatePreviewDimensions(width: Float, height: Float) {
        // Removed: faceMeshDetectorService.updatePreviewDimensions(width, height)
    }

    override fun onCleared() {
        super.onCleared()
        // Removed: faceMeshDetectorService.release()
    }
}

data class FaceUiState(
    val hasFace: Boolean = false,
    val confidence: Float = 0.0f,
    val error: String? = null,
    val isProcessing: Boolean = false
) 