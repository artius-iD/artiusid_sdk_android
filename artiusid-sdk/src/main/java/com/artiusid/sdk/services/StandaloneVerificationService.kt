package com.artiusid.sdk.services

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.artiusid.sdk.data.api.StandaloneApiService
import com.artiusid.sdk.data.models.StandaloneVerificationRequest
import com.artiusid.sdk.data.models.StandaloneVerificationResponse
// StandaloneVerificationResults is defined at the bottom of this file
import com.artiusid.sdk.data.models.StandaloneVerificationResultData
import com.artiusid.sdk.utils.ImageUtils
import com.artiusid.sdk.utils.ImageStorage
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Complete embedded verification service from the standalone application
 * This contains the EXACT verification logic, API calls, and processing
 * from the standalone ArtiusID application
 */
@Singleton
class StandaloneVerificationService @Inject constructor(
    private val apiService: StandaloneApiService
) {
    companion object {
        private const val TAG = "StandaloneVerificationService"
        private const val MAX_RETRIES = 3
    }

    private val _verificationState = MutableStateFlow<StandaloneVerificationState>(StandaloneVerificationState.Idle)
    val verificationState: StateFlow<StandaloneVerificationState> = _verificationState.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _currentStep = MutableStateFlow("Ready to start verification...")
    val currentStep: StateFlow<String> = _currentStep.asStateFlow()

    private var retryCount = 0

    /**
     * Start the complete verification process exactly like the standalone app
     * This is the main entry point that replicates VerificationProcessingViewModel.startVerification()
     */
    suspend fun startCompleteVerification(
        frontImageBitmap: Bitmap?,
        backImageBitmap: Bitmap?,
        faceImageBitmap: Bitmap?,
        passportImageBitmap: Bitmap? = null,
        context: Context
    ): StandaloneVerificationResult {
        Log.d(TAG, "=== STANDALONE VERIFICATION FLOW STARTED ===")
        
        try {
            _verificationState.value = StandaloneVerificationState.Processing
            _currentStep.value = "Initializing verification..."
            _progress.value = 0.1f
            
            // Validate images exactly like standalone app
            Log.d(TAG, "Received images - Front: ${frontImageBitmap != null}, Back: ${backImageBitmap != null}, Face: ${faceImageBitmap != null}, Passport: ${passportImageBitmap != null}")
            
            if (frontImageBitmap != null) {
                Log.d(TAG, "Front image size: ${frontImageBitmap.width}x${frontImageBitmap.height}")
            }
            if (backImageBitmap != null) {
                Log.d(TAG, "Back image size: ${backImageBitmap.width}x${backImageBitmap.height}")
            }
            if (faceImageBitmap != null) {
                Log.d(TAG, "Face image size: ${faceImageBitmap.width}x${faceImageBitmap.height}")
            }
            if (passportImageBitmap != null) {
                Log.d(TAG, "Passport image size: ${passportImageBitmap.width}x${passportImageBitmap.height}")
            }

            _currentStep.value = "Preparing images..."
            _progress.value = 0.2f
            delay(500)

            // iOS logic: If passport image is present, use it as front, set back to empty, docType=2
            val (frontImageBase64, backImageBase64, documentType) = if (passportImageBitmap != null) {
                Triple(
                    ImageUtils.bitmapToDocumentBase64(passportImageBitmap),
                    "",
                    2 // Passport
                )
            } else {
                // Validate images for ID
                if (frontImageBitmap == null || backImageBitmap == null || faceImageBitmap == null) {
                    val error = "Missing images - Front: ${frontImageBitmap != null}, Back: ${backImageBitmap != null}, Face: ${faceImageBitmap != null}"
                    Log.e(TAG, error)
                    _verificationState.value = StandaloneVerificationState.Error(error)
                    return StandaloneVerificationResult.Error(error)
                }
                Triple(
                    ImageUtils.bitmapToDocumentBase64(frontImageBitmap),
                    ImageUtils.bitmapToDocumentBase64(backImageBitmap),
                    1 // State ID
                )
            }
            
            val faceImageBase64 = if (faceImageBitmap != null) ImageUtils.bitmapToFaceBase64(faceImageBitmap) else ""

            // Strict validation based on document type (passport vs ID)
            if (passportImageBitmap != null) {
                // Passport flow: only requires passport + face (matching iOS logic)
                if (faceImageBitmap == null) {
                    Log.e(TAG, "[STRICT] Missing face image for passport verification")
                    val error = "Missing face image for verification"
                    _verificationState.value = StandaloneVerificationState.Error(error)
                    return StandaloneVerificationResult.Error(error)
                }
                Log.d(TAG, "[STRICT] Passport images present. Sizes: passport=${passportImageBitmap.width}x${passportImageBitmap.height}, face=${faceImageBitmap.width}x${faceImageBitmap.height}")
            } else {
                // ID flow: requires front + back + face
                if (frontImageBitmap == null || backImageBitmap == null || faceImageBitmap == null) {
                    val error = "Missing required images for ID verification"
                    Log.e(TAG, "[STRICT] $error")
                    _verificationState.value = StandaloneVerificationState.Error(error)
                    return StandaloneVerificationResult.Error(error)
                }
                Log.d(TAG, "[STRICT] ID images present. Sizes: front=${frontImageBitmap.width}x${frontImageBitmap.height}, back=${backImageBitmap.width}x${backImageBitmap.height}, face=${faceImageBitmap.width}x${faceImageBitmap.height}")
            }

            _currentStep.value = "Preparing device information..."
            _progress.value = 0.3f
            delay(500)

            // Get device information in iOS UUID format for server compatibility
            val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
            val deviceId = convertAndroidIdToUUID(androidId)
            val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}; Android: ${Build.VERSION.RELEASE}"
            
            // FCM token (simplified for SDK)
            val fcmToken = ""

            Log.d(TAG, "[DEBUG] Before creating request:")
            Log.d(TAG, "  frontImageBase64 length: ${frontImageBase64.length}")
            Log.d(TAG, "  backImageBase64 length: ${backImageBase64.length}")
            Log.d(TAG, "  faceImageBase64 length: ${faceImageBase64.length}")
            Log.d(TAG, "  documentType: $documentType")
            Log.d(TAG, "  deviceId: $deviceId")
            Log.d(TAG, "  deviceModel: $deviceModel")

            // Build request matching iOS format exactly
            val request = StandaloneVerificationRequest(
                frontImageBase64 = frontImageBase64,
                backImageBase64 = backImageBase64,
                faceImageBase64 = faceImageBase64,
                documentType = documentType,
                deviceId = deviceId,
                deviceModel = deviceModel,
                fcmToken = fcmToken
            )

            _currentStep.value = "Sending verification request..."
            _progress.value = 0.5f
            delay(1000)

            Log.d(TAG, "[RETROFIT] Sending VerificationRequest to standalone API")
            
            // Make API call exactly like standalone app
            val response = apiService.verify(
                clientId = 1,
                clientGroupId = 1,
                request = request.toOrderedMap()
            )
            
            Log.d(TAG, "[RETROFIT] Verification response: $response")

            _currentStep.value = "Processing verification results..."
            _progress.value = 0.8f
            delay(1000)

            // Process response exactly like standalone app
            val verificationResult = processVerificationResponse(response)
            
            when (verificationResult) {
                StandaloneVerificationResults.SUCCESS -> {
                    Log.d(TAG, "Verification completed successfully")
                    
                    // Parse and store verification result data like iOS
                    val resultData = StandaloneVerificationResultData.fromPayload(response.verificationData?.payload)
                    
                    _currentStep.value = "Verification complete!"
                    _progress.value = 1.0f
                    delay(500)
                    
                    _verificationState.value = StandaloneVerificationState.Success(resultData)
                    return StandaloneVerificationResult.Success(resultData)
                }
                else -> {
                    Log.w(TAG, "Verification failed: ${verificationResult.localizedDescription}")
                    val error = "Verification failed: ${verificationResult.localizedDescription}"
                    _verificationState.value = StandaloneVerificationState.Error(error)
                    return StandaloneVerificationResult.Error(error)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Verification error: ${e.message}", e)
            val error = "Verification failed: ${e.message}"
            _verificationState.value = StandaloneVerificationState.Error(error)
            return StandaloneVerificationResult.Error(error)
        }
    }

    /**
     * Process verification response exactly like standalone app
     */
    private fun processVerificationResponse(response: StandaloneVerificationResponse): StandaloneVerificationResults {
        Log.d(TAG, "Processing verification response...")
        
        val verificationData = response.verificationData
        if (verificationData == null) {
            Log.e(TAG, "No verification data in response")
            return StandaloneVerificationResults.GENERAL_ERROR
        }

        Log.d(TAG, "Response status code: ${verificationData.statusCode}")
        Log.d(TAG, "Response message: ${verificationData.message}")

        return when (verificationData.statusCode) {
            200 -> {
                Log.d(TAG, "Verification successful (status 200)")
                StandaloneVerificationResults.SUCCESS
            }
            400 -> {
                Log.w(TAG, "Bad request (status 400)")
                StandaloneVerificationResults.GENERAL_ERROR
            }
            401 -> {
                Log.w(TAG, "Unauthorized (status 401)")
                StandaloneVerificationResults.GENERAL_ERROR
            }
            500 -> {
                Log.w(TAG, "Server error (status 500)")
                StandaloneVerificationResults.GENERAL_ERROR
            }
            else -> {
                Log.w(TAG, "Unknown status code: ${verificationData.statusCode}")
                StandaloneVerificationResults.GENERAL_ERROR
            }
        }
    }

    /**
     * Convert Android ID to iOS-style UUID format for server compatibility
     */
    private fun convertAndroidIdToUUID(androidId: String): String {
        // Pad or truncate to 32 characters
        val normalizedId = androidId.padEnd(32, '0').take(32)
        
        // Format as UUID: 8-4-4-4-12
        return "${normalizedId.substring(0, 8)}-${normalizedId.substring(8, 12)}-${normalizedId.substring(12, 16)}-${normalizedId.substring(16, 20)}-${normalizedId.substring(20, 32)}"
    }

    /**
     * Reset verification state
     */
    fun reset() {
        _verificationState.value = StandaloneVerificationState.Idle
        _progress.value = 0f
        _currentStep.value = "Ready to start verification..."
        retryCount = 0
    }
}

/**
 * Verification state exactly like standalone app
 */
sealed class StandaloneVerificationState {
    object Idle : StandaloneVerificationState()
    object Processing : StandaloneVerificationState()
    data class Success(val resultData: StandaloneVerificationResultData?) : StandaloneVerificationState()
    data class Error(val message: String) : StandaloneVerificationState()
}

/**
 * Verification result for SDK consumers
 */
sealed class StandaloneVerificationResult {
    data class Success(val resultData: StandaloneVerificationResultData?) : StandaloneVerificationResult()
    data class Error(val message: String) : StandaloneVerificationResult()
}

/**
 * Verification results enum exactly like standalone app
 */
enum class StandaloneVerificationResults(val localizedDescription: String) {
    SUCCESS("Verification successful"),
    GENERAL_ERROR("Verification failed"),
    NETWORK_ERROR("Network error"),
    TIMEOUT_ERROR("Request timeout"),
    INVALID_RESPONSE("Invalid response")
}
