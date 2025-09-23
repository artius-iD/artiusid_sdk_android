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
 */
data class ApprovalRequest(
    @SerializedName("clientId")
    val clientId: Int? = 1,
    
    @SerializedName("clientGroupId")
    val clientGroupId: Int? = 1,
    
    @SerializedName("deviceId")
    val deviceId: String? = "",
    
    @SerializedName("requestId")
    val requestId: Int? = 0,
    
    @SerializedName("responseValue")
    val responseValue: String? = "",
    
    @SerializedName("timeout")
    val timeout: String? = "30"
) {
    /**
     * Convert to encodable body format like iOS toEncodableBody()
     * iOS returns [String: String] but we use [String: Any] for flexibility
     */
    fun toEncodableBody(): Map<String, Any> {
        return mapOf(
            "clientId" to (clientId ?: 1),
            "clientGroupId" to (clientGroupId ?: 1),
            "deviceId" to (deviceId ?: ""),
            "requestId" to (requestId ?: 0),
            "responseValue" to (responseValue ?: ""),
            "timeout" to (timeout ?: "30")
        )
    }
}