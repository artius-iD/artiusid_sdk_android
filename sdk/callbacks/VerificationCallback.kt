/*
 * File: VerificationCallback.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.callbacks

import com.artiusid.sdk.models.VerificationResult
import com.artiusid.sdk.models.SDKError

/**
 * Callback interface for verification flow results from standalone application
 */
interface VerificationCallback {
    
    /**
     * Called when verification completes successfully in standalone app
     * @param result Complete verification results from standalone app
     */
    fun onVerificationSuccess(result: VerificationResult)
    
    /**
     * Called when verification fails or encounters an error in standalone app
     * @param error Details about the error that occurred
     */
    fun onVerificationError(error: SDKError)
    
    /**
     * Called when user cancels the verification flow in standalone app
     */
    fun onVerificationCancelled()
}