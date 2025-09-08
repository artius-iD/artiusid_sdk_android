package com.artiusid.sdk.models

import android.graphics.Bitmap
import com.artiusid.sdk.data.models.passport.PassportMRZData

/**
 * Passport data model - EXACT STANDALONE MATCH
 */
data class PassportData(
    val mrzData: PassportMRZData? = null,
    val frontImage: Bitmap? = null,
    val backImage: Bitmap? = null,
    val faceImage: Bitmap? = null,
    val isVisualScanComplete: Boolean = false,
    val isNFCScanComplete: Boolean = false,
    val confidence: Float = 0.0f,
    val processingTime: Long = 0L,
    val sessionId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
