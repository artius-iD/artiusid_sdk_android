/*
 * File: ApprovalResponse.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.content.Context
import android.util.Log
import com.artiusid.sdk.config.ClientConfiguration
import com.artiusid.sdk.data.api.ApiService
import com.artiusid.sdk.data.model.AppNotificationState
import com.artiusid.sdk.data.model.ApprovalRequest
import com.artiusid.sdk.data.model.ApprovalResultData
import com.artiusid.sdk.util.DeviceUtils

/**
 * Matches iOS ApprovalResponse.swift EXACTLY
 * Handles sending approval responses (approve/deny) to the server
 */
class ApprovalResponse(
    private val context: Context,
    private val apiService: ApiService
) {
    companion object {
        private const val TAG = "ApprovalResponse"
        
        // REMOVED: No longer converting Android ID to iOS UUID format
        // Use native Android UUID format instead
    }

    /**
     * Send approval response - matches iOS sendApprovalResponse() function exactly
     * @param approvalValue "yes" for approve, "no" for deny
     * @return ApprovalResultData or null if failed
     */
    suspend fun sendApprovalResponse(approvalValue: String): ApprovalResultData? {
        return try {
            // Get device ID in native Android format
            val deviceId = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            ) ?: ""
            
            // Get request ID from notification state (like iOS AppNotificationState.shared.requestId)
            val requestId = AppNotificationState.requestId.value
            
            Log.d(TAG, "📤 Sending approval response:")
            Log.d(TAG, "📤   Device ID: $deviceId (native Android format)")
            Log.d(TAG, "📤   Request ID: $requestId")
            Log.d(TAG, "📤   Response Value: $approvalValue")
            
            // Create request exactly like iOS - ALL FIELDS AS STRINGS
            // iOS toEncodableBody() converts all fields to strings
            // NOTE: iOS does NOT include timeout field for approval responses
            val request = ApprovalRequest(
                clientId = ClientConfiguration.getClientId().toString(), // Configurable client ID as string
                clientGroupId = ClientConfiguration.getClientGroupId().toString(), // Configurable client group ID as string
                deviceId = deviceId,
                requestId = requestId.toString(), // Convert Int to String like iOS
                responseValue = approvalValue
            )
            
            Log.d(TAG, "📤 Request payload (all strings like iOS):")
            Log.d(TAG, "📤   clientId: ${request.clientId} (String)")
            Log.d(TAG, "📤   clientGroupId: ${request.clientGroupId} (String)")
            Log.d(TAG, "📤   deviceId: ${request.deviceId} (String)")
            Log.d(TAG, "📤   requestId: ${request.requestId} (String)")
            Log.d(TAG, "📤   responseValue: ${request.responseValue} (String)")
            Log.d(TAG, "📤   NOTE: timeout field NOT included (iOS doesn't send it for responses)")
            
            // Build endpoint URL (using ApprovalResponseFunction like iOS ServiceTypes.ApprovalResponse)
            val baseUrl = UrlBuilder.getApprovalResponseBaseUrl(context)
            Log.d(TAG, "🌐 Approval Response API Base URL: $baseUrl")
            Log.d(TAG, "🌐 Full endpoint: ${baseUrl}ApprovalResponseFunction")
            
            // Call API exactly like iOS apiService.approval()
            val result = apiService.approval(request)
            
            Log.d(TAG, "✅ Approval response sent successfully")
            Log.d(TAG, "✅ Server response: $result")
            
            result
            
        } catch (error: Exception) {
            Log.e(TAG, "❌ Approval error: ${error.localizedMessage}", error)
            null
        }
    }
}
