/*
 * File: FaceVerificationService.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.services

import kotlinx.coroutines.flow.Flow
import com.artiusid.sdk.data.model.FaceVerificationResult

interface FaceVerificationService {
    suspend fun verifyFace(imageData: ByteArray): FaceVerificationResult
    fun isFaceVerified(): Boolean
    fun startVerification(): Flow<FaceVerificationResult>
} 