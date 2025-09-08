package com.artiusid.sdk.data.models

import com.google.gson.annotations.SerializedName

/**
 * Authentication response data model - EXACT STANDALONE MATCH
 */
data class AuthenticationResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("token")
    val token: String? = null,
    
    @SerializedName("userId")
    val userId: String? = null,
    
    @SerializedName("expiresAt")
    val expiresAt: Long? = null,
    
    @SerializedName("sessionId")
    val sessionId: String? = null,
    
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)