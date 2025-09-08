package com.artiusid.sdk.models

import android.graphics.Bitmap

/**
 * Face verification result - EXACT STANDALONE MATCH
 */
data class FaceVerificationResult(
    val isVerified: Boolean,
    val confidence: Float,
    val faceBitmap: Bitmap?,
    val timestamp: Long = System.currentTimeMillis(),
    val errorMessage: String? = null,
    val success: Boolean = isVerified
)