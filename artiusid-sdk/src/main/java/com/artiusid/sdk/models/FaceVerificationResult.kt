package com.artiusid.sdk.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * Result of face verification process
 */
@Parcelize
data class FaceVerificationResult(
    val success: Boolean,
    val confidence: Float = 0f,
    val matchScore: Float = 0f,
    val livenessScore: Float = 0f,
    val isLive: Boolean = false,
    val faceDetected: Boolean = false,
    val message: String? = null,
    val error: @RawValue SDKError? = null,
    val processingTime: Long = 0L,
    val sessionId: String? = null
) : Parcelable
