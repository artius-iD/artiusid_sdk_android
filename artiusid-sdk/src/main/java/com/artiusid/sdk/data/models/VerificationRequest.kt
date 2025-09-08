package com.artiusid.sdk.data.models

import com.google.gson.annotations.SerializedName

/**
 * Verification request data model - EXACT STANDALONE MATCH
 */
data class VerificationRequest(
    @SerializedName("clientId")
    val clientId: Int,
    
    @SerializedName("clientGroupId")
    val clientGroupId: Int,
    
    @SerializedName("faceImage")
    val faceImage: String? = null,
    
    @SerializedName("documentFrontImage")
    val documentFrontImage: String? = null,
    
    @SerializedName("documentBackImage")
    val documentBackImage: String? = null,
    
    @SerializedName("documentType")
    val documentType: String? = null,
    
    @SerializedName("mrzData")
    val mrzData: Map<String, String>? = null,
    
    @SerializedName("nfcData")
    val nfcData: Map<String, String>? = null,
    
    @SerializedName("deviceInfo")
    val deviceInfo: Map<String, String>? = null,
    
    @SerializedName("sessionId")
    val sessionId: String,
    
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)