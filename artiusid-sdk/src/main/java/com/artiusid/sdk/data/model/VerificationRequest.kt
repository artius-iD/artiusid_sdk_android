/*
 * File: VerificationRequest.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.model

import com.google.gson.annotations.SerializedName

data class VerificationRequest(
    @SerializedName("frontImageBase64")
    val frontImageBase64: String, // Non-nullable to match iOS
    @SerializedName("backImageBase64")
    val backImageBase64: String, // Non-nullable to match iOS 
    @SerializedName("faceImageBase64")
    val faceImageBase64: String, // Non-nullable to match iOS
    @SerializedName("documentType")
    val documentType: Int, // Int to match iOS APIManager
    @SerializedName("deviceId")
    val deviceId: String,
    @SerializedName("deviceModel")
    val deviceModel: String,
    @SerializedName("fcmToken")
    val fcmToken: String,
    @SerializedName("oktaId")
    val oktaId: String? = null, // Optional Okta ID (matches iOS v2.0.12)
    @SerializedName("accountNumber")
    val accountNumber: String? = null // Re-verification: member ID from previous verification (iOS v2.0.17)
) {
    /**
     * Convert to LinkedHashMap to preserve field order during JSON serialization
     * Matches exact iOS APIManager.swift body structure from lines 56-64
     * For passport documents (documentType=2), backImageBase64 will be empty string
     */
    fun toOrderedMap(): LinkedHashMap<String, Any> {
        val map = linkedMapOf<String, Any>(
            "frontImageBase64" to frontImageBase64,
            "backImageBase64" to backImageBase64, // Always include, even if empty for passports
            "faceImageBase64" to faceImageBase64,
            "documentType" to documentType.toString(), // Convert to String like iOS toEncodableBody
            "deviceId" to deviceId,
            "deviceModel" to deviceModel,
            "fcmToken" to fcmToken
        )
        
        // Add oktaId if present (matches iOS conditional inclusion)
        oktaId?.let {
            if (it.isNotBlank()) {
                map["oktaId"] = it
            }
        }
        // Always include accountNumber (iOS parity: toEncodableBody uses "" when empty)
        map["accountNumber"] = accountNumber?.takeIf { it.isNotBlank() } ?: ""
        return map
    }
} 