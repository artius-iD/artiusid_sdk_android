package com.artiusid.sdk.managers

import android.util.Log

/**
 * Analytics manager for tracking SDK events - EXACT STANDALONE MATCH
 */
object AnalyticsManager {
    private const val TAG = "AnalyticsManager"
    
    fun trackEvent(eventName: String, parameters: Map<String, Any> = emptyMap()) {
        Log.d(TAG, "Event: $eventName, Parameters: $parameters")
        // Implementation would integrate with analytics service
    }
    
    fun trackError(error: Throwable, context: String = "") {
        Log.e(TAG, "Error in $context", error)
        // Implementation would report to crash analytics
    }
    
    fun setUserId(userId: String) {
        Log.d(TAG, "User ID set: $userId")
        // Implementation would set user context
    }
    
    fun trackVerificationStarted() {
        trackEvent("verification_started")
    }
    
    fun trackAuthenticationStarted() {
        trackEvent("authentication_started")
    }
    
    fun trackVerificationCompleted(success: Boolean) {
        trackEvent("verification_completed", mapOf("success" to success))
    }
    
    fun trackAuthenticationCompleted(success: Boolean) {
        trackEvent("authentication_completed", mapOf("success" to success))
    }
}