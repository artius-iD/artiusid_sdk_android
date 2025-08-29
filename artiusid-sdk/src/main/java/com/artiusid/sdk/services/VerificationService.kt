/*
 * Author: Todd Bryant
 * Company: artius.iD
 */
package com.artiusid.sdk.services

import com.artiusid.sdk.models.VerificationRequest
import com.artiusid.sdk.models.VerificationResponse

interface VerificationService {
    suspend fun submitVerification(verificationData: String): String
    suspend fun checkVerificationStatus(verificationId: String): String
    suspend fun getVerificationHistory(): String
} 