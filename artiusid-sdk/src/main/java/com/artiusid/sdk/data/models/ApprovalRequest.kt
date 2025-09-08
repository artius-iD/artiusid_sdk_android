package com.artiusid.sdk.data.models

import com.google.gson.annotations.SerializedName

/**
 * Approval request data model - EXACT STANDALONE MATCH
 */
data class ApprovalRequest(
    @SerializedName("clientId")
    val clientId: Int,
    
    @SerializedName("requestType")
    val requestType: String,
    
    @SerializedName("requestData")
    val requestData: Map<String, Any>? = null,
    
    @SerializedName("sessionId")
    val sessionId: String,
    
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)