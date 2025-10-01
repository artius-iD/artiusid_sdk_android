/*
 * File: FaceVerificationViewModelFactory.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.face

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.artiusid.sdk.services.FaceMeshDetectorService

class FaceVerificationViewModelFactory(
    private val faceMeshDetectorService: FaceMeshDetectorService
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FaceVerificationViewModel::class.java)) {
            return FaceVerificationViewModel(faceMeshDetectorService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 