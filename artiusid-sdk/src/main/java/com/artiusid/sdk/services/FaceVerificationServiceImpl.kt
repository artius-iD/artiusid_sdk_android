package com.artiusid.sdk.services

import android.content.Context
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.artiusid.sdk.models.FaceVerificationResult

class FaceVerificationServiceImpl(private val context: Context) : FaceVerificationService {
    companion object {
        private const val TAG = "FaceVerificationServiceImpl"
    }

    private var faceVerified = false

    override suspend fun verifyFace(imageData: ByteArray): FaceVerificationResult {
        Log.d(TAG, "Verifying face in image data")
        // TODO: Implement actual face verification using ML Kit or similar
        // For now, simulate a delay and return success
        delay(1000) // Simulate processing time
        faceVerified = true
        return FaceVerificationResult(
            isVerified = true,
            confidence = 0.95f,
            faceBitmap = null
        )
    }

    override fun isFaceVerified(): Boolean {
        return faceVerified
    }

    override fun startVerification(): Flow<FaceVerificationResult> = flow {
        emit(FaceVerificationResult(
            isVerified = false,
            confidence = 0f,
            faceBitmap = null,
            errorMessage = "Verification in progress"
        ))
        delay(1000) // Simulate processing time
        faceVerified = true
        emit(FaceVerificationResult(
            isVerified = true,
            confidence = 0.95f,
            faceBitmap = null
        ))
    }
} 