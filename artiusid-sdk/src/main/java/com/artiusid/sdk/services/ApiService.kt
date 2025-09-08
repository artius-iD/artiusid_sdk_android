package com.artiusid.sdk.services

import com.artiusid.sdk.data.models.VerificationRequest
import com.artiusid.sdk.data.models.VerificationResponse
import com.artiusid.sdk.data.models.ApprovalRequestTestingRequest
import com.artiusid.sdk.data.models.ApprovalResponse

/**
 * API service interface for verification requests
 * Simplified stub implementation for SDK
 */
interface ApiService {
    
    /**
     * Submit verification request
     */
    suspend fun submitVerification(request: VerificationRequest): VerificationResponse
    
    /**
     * Verify request with client parameters
     */
    suspend fun verify(
        clientId: Int,
        clientGroupId: Int,
        request: VerificationRequest
    ): VerificationResponse
    
    /**
     * Get verification status
     */
    suspend fun getVerificationStatus(sessionId: String): VerificationResponse
    
    /**
     * Send approval request
     */
    suspend fun sendApprovalRequest(request: ApprovalRequestTestingRequest): ApprovalResponse
}

