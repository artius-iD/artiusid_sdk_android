package com.artiusid.sdk.data.models

import com.google.gson.annotations.SerializedName

/**
 * Load certificate request data model - EXACT STANDALONE MATCH
 */
data class LoadCertificateRequest(
    @SerializedName("clientId")
    val clientId: Int,
    
    @SerializedName("certificateType")
    val certificateType: String,
    
    @SerializedName("deviceInfo")
    val deviceInfo: Map<String, String>? = null,
    
    @SerializedName("sessionId")
    val sessionId: String,
    
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)