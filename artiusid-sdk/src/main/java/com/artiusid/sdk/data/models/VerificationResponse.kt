package com.artiusid.sdk.data.models

import com.google.gson.annotations.SerializedName

/**
 * Verification response data model - EXACT STANDALONE MATCH
 */
data class VerificationResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("verificationData")
    val verificationData: VerificationData? = null,
    
    @SerializedName("sessionId")
    val sessionId: String? = null,
    
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Verification data contained in response - EXACT STANDALONE MATCH
 */
data class VerificationData(
    @SerializedName("personScore")
    val personScore: Double = 0.0,
    
    @SerializedName("personResult")
    val personResult: String? = null,
    
    @SerializedName("personRating")
    val personRating: String? = null,
    
    @SerializedName("documentStatus")
    val documentStatus: String? = null,
    
    @SerializedName("documentScore")
    val documentScore: Int = 0,
    
    @SerializedName("faceMatchScore")
    val faceMatchScore: Int = 0,
    
    @SerializedName("antiSpoofingFaceScore")
    val antiSpoofingFaceScore: Int = 0,
    
    @SerializedName("riskInformationScore")
    val riskInformationScore: Int = 0,
    
    @SerializedName("riskInformationResult")
    val riskInformationResult: String? = null,
    
    @SerializedName("riskInformationRating")
    val riskInformationRating: String? = null,
    
    @SerializedName("accountNumber")
    val accountNumber: String? = null,
    
    @SerializedName("firstName")
    val firstName: String? = null,
    
    @SerializedName("lastName")
    val lastName: String? = null
)