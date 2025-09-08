package com.artiusid.sdk.data.models

import com.google.gson.annotations.SerializedName

/**
 * Authentication request data model - EXACT STANDALONE MATCH
 */
data class AuthenticationRequest(
    @SerializedName("username")
    val username: String,
    
    @SerializedName("password")
    val password: String? = null,
    
    @SerializedName("token")
    val token: String? = null,
    
    @SerializedName("biometricData")
    val biometricData: String? = null,
    
    @SerializedName("deviceInfo")
    val deviceInfo: Map<String, String>? = null,
    
    @SerializedName("sessionId")
    val sessionId: String,
    
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)