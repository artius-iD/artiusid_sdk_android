/*
 * File: SDKModels.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * SDK Error codes for standardized error handling
 */
enum class SDKErrorCode {
    INVALID_CONFIG,
    NETWORK_ERROR,
    PERMISSION_DENIED,
    CAMERA_ERROR,
    VERIFICATION_FAILED,
    AUTHENTICATION_FAILED,
    UNKNOWN_ERROR
}

/**
 * SDK Error with detailed information
 */
@Parcelize
data class SDKError(
    val code: SDKErrorCode,
    val message: String,
    val cause: @RawValue Throwable? = null
) : Parcelable

/**
 * Verification result from the standalone application
 */
@Parcelize
data class VerificationResult(
    val success: Boolean,
    val verificationId: String,
    val confidence: Float,
    val documentType: String? = null,
    val extractedData: Map<String, String> = emptyMap(),
    val processingTime: Long,
    val sessionId: String,
    val rawResponse: String? = null // JSON payload for detailed results parsing
) : Parcelable

/**
 * Authentication result from the standalone application
 */
@Parcelize
data class AuthenticationResult(
    val success: Boolean,
    val authenticationId: String,
    val confidence: Float,
    val processingTime: Long,
    val sessionId: String
) : Parcelable

/**
 * Theme configuration for the standalone application
 * Using simple color values that can be passed to standalone app
 */
@Parcelize
data class SDKThemeConfiguration(
    // Brand Identity
    val brandName: String = "artius.iD",
    val brandLogoUrl: String? = null,
    
    // Primary Colors (as hex strings for easy passing)
    val primaryColorHex: String = "#263238", // Bluegray900
    val secondaryColorHex: String = "#F57C00", // Yellow900
    val backgroundColorHex: String = "#263238",
    val surfaceColorHex: String = "#37474F",
    
    // Text Colors
    val onPrimaryColorHex: String = "#FFFFFF",
    val onSecondaryColorHex: String = "#263238",
    val onBackgroundColorHex: String = "#FFFFFF",
    val onSurfaceColorHex: String = "#FFFFFF",
    
    // Status Colors
    val successColorHex: String = "#4CAF50",
    val errorColorHex: String = "#D32F2F",
    val warningColorHex: String = "#FF9800",
    
    // Verification Specific Colors
    val faceDetectionOverlayColorHex: String = "#4CAF50",
    val documentScanOverlayColorHex: String = "#F57C00",
    val pendingStepColorHex: String = "#9E9E9E",
    val completedStepColorHex: String = "#4CAF50",
    
    // Dark Mode Support
    val isDarkMode: Boolean = false
) : Parcelable