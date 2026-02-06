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
import com.artiusid.sdk.config.ClientConfiguration
import com.artiusid.sdk.data.model.VerificationRequest
import com.artiusid.sdk.data.model.VerificationResponse
import com.artiusid.sdk.data.model.VerificationResults
import com.artiusid.sdk.data.model.VerificationResultData
import com.artiusid.sdk.services.VerificationService
import com.artiusid.sdk.utils.ImageUtils
import com.artiusid.sdk.utils.ImageStorage
import com.artiusid.sdk.data.repository.LogManager
import com.artiusid.sdk.utils.UrlBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

/**
 * SDK v1.2.39 CRITICAL FIX: VerificationGuard Stuck State
 * 
 * Singleton object to track verification state across ViewModel instances.
 * This prevents duplicate verifications even if the ViewModel is recreated.
 * 
 * FIXES:
 * 1. Added initialization block to ensure clean state on app startup
 * 2. Fixed timeout calculation to handle edge cases (0L timestamps)
 * 3. Added comprehensive state validation and recovery
 * 4. Enhanced logging for debugging stuck states
 */
object VerificationGuard {
    @Volatile
    private var isVerificationInProgress = false
    @Volatile
    private var lastVerificationStartTime = 0L
    private val lock = Any()
    private const val VERIFICATION_TIMEOUT_MS = 120_000L // 2 minutes timeout
    private const val STUCK_STATE_TIMEOUT_MS = 30_000L // 30 seconds for stuck state recovery
    
    // SDK v1.2.45 CRITICAL FIX: Add global verification API call counter
    @Volatile
    private var totalApiCallCount = 0
    @Volatile
    private var currentSessionApiCalls = 0
    
    // SDK v1.2.45 CRITICAL FIX: Add global UI guard to prevent duplicate LaunchedEffect triggers
    @Volatile
    private var hasUIStartedVerification = false
    
    // ARCHITECTURAL FIX: Add global trigger guard to prevent duplicate triggerVerificationStart calls
    @Volatile
    private var hasTriggeredVerification = false
    
    init {
        // CRITICAL FIX: Always ensure clean state on initialization
        // This prevents stuck states from persisting across app restarts
        synchronized(lock) {
            isVerificationInProgress = false
            lastVerificationStartTime = 0L
            android.util.Log.i("VerificationGuard", "🚀 ========================================")
            android.util.Log.i("VerificationGuard", "🚀 SINGLETON: VerificationGuard initialized")
            android.util.Log.i("VerificationGuard", "🚀 State reset - ready for verification")
            android.util.Log.i("VerificationGuard", "🚀 ========================================")
        }
    }
    
    fun tryStartVerification(): Boolean {
        synchronized(lock) {
            val currentTime = System.currentTimeMillis()
            
            // SDK v1.2.40 CRITICAL FIX: Add stack trace logging to identify duplicate call source
            val stackTrace = Thread.currentThread().stackTrace
            android.util.Log.d("VerificationGuard", "🔍 ========================================")
            android.util.Log.d("VerificationGuard", "🔍 SINGLETON: tryStartVerification() called from:")
            stackTrace.take(8).forEachIndexed { index, element ->
                if (index > 0) { // Skip the current method
                    android.util.Log.d("VerificationGuard", "🔍   at ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
                }
            }
            android.util.Log.d("VerificationGuard", "🔍 ========================================")
            
            // CRITICAL FIX: Handle edge case where lastVerificationStartTime is 0 but isVerificationInProgress is true
            if (isVerificationInProgress && lastVerificationStartTime == 0L) {
                android.util.Log.e("VerificationGuard", "🚨 ========================================")
                android.util.Log.e("VerificationGuard", "🚨 CRITICAL: Detected inconsistent state!")
                android.util.Log.e("VerificationGuard", "🚨 isVerificationInProgress=true but lastVerificationStartTime=0")
                android.util.Log.e("VerificationGuard", "🚨 This indicates a stuck state - forcing reset")
                android.util.Log.e("VerificationGuard", "🚨 ========================================")
                isVerificationInProgress = false
                lastVerificationStartTime = 0L
            }
            
            // Check if verification is stuck (timeout safety)
            if (isVerificationInProgress && lastVerificationStartTime > 0L) {
                val elapsedMs = currentTime - lastVerificationStartTime
                if (elapsedMs > VERIFICATION_TIMEOUT_MS) {
                    val elapsedSeconds = elapsedMs / 1000
                    android.util.Log.w("VerificationGuard", "⏱️ ========================================")
                    android.util.Log.w("VerificationGuard", "⏱️ SINGLETON: Verification timed out after ${elapsedSeconds}s")
                    android.util.Log.w("VerificationGuard", "⏱️ Auto-resetting guard to prevent permanent stuck state")
                    android.util.Log.w("VerificationGuard", "⏱️ ========================================")
                    isVerificationInProgress = false
                    lastVerificationStartTime = 0L
                }
            }
            
            if (isVerificationInProgress) {
                val elapsedMs = if (lastVerificationStartTime > 0L) {
                    currentTime - lastVerificationStartTime
                } else {
                    0L // Handle edge case where timestamp is 0
                }
                val elapsedSeconds = elapsedMs / 1000
                
                // CRITICAL FIX v1.2.44: Handle race conditions (<100ms) differently than stuck states (>30s)
                if (elapsedMs < 100L) {
                    // Race condition detected - likely duplicate call from Compose recomposition
                    android.util.Log.w("VerificationGuard", "🚨 ========================================")
                    android.util.Log.w("VerificationGuard", "🚨 RACE CONDITION DETECTED: Duplicate call within ${elapsedMs}ms")
                    android.util.Log.w("VerificationGuard", "🚨 This is likely a Compose recomposition issue")
                    android.util.Log.w("VerificationGuard", "🚨 RESETTING guard to allow verification to proceed")
                    android.util.Log.w("VerificationGuard", "🚨 ========================================")
                    isVerificationInProgress = false
                    lastVerificationStartTime = 0L
                    // Allow this call to proceed - it's likely the legitimate call
                } else if (elapsedMs > STUCK_STATE_TIMEOUT_MS) {
                    // Stuck state detected - reset after 30 seconds
                    android.util.Log.e("VerificationGuard", "🚨 ========================================")
                    android.util.Log.e("VerificationGuard", "🚨 SINGLETON: First verification call appears STUCK")
                    android.util.Log.e("VerificationGuard", "🚨 No progress for ${elapsedSeconds}s - allowing recovery")
                    android.util.Log.e("VerificationGuard", "🚨 Resetting guard to allow new verification attempt")
                    android.util.Log.e("VerificationGuard", "🚨 ========================================")
                    isVerificationInProgress = false
                    lastVerificationStartTime = 0L
                    // Allow this call to proceed
                } else {
                    // Normal duplicate call - block it
                    android.util.Log.w("VerificationGuard", "⚠️ ========================================")
                    android.util.Log.w("VerificationGuard", "⚠️ SINGLETON: Verification already in progress (${elapsedSeconds}s)")
                    android.util.Log.w("VerificationGuard", "⚠️ State: isInProgress=$isVerificationInProgress, startTime=$lastVerificationStartTime")
                    android.util.Log.w("VerificationGuard", "⚠️ Elapsed: ${elapsedMs}ms since start")
                    android.util.Log.w("VerificationGuard", "⚠️ BLOCKING duplicate verification (will allow recovery after 30s)")
                    android.util.Log.w("VerificationGuard", "⚠️ Call originated from stack trace above")
                    android.util.Log.w("VerificationGuard", "⚠️ ========================================")
                    return false
                }
            }
            
            // Start verification
            isVerificationInProgress = true
            lastVerificationStartTime = currentTime
            
            // SDK v1.2.45: Reset session API call counter when starting new verification
            currentSessionApiCalls = 0
            
            val currentClientId = com.artiusid.sdk.config.ClientConfiguration.getClientId()
            val currentClientGroupId = com.artiusid.sdk.config.ClientConfiguration.getClientGroupId()
            
            android.util.Log.i("VerificationGuard", "✅ ========================================")
            android.util.Log.i("VerificationGuard", "✅ SINGLETON: Verification started at $currentTime")
            android.util.Log.i("VerificationGuard", "✅ 🎯 ClientId=$currentClientId, ClientGroupId=$currentClientGroupId")
            android.util.Log.i("VerificationGuard", "✅ State: isInProgress=$isVerificationInProgress, startTime=$lastVerificationStartTime")
            android.util.Log.i("VerificationGuard", "✅ Session API call counter RESET to 0")
            android.util.Log.i("VerificationGuard", "✅ Guard flag set - no duplicates allowed")
            android.util.Log.i("VerificationGuard", "✅ Call originated from stack trace above")
            android.util.Log.i("VerificationGuard", "✅ ========================================")
            return true
        }
    }
    
    fun resetVerification() {
        synchronized(lock) {
            val wasInProgress = isVerificationInProgress
            val previousStartTime = lastVerificationStartTime
            
            isVerificationInProgress = false
            lastVerificationStartTime = 0L
            currentSessionApiCalls = 0 // Reset session counter
            hasUIStartedVerification = false // Reset UI guard
            hasTriggeredVerification = false // Reset trigger guard
            
            android.util.Log.d("VerificationGuard", "🔄 ========================================")
            android.util.Log.d("VerificationGuard", "🔄 SINGLETON: Verification guard reset")
            android.util.Log.d("VerificationGuard", "🔄 Previous state: isInProgress=$wasInProgress, startTime=$previousStartTime")
            android.util.Log.d("VerificationGuard", "🔄 New state: isInProgress=$isVerificationInProgress, startTime=$lastVerificationStartTime")
            android.util.Log.d("VerificationGuard", "🔄 UI guard reset: hasUIStartedVerification=$hasUIStartedVerification")
            android.util.Log.d("VerificationGuard", "🔄 TRIGGER guard reset: hasTriggeredVerification=$hasTriggeredVerification")
            android.util.Log.d("VerificationGuard", "🔄 ========================================")
        }
    }
    
    // SDK v1.2.45: UI guard methods to prevent duplicate LaunchedEffect triggers
    fun tryStartUI(): Boolean {
        synchronized(lock) {
            if (hasUIStartedVerification) {
                android.util.Log.w("VerificationGuard", "⚠️ ========================================")
                android.util.Log.w("VerificationGuard", "⚠️ UI: DUPLICATE LaunchedEffect DETECTED!")
                android.util.Log.w("VerificationGuard", "⚠️ UI verification already started, BLOCKING this duplicate")
                android.util.Log.w("VerificationGuard", "⚠️ ========================================")
                return false
            }
            hasUIStartedVerification = true
            android.util.Log.d("VerificationGuard", "✅ ========================================")
            android.util.Log.d("VerificationGuard", "✅ UI: FIRST AND ONLY verification start - proceeding")
            android.util.Log.d("VerificationGuard", "✅ UI guard set: hasUIStartedVerification=$hasUIStartedVerification")
            android.util.Log.d("VerificationGuard", "✅ ========================================")
            return true
        }
    }
    
    /**
     * ARCHITECTURAL FIX: Global guard for triggerVerificationStart calls
     * Prevents duplicate verification triggers across all ViewModel instances
     */
    fun tryStartTrigger(): Boolean {
        synchronized(lock) {
            val currentTime = System.currentTimeMillis()
            if (hasTriggeredVerification) {
                android.util.Log.w("VerificationGuard", "🎯 ========================================")
                android.util.Log.w("VerificationGuard", "🎯 TRIGGER: DUPLICATE triggerVerificationStart DETECTED!")
                android.util.Log.w("VerificationGuard", "🎯 Verification trigger already called, BLOCKING this duplicate")
                android.util.Log.w("VerificationGuard", "🎯 Current state: isInProgress=$isVerificationInProgress, startTime=$lastVerificationStartTime")
                android.util.Log.w("VerificationGuard", "🎯 Time since last start: ${if (lastVerificationStartTime > 0) currentTime - lastVerificationStartTime else 0}ms")
                android.util.Log.w("VerificationGuard", "🎯 ========================================")
                
                // UI FIX: If no verification is actually in progress, reset the trigger guard
                if (!isVerificationInProgress && lastVerificationStartTime == 0L) {
                    android.util.Log.e("VerificationGuard", "🚨 TRIGGER GUARD STUCK: No verification in progress but trigger blocked!")
                    android.util.Log.e("VerificationGuard", "🚨 Resetting trigger guard to allow new verification")
                    hasTriggeredVerification = false
                    return tryStartTrigger() // Retry after reset
                }
                
                return false
            }
            hasTriggeredVerification = true
            android.util.Log.d("VerificationGuard", "🎯 ========================================")
            android.util.Log.d("VerificationGuard", "🎯 TRIGGER: FIRST AND ONLY verification trigger - proceeding")
            android.util.Log.d("VerificationGuard", "🎯 Trigger guard set: hasTriggeredVerification=$hasTriggeredVerification")
            android.util.Log.d("VerificationGuard", "🎯 ========================================")
            return true
        }
    }
    
    /**
     * Force reset the guard state - use only for debugging or emergency recovery
     */
    fun forceReset() {
        synchronized(lock) {
            android.util.Log.w("VerificationGuard", "🚨 FORCE RESET: Clearing all guard state")
            isVerificationInProgress = false
            lastVerificationStartTime = 0L
            hasUIStartedVerification = false
            hasTriggeredVerification = false // Reset trigger guard too
            android.util.Log.w("VerificationGuard", "🚨 FORCE RESET: All guards reset - ready for new verification")
        }
    }
    
    /**
     * Get current guard state for debugging
     */
    fun getDebugState(): String {
        synchronized(lock) {
            val currentTime = System.currentTimeMillis()
            val elapsedMs = if (lastVerificationStartTime > 0L) currentTime - lastVerificationStartTime else 0L
            return "VerificationGuard[isInProgress=$isVerificationInProgress, startTime=$lastVerificationStartTime, elapsed=${elapsedMs}ms, totalApiCalls=$totalApiCallCount, sessionApiCalls=$currentSessionApiCalls]"
        }
    }
    
    // SDK v1.2.45: Track API calls to detect duplicates
    fun incrementApiCallCount(): Int {
        synchronized(lock) {
            totalApiCallCount++
            currentSessionApiCalls++
            android.util.Log.w("VerificationGuard", "🚨 ========================================")
            android.util.Log.w("VerificationGuard", "🚨 API CALL COUNTER INCREMENTED")
            android.util.Log.w("VerificationGuard", "🚨 Total API calls (all time): $totalApiCallCount")
            android.util.Log.w("VerificationGuard", "🚨 Session API calls: $currentSessionApiCalls")
            android.util.Log.w("VerificationGuard", "🚨 ========================================")
            return currentSessionApiCalls
        }
    }
    
    fun resetSessionApiCalls() {
        synchronized(lock) {
            currentSessionApiCalls = 0
            android.util.Log.d("VerificationGuard", "✅ Session API call counter reset")
        }
    }
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

    // Guard flag to prevent duplicate verification calls
    private var hasStartedVerification = false
    private val verificationLock = Any()
    
    // Guard flag to prevent processing duplicate responses from backend/Retrofit
    @Volatile
    private var hasProcessedResponse = false
    private val responseLock = Any()
    
    // Track the active verification job to prevent concurrent executions
    private var activeVerificationJob: Job? = null
    
    // Flag to auto-trigger verification when context becomes available
    private var shouldAutoTrigger = false

    init {
        // ✅ CRITICAL FIX v1.2.47: Auto-trigger verification on ViewModel initialization
        // This eliminates duplicate triggers from multiple ViewModel instances
        Log.d(TAG, "🎯 ========================================")
        Log.d(TAG, "🎯 ViewModel INITIALIZED: ${this.hashCode()}")
        Log.d(TAG, "🎯 Auto-triggering verification on ViewModel creation")
        Log.d(TAG, "🎯 This ensures verification happens once per ViewModel instance")
        Log.d(TAG, "🎯 ========================================")
        
        // Auto-trigger verification when context becomes available
        // The UI will provide context via the first triggerVerificationStart call
        Log.d(TAG, "🎯 ViewModel ready - waiting for UI to provide context")
        
        // Set a flag to auto-trigger when context is available
        shouldAutoTrigger = true
    }

    /**
     * ARCHITECTURAL FIX: Trigger verification start as a one-time event
     * This method validates images and starts verification without being tied to UI recomposition
     */
    fun triggerVerificationStart(context: Context) {
        // ✅ CRITICAL FIX v1.2.47: Only trigger if this ViewModel should auto-trigger
        if (!shouldAutoTrigger) {
            Log.d(TAG, "🎯 TRIGGER: ViewModel already triggered, ignoring duplicate UI call")
            return
        }
        
        // Mark as triggered for this ViewModel instance
        shouldAutoTrigger = false
        
        // Use global singleton guard to prevent duplicate triggers across all ViewModel instances
        if (!VerificationGuard.tryStartTrigger()) {
            Log.d(TAG, "🎯 TRIGGER: Already triggered globally, ignoring duplicate call")
            return
        }
        
        viewModelScope.launch {
            Log.d(TAG, "🎯 ========================================")
            Log.d(TAG, "🎯 TRIGGER: Verification start requested (FIRST TIME)")
            Log.d(TAG, "🎯 Thread: ${Thread.currentThread().name}")
            Log.d(TAG, "🎯 Current guard state: ${VerificationGuard.getDebugState()}")
            Log.d(TAG, "🎯 ========================================")
            
            // CRITICAL FIX: Set UI state to Processing when verification starts
            _uiState.value = VerificationProcessingUiState.Processing
            _currentStep.value = "Initializing verification..."
            _progress.value = 0.0f
            Log.d(TAG, "🎯 UI State set to Processing - Initializing verification...")
            
            // UI FIX: Add a small delay to ensure UI updates are visible
            delay(100)
            
            val capturedImages = ImageStorage.getCapturedImages()
            val missing = mutableListOf<String>()
            
            // Validate based on document type (passport vs ID) - matching iOS logic
            if (capturedImages.passportImage != null) {
                // Passport flow: only requires passport + face
                if (capturedImages.faceImage == null) missing.add("face")
                Log.d(TAG, "PASSPORT validation - Image presence: passport=${capturedImages.passportImage != null}, face=${capturedImages.faceImage != null}")
            } else {
                // ID flow: requires front + back + face
                if (capturedImages.frontImage == null) missing.add("front")
                if (capturedImages.backImage == null) missing.add("back")
                if (capturedImages.faceImage == null) missing.add("face")
                Log.d(TAG, "ID validation - Image presence: front=${capturedImages.frontImage != null}, back=${capturedImages.backImage != null}, face=${capturedImages.faceImage != null}")
            }
            
            if (missing.isEmpty()) {
                // All required images are present, start verification
                _currentStep.value = "Preparing images..."
                _progress.value = 0.1f
                Log.d(TAG, "🎯 UI State updated: Preparing images...")
                
                if (capturedImages.passportImage != null) {
                    startVerification(
                        frontImageBitmap = null,
                        backImageBitmap = null,
                        faceImageBitmap = capturedImages.faceImage,
                        passportImageBitmap = capturedImages.passportImage,
                        context = context
                    )
                } else {
                    startVerification(
                        frontImageBitmap = capturedImages.frontImage,
                        backImageBitmap = capturedImages.backImage,
                        faceImageBitmap = capturedImages.faceImage,
                        context = context
                    )
                }
            } else {
                Log.e(TAG, "Cannot start verification, missing images: ${missing.joinToString()}")
                _uiState.value = VerificationProcessingUiState.Error("Missing required images: ${missing.joinToString()}. Please complete all steps.")
            }
        }
    }

    fun startVerification(
        frontImageBitmap: Bitmap?,
        backImageBitmap: Bitmap?,
        faceImageBitmap: Bitmap?,
        passportImageBitmap: Bitmap? = null, // Add passport image option
        context: Context
    ) {
        // SDK v1.2.40 CRITICAL FIX: Add comprehensive debugging for duplicate call investigation
        val currentThread = Thread.currentThread()
        val stackTrace = currentThread.stackTrace
        
        Log.d(TAG, "🟢 ========================================")
        Log.d(TAG, "🟢 ViewModel: startVerification() CALLED")
        Log.d(TAG, "🟢 Thread: ${currentThread.name} (ID: ${currentThread.id})")
        Log.d(TAG, "🟢 ViewModel instance: ${this.hashCode()}")
        Log.d(TAG, "🟢 hasStartedVerification = $hasStartedVerification")
        Log.d(TAG, "🟢 activeVerificationJob?.isActive = ${activeVerificationJob?.isActive}")
        Log.d(TAG, "🟢 startVerification() called from:")
        stackTrace.take(8).forEachIndexed { index, element ->
            if (index > 0) { // Skip the current method
                Log.d(TAG, "🟢   at ${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
            }
        }
        Log.d(TAG, "🟢 ========================================")
        
        // CRITICAL: Check singleton guard FIRST to prevent duplicate verifications
        // even if multiple ViewModel instances are created
        if (!VerificationGuard.tryStartVerification()) {
            val currentClientId = ClientConfiguration.getClientId()
            val currentClientGroupId = ClientConfiguration.getClientGroupId()
            val guardState = VerificationGuard.getDebugState()
            
            Log.w(TAG, "⚠️ ========================================")
            Log.w(TAG, "⚠️ Singleton guard blocked verification")
            Log.w(TAG, "⚠️ 🎯 ClientId=$currentClientId, ClientGroupId=$currentClientGroupId")
            Log.w(TAG, "⚠️ Guard state: $guardState")
            Log.w(TAG, "⚠️ ========================================")
            
            // UI FIX: Check if guard might be stuck and force reset if needed
            if (guardState.contains("elapsed=") && guardState.contains("ms")) {
                val elapsedMatch = Regex("elapsed=(\\d+)ms").find(guardState)
                val elapsedMs = elapsedMatch?.groupValues?.get(1)?.toLongOrNull() ?: 0L
                
                if (elapsedMs > 30000L) { // If stuck for more than 30 seconds
                    Log.e(TAG, "🚨 GUARD APPEARS STUCK (${elapsedMs}ms) - FORCING RESET")
                    VerificationGuard.forceReset()
                    
                    // Try again after reset
                    if (VerificationGuard.tryStartVerification()) {
                        Log.i(TAG, "✅ Verification started after force reset")
                    } else {
                        Log.e(TAG, "❌ Still blocked after force reset")
                        _uiState.value = VerificationProcessingUiState.Error("Verification system is busy. Please try again.")
                        return
                    }
                } else {
                    // Normal duplicate - silently block
                    return
                }
            } else {
                return
            }
        }
        
        // Prevent duplicate verification calls at ViewModel level
        synchronized(verificationLock) {
            if (hasStartedVerification) {
                Log.w(TAG, "⚠️ ========================================")
                Log.w(TAG, "⚠️ ViewModel: DUPLICATE CALL DETECTED")
                Log.w(TAG, "⚠️ Verification already in progress")
                Log.w(TAG, "⚠️ BLOCKING duplicate call")
                Log.w(TAG, "⚠️ NOTE: NOT resetting singleton guard - first call should complete")
                Log.w(TAG, "⚠️ ========================================")
                // CRITICAL FIX: Do NOT reset singleton guard here - let first call complete
                return
            }
            
            // Check if there's already an active verification job
            if (activeVerificationJob?.isActive == true) {
                Log.w(TAG, "⚠️ ========================================")
                Log.w(TAG, "⚠️ ViewModel: Active job already running")
                Log.w(TAG, "⚠️ BLOCKING duplicate call")
                Log.w(TAG, "⚠️ ========================================")
                VerificationGuard.resetVerification()  // Reset singleton if ViewModel blocks it
                return
            }
            
            hasStartedVerification = true
            Log.d(TAG, "✅ ========================================")
            Log.d(TAG, "✅ ViewModel: Guard flag SET")
            Log.d(TAG, "✅ Starting verification coroutine")
            Log.d(TAG, "✅ ========================================")
        }
        
        activeVerificationJob = viewModelScope.launch {
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
                // Retrieve FCM token using FirebaseConfigurationManager (NEW in v1.2.43)
                // Supports client-provided tokens and optional Firebase handling
                val fcmToken = try {
                    Log.d(TAG, "🔥 Getting FCM token via FirebaseConfigurationManager...")
                    val token = com.artiusid.sdk.utils.FirebaseConfigurationManager.getFcmToken(context)
                    
                    if (token.isNotEmpty()) {
                        Log.d(TAG, "✅ FCM token retrieved successfully: ${token.take(20)}...")
                        token
                    } else {
                        Log.w(TAG, "⚠️ No FCM token available (client or SDK), continuing without token")
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
                
                // Get Okta ID if enabled: pre-set (iOS parity) takes precedence over in-flow collection
                val oktaId = if (ClientConfiguration.shouldIncludeOktaID()) {
                    val id = com.artiusid.sdk.ArtiusIDSDK.getOktaUserId() ?: com.artiusid.sdk.utils.OktaIDHolder.getOktaID()
                    Log.d(TAG, "  oktaId: '${id ?: "<not provided>"}'")
                    id
                } else {
                    Log.d(TAG, "  oktaId: <disabled by configuration>")
                    null
                }

                // Re-verification: include account number from previous verification if available (iOS v2.0.17)
                val envName = when (ClientConfiguration.getCurrentConfig()?.environment) {
                    com.artiusid.sdk.config.Environment.SANDBOX -> "Sandbox"
                    com.artiusid.sdk.config.Environment.DEVELOPMENT -> "Development"
                    com.artiusid.sdk.config.Environment.STAGING -> "Staging"
                    null -> "Sandbox"
                }
                val accountNumber = com.artiusid.sdk.utils.VerificationStateManager(context).getAccountNumber(envName)
                if (!accountNumber.isNullOrEmpty()) {
                    Log.d(TAG, "  accountNumber (re-verification): '${accountNumber.take(10)}...'")
                }

                // Build request matching iOS format exactly - all fields required (non-nullable)
                val request = VerificationRequest(
                    frontImageBase64 = frontImageBase64,
                    backImageBase64 = backImageBase64,
                    faceImageBase64 = faceImageBase64,
                    documentType = documentType,
                    deviceId = deviceId,
                    deviceModel = deviceModel ?: "", // Default to empty string if null
                    fcmToken = fcmToken,
                    oktaId = oktaId,
                    accountNumber = accountNumber
                )

                Log.d(TAG, "[RETROFIT] Outgoing VerificationRequest payload (iOS format):")
                Log.d(TAG, "  frontImageBase64 length: ${request.frontImageBase64.length}")
                Log.d(TAG, "  backImageBase64 length: ${request.backImageBase64.length}")
                Log.d(TAG, "  faceImageBase64 length: ${request.faceImageBase64.length}")
                Log.d(TAG, "  documentType: ${request.documentType}")
                Log.d(TAG, "  deviceId: ${request.deviceId}")
                Log.d(TAG, "  deviceModel: ${request.deviceModel}")
                Log.d(TAG, "  fcmToken: '${request.fcmToken}'")
                Log.d(TAG, "  oktaId: '${request.oktaId ?: "<not included>"}'")
                Log.d(TAG, "  clientId=${ClientConfiguration.getClientId()} & clientGroupId=${ClientConfiguration.getClientGroupId()} will be added as URL query parameters (matching iOS)")

                // Use Retrofit ApiService for verification submission (back to original working approach)
                Log.d(TAG, "[RETROFIT] Sending VerificationRequest object directly")
                
                // Debug: Log the actual JSON that will be sent
                val gson = com.google.gson.Gson()
                val orderedMap = request.toOrderedMap()
                Log.d(TAG, "[DEBUG] LinkedHashMap contents: documentType = '${orderedMap["documentType"]}' (${orderedMap["documentType"]?.javaClass?.simpleName})")
                val requestJson = gson.toJson(orderedMap)
                Log.d(TAG, "[DEBUG] Actual JSON being sent (LinkedHashMap): $requestJson")
                
                // SDK v1.2.45 CRITICAL FIX: Add global API call tracking to detect duplicates
                val apiCallId = java.util.UUID.randomUUID().toString().substring(0, 8)
                val apiCallStartTime = System.currentTimeMillis()
                val sessionCallCount = VerificationGuard.incrementApiCallCount()
                
                Log.d(TAG, "🌐 ========================================")
                Log.d(TAG, "🌐 [API $apiCallId] VERIFICATION API CALL STARTING")
                Log.d(TAG, "🌐 [API $apiCallId] Time: $apiCallStartTime")
                Log.d(TAG, "🌐 [API $apiCallId] Thread: ${Thread.currentThread().name}")
                Log.d(TAG, "🌐 [API $apiCallId] ClientId: ${ClientConfiguration.getClientId()}")
                Log.d(TAG, "🌐 [API $apiCallId] ClientGroupId: ${ClientConfiguration.getClientGroupId()}")
                Log.d(TAG, "🌐 [API $apiCallId] hasProcessedResponse: $hasProcessedResponse")
                Log.d(TAG, "🌐 [API $apiCallId] Session call count: $sessionCallCount")
                
                if (sessionCallCount > 1) {
                    Log.w(TAG, "🚨 ========================================")
                    Log.w(TAG, "🚨 [API $apiCallId] DUPLICATE API CALL DETECTED!")
                    Log.w(TAG, "🚨 [API $apiCallId] This is call #$sessionCallCount in this session")
                    Log.w(TAG, "🚨 [API $apiCallId] SDK should only make 1 verification API call per session")
                    Log.w(TAG, "🚨 [API $apiCallId] SILENTLY BLOCKING duplicate call - no error shown to user")
                    Log.w(TAG, "🚨 [API $apiCallId] The first API call will continue processing normally")
                    Log.w(TAG, "🚨 ========================================")
                    
                    // CRITICAL: Silently block duplicate API calls - let the first call continue
                    return@launch
                }
                
                Log.d(TAG, "🌐 [API $apiCallId] ========================================")
                
                // Update UI state to show we're submitting to server
                _currentStep.value = "Submitting verification..."
                _progress.value = 0.3f
                Log.d(TAG, "🌐 [API $apiCallId] UI State updated: Submitting verification...")
                
                val response = apiService.verify(
                    clientId = ClientConfiguration.getClientId(), // Configurable client ID
                    clientGroupId = ClientConfiguration.getClientGroupId(), // Configurable client group ID
                    request = request.toOrderedMap()
                )
                
                val apiCallDuration = System.currentTimeMillis() - apiCallStartTime
                Log.d(TAG, "🌐 ========================================")
                Log.d(TAG, "🌐 [API $apiCallId] VERIFICATION API CALL COMPLETED")
                Log.d(TAG, "🌐 [API $apiCallId] Duration: ${apiCallDuration}ms")
                Log.d(TAG, "🌐 [API $apiCallId] Response received")
                Log.d(TAG, "🌐 [API $apiCallId] ========================================")
                
                // Update UI state to show we're processing the response
                _currentStep.value = "Processing response..."
                _progress.value = 0.7f
                Log.d(TAG, "🌐 [API $apiCallId] UI State updated: Processing response...")
                
                Log.d(TAG, "[RETROFIT] Verification response received - hasProcessedResponse=$hasProcessedResponse")
                Log.d(TAG, "[RETROFIT] Response data: $response")
                
                // CRITICAL: Guard against processing duplicate responses from Retrofit/backend
                // Use atomic check-and-set to prevent race conditions
                Log.d(TAG, "[GUARD_CHECK] About to check guard flag - current value: $hasProcessedResponse")
                
                // First check without lock for fast path
                if (hasProcessedResponse) {
                    Log.w(TAG, "⚠️ ========================================")
                    Log.w(TAG, "⚠️ DUPLICATE RESPONSE DETECTED (fast check)")
                    Log.w(TAG, "⚠️ Exiting immediately - no processing")
                    Log.w(TAG, "⚠️ ========================================")
                    return@launch
                }
                
                // Double-checked locking pattern for thread safety
                val shouldProcess = synchronized(responseLock) {
                    Log.d(TAG, "[GUARD_CHECK] Inside synchronized block - hasProcessedResponse=$hasProcessedResponse")
                    if (hasProcessedResponse) {
                        Log.w(TAG, "⚠️ ========================================")
                        Log.w(TAG, "⚠️ DUPLICATE RESPONSE DETECTED (synchronized check)")
                        Log.w(TAG, "⚠️ Another thread already processing")
                        Log.w(TAG, "⚠️ ========================================")
                        false  // Don't process
                    } else {
                        hasProcessedResponse = true
                        Log.d(TAG, "✅ ========================================")
                        Log.d(TAG, "✅ FIRST RESPONSE - PROCESSING")
                        Log.d(TAG, "✅ Guard flag NOW SET to true")
                        Log.d(TAG, "✅ No other responses will be processed")
                        Log.d(TAG, "✅ ========================================")
                        true  // Process this response
                    }
                }
                
                if (!shouldProcess) {
                    Log.w(TAG, "[GUARD_BLOCK] Exiting coroutine - duplicate response blocked in synchronized check")
                    return@launch
                }
                
                // Process the response
                _currentStep.value = "Processing verification results..."
                _progress.value = 0.9f
                delay(1000)
                
                // Process response exactly like iOS
                val verificationResult = processVerificationResponse(response)
                Log.d(TAG, "🔍 VERIFICATION RESULT: $verificationResult")
                Log.d(TAG, "🔍 Expected SUCCESS: ${VerificationResults.SUCCESS}")
                Log.d(TAG, "🔍 Result matches SUCCESS: ${verificationResult == VerificationResults.SUCCESS}")
                
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
                             
                             // 🚨 CRITICAL: Get current environment for verification storage
                             val currentEnvironment = UrlBuilder.getCurrentEnvironment(context)
                             val environmentForStorage = when (currentEnvironment.uppercase()) {
                                 "SANDBOX" -> "Sandbox"
                                 "DEVELOPMENT" -> "Development"
                                 "STAGING" -> "Staging"
                                 else -> "Sandbox"
                             }
                             
                             verificationStateManager.storeVerificationSuccess(
                                accountNumber = resultData.accountNumber ?: "",
                                accountFullName = fullName.takeIf { !it.isNullOrEmpty() },
                                isAccountActive = true,
                                environment = environmentForStorage // 🚨 CRITICAL: Tag with environment
                             )
                             Log.d(TAG, "🚨 Stored verification success with account: ${resultData.accountNumber} for environment: $environmentForStorage")
                         }
                         
                         Log.d(TAG, "🎉 Setting final UI states...")
                         _currentStep.value = "Verification complete!"
                         _progress.value = 1.0f
                         Log.d(TAG, "🎉 UI State: Verification complete! (progress: 1.0)")
                         delay(500)
                         Log.d(TAG, "🎉 Setting UI state to SUCCESS")
                         Log.d(TAG, "🎉 Current UI state before: ${_uiState.value}")
                         Log.d(TAG, "🎉 Current thread: ${Thread.currentThread().name}")
                         
                         // Ensure state update happens on main thread to trigger recomposition
                         withContext(Dispatchers.Main) {
                             _uiState.value = VerificationProcessingUiState.Success
                             Log.d(TAG, "🎉 UI state set to SUCCESS on Main thread")
                             Log.d(TAG, "🎉 Current UI state after: ${_uiState.value}")
                             Log.d(TAG, "🎉 State flow value: ${_uiState.value}")
                         }
                         
                         Log.d(TAG, "🎉 UI STATE SET TO SUCCESS - should navigate now")
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
                VerificationGuard.resetVerification()  // Reset singleton guard on success

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
                        601 -> {
                            // HTTP 601: MRZ_OCR_ERROR - Navigate back to Passport capture for passport flows
                            if (isPassportFlow) {
                                Log.w(TAG, "HTTP 601: MRZ_OCR_ERROR - navigating back to Passport capture")
                                LogManager.addLog("MRZ capture failed - returning to passport capture")
                                
                                // Navigate back to passport capture for retry
                                withContext(Dispatchers.Main) {
                                    _uiState.value = VerificationProcessingUiState.PassportRecaptureRequired(
                                        recaptureType = DocumentRecaptureType.PASSPORT_MRZ_ERROR
                                    )
                                    Log.d(TAG, "🔄 UI state set to PassportRecaptureRequired for MRZ error")
                                }
                                
                                Log.d(TAG, "=== VERIFICATION FLOW ENDED: PASSPORT RECAPTURE (HTTP 601) ===")
                                return@launch
                            } else {
                                // For non-passport flows, show failure screen
                                val verificationResult = VerificationResults.fromHttpStatusCode(e.code())
                                val failureType = getFailureTypeFromResult(verificationResult)
                                val errorReason = verificationResult.localizedDescription
                                
                                Log.w(TAG, "HTTP 601: ${verificationResult.name} - navigating to failure screen (non-passport)")
                                LogManager.addLog("Verification failed: $errorReason")
                                
                                withContext(Dispatchers.Main) {
                                    _uiState.value = VerificationProcessingUiState.Failure(
                                        failureType = failureType,
                                        errorReason = errorReason
                                    )
                                }
                                
                                Log.d(TAG, "=== VERIFICATION FLOW ENDED: FAILURE SCREEN (HTTP 601) ===")
                                return@launch
                            }
                        }
                        600, 602, 603, 604, 605 -> {
                            // Convert HTTP status code to VerificationResults like iOS
                            val verificationResult = VerificationResults.fromHttpStatusCode(e.code())
                            val failureType = getFailureTypeFromResult(verificationResult)
                            val errorReason = verificationResult.localizedDescription
                            
                            Log.w(TAG, "HTTP ${e.code()}: ${verificationResult.name} - navigating to failure screen")
                            LogManager.addLog("Verification failed: $errorReason")
                            
                            // Navigate to failure screen like iOS
                            Log.d(TAG, "🔴 Setting UI state to FAILURE")
                            Log.d(TAG, "🔴 Current UI state before: ${_uiState.value}")
                            Log.d(TAG, "🔴 Failure type: $failureType, Error reason: $errorReason")
                            
                            // Ensure state update happens on main thread
                            withContext(Dispatchers.Main) {
                                _uiState.value = VerificationProcessingUiState.Failure(
                                    failureType = failureType,
                                    errorReason = errorReason
                                )
                                Log.d(TAG, "🔴 UI state set to FAILURE: ${_uiState.value}")
                                Log.d(TAG, "🔴 UI state type: ${_uiState.value.javaClass.simpleName}")
                            }
                            
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
                VerificationGuard.resetVerification()  // Reset singleton guard on error
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