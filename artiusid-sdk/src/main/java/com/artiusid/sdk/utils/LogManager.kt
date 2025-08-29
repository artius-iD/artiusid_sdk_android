package com.artiusid.sdk.utils

import com.artiusid.sdk.utils.*

import android.util.Log

/**
 * Centralized logging manager for the SDK
 */
object LogManager {
    
    private const val TAG_PREFIX = "ArtiusSDK"
    private var isDebugEnabled = true
    
    /**
     * Enable or disable debug logging
     */
    fun setDebugEnabled(enabled: Boolean) {
        isDebugEnabled = enabled
    }
    
    /**
     * Log debug message
     */
    fun d(tag: String, message: String) {
        if (isDebugEnabled) {
            Log.d("$TAG_PREFIX-$tag", message)
        }
    }
    
    /**
     * Log info message
     */
    fun i(tag: String, message: String) {
        Log.i("$TAG_PREFIX-$tag", message)
    }
    
    /**
     * Log warning message
     */
    fun w(tag: String, message: String) {
        Log.w("$TAG_PREFIX-$tag", message)
    }
    
    /**
     * Log error message
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e("$TAG_PREFIX-$tag", message, throwable)
        } else {
            Log.e("$TAG_PREFIX-$tag", message)
        }
    }
    
    /**
     * Log verification event
     */
    fun logVerificationEvent(event: String, details: Map<String, Any> = emptyMap()) {
        val message = buildString {
            append("Verification Event: $event")
            if (details.isNotEmpty()) {
                append(" - Details: $details")
            }
        }
        i("Verification", message)
    }
    
    /**
     * Log authentication event
     */
    fun logAuthenticationEvent(event: String, details: Map<String, Any> = emptyMap()) {
        val message = buildString {
            append("Authentication Event: $event")
            if (details.isNotEmpty()) {
                append(" - Details: $details")
            }
        }
        i("Authentication", message)
    }
    
    /**
     * Log performance metrics
     */
    fun logPerformance(operation: String, duration: Long, details: Map<String, Any> = emptyMap()) {
        val message = buildString {
            append("Performance: $operation took ${duration}ms")
            if (details.isNotEmpty()) {
                append(" - Details: $details")
            }
        }
        i("Performance", message)
    }
}
