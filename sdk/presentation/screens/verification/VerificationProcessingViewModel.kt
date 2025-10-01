/*
 * File: VerificationProcessingViewModel.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.verification

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artiusid.sdk.data.model.VerificationRequest
import com.artiusid.sdk.data.model.VerificationResponse
import com.artiusid.sdk.data.model.VerificationResults
import com.artiusid.sdk.data.model.VerificationResultData
import com.artiusid.sdk.services.VerificationService
import com.artiusid.sdk.utils.ImageUtils
import com.artiusid.sdk.utils.ImageStorage
import com.artiusid.sdk.data.repository.LogManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import com.artiusid.sdk.services.MyFirebaseMessagingService
import com.artiusid.sdk.utils.FirebaseTokenManager
import com.artiusid.sdk.utils.VerificationDataHolder
import com.google.gson.Gson
import com.artiusid.sdk.data.model.DocumentRecaptureType
import com.artiusid.sdk.data.model.VerificationFailureType

sealed class VerificationProcessingUiState {
    object Processing : VerificationProcessingUiState()
    object Success : VerificationProcessingUiState()
    data class Error(val message: String) : VerificationProcessingUiState()
    data class ConnectionError(val message: String) : VerificationProcessingUiState()
    data class Failure(
        val failureType: VerificationFailureType,
        val errorReason: String
    ) : VerificationProcessingUiState()
    
    // Enhanced recapture states with DocumentRecaptureType (matching existing usage)
    data class PassportRecaptureRequired(
        val recaptureType: DocumentRecaptureType
    ) : VerificationProcessingUiState()
    
    data class StateIdFrontRecaptureRequired(
        val recaptureType: DocumentRecaptureType
    ) : VerificationProcessingUiState()
    
    data class StateIdBackRecaptureRequired(
        val recaptureType: DocumentRecaptureType
    ) : VerificationProcessingUiState()
    
    data class DocumentRecaptureRequired(
        val recaptureType: DocumentRecaptureType
    ) : VerificationProcessingUiState()
}

@HiltViewModel
class VerificationProcessingViewModel @Inject constructor(
    private val apiService: com.artiusid.sdk.data.api.ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<VerificationProcessingUiState>(VerificationProcessingUiState.Processing)
    val uiState: StateFlow<VerificationProcessingUiState> = _uiState.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _currentStep = MutableStateFlow("Initializing verification...")
    val currentStep: StateFlow<String> = _currentStep.asStateFlow()
    
    private val _verificationResultData = MutableStateFlow<VerificationResultData?>(null)
    val verificationResultData: StateFlow<VerificationResultData?> = _verificationResultData.asStateFlow()

    private var retryCount = 0
    private val maxRetries = 3

    fun startVerification(
        frontImageBitmap: Bitmap?,
        backImageBitmap: Bitmap?,
        faceImageBitmap: Bitmap?,
        passportImageBitmap: Bitmap? = null, // Add passport image option
        context: Context
    ) {
        viewModelScope.launch {
            Log.d(TAG, "=== ENTERED startVerification() ===")
            Log.d(TAG, "=== VERIFICATION FLOW STARTED ===")
            try {
                Log.d(TAG, "Starting verification process")
                LogManager.addLog("Starting verification process")
                
                // Clear any existing verification result data
                // Note: VerificationDataHolder should be injected via Hilt
                Log.d(TAG, "Received images - Front: "+(frontImageBitmap != null)+", Back: "+(backImageBitmap != null)+", Face: "+(faceImageBitmap != null)+", Passport: "+(passportImageBitmap != null))
                LogManager.addLog("Received images - Front: "+(frontImageBitmap != null)+", Back: "+(backImageBitmap != null)+", Face: "+(faceImageBitmap != null)+", Passport: "+(passportImageBitmap != null))
                
                if (frontImageBitmap != null) {
                    Log.d(TAG, "Front image size: ${frontImageBitmap.width}x${frontImageBitmap.height}")
                    LogManager.addLog("Front image size: ${frontImageBitmap.width}x${frontImageBitmap.height}")
                }
                if (backImageBitmap != null) {
                    Log.d(TAG, "Back image size: ${backImageBitmap.width}x${backImageBitmap.height}")
                    LogManager.addLog("Back image size: ${backImageBitmap.width}x${backImageBitmap.height}")
                }
                if (faceImageBitmap != null) {
                    Log.d(TAG, "Face image size: ${faceImageBitmap.width}x${faceImageBitmap.height}")
                    LogManager.addLog("Face image size: ${faceImageBitmap.width}x${faceImageBitmap.height}")
                }
                if (passportImageBitmap != null) {
                    Log.d(TAG, "Passport image size: ${passportImageBitmap.width}x${passportImageBitmap.height}")
                    LogManager.addLog("Passport image size: ${passportImageBitmap.width}x${passportImageBitmap.height}")
                }
                
                _currentStep.value = "Preparing images..."
                _progress.value = 0.1f
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
                        val err = "Missing images - Front: ${frontImageBitmap != null}, Back: ${backImageBitmap != null}, Face: ${faceImageBitmap != null}"
                        Log.e(TAG, err)
                        LogManager.addLog(err)
                        _uiState.value = VerificationProcessingUiState.Error("Missing required images")
                        Log.d(TAG, "=== VERIFICATION FLOW ENDED: ERROR (missing images) ===")
                        return@launch
                    }
                    Triple(
                        ImageUtils.bitmapToDocumentBase64(frontImageBitmap),
                        ImageUtils.bitmapToDocumentBase64(backImageBitmap),
                        1 // State ID
                    )
                }
                val faceImageBase64 = if (faceImageBitmap != null) ImageUtils.bitmapToFaceBase64(faceImageBitmap) else ""

                // --- COMPRESSION EFFECTIVENESS LOGGING ---
                Log.d(TAG, "[COMPRESSION] Image compression results:")
                Log.d(TAG, "  frontImageBase64 length: ${frontImageBase64.length} chars (≈${ImageUtils.getEstimatedPayloadSizeKB(frontImageBase64)}KB)")
                Log.d(TAG, "  backImageBase64 length: ${backImageBase64.length} chars (≈${ImageUtils.getEstimatedPayloadSizeKB(backImageBase64)}KB)")
                Log.d(TAG, "  faceImageBase64 length: ${faceImageBase64.length} chars (≈${ImageUtils.getEstimatedPayloadSizeKB(faceImageBase64)}KB)")
                
                val totalPayloadSizeKB = ImageUtils.getEstimatedPayloadSizeKB(frontImageBase64) + 
                                       ImageUtils.getEstimatedPayloadSizeKB(backImageBase64) + 
                                       ImageUtils.getEstimatedPayloadSizeKB(faceImageBase64)
                Log.d(TAG, "  TOTAL estimated payload size: ${totalPayloadSizeKB}KB")
                
                Log.d(TAG, "  frontImageBase64 preview: ${frontImageBase64.take(100)}...")
                Log.d(TAG, "  backImageBase64 preview: ${backImageBase64.take(100)}...")
                Log.d(TAG, "  faceImageBase64 preview: ${faceImageBase64.take(100)}...")
                // --- END PATCH ---

                // Strict check: validate based on document type (passport vs ID)
                if (passportImageBitmap != null) {
                    // Passport flow: only requires passport + face (matching iOS logic)
                    if (faceImageBitmap == null) {
                        Log.e(TAG, "[STRICT] Missing face image for passport verification")
                        _uiState.value = VerificationProcessingUiState.Error("Missing face image for verification")
                        return@launch
                    }
                    Log.d(TAG, "[STRICT] Passport images present. Sizes: passport=${passportImageBitmap.width}x${passportImageBitmap.height}, face=${faceImageBitmap.width}x${faceImageBitmap.height}")
                } else {
                    // ID flow: requires front + back + face
                    if (frontImageBitmap == null) {
                        Log.e(TAG, "[STRICT] Missing front image, cannot proceed with verification")
                        _uiState.value = VerificationProcessingUiState.Error("Missing front image for verification")
                        return@launch
                    }
                    if (backImageBitmap == null) {
                        Log.e(TAG, "[STRICT] Missing back image, cannot proceed with verification")
                        _uiState.value = VerificationProcessingUiState.Error("Missing back image for verification")
                        return@launch
                    }
                    if (faceImageBitmap == null) {
                        Log.e(TAG, "[STRICT] Missing face image, cannot proceed with verification")
                        _uiState.value = VerificationProcessingUiState.Error("Missing face image for verification")
                        return@launch
                    }
                    Log.d(TAG, "[STRICT] ID images present. Sizes: front=${frontImageBitmap.width}x${frontImageBitmap.height}, back=${backImageBitmap.width}x${backImageBitmap.height}, face=${faceImageBitmap.width}x${faceImageBitmap.height}")
                }

                // Get device information in native Android format
                val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
                val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}; Android: ${Build.VERSION.RELEASE}"
                // Retrieve FCM token securely (match iOS Keychain)
                // Get FCM token using shared context (sample app's token)
                val fcmToken = try {
                    // Try to get FCM token from shared context manager first (sample app's token)
                    val sharedContextManager = com.artiusid.sdk.ArtiusIDSDK.getSharedContextManager()
                    val sharedTokenManager = sharedContextManager?.getSharedFirebaseTokenManager()
                    
                    val cachedToken = if (sharedTokenManager != null) {
                        Log.d(TAG, "Using shared FCM token from sample app context")
                        sharedTokenManager.getFCMToken()
                    } else {
                        Log.d(TAG, "No shared context, trying local FirebaseTokenManager")
                        val tokenManager = FirebaseTokenManager.getInstance()
                        tokenManager?.getFCMToken()
                    }
                    
                    if (!cachedToken.isNullOrEmpty()) {
                        Log.d(TAG, "✅ FCM token retrieved successfully: ${cachedToken.take(20)}...")
                        cachedToken
                    } else {
                        Log.w(TAG, "❌ No FCM token available, continuing without token")
                        ""
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "❌ FCM token unavailable: ${e.message}")
                    ""
                }

                // Debug: Log all values before creating request
                Log.d(TAG, "[DEBUG] Before creating request:")
                Log.d(TAG, "  frontImageBase64 length: ${frontImageBase64.length}")
                Log.d(TAG, "  backImageBase64 length: ${backImageBase64.length}")
                Log.d(TAG, "  faceImageBase64 length: ${faceImageBase64.length}")
                Log.d(TAG, "  documentType: $documentType")
                Log.d(TAG, "  deviceId: $deviceId")
                Log.d(TAG, "  deviceModel: $deviceModel")
                Log.d(TAG, "  fcmToken: '$fcmToken'")

                // Build request matching iOS format exactly - all fields required (non-nullable)
                val request = VerificationRequest(
                    frontImageBase64 = frontImageBase64,
                    backImageBase64 = backImageBase64,
                    faceImageBase64 = faceImageBase64,
                    documentType = documentType,
                    deviceId = deviceId,
                    deviceModel = deviceModel ?: "", // Default to empty string if null
                    fcmToken = fcmToken
                )

                Log.d(TAG, "[RETROFIT] Outgoing VerificationRequest payload (iOS format):")
                Log.d(TAG, "  frontImageBase64 length: ${request.frontImageBase64.length}")
                Log.d(TAG, "  backImageBase64 length: ${request.backImageBase64.length}")
                Log.d(TAG, "  faceImageBase64 length: ${request.faceImageBase64.length}")
                Log.d(TAG, "  documentType: ${request.documentType}")
                Log.d(TAG, "  deviceId: ${request.deviceId}")
                Log.d(TAG, "  deviceModel: ${request.deviceModel}")
                Log.d(TAG, "  fcmToken: '${request.fcmToken}'")
                Log.d(TAG, "  clientId=1 & clientGroupId=1 will be added as URL query parameters (matching iOS)")

                // Use Retrofit ApiService for verification submission (back to original working approach)
                Log.d(TAG, "[RETROFIT] Sending VerificationRequest object directly")
                
                // Debug: Log the actual JSON that will be sent
                val gson = com.google.gson.Gson()
                val orderedMap = request.toOrderedMap()
                Log.d(TAG, "[DEBUG] LinkedHashMap contents: documentType = '${orderedMap["documentType"]}' (${orderedMap["documentType"]?.javaClass?.simpleName})")
                val requestJson = gson.toJson(orderedMap)
                Log.d(TAG, "[DEBUG] Actual JSON being sent (LinkedHashMap): $requestJson")
                
                val response = apiService.verify(
                    clientId = 1, // AppConstants.clientId
                    clientGroupId = 1, // AppConstants.clientGroupId 
                    request = request.toOrderedMap()
                )
                Log.d(TAG, "[RETROFIT] Verification response: $response")
                
                // Process the response
                _currentStep.value = "Processing verification results..."
                _progress.value = 0.9f
                delay(1000)
                
                // Process response exactly like iOS
                val verificationResult = processVerificationResponse(response)
                
                                 when (verificationResult) {
                     VerificationResults.SUCCESS -> {
                         Log.d(TAG, "Verification completed successfully")
                         LogManager.addLog("Verification completed successfully")
                         
                        // Parse and store verification result data like iOS
                        val resultData = VerificationResultData.fromPayload(response.verificationData?.payload)
                        _verificationResultData.value = resultData
                        VerificationDataHolder.setVerificationData(resultData)
                        Log.d(TAG, "Parsed verification result data: $resultData")
                        Log.d(TAG, "Stored verification data in VerificationDataHolder for SDK callback")
                         
                         // Store verification success in secure storage like iOS keychain
                         if (!resultData.accountNumber.isNullOrEmpty()) {
                             val verificationStateManager = com.artiusid.sdk.utils.VerificationStateManager(context)
                             val fullName = "${resultData.firstName ?: ""} ${resultData.lastName ?: ""}".trim()
                             verificationStateManager.storeVerificationSuccess(
                                accountNumber = resultData.accountNumber ?: "",
                                accountFullName = fullName.takeIf { !it.isNullOrEmpty() },
                                 isAccountActive = true
                             )
                             Log.d(TAG, "Stored verification success with account: ${resultData.accountNumber}")
                         }
                         
                         _currentStep.value = "Verification complete!"
                         _progress.value = 1.0f
                         delay(500)
                         _uiState.value = VerificationProcessingUiState.Success
                     }
                    else -> {
                        Log.w(TAG, "Verification failed: ${verificationResult.localizedDescription}")
                        LogManager.addLog("Verification failed: ${verificationResult.name}")
                        
                        // Determine failure type and error reason based on verification result (like iOS)
                        val failureType = getFailureTypeFromResult(verificationResult)
                        val errorReason = verificationResult.localizedDescription
                        
                        _uiState.value = VerificationProcessingUiState.Failure(
                            failureType = failureType,
                            errorReason = errorReason
                        )
                    }
                }
                
                Log.d(TAG, "=== VERIFICATION FLOW ENDED: SUCCESS ===")

            } catch (e: Exception) {
                Log.e(TAG, "Error in verification process", e)
                
                // Handle connection reset and certificate issues specifically
                if (e is java.io.IOException && (e.message?.contains("Connection reset") == true || 
                    e.message?.contains("SSL") == true || e.message?.contains("certificate") == true)) {
                    Log.e(TAG, "🔐 mTLS/Connection issue detected: ${e.message}")
                    LogManager.addLog("Connection/Certificate error: ${e.message}")
                    
                    // This should return to sample app with error, not navigate back to NFC
                    _uiState.value = VerificationProcessingUiState.ConnectionError(
                        "Connection failed. Please check your network and try again."
                    )
                    Log.d(TAG, "=== VERIFICATION FLOW ENDED: CONNECTION ERROR (should return to sample app) ===")
                    return@launch
                }
                
                // Enhanced HTTP exception handling with comprehensive error codes
                if (e is retrofit2.HttpException) {
                    Log.e(TAG, "HTTP Error: ${e.code()} - ${e.message()}")
                    val errorBody = try {
                        e.response()?.errorBody()?.string()
                    } catch (ex: Exception) {
                        "Could not read error body: ${ex.message}"
                    }
                    Log.e(TAG, "Error response body: $errorBody")
                    
                    val capturedImages = ImageStorage.getCapturedImages()
                    val isPassportFlow = capturedImages.passportImage != null
                    
                    // Handle HTTP error codes exactly like iOS
                    when (e.code()) {
                        600, 601, 602, 603, 604, 605 -> {
                            // Convert HTTP status code to VerificationResults like iOS
                            val verificationResult = VerificationResults.fromHttpStatusCode(e.code())
                            val failureType = getFailureTypeFromResult(verificationResult)
                            val errorReason = verificationResult.localizedDescription
                            
                            Log.w(TAG, "HTTP ${e.code()}: ${verificationResult.name} - navigating to failure screen")
                            LogManager.addLog("Verification failed: $errorReason")
                            
                            // Navigate to failure screen like iOS
                            _uiState.value = VerificationProcessingUiState.Failure(
                                failureType = failureType,
                                errorReason = errorReason
                            )
                            Log.d(TAG, "=== VERIFICATION FLOW ENDED: FAILURE SCREEN (HTTP ${e.code()}) ===")
                            return@launch
                        }
                        
                        400 -> {
                            // HTTP 400 Bad Request - like iOS
                            Log.w(TAG, "HTTP 400: Bad Request - navigating to failure screen")
                            LogManager.addLog("Server rejected request - validation error")
                            
                            _uiState.value = VerificationProcessingUiState.Failure(
                                failureType = VerificationFailureType.GENERAL,
                                errorReason = "General error has occurred, please GO BACK and try again (400)"
                            )
                            Log.d(TAG, "=== VERIFICATION FLOW ENDED: FAILURE SCREEN (HTTP 400) ===")
                            return@launch
                        }
                        
                        else -> {
                            // Other HTTP errors - generic failure like iOS
                            Log.w(TAG, "HTTP ${e.code()}: Unexpected error - navigating to failure screen")
                            LogManager.addLog("Unexpected API error ${e.code()}")
                            
                            _uiState.value = VerificationProcessingUiState.Failure(
                                failureType = VerificationFailureType.GENERAL,
                                errorReason = "General error has occurred, please GO BACK and try again (${e.code()})"
                            )
                            Log.d(TAG, "=== VERIFICATION FLOW ENDED: FAILURE SCREEN (HTTP ${e.code()}) ===")
                            return@launch
                        }
                    }
                }
                
                val errorMsg = "Verification error: ${e.message}"
                LogManager.addLog(errorMsg)
                _uiState.value = VerificationProcessingUiState.Error(errorMsg)
                Log.d(TAG, "=== VERIFICATION FLOW ENDED: ERROR (exception) ===")
            }
        }
    }
    
    // Process verification response exactly like iOS
    private fun processVerificationResponse(response: VerificationResponse): VerificationResults {
        Log.d(TAG, "[PROCESSING] Processing verification response like iOS")
        
        // Check if verificationData exists
        val verificationData = response.verificationData
        if (verificationData == null) {
            Log.w(TAG, "No verification data in response")
            return VerificationResults.FAILED
        }
        
        val responseStatusCode = verificationData.statusCode
        Log.d(TAG, "[PROCESSING] Response status code: $responseStatusCode")
        
        // Handle success (exactly like iOS)
        if (responseStatusCode == 200) {
            val payload = verificationData.payload
            if (payload != null && payload.isNotEmpty()) {
                Log.d(TAG, "[PROCESSING] Success response with payload")
                
                // Parse payload JSON to check for failure status like iOS does
                val failureResult = checkForFailureInPayload(payload)
                if (failureResult != null) {
                    Log.w(TAG, "[PROCESSING] Found failure in payload: $failureResult")
                    return failureResult
                }
                
                return VerificationResults.SUCCESS
            } else {
                Log.w(TAG, "[PROCESSING] Success response but empty payload")
                return VerificationResults.SUCCESS
            }
        } else {
            // Handle error status codes exactly like iOS
            Log.w(TAG, "[PROCESSING] Error response: $responseStatusCode")
            return VerificationResults.fromHttpStatusCode(responseStatusCode)
        }
    }
    
    /**
     * Check for failure status in JSON payload - looks for "fail" in various fields
     * Returns appropriate VerificationResults if failure found, null if success
     */
    private fun checkForFailureInPayload(payload: String): VerificationResults? {
        try {
            val jsonObject = org.json.JSONObject(payload)
            Log.d(TAG, "[FAILURE_CHECK] Checking payload for failure status")
            
            // Check document status for "fail" - like iOS, this should be general failure
            if (jsonObject.has("documentData")) {
                val documentObject = jsonObject.getJSONObject("documentData")
                if (documentObject.has("payload")) {
                    val documentPayload = documentObject.getJSONObject("payload")
                    if (documentPayload.has("document_data")) {
                        val documentData = documentPayload.getJSONObject("document_data")
                        val documentStatus = documentData.optString("documentStatus", "").lowercase()
                        if (documentStatus.contains("fail")) {
                            Log.w(TAG, "[FAILURE_CHECK] Document status failure: $documentStatus")
                            // Like iOS, document status fail should be general failure, not recapture
                            return VerificationResults.FAILED
                        }
                        
                        // Also check for low face match scores (like the faceMatchScore:13 in logs)
                        val faceMatchScore = documentData.optInt("faceMatchScore", 100)
                        if (faceMatchScore < 50) {
                            Log.w(TAG, "[FAILURE_CHECK] Low face match score: $faceMatchScore")
                            // Low face match should also be general failure, not recapture
                            return VerificationResults.FAILED
                        }
                    }
                }
            }
            
            // Check risk data for failures
            if (jsonObject.has("riskData")) {
                val riskObject = jsonObject.getJSONObject("riskData")
                
                // Check person search result for "fail"
                if (riskObject.has("personSearchDataResults")) {
                    val personSearchObject = riskObject.getJSONObject("personSearchDataResults")
                    if (personSearchObject.has("personsearch_data")) {
                        val personData = personSearchObject.getJSONObject("personsearch_data")
                        val personResult = personData.optString("personSearchResult", "").lowercase()
                        if (personResult.contains("fail")) {
                            Log.w(TAG, "[FAILURE_CHECK] Person search failure: $personResult")
                            return VerificationResults.FAILED
                        }
                    }
                }
                
                // Check information search result for "fail"
                if (riskObject.has("informationSearchDataResults")) {
                    val infoSearchObject = riskObject.getJSONObject("informationSearchDataResults")
                    if (infoSearchObject.has("informationsearch_data")) {
                        val infoData = infoSearchObject.getJSONObject("informationsearch_data")
                        val infoResult = infoData.optString("informationSearchResult", "").lowercase()
                        if (infoResult.contains("fail")) {
                            Log.w(TAG, "[FAILURE_CHECK] Information search failure: $infoResult")
                            return VerificationResults.FAILED
                        }
                    }
                }
            }
            
            // Check for any top-level status fields containing "fail"
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                if (key.lowercase().contains("status") || key.lowercase().contains("result")) {
                    val value = jsonObject.optString(key, "").lowercase()
                    if (value.contains("fail")) {
                        Log.w(TAG, "[FAILURE_CHECK] General failure found in $key: $value")
                        return VerificationResults.FAILED
                    }
                }
            }
            
            Log.d(TAG, "[FAILURE_CHECK] No failure status found in payload")
            return null
            
        } catch (e: Exception) {
            Log.e(TAG, "[FAILURE_CHECK] Error parsing payload for failure check: ${e.message}")
            // If we can't parse the payload, assume success to avoid false failures
            return null
        }
    }
    
    /**
     * Map VerificationResults to appropriate VerificationFailureType
     */
    private fun getFailureTypeFromResult(result: VerificationResults): VerificationFailureType {
        return when (result) {
            VerificationResults.FACE_IMAGE_VALIDATION_ERROR -> VerificationFailureType.FACE
            VerificationResults.OCR_ERROR -> {
                // Determine document type based on captured images
                val capturedImages = ImageStorage.getCapturedImages()
                if (capturedImages.passportImage != null) {
                    VerificationFailureType.PASSPORT
                } else {
                    VerificationFailureType.STATE_ID_FRONT
                }
            }
            VerificationResults.MRZ_OCR_ERROR -> VerificationFailureType.PASSPORT
            VerificationResults.PRD417_ERROR -> VerificationFailureType.STATE_ID_BACK
            VerificationResults.PRE_PROCESS_ERROR -> {
                // Determine document type based on captured images
                val capturedImages = ImageStorage.getCapturedImages()
                if (capturedImages.passportImage != null) {
                    VerificationFailureType.PASSPORT
                } else {
                    VerificationFailureType.STATE_ID_FRONT
                }
            }
            VerificationResults.DOCUMENT_VALIDATION_ERROR -> VerificationFailureType.GENERAL
            VerificationResults.FAILED -> VerificationFailureType.GENERAL
            VerificationResults.SUCCESS -> VerificationFailureType.GENERAL // Should not happen
        }
    }
    


    companion object {
        private const val TAG = "VerifProcessVM"
        
        // REMOVED: No longer converting Android ID to iOS UUID format
        // Use native Android UUID format instead
    }
} 