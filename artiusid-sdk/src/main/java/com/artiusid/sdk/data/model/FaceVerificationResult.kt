package com.artiusid.sdk.data.model

data class FaceVerificationResult(
    val success: Boolean,
    val confidence: Float = 0f,
    val message: String? = null,
    val error: String? = null
) 