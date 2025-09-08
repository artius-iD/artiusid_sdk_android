package com.artiusid.sdk.data.models.passport

import android.graphics.Bitmap

/**
 * Passport NFC chip data - EXACT STANDALONE MATCH
 */
data class PassportNFCData(
    val personalNumber: String? = null,
    val surname: String? = null,
    val givenNames: String? = null,
    val dateOfBirth: String? = null,
    val placeOfBirth: String? = null,
    val nationality: String? = null,
    val sex: String? = null,
    val passportNumber: String? = null,
    val issuingCountry: String? = null,
    val issuingAuthority: String? = null,
    val dateOfIssue: String? = null,
    val dateOfExpiry: String? = null,
    val photo: Bitmap? = null,
    val signature: Bitmap? = null,
    val fingerprints: List<Bitmap>? = null,
    val authenticationStatus: PassportAuthenticationStatus = PassportAuthenticationStatus.NOT_AUTHENTICATED,
    val rawData: Map<String, Any>? = null,
    val isValid: Boolean = false,
    val isAuthenticated: Boolean = false,
    val hasEssentialData: Boolean = false,
    val faceImage: Bitmap? = null,
    val processingTimeMs: Long = 0L
) {
    fun generateMRZKey(): String {
        return "${passportNumber}_${dateOfBirth}_${dateOfExpiry}"
    }
}