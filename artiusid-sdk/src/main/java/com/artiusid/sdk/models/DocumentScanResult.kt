package com.artiusid.sdk.models

import android.graphics.Bitmap

/**
 * Result from document scanning - EXACT STANDALONE MATCH
 */
data class DocumentScanResult(
    val success: Boolean,
    val frontImage: Bitmap?,
    val backImage: Bitmap?,
    val documentType: String,
    val extractedData: Map<String, String> = emptyMap(),
    val confidence: Float,
    val processingTime: Long,
    val sessionId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val errorMessage: String? = null,
    val barcodeData: String? = null
)
