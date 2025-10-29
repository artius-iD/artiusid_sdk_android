/*
 * File: EnvironmentCredentialManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.content.Context
import android.util.Log
import com.artiusid.sdk.ArtiusIDSDK

/**
 * 🚨 CRITICAL: Environment Credential Manager
 * 
 * Manages environment-specific credentials and auto-detects the correct environment
 * based on stored verification data, FCM tokens, and certificates.
 * 
 * This ensures that when the app opens, it automatically sets the environment
 * to match the stored credentials, preventing environment mismatch issues.
 */
class EnvironmentCredentialManager(private val context: Context) {
    
    companion object {
        private const val TAG = "EnvironmentCredentialManager"
        
        @Volatile
        private var INSTANCE: EnvironmentCredentialManager? = null
        
        fun getInstance(context: Context): EnvironmentCredentialManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: EnvironmentCredentialManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val verificationStateManager by lazy { VerificationStateManager(context) }
    private val firebaseTokenManager by lazy { FirebaseTokenManager.getInstance(context) }
    
    /**
     * 🚨 CRITICAL: Auto-detect environment from stored credentials
     * 
     * Checks verification data, FCM tokens, and certificates to determine
     * which environment the user is currently verified in.
     * 
     * @return The detected environment or null if no credentials found
     */
    fun autoDetectEnvironmentFromCredentials(): String? {
        Log.d(TAG, "🚨 ========================================")
        Log.d(TAG, "🚨 AUTO-DETECTING ENVIRONMENT FROM CREDENTIALS")
        Log.d(TAG, "🚨 ========================================")
        
        // Priority 1: Check verification state manager for environment
        val verificationEnv = verificationStateManager.getCurrentEnvironmentFromCredentials()
        if (!verificationEnv.isNullOrEmpty()) {
            Log.d(TAG, "🚨 ✅ Environment detected from verification data: $verificationEnv")
            return verificationEnv
        }
        
        // Priority 2: Check FCM token manager for environment
        val fcmEnv = firebaseTokenManager?.getCurrentEnvironmentFromTokens()
        if (!fcmEnv.isNullOrEmpty()) {
            Log.d(TAG, "🚨 ✅ Environment detected from FCM tokens: $fcmEnv")
            // Sync verification state manager
            verificationStateManager.setCurrentEnvironment(fcmEnv)
            return fcmEnv
        }
        
        // Priority 3: Check if any environment has verification data
        val environments = listOf("Development", "Staging", "Sandbox")
        for (env in environments) {
            val hasVerification = verificationStateManager.isVerified(env)
            if (hasVerification) {
                Log.d(TAG, "🚨 ✅ Environment detected from verification check: $env")
                // Sync both managers
                verificationStateManager.setCurrentEnvironment(env)
                firebaseTokenManager?.setCurrentEnvironment(env)
                return env
            }
        }
        
        Log.d(TAG, "🚨 ❌ No environment credentials found - user needs to verify")
        return null
    }
    
    /**
     * 🚨 CRITICAL: Set environment for all credential managers
     * 
     * When the user changes environment, this ensures all credential managers
     * are synchronized to the new environment.
     */
    fun setEnvironmentForAllCredentials(environment: String) {
        Log.d(TAG, "🚨 ========================================")
        Log.d(TAG, "🚨 SETTING ENVIRONMENT FOR ALL CREDENTIALS")
        Log.d(TAG, "🚨 Environment: $environment")
        Log.d(TAG, "🚨 ========================================")
        
        try {
            // Set environment in verification state manager
            verificationStateManager.setCurrentEnvironment(environment)
            Log.d(TAG, "🚨 ✅ Set verification environment: $environment")
            
            // Set environment in FCM token manager
            firebaseTokenManager?.setCurrentEnvironment(environment)
            Log.d(TAG, "🚨 ✅ Set FCM environment: $environment")
            
            // Set environment in URL builder
            UrlBuilder.setEnvironment(context, environment)
            Log.d(TAG, "🚨 ✅ Set URL builder environment: $environment")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to set environment for all credentials", e)
        }
    }
    
    /**
     * 🚨 CRITICAL: Clear credentials for specific environment
     * 
     * When switching environments, clear the old environment's credentials
     * to prevent cross-environment contamination.
     */
    fun clearCredentialsForEnvironment(environment: String) {
        Log.d(TAG, "🚨 ========================================")
        Log.d(TAG, "🚨 CLEARING CREDENTIALS FOR ENVIRONMENT")
        Log.d(TAG, "🚨 Environment: $environment")
        Log.d(TAG, "🚨 ========================================")
        
        try {
            // Clear verification data for environment
            verificationStateManager.clearVerificationData(environment)
            Log.d(TAG, "🚨 ✅ Cleared verification data for: $environment")
            
            // Clear FCM token for environment (if needed)
            // Note: FCM tokens are typically reused across environments
            // but we track which environment they belong to
            
            // Clear certificates for environment
            ArtiusIDSDK.clearCertificate(context)
            Log.d(TAG, "🚨 ✅ Cleared certificates for: $environment")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to clear credentials for environment: $environment", e)
        }
    }
    
    /**
     * 🚨 CRITICAL: Get credentials summary for debugging
     */
    fun getCredentialsSummary(): String {
        val sb = StringBuilder()
        sb.appendLine("🚨 CREDENTIALS SUMMARY:")
        sb.appendLine("🚨 ========================================")
        
        try {
            // Check verification state
            val verificationEnv = verificationStateManager.getCurrentEnvironmentFromCredentials()
            sb.appendLine("🚨 Verification Environment: $verificationEnv")
            
            val environments = listOf("Sandbox", "Development", "Staging")
            for (env in environments) {
                val hasVerification = verificationStateManager.isVerified(env)
                val accountNumber = verificationStateManager.getAccountNumber(env)
                sb.appendLine("🚨 $env: Verified=$hasVerification, Account=$accountNumber")
            }
            
            // Check FCM state
            val fcmEnv = firebaseTokenManager?.getCurrentEnvironmentFromTokens()
            sb.appendLine("🚨 FCM Environment: $fcmEnv")
            
            // Check URL builder state
            val urlEnv = UrlBuilder.getCurrentEnvironment(context)
            sb.appendLine("🚨 URL Builder Environment: $urlEnv")
            
        } catch (e: Exception) {
            sb.appendLine("🚨 ❌ Error getting credentials summary: ${e.message}")
        }
        
        sb.appendLine("🚨 ========================================")
        return sb.toString()
    }
}
