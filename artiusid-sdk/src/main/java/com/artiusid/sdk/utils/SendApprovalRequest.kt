package com.artiusid.sdk.utils

import android.content.Context
import android.util.Log
import com.artiusid.sdk.services.ApiService
import com.artiusid.sdk.models.ApprovalRequestTestingRequest
import com.artiusid.sdk.models.ApprovalResponse
import com.artiusid.sdk.utils.VerificationStateManager
// Removed Dagger imports

/**
 * Matches iOS SendApprovalRequest.swift exactly
 * Sends test approval requests to the server
 */
class SendApprovalRequest constructor(
    // @Named annotation removed - using manual initialization
    private val apiService: ApiService?,
     private val context: Context
) {
    
    companion object {
        private const val TAG = "SendApprovalRequest"
        
        /**
         * Convert Android Secure ID to iOS-compatible UUID format
         * CRITICAL: Server expects iOS UUID format for device lookup
         * iOS: "A1B2C3D4-E5F6-7890-ABCD-EF1234567890"
         * Android: "b911b2b9bf9076ad" -> "B911B2B9-BF90-76AD-0000-000000000000"
         */
        private fun convertAndroidIdToUUID(androidId: String): String {
            // Ensure we have enough characters, pad with zeros if needed
            val paddedId = androidId.padEnd(32, '0').take(32)
            
            // Format as UUID: 8-4-4-4-12 and uppercase like iOS
            return "${paddedId.substring(0, 8)}-${paddedId.substring(8, 12)}-${paddedId.substring(12, 16)}-${paddedId.substring(16, 20)}-${paddedId.substring(20, 32)}".uppercase()
        }
    }
    
    /**
     * Send approval request - matches iOS send() function exactly
     * Returns (success, requestId)
     */
    suspend fun send(): Pair<Boolean, String?> {
        return try {
            // Get device ID in iOS UUID format for server compatibility
            val androidId = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            ) ?: ""
            // Convert to iOS-compatible UUID format - CRITICAL for server device lookup
            val deviceId = convertAndroidIdToUUID(androidId)
            
            // Get member ID from verification state (like iOS keychain["verification"])
            val verificationStateManager = VerificationStateManager(context)
            val accountNumber = verificationStateManager.getAccountNumber()
            
            if (accountNumber.isNullOrEmpty()) {
                Log.e(TAG, "No account number found - user must complete verification first")
                return Pair(false, null)
            }
            
            // Create request exactly like iOS (NO account number in body)
            val request = ApprovalRequestTestingRequest(
                sessionId = "test-session-${System.currentTimeMillis()}",
                mockData = mapOf("deviceId" to deviceId)
            )
            
            Log.d(TAG, "🔧 Android ID: $androidId -> UUID: $deviceId")
            Log.d(TAG, "Sending approval request for deviceId: $deviceId (iOS UUID format)")
            Log.d(TAG, "Account Number (Member ID): $accountNumber")
            Log.d(TAG, "Using approval request ApiService exactly like iOS")
            Log.d(TAG, "✅ Server should now find device mapping with UUID format")
            
            // Call API endpoint exactly like iOS (NO query parameters)
            val response = apiService?.sendApprovalRequest(request)
            
            // Check if response is valid
            if (response != null && response.success) {
                val requestId = response.approvalId
                Log.d(TAG, "Received requestId: $requestId")
                Log.d(TAG, "Approval request sent successfully")
                Pair(true, requestId)
            } else {
                Log.e(TAG, "Approval response contained null approvalData - server may be unavailable")
                Log.w(TAG, "Real server integration: No Firebase notification will be sent")
                Log.w(TAG, "Check server configuration and network connectivity")
                Pair(false, null)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Approval request failed: ${e.localizedMessage}", e)
            Pair(false, null)
        }
    }
} 