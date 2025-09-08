package com.artiusid.sdk.data.models

import com.google.gson.annotations.SerializedName

/**
 * Approval request testing response data model - EXACT STANDALONE MATCH
 */
data class ApprovalRequestTestingResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("testResult")
    val testResult: String? = null,
    
    @SerializedName("testData")
    val testData: Map<String, Any>? = null,
    
    @SerializedName("sessionId")
    val sessionId: String? = null,
    
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)