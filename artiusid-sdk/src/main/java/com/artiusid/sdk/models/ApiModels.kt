package com.artiusid.sdk.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * API request and response models for the SDK
 */

@Parcelize
data class ApprovalRequest(
    val sessionId: String,
    val userId: String,
    val requestType: String,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class ApprovalResponse(
    val success: Boolean,
    val approvalId: String = "",
    val status: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class ApprovalRequestTestingRequest(
    val testMode: Boolean = true,
    val sessionId: String,
    val mockData: Map<String, String> = emptyMap()
) : Parcelable

@Parcelize
data class ApprovalRequestTestingResponse(
    val success: Boolean,
    val testResults: Map<String, String> = emptyMap(),
    val message: String = ""
) : Parcelable

@Parcelize
data class AuthenticationRequest(
    val sessionId: String,
    val faceImage: String? = null,
    val biometricData: String? = null,
    val deviceInfo: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class AuthenticationResponse(
    val success: Boolean,
    val confidence: Float = 0f,
    val authenticationId: String = "",
    val result: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable

@Parcelize
data class LoadCertificateRequest(
    val certificateType: String,
    val environment: String = "production",
    val clientId: String
) : Parcelable

@Parcelize
data class LoadCertificateResponse(
    val success: Boolean,
    val certificate: String = "",
    val expiryDate: String = "",
    val message: String = ""
) : Parcelable

// Note: VerificationRequest and VerificationResponse are already defined in VerificationModels.kt
