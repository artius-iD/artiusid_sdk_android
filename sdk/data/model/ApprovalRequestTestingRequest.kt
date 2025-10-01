/*
 * File: ApprovalRequestTestingRequest.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.model

import com.google.gson.annotations.SerializedName

/**
 * Matches iOS ApprovalRequestTestingRequest.swift EXACTLY
 * CRITICAL: iOS includes timeout field with default value 30
 */
data class ApprovalRequestTestingRequest(
    @SerializedName("clientId")
    val clientId: Int,
    
    @SerializedName("clientGroupId")
    val clientGroupId: Int,
    
    @SerializedName("deviceId")
    val deviceId: String,
    
    @SerializedName("approvalTitle")
    val approvalTitle: String,
    
    @SerializedName("approvalDescription")
    val approvalDescription: String,
    
    @SerializedName("timeout")
    val timeout: Int = 30  // ✅ CRITICAL: iOS has this field!
) 