package com.artiusid.sdk.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * Result of barcode scanning operation
 */
@Parcelize
data class BarcodeScanResult(
    val success: Boolean,
    val rawValue: String? = null,
    val format: String? = null,
    val aamvaData: AAMVAData? = null,
    val confidence: Float = 0f,
    val boundingBox: android.graphics.Rect? = null,
    val processingTime: Long = 0L,
    val error: @RawValue SDKError? = null
) : Parcelable
