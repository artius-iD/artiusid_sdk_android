/*
 * File: ApprovalRequestTestingResponse.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.model

import com.google.gson.annotations.SerializedName

/**
 * Matches iOS ApprovalRequestTestingResponse.swift EXACTLY
 * CRITICAL: iOS expects DIRECT fields from API Gateway unwrapped response
 */
data class ApprovalRequestTestingResponse(
    @SerializedName("requestId")
    val requestId: Int,
    
    @SerializedName("success")
    val success: Boolean
) {
    // Helper property to maintain compatibility with existing code
    val approvalData: ApprovalTestingData?
        get() = if (success) ApprovalTestingData(200, "Success", requestId) else null
}

data class ApprovalTestingData(
    val statusCode: Int,
    val message: String,
    val requestId: Int
) 