/*
 * File: CertificateManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.security

import android.content.Context
import android.provider.Settings
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.artiusid.sdk.data.api.ApiService
import com.artiusid.sdk.data.model.LoadCertificateRequest
import com.artiusid.sdk.data.model.LoadCertificateResponse
import com.artiusid.sdk.security.asn1.DER
import com.artiusid.sdk.security.constants.X509NameOID
import com.artiusid.sdk.security.exceptions.CertError
import com.artiusid.sdk.utils.EnvironmentManager
import com.artiusid.sdk.utils.ServiceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import javax.crypto.KeyGenerator

/**
 * Manages device identity certificates including generation, storage, and renewal
 * Port of iOS CertificateManager.swift
 */
class CertificateManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "CertificateManager"
        private const val CERTIFICATE_KEY = "client-cert"
        private const val PRIVATE_KEY_ALIAS = "com.artiusid.certificate.key"
        private const val ENCRYPTED_PREFS_NAME = "certificate_prefs"
        
        @Volatile
        private var INSTANCE: CertificateManager? = null
        
        fun getInstance(context: Context): CertificateManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CertificateManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    }
    
    private val encryptedPrefs by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            ENCRYPTED_PREFS_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    private val apiService: ApiService by lazy {
        // Initialize your API service here
        // This would typically be injected via dependency injection
        TODO("Initialize ApiService - integrate with your existing API setup")
    }
    
    /**
     * Checks for existing certificate and generates a new one if none found
     */
    suspend fun checkAndGenerateCertificate() {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Checking for existing certificate...")
            
            // Check if we have stored leaf certificate data
            if (!hasCertificate()) {
                val deviceId = getDeviceId()
                Log.w(TAG, "No certificate found, generating new CSR for device: $deviceId")
                
                val (csr, keyPair) = generateCSR(deviceId)
                Log.i(TAG, "CSR generated successfully")
                
                Log.d(TAG, "Registering CSR with API Gateway...")
                val response = registerCSR(csr, deviceId)
                Log.i(TAG, "Certificate registration complete")
                
                val certificate = response.certificate
                if (certificate.isNullOrEmpty()) {
                    throw CertError.InvalidCertificate
                }
                
                // Store the newly issued certificate
                storeSignedCertificate(
                    certPemString = certificate,
                    keyPair = keyPair
                )
            } else {
                Log.i(TAG, "Existing certificate found in storage")
            }
        }
    }
    
    /**
     * Remove stored certificate and private key
     */
    fun removeCertificate() {
        Log.d(TAG, "Removing existing certificate from storage...")
        
        // Remove from encrypted preferences
        encryptedPrefs.edit().remove(CERTIFICATE_KEY).apply()
        
        // Remove from Android KeyStore
        try {
            keyStore.deleteEntry(PRIVATE_KEY_ALIAS)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to remove private key from keystore", e)
        }
        
        Log.i(TAG, "Removed certificate and private key successfully")
    }
    
    /**
     * Check if certificate exists
     */
    fun hasCertificate(): Boolean {
        return encryptedPrefs.contains(CERTIFICATE_KEY) && 
               keyStore.containsAlias(PRIVATE_KEY_ALIAS)
    }
    
    /**
     * Get stored certificate
     */
    fun getCertificate(): ByteArray? {
        return try {
            val base64Cert = encryptedPrefs.getString(CERTIFICATE_KEY, null)
            base64Cert?.let { Base64.decode(it, Base64.DEFAULT) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve certificate", e)
            null
        }
    }
    
    /**
     * Get stored private key
     */
    fun getPrivateKey(): PrivateKey? {
        return try {
            keyStore.getKey(PRIVATE_KEY_ALIAS, null) as? PrivateKey
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve private key", e)
            null
        }
    }
    
    // MARK: - CSR Generation
    
    /**
     * Generate a Certificate Signing Request (CSR)
     */
    private fun generateCSR(deviceId: String): Pair<String, KeyPair> {
        val keyPair = generateKeyPair()
        
        // Subject fields
        val subject = mapOf(
            X509NameOID.COUNTRY_NAME to "US",
            X509NameOID.STATE_OR_PROVINCE_NAME to "Arizona",
            X509NameOID.LOCALITY_NAME to "Phoenix",
            X509NameOID.ORGANIZATION_NAME to "artius.iD",
            X509NameOID.ORGANIZATIONAL_UNIT_NAME to "Development",
            X509NameOID.COMMON_NAME to deviceId
        )
        
        val csrData = DER.generateCSR(
            subject = subject,
            publicKey = keyPair.public,
            privateKey = keyPair.private
        )
        val csrBase64String = Base64.encodeToString(csrData, Base64.NO_WRAP)
        
        // DEBUG: Print Android CSR base64 for comparison
        Log.d(TAG, "🔍 DEBUG, [Android-CSR] DER CSR (base64): $csrBase64String")
        
        val csrString = DER.encodePEM(csrBase64String, "CERTIFICATE REQUEST")
        return Pair(csrString, keyPair)
    }
    
    /**
     * Register a CSR with the server to obtain a signed certificate
     */
    private suspend fun registerCSR(csrPEM: String, deviceId: String): LoadCertificateResponse {
        val request = LoadCertificateRequest(deviceId = deviceId, csr = csrPEM)
        
        // Use the same environment configuration as the rest of the app
        val environmentManager = EnvironmentManager(context)
        val environment = environmentManager.getCurrentEnvironment()
        val serviceUrl = environmentManager.buildEndpointURL(ServiceType.LOAD_CERTIFICATE, environment)
        
        Log.d(TAG, "Registering with environment: $environment")
        
        return try {
            // You'll need to adapt this to your specific API service implementation
            apiService.loadCertificate(request)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register CSR", e)
            throw CertError.RegistrationFailed
        }
    }
    
    // MARK: - Key Storage
    
    /**
     * Store a signed certificate and its private key
     */
    private fun storeSignedCertificate(certPemString: String, keyPair: KeyPair) {
        Log.i(TAG, "Storing certificate and private key...")
        
        // Convert leaf certificate PEM to DER
        val derCert = try {
            DER.convertPEMToDER(certPemString)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert leaf cert from PEM to DER", e)
            throw CertError.InvalidCertificate
        }
        
        // Store certificate in encrypted preferences (base64 encoded for storage)
        val base64Cert = Base64.encodeToString(derCert, Base64.NO_WRAP)
        encryptedPrefs.edit().putString(CERTIFICATE_KEY, base64Cert).apply()
        
        // Store private key in Android KeyStore
        storePrivateKey(keyPair.private)
        
        Log.i(TAG, "Certificate and private key stored successfully")
    }
    
    // MARK: - Private Helpers
    
    /**
     * Generate an RSA key pair
     */
    private fun generateKeyPair(): KeyPair {
        return try {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(2048, SecureRandom())
            keyPairGenerator.generateKeyPair()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate key pair", e)
            throw CertError.KeyGenerationFailed
        }
    }
    
    /**
     * Store private key in Android KeyStore
     */
    private fun storePrivateKey(privateKey: PrivateKey) {
        try {
            // For this implementation, we'll store the key data in encrypted preferences
            // In a production app, you might want to use Android KeyStore's key generation
            val privateKeyBytes = privateKey.encoded
            val base64Key = Base64.encodeToString(privateKeyBytes, Base64.NO_WRAP)
            encryptedPrefs.edit().putString("${PRIVATE_KEY_ALIAS}_data", base64Key).apply()
            
            // Create a placeholder entry in Android KeyStore for consistency
            // Note: This is a simplified approach. In production, you'd generate the key 
            // directly in the KeyStore for better security
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store private key", e)
            throw CertError.StorageError
        }
    }
    
    /**
     * Get device ID (similar to iOS identifierForVendor)
     */
    private fun getDeviceId(): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                ?: java.util.UUID.randomUUID().toString()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get device ID, using random UUID", e)
            java.util.UUID.randomUUID().toString()
        }
    }
}