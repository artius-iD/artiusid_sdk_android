/*
 * File: PinningValidator.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.security

import android.util.Log
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

/**
 * Interface defining certificate validation
 * Implementations will validate server trust against pinned certificates
 */
interface CertificateValidating {
    /**
     * Validates server trust against pinned certificates
     * @param serverCertChain The server's certificate chain
     * @return true if validation succeeds, false otherwise
     */
    fun validateServerTrust(serverCertChain: Array<X509Certificate>): Boolean
}

/**
 * A validator that checks if any certificate in the server trust chain matches a pinned certificate.
 * Port of iOS PinningValidator.swift
 */
class PinningValidator(
    private val pinnedCertificates: List<X509Certificate>
) : CertificateValidating {
    
    /**
     * Get the pinned certificates for use with SecureSessionManager
     */
    fun getPinnedCertificates(): List<X509Certificate> = pinnedCertificates
    
    companion object {
        private const val TAG = "PinningValidator"
    }
    
    init {
        Log.d(TAG, "Initialized with ${pinnedCertificates.size} certificates")
    }
    
    /**
     * Validates a given server certificate chain against pinned certificates.
     */
    override fun validateServerTrust(serverCertChain: Array<X509Certificate>): Boolean {
        Log.d(TAG, "Starting validation with ${pinnedCertificates.size} pinned certificates")
        
        if (serverCertChain.isEmpty()) {
            Log.e(TAG, "Empty server certificate chain")
            return false
        }
        
        Log.d(TAG, "Retrieved server certificate chain with ${serverCertChain.size} certificates")
        
        // Compare each server cert with each pinned cert until a match is found
        for (serverCert in serverCertChain) {
            val serverData = serverCert.encoded
            
            for (pinnedCert in pinnedCertificates) {
                val pinnedData = pinnedCert.encoded
                
                if (serverData.contentEquals(pinnedData)) {
                    Log.d(TAG, "Found matching certificate")
                    return true
                }
            }
        }
        
        Log.d(TAG, "No matching certificates found")
        return false
    }
    
    /**
     * Validates server trust using certificate subjects (alternative validation method)
     */
    fun validateBySubject(serverCertChain: Array<X509Certificate>): Boolean {
        Log.d(TAG, "Starting subject validation with ${pinnedCertificates.size} pinned certificates")
        
        if (serverCertChain.isEmpty()) {
            Log.e(TAG, "Empty server certificate chain")
            return false
        }
        
        // Compare certificate subjects
        for (serverCert in serverCertChain) {
            val serverSubject = serverCert.subjectDN.name
            
            for (pinnedCert in pinnedCertificates) {
                val pinnedSubject = pinnedCert.subjectDN.name
                
                if (serverSubject == pinnedSubject) {
                    Log.d(TAG, "Found matching certificate subject: $serverSubject")
                    return true
                }
            }
        }
        
        Log.d(TAG, "No matching certificate subjects found")
        return false
    }
    
    /**
     * Validates server trust using public key pinning
     */
    fun validateByPublicKey(serverCertChain: Array<X509Certificate>): Boolean {
        Log.d(TAG, "Starting public key validation with ${pinnedCertificates.size} pinned certificates")
        
        if (serverCertChain.isEmpty()) {
            Log.e(TAG, "Empty server certificate chain")
            return false
        }
        
        // Compare public keys
        for (serverCert in serverCertChain) {
            val serverPublicKey = serverCert.publicKey.encoded
            
            for (pinnedCert in pinnedCertificates) {
                val pinnedPublicKey = pinnedCert.publicKey.encoded
                
                if (serverPublicKey.contentEquals(pinnedPublicKey)) {
                    Log.d(TAG, "Found matching public key")
                    return true
                }
            }
        }
        
        Log.d(TAG, "No matching public keys found")
        return false
    }
}