/*
 * File: NFCSecurityProvider.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

//
// NFCSecurityProvider.kt
// artiusid
//
// Author: Todd Bryant
// Company: artius.iD
//

package com.artiusid.sdk.utils.passport

import android.util.Log
import org.spongycastle.jce.provider.BouncyCastleProvider
import java.security.Provider
import java.security.Security
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages cryptographic security providers for NFC passport reading
 * Ensures SpongyCastle (Android-compatible BouncyCastle) is properly configured for JMRTD
 */
@Singleton
class NFCSecurityProvider @Inject constructor() {
    
    companion object {
        private const val TAG = "NFCSecurityProvider"
        private const val SPONGY_CASTLE_PROVIDER = "SC" // SpongyCastle provider name
        
        @Volatile
        private var isInitialized = false
    }
    
    /**
     * Initialize security providers for NFC operations
     * Must be called before any JMRTD operations
     */
    fun initializeSecurityProviders() {
        if (isInitialized) {
            Log.d(TAG, "Security providers already initialized")
            return
        }
        
        synchronized(this) {
            if (isInitialized) return
            
            try {
                // Remove any existing BouncyCastle providers to avoid conflicts
                Security.removeProvider("BC")
                Security.removeProvider("SC")
                
                // Add SpongyCastle as the primary cryptographic provider
                val spongyCastleProvider = BouncyCastleProvider()
                val insertResult = Security.insertProviderAt(spongyCastleProvider, 1)
                
                if (insertResult == 1) {
                    Log.i(TAG, "✅ SpongyCastle provider installed successfully at position 1")
                } else {
                    Log.w(TAG, "⚠️ SpongyCastle provider installed at position $insertResult (expected 1)")
                }
                
                // Verify provider installation
                verifyProviderInstallation()
                
                isInitialized = true
                Log.i(TAG, "🔐 NFC security providers initialized successfully")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to initialize security providers", e)
                throw RuntimeException("NFC security provider initialization failed", e)
            }
        }
    }
    
    /**
     * Verify that required cryptographic algorithms are available
     */
    private fun verifyProviderInstallation() {
        val requiredAlgorithms = listOf(
            "Cipher.DESede/CBC/NoPadding",
            "Cipher.AES/CBC/NoPadding", 
            "Mac.HmacSHA1",
            "Mac.HmacSHA256",
            "MessageDigest.SHA-1",
            "MessageDigest.SHA-256",
            "KeyAgreement.DH",
            "KeyAgreement.ECDH",
            "KeyPairGenerator.RSA",
            "KeyPairGenerator.EC"
        )
        
        val unavailableAlgorithms = mutableListOf<String>()
        
        for (algorithm in requiredAlgorithms) {
            try {
                val parts = algorithm.split(".")
                when (parts[0]) {
                    "Cipher" -> javax.crypto.Cipher.getInstance(parts[1])
                    "Mac" -> javax.crypto.Mac.getInstance(parts[1])
                    "MessageDigest" -> java.security.MessageDigest.getInstance(parts[1])
                    "KeyAgreement" -> javax.crypto.KeyAgreement.getInstance(parts[1])
                    "KeyPairGenerator" -> java.security.KeyPairGenerator.getInstance(parts[1])
                }
                Log.v(TAG, "✅ Algorithm available: $algorithm")
            } catch (e: Exception) {
                unavailableAlgorithms.add(algorithm)
                Log.w(TAG, "❌ Algorithm unavailable: $algorithm - ${e.message}")
            }
        }
        
        if (unavailableAlgorithms.isNotEmpty()) {
            throw RuntimeException("Required cryptographic algorithms unavailable: $unavailableAlgorithms")
        }
        
        Log.i(TAG, "✅ All required cryptographic algorithms verified")
    }
    
    /**
     * Get the SpongyCastle provider instance
     */
    fun getSpongyCastleProvider(): Provider? {
        return Security.getProvider(SPONGY_CASTLE_PROVIDER)
    }
    
    /**
     * Check if security providers are properly initialized
     */
    fun isSecurityProvidersInitialized(): Boolean = isInitialized
    
    /**
     * Get security provider information for debugging
     */
    fun getProviderInfo(): String = buildString {
        appendLine("Security Providers:")
        Security.getProviders().forEachIndexed { index, provider ->
            appendLine("${index + 1}. ${provider.name} v${provider.version} - ${provider.info}")
        }
        appendLine()
        appendLine("SpongyCastle Status: ${if (getSpongyCastleProvider() != null) "Available" else "Not Available"}")
        appendLine("Initialization Status: ${if (isInitialized) "Initialized" else "Not Initialized"}")
    }
}
