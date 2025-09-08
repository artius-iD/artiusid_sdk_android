package com.artiusid.sdk.models

import android.graphics.Bitmap

/**
 * Result from face liveness detection - EXACT STANDALONE MATCH
 */
data class LivenessResult(
    val success: Boolean,
    val isLive: Boolean,
    val confidence: Float,
    val faceBitmap: Bitmap?,
    val livenessScore: Float,
    val processingTime: Long,
    val sessionId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)
