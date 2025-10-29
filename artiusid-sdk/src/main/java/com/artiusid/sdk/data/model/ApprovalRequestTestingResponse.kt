/*
 * File: ApprovalRequestTestingResponse.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.model

import com.google.gson.annotations.SerializedName

/**
 * 🚨 CRITICAL FIX: Matches iOS ApprovalRequestTestingResponse structure EXACTLY
 * iOS uses DIRECT decode of requestId and success fields, NOT nested in approvalData
 * 
 * iOS Implementation (ApprovalRequestTestingResponse.swift lines 50-53):
 * let requestId = try container.decode(Int.self, forKey: .requestId)
 * let success = try container.decode(Bool.self, forKey: .success)
 * 
 * Backend returns: {requestId: Int, success: Boolean}
 * NOT: {approvalData: {requestId: Int, success: Boolean}}
 */
data class ApprovalRequestTestingResponse(
    @SerializedName("requestId")
    val requestId: Int,
    
    @SerializedName("success")
    val success: Boolean
)