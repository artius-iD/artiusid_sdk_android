package com.artiusid.sdk.data.models

import com.google.gson.annotations.SerializedName

/**
 * Approval response data model - EXACT STANDALONE MATCH
 */
data class ApprovalResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("approvalStatus")
    val approvalStatus: String? = null,
    
    @SerializedName("approvalData")
    val approvalData: Map<String, Any>? = null,
    
    @SerializedName("sessionId")
    val sessionId: String? = null,
    
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)