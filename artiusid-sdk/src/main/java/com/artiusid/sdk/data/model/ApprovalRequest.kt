/*
 * File: ApprovalRequest.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.model

import com.google.gson.annotations.SerializedName

/**
 * Matches iOS ApprovalRequest.swift EXACTLY
 * Used for sending approval responses (approve/deny) to the server
 * 
 * CRITICAL: iOS sends ALL fields as strings via toEncodableBody()
 * Android must match this exactly for backend compatibility
 * 
 * NOTE: iOS does NOT set the timeout field when sending approval responses
 * (timeout is only used for approval requests, not responses)
 */
data class ApprovalRequest(
    @SerializedName("clientId")
    val clientId: String = "1",
    
    @SerializedName("clientGroupId")
    val clientGroupId: String = "1",
    
    @SerializedName("deviceId")
    val deviceId: String = "",
    
    @SerializedName("requestId")
    val requestId: String = "0",
    
    @SerializedName("responseValue")
    val responseValue: String = ""
) {
    /**
     * Convert to encodable body format like iOS toEncodableBody()
     * iOS returns [String: String] - all fields are strings
     * 
     * NOTE: timeout field removed - iOS doesn't send it for approval responses
     */
    fun toEncodableBody(): Map<String, String> {
        return mapOf(
            "clientId" to clientId,
            "clientGroupId" to clientGroupId,
            "deviceId" to deviceId,
            "requestId" to requestId,
            "responseValue" to responseValue
        )
    }
}