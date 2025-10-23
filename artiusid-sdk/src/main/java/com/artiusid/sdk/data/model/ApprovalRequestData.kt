/*
 * File: ApprovalRequestData.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.model

import com.google.gson.annotations.SerializedName

/**
 * Matches iOS ApprovalRequestData.swift EXACTLY
 * Response data from the ApprovalRequestTestingFunction API endpoint
 * 
 * iOS Implementation:
 * struct ApprovalRequestData: Codable {
 *     let requestId: Int
 *     let success: Bool
 * }
 */
data class ApprovalRequestData(
    @SerializedName("requestId")
    val requestId: Int,
    
    @SerializedName("success")
    val success: Boolean
)
