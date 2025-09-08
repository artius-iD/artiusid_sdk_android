/*
 * Author: Todd Bryant
 * Company: artius.iD
 */
package com.artiusid.sdk.services

import com.artiusid.sdk.data.models.VerificationRequest
import com.artiusid.sdk.data.models.VerificationResponse

interface VerificationService {
    suspend fun submitVerification(verificationData: String): String
    suspend fun checkVerificationStatus(verificationId: String): String
    suspend fun getVerificationHistory(): String
} 