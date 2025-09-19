/*
 * File: ApprovalRequestTestingResponse.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.model

import com.google.gson.annotations.SerializedName

/**
 * Matches iOS ApprovalRequestTestingResponse.swift exactly
 * iOS expects direct fields: requestId, success (not nested under approval_data)
 */
data class ApprovalRequestTestingResponse(
    @SerializedName("requestId")
    val requestId: Int,
    
    @SerializedName("success")
    val success: Boolean
) {
    // Provide approvalData for backward compatibility
    val approvalData: ApprovalTestingData?
        get() = ApprovalTestingData(
            statusCode = if (success) 200 else 400,
            message = if (success) "Success" else "Failed",
            requestId = requestId
        )
}

data class ApprovalTestingData(
    val statusCode: Int,
    val message: String,
    val requestId: Int
) 