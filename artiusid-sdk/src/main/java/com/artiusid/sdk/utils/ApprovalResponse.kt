/*
 * File: ApprovalResponse.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.content.Context
import android.util.Log
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
        
        /**
         * Convert Android Secure ID to iOS-compatible UUID format
         * CRITICAL: Server expects iOS UUID format for device lookup
         */
        private fun convertAndroidIdToUUID(androidId: String): String {
            // Ensure we have enough characters, pad with zeros if needed
            val paddedId = androidId.padEnd(32, '0').take(32)
            
            // Format as UUID: 8-4-4-4-12 and uppercase like iOS
            return "${paddedId.substring(0, 8)}-${paddedId.substring(8, 12)}-${paddedId.substring(12, 16)}-${paddedId.substring(16, 20)}-${paddedId.substring(20, 32)}".uppercase()
        }
    }

    /**
     * Send approval response - matches iOS sendApprovalResponse() function exactly
     * @param approvalValue "yes" for approve, "no" for deny
     * @return ApprovalResultData or null if failed
     */
    suspend fun sendApprovalResponse(approvalValue: String): ApprovalResultData? {
        return try {
            // Get device ID in iOS UUID format (like iOS device.identifierForVendor?.uuidString)
            val androidId = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            ) ?: ""
            val deviceId = convertAndroidIdToUUID(androidId)
            
            // Get request ID from notification state (like iOS AppNotificationState.shared.requestId)
            val requestId = AppNotificationState.requestId.value
            
            Log.d(TAG, "📤 Sending approval response:")
            Log.d(TAG, "📤   Device ID: $deviceId (iOS UUID format)")
            Log.d(TAG, "📤   Request ID: $requestId")
            Log.d(TAG, "📤   Response Value: $approvalValue")
            
            // Create request exactly like iOS
            val request = ApprovalRequest(
                clientId = 1, // AppConstants.clientId
                clientGroupId = 1, // AppConstants.clientGroupId
                deviceId = deviceId,
                requestId = requestId,
                responseValue = approvalValue,
                timeout = "30"
            )
            
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
