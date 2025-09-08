package com.artiusid.sdk.data.models

import com.google.gson.annotations.SerializedName

/**
 * Approval request testing data model - EXACT STANDALONE MATCH
 */
data class ApprovalRequestTestingRequest(
    @SerializedName("clientId")
    val clientId: Int,
    
    @SerializedName("requestType")
    val requestType: String,
    
    @SerializedName("testData")
    val testData: Map<String, Any>? = null,
    
    @SerializedName("sessionId")
    val sessionId: String,
    
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)