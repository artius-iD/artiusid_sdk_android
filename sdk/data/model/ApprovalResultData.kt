/*
 * File: ApprovalResultData.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.model

import com.google.gson.annotations.SerializedName

/**
 * Matches iOS ApprovalResultData.swift EXACTLY
 * Response data from the ApprovalFunction API endpoint
 */
data class ApprovalResultData(
    @SerializedName("statusCode")
    val statusCode: Int,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("requestId")
    val requestId: Int,
    
    @SerializedName("clientId")
    val clientId: Int,
    
    @SerializedName("clientGroupId")
    val clientGroupId: Int,
    
    @SerializedName("responseValue")
    val responseValue: String
)
