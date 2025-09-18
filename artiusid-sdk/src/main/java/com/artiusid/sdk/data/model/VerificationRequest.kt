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
    val fcmToken: String
) {
    /**
     * Convert to LinkedHashMap to preserve field order during JSON serialization
     * Matches exact iOS APIManager.swift body structure from lines 56-64
     */
    fun toOrderedMap(): LinkedHashMap<String, Any> {
        return linkedMapOf(
            "frontImageBase64" to frontImageBase64,
            "backImageBase64" to backImageBase64,
            "faceImageBase64" to faceImageBase64,
            "documentType" to documentType.toString(), // Convert to String like iOS toEncodableBody
            "deviceId" to deviceId,
            "deviceModel" to deviceModel,
            "fcmToken" to fcmToken
        )
    }
} 