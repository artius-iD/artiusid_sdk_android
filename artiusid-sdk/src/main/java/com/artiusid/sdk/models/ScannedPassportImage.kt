package com.artiusid.sdk.models

import android.graphics.Bitmap

/**
 * Scanned passport image data - EXACT STANDALONE MATCH
 */
data class ScannedPassportImage(
    val bitmap: Bitmap,
    val side: String, // "front" or "back"
    val quality: Float = 0.0f,
    val timestamp: Long = System.currentTimeMillis(),
    val processingTime: Long = 0L
)
