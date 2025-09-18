/*
 * File: FaceScanViewModel.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.face

import android.graphics.Bitmap
import androidx.camera.core.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artiusid.sdk.utils.FaceDetectionManager
import com.artiusid.sdk.utils.ImageStorage
import com.google.mlkit.vision.face.Face
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FaceScanViewModel @Inject constructor(
    private val faceDetectionManager: FaceDetectionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<FaceScanUiState>(FaceScanUiState.Initial)
    val uiState: StateFlow<FaceScanUiState> = _uiState.asStateFlow()

    private val _previewState = MutableStateFlow<Preview?>(null)
    val previewState: StateFlow<Preview?> = _previewState.asStateFlow()

    fun processImage(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                val faces = faceDetectionManager.detectFaces(bitmap)
                if (faces.isEmpty()) {
                    _uiState.value = FaceScanUiState.NoFaceDetected
                    return@launch
                }

                val face = faces.first()
                if (!faceDetectionManager.hasRequiredLandmarks(face)) {
                    _uiState.value = FaceScanUiState.FaceNotAligned
                    return@launch
                }

                val qualityScore = faceDetectionManager.getFaceQualityScore(face)
                if (qualityScore < 0.7f) {
                    _uiState.value = FaceScanUiState.LowQuality(qualityScore)
                    return@launch
                }

                // Store the face image for verification
                ImageStorage.setFaceImage(bitmap)
                _uiState.value = FaceScanUiState.Success(face, qualityScore)
            } catch (e: Exception) {
                _uiState.value = FaceScanUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class FaceScanUiState {
    object Initial : FaceScanUiState()
    object NoFaceDetected : FaceScanUiState()
    object FaceNotAligned : FaceScanUiState()
    data class LowQuality(val score: Float) : FaceScanUiState()
    data class Success(val face: Face, val qualityScore: Float) : FaceScanUiState()
    data class Error(val message: String) : FaceScanUiState()
} 