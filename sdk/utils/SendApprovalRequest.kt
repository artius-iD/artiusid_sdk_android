/*
 * File: SendApprovalRequest.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.content.Context
import android.util.Log
import com.artiusid.sdk.data.api.ApiService
import com.artiusid.sdk.data.model.ApprovalRequestTestingRequest
import com.artiusid.sdk.utils.VerificationStateManager

/**
 * Matches iOS SendApprovalRequest.swift exactly
 * Sends test approval requests to the server
 */
class SendApprovalRequest(
    private val apiService: ApiService,
    private val context: Context
) {
    
    companion object {
        private const val TAG = "SendApprovalRequest"
        
        // REMOVED: No longer converting Android ID to iOS UUID format
        // Use native Android UUID format instead
    }
    
    /**
     * Send approval request - matches iOS send() function exactly
     * Returns (success, requestId)
     */
    suspend fun send(): Pair<Boolean, Int?> {
        return try {
            // Get device ID in native Android format
            val deviceId = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            ) ?: ""
            
            // Get member ID from verification state (like standalone Android app)
            val verificationStateManager = VerificationStateManager(context)
            val accountNumber = verificationStateManager.getAccountNumber()

            if (accountNumber.isNullOrEmpty()) {
                Log.e(TAG, "No account number found - user must complete verification first")
                return Pair(false, null)
            }

                // Create request exactly like iOS ApprovalRequestTestingRequest.swift
                val request = ApprovalRequestTestingRequest(
                    clientId = 1, // AppConstants.clientId
                    clientGroupId = 1, // AppConstants.clientGroupId
                    deviceId = deviceId,
                    approvalTitle = "Approval Request",
                    approvalDescription = "This is a test approval request.",
                    timeout = 30 // ✅ CRITICAL: iOS includes this field!
                )
            
            Log.d(TAG, "🔧 Using native Android device ID: $deviceId")
            Log.d(TAG, "Sending approval request for deviceId: $deviceId (native Android format)")
            Log.d(TAG, "Account Number (Member ID): $accountNumber")
            Log.d(TAG, "Using approval request ApiService exactly like iOS")
            Log.d(TAG, "✅ Server should now find device mapping with UUID format")
            
            // Log the full request for debugging
                Log.d(TAG, "📤 Request being sent (body only, exactly like iOS):")
                Log.d(TAG, "📤   ClientId: ${request.clientId}")
                Log.d(TAG, "📤   ClientGroupId: ${request.clientGroupId}")
                Log.d(TAG, "📤   DeviceId: ${request.deviceId}")
                Log.d(TAG, "📤   ApprovalTitle: ${request.approvalTitle}")
                Log.d(TAG, "📤   ApprovalDescription: ${request.approvalDescription}")
                Log.d(TAG, "📤   Timeout: ${request.timeout} (iOS field)")
            
                // Call API endpoint exactly like standalone Android app
                val response = apiService.sendApprovalRequestIOS(request)

                // Log the full response for debugging
                Log.d(TAG, "📋 Server response received (iOS format):")
                Log.d(TAG, "📋 Response object: $response")
                Log.d(TAG, "📋 RequestId: ${response.requestId}")
                Log.d(TAG, "📋 Success: ${response.success}")

                // Check response exactly like iOS (direct fields, not nested)
                if (response.success) {
                    val requestId = response.requestId
                    Log.d(TAG, "✅ Approval request sent successfully (iOS format)")
                    Log.d(TAG, "✅ Received requestId: $requestId")
                    Log.d(TAG, "✅ Success: ${response.success}")
                    Pair(true, requestId)
                } else {
                    Log.e(TAG, "❌ Approval response success=false - server rejected request")
                    Log.w(TAG, "Real server integration: No Firebase notification will be sent")
                    Log.w(TAG, "Check server configuration and device registration")
                    Pair(false, null)
                }
            
        } catch (e: Exception) {
            Log.e(TAG, "Approval request failed: ${e.localizedMessage}", e)
            Pair(false, null)
        }
    }
} 