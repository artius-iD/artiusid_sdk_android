/*
 * File: AuthenticationCallback.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.callbacks

import com.artiusid.sdk.models.AuthenticationResult
import com.artiusid.sdk.models.SDKError

/**
 * Callback interface for authentication flow results from standalone application
 */
interface AuthenticationCallback {
    
    /**
     * Called when authentication completes successfully in standalone app
     * @param result Complete authentication results from standalone app
     */
    fun onAuthenticationSuccess(result: AuthenticationResult)
    
    /**
     * Called when authentication fails or encounters an error in standalone app
     * @param error Details about the error that occurred
     */
    fun onAuthenticationError(error: SDKError)
    
    /**
     * Called when user cancels the authentication flow in standalone app
     */
    fun onAuthenticationCancelled()
}