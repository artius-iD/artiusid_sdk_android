package com.artiusid.sdk.models

import android.graphics.Bitmap
import com.artiusid.sdk.data.models.passport.PassportNFCData

/**
 * Result from NFC passport reading - EXACT STANDALONE MATCH
 */
data class NFCPassportResult(
    val nfcData: PassportNFCData?,
    val success: Boolean,
    val isAuthenticated: Boolean,
    val expiresAt: Long,
    val processingTime: Long,
    val sessionId: String,
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val passportPhoto: Bitmap? = null,
    val fingerprints: List<Bitmap>? = null
)
