/*
 * File: SendApprovalRequest.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.content.Context
import android.util.Log
import com.artiusid.sdk.config.ClientConfiguration
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
        // Generate unique call ID for tracking
        val callId = java.util.UUID.randomUUID().toString().substring(0, 8)
        val startTime = System.currentTimeMillis()
        
        return try {
            Log.d(TAG, "📞 [Call $callId] ========================================")
            Log.d(TAG, "📞 [Call $callId] send() STARTED at $startTime")
            Log.d(TAG, "📞 [Call $callId] ========================================")
            
            // Get device ID in native Android format
            val deviceId = android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            ) ?: ""
            
            // Get member ID from verification state (like standalone Android app)
            val verificationStateManager = VerificationStateManager(context)
            val accountNumber = verificationStateManager.getAccountNumber()

            if (accountNumber.isNullOrEmpty()) {
                Log.e(TAG, "📞 [Call $callId] ❌ No account number found - user must complete verification first")
                return Pair(false, null)
            }

                // Create request exactly like iOS ApprovalRequestTestingRequest.swift
                // CRITICAL FIX: NO FCM token - iOS doesn't include it!
                val request = ApprovalRequestTestingRequest(
                    clientId = ClientConfiguration.getClientId(), // Configurable client ID
                    clientGroupId = ClientConfiguration.getClientGroupId(), // Configurable client group ID
                    deviceId = deviceId,
                    approvalTitle = "Approval Request",
                    approvalDescription = "This is a test approval request.",
                    timeout = 30 // ✅ CRITICAL: iOS includes this field!
                )
            
            Log.d(TAG, "📞 [Call $callId] 🔧 Using native Android device ID: $deviceId")
            Log.d(TAG, "📞 [Call $callId] Account Number (Member ID): $accountNumber")
            
            // Log the full request for debugging
                Log.d(TAG, "📞 [Call $callId] 📤 Request payload (matching iOS exactly):")
                Log.d(TAG, "📞 [Call $callId] 📤   ClientId: ${request.clientId}")
                Log.d(TAG, "📞 [Call $callId] 📤   ClientGroupId: ${request.clientGroupId}")
                Log.d(TAG, "📞 [Call $callId] 📤   DeviceId: ${request.deviceId}")
                Log.d(TAG, "📞 [Call $callId] 📤   ApprovalTitle: ${request.approvalTitle}")
                Log.d(TAG, "📞 [Call $callId] 📤   ApprovalDescription: ${request.approvalDescription}")
                Log.d(TAG, "📞 [Call $callId] 📤   Timeout: ${request.timeout}")
                Log.d(TAG, "📞 [Call $callId] 📤   NOTE: NO FCM token (iOS doesn't include it)")
            
                Log.d(TAG, "📞 [Call $callId] 🌐 ========================================")
                Log.d(TAG, "📞 [Call $callId] 🌐 CALLING BACKEND API: apiService.sendApprovalRequestIOS()")
                Log.d(TAG, "📞 [Call $callId] 🌐 This is the ACTUAL network call that triggers Firebase notifications")
                Log.d(TAG, "📞 [Call $callId] 🌐 If you see 3 Firebase notifications, check if this log appears 3 times")
                Log.d(TAG, "📞 [Call $callId] 🌐 Thread: ${Thread.currentThread().name}")
                Log.d(TAG, "📞 [Call $callId] 🌐 ========================================")
                val apiCallStartTime = System.currentTimeMillis()
                
                // Call API endpoint exactly like standalone Android app
                val response = apiService.sendApprovalRequestIOS(request)
                
                val apiCallDuration = System.currentTimeMillis() - apiCallStartTime
                Log.d(TAG, "📞 [Call $callId] ✅ API call completed in ${apiCallDuration}ms")

                // 🚨 CRITICAL FIX: iOS-style direct response handling
                // Log the full response for debugging
                Log.d(TAG, "📞 [Call $callId] 📋 Server response received (iOS-style direct decode):")
                Log.d(TAG, "📞 [Call $callId] 📋   Response object: $response")
                Log.d(TAG, "📞 [Call $callId] 📋   RequestId: ${response.requestId}")
                Log.d(TAG, "📞 [Call $callId] 📋   Success: ${response.success}")
                
                // Check response exactly like iOS (direct structure)
                if (response.success) {
                    val requestId = response.requestId
                    val totalDuration = System.currentTimeMillis() - startTime
                    Log.d(TAG, "📞 [Call $callId] ✅ Approval request sent successfully (iOS-style)")
                    Log.d(TAG, "📞 [Call $callId] ✅ Received requestId: $requestId")
                    Log.d(TAG, "📞 [Call $callId] ✅ Total duration: ${totalDuration}ms")
                    Log.d(TAG, "📞 [Call $callId] ========================================")
                    Pair(true, requestId)
                } else {
                    Log.e(TAG, "📞 [Call $callId] ❌ Approval response success=false - server rejected request")
                    Log.w(TAG, "📞 [Call $callId] ⚠️ No Firebase notification will be sent")
                    Log.d(TAG, "📞 [Call $callId] ========================================")
                    Pair(false, null)
                }
            
        } catch (e: Exception) {
            val totalDuration = System.currentTimeMillis() - startTime
            
            // Enhanced error logging for HTTP exceptions
            if (e is retrofit2.HttpException) {
                val statusCode = e.code()
                val errorBody = try {
                    e.response()?.errorBody()?.string() ?: "No error body"
                } catch (ex: Exception) {
                    "Could not read error body: ${ex.message}"
                }
                
                Log.e(TAG, "📞 [Call $callId] ❌ Approval request failed after ${totalDuration}ms: HTTP $statusCode")
                Log.e(TAG, "📞 [Call $callId] ❌ Error response body: $errorBody")
                Log.e(TAG, "📞 [Call $callId] ❌ Exception: ${e.message}", e)
            } else {
                Log.e(TAG, "📞 [Call $callId] ❌ Approval request failed after ${totalDuration}ms: ${e.localizedMessage}", e)
            }
            
            Log.d(TAG, "📞 [Call $callId] ========================================")
            Pair(false, null)
        }
    }
} 