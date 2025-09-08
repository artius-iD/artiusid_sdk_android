package com.artiusid.sdk.models

import android.graphics.Bitmap

/**
 * Face mesh detection result - EXACT STANDALONE MATCH
 */
data class FaceMeshResult(
    val faceBitmap: Bitmap?,
    val isLive: Boolean,
    val confidence: Float,
    val livenessScore: Float,
    val meshPoints: List<android.graphics.PointF>? = null,
    val boundingBox: android.graphics.RectF? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val hasFace: Boolean = false,
    val error: String? = null,
    val processingStage: ProcessingStage = ProcessingStage.INITIALIZING,
    val alignmentDirection: String? = null,
    val hintText: String? = null
)
