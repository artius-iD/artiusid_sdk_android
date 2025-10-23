/*
 * File: ApprovalRequestTestingResponse.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.model

import com.google.gson.annotations.SerializedName

/**
 * Matches iOS ApprovalRequestTestingResponse structure
 * This wraps the ApprovalRequestData in the same way iOS does
 * 
 * iOS Implementation:
 * The response contains an "approvalData" field that contains ApprovalRequestData
 */
data class ApprovalRequestTestingResponse(
    @SerializedName("approvalData")
    val approvalData: ApprovalRequestData?
)