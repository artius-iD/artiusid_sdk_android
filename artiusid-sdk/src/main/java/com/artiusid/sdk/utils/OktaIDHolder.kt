/*
 * File: OktaIDHolder.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 * 
 * Singleton to hold Okta ID across verification flow
 * Similar to ImageStorage pattern
 */

package com.artiusid.sdk.utils

import android.util.Log

/**
 * Singleton holder for Okta ID during verification flow
 * Matches iOS pattern of storing Okta ID in verification state
 */
object OktaIDHolder {
    private const val TAG = "OktaIDHolder"
    
    private var oktaId: String? = null
    
    /**
     * Set the Okta ID
     */
    fun setOktaID(id: String?) {
        oktaId = id?.trim()
        Log.d(TAG, "✅ Okta ID stored: ${if (oktaId.isNullOrBlank()) "<empty>" else oktaId}")
    }
    
    /**
     * Get the Okta ID
     */
    fun getOktaID(): String? {
        Log.d(TAG, "🔍 Retrieved Okta ID: ${if (oktaId.isNullOrBlank()) "<empty>" else oktaId}")
        return oktaId
    }
    
    /**
     * Clear the Okta ID (call after verification completes or fails)
     */
    fun clear() {
        Log.d(TAG, "🧹 Clearing Okta ID")
        oktaId = null
    }
    
    /**
     * Check if Okta ID has been set
     */
    fun hasOktaID(): Boolean {
        return !oktaId.isNullOrBlank()
    }
}

