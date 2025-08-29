package com.artiusid.sdk.services

import kotlinx.coroutines.flow.Flow
import com.artiusid.sdk.models.FaceVerificationResult

interface FaceVerificationService {
    suspend fun verifyFace(imageData: ByteArray): FaceVerificationResult
    fun isFaceVerified(): Boolean
    fun startVerification(): Flow<FaceVerificationResult>
} 