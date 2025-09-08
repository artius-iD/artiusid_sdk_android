package com.artiusid.sdk.data.models

import com.google.gson.annotations.SerializedName

/**
 * Load certificate response data model - EXACT STANDALONE MATCH
 */
data class LoadCertificateResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("certificate")
    val certificate: String? = null,
    
    @SerializedName("certificateType")
    val certificateType: String? = null,
    
    @SerializedName("expirationDate")
    val expirationDate: Long? = null,
    
    @SerializedName("sessionId")
    val sessionId: String? = null,
    
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)