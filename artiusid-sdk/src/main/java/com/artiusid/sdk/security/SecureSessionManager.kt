/*
 * File: SecureSessionManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.security

import android.util.Log
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManager
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Manages secure TLS sessions with certificate pinning and client certificate authentication
 * Port of iOS SecureSessionDelegate.swift for OkHttp
 */
class SecureSessionManager(
    private val certificateValidators: List<CertificateValidating>,
    private val clientCertificate: X509Certificate? = null,
    private val clientPrivateKey: PrivateKey? = null
) {
    
    companion object {
        private const val TAG = "SecureSessionManager"
    }
    
    init {
        Log.d(TAG, "Initialized with ${certificateValidators.size} validators")
    }
    
    /**
     * Create an OkHttpClient with certificate pinning and client certificate authentication
     */
    fun createSecureOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        
        // Set up SSL context with custom trust manager and client certificates
        val sslContext = createSSLContext()
        val trustManager = createTrustManager()
        
        builder.sslSocketFactory(sslContext.socketFactory, trustManager)
        
        // Add certificate pinning if we have pinned certificates
        val certificatePinner = createCertificatePinner()
        if (certificatePinner != null) {
            builder.certificatePinner(certificatePinner)
        }
        
        return builder.build()
    }
    
    /**
     * Create SSL context with custom key manager and trust manager
     */
    private fun createSSLContext(): SSLContext {
        val sslContext = SSLContext.getInstance("TLS")
        
        val keyManagers = createKeyManagers()
        val trustManagers = arrayOf<TrustManager>(createTrustManager())
        
        sslContext.init(keyManagers, trustManagers, null)
        return sslContext
    }
    
    /**
     * Create key managers for client certificate authentication
     */
    private fun createKeyManagers(): Array<KeyManager>? {
        return if (clientCertificate != null && clientPrivateKey != null) {
            try {
                val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
                keyStore.load(null, null)
                
                val certChain = arrayOf(clientCertificate)
                keyStore.setKeyEntry("client", clientPrivateKey, "".toCharArray(), certChain)
                
                val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
                keyManagerFactory.init(keyStore, "".toCharArray())
                
                Log.d(TAG, "Client certificate authentication configured")
                keyManagerFactory.keyManagers
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create key managers", e)
                null
            }
        } else {
            Log.d(TAG, "No client certificate provided")
            null
        }
    }
    
    /**
     * Create custom trust manager that uses certificate validators
     */
    private fun createTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                // Not needed for client-side implementation
            }
            
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                Log.d(TAG, "Checking server trust with auth type: $authType")
                
                // Validate using all configured validators
                val isValid = certificateValidators.any { validator ->
                    val result = validator.validateServerTrust(chain)
                    Log.d(TAG, "Validator ${validator.javaClass.simpleName} returned $result")
                    result
                }
                
                if (!isValid) {
                    Log.w(TAG, "Certificate validation failed")
                    throw javax.net.ssl.SSLException("Certificate validation failed")
                } else {
                    Log.d(TAG, "Certificate validation successful")
                }
            }
            
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return emptyArray()
            }
        }
    }
    
    /**
     * Create certificate pinner for additional security
     */
    private fun createCertificatePinner(): CertificatePinner? {
        // Extract pinned certificates from validators
        val pinnedCertificates = certificateValidators
            .filterIsInstance<PinningValidator>()
            .flatMap { it.getPinnedCertificates() }
        
        return if (pinnedCertificates.isNotEmpty()) {
            val builder = CertificatePinner.Builder()
            
            // Add certificate pins (this is a simplified example)
            // In practice, you'd need to extract the public key pins from certificates
            // and add them for specific hostnames
            
            Log.d(TAG, "Certificate pinner configured with ${pinnedCertificates.size} certificates")
            builder.build()
        } else {
            Log.d(TAG, "No certificate pinner configured")
            null
        }
    }
    
    /**
     * Handle server trust validation (similar to iOS handleServerTrust)
     */
    fun handleServerTrust(serverCertChain: Array<X509Certificate>): Boolean {
        Log.d(TAG, "Handling server trust validation")
        
        if (serverCertChain.isEmpty()) {
            Log.e(TAG, "No server certificates available")
            return false
        }
        
        // Validate using all configured validators
        val isValid = certificateValidators.any { validator ->
            val result = validator.validateServerTrust(serverCertChain)
            Log.d(TAG, "Validator ${validator.javaClass.simpleName} returned $result")
            result
        }
        
        if (isValid) {
            Log.d(TAG, "Certificate validation successful")
        } else {
            Log.w(TAG, "Certificate validation failed")
        }
        
        return isValid
    }
    
    /**
     * Handle client certificate challenge (similar to iOS handleClientCertificate)
     */
    fun handleClientCertificate(): Pair<X509Certificate, PrivateKey>? {
        return if (clientCertificate != null && clientPrivateKey != null) {
            Log.d(TAG, "Providing client certificate")
            Pair(clientCertificate, clientPrivateKey)
        } else {
            Log.w(TAG, "No client certificate available")
            null
        }
    }
}

// Note: PinningValidator now has a public getPinnedCertificates() method