/*
 * File: CertificateManager.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.*
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.security.auth.x500.X500Principal
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.spec.PKCS8EncodedKeySpec
import javax.net.ssl.KeyManagerFactory

/**
 * Hybrid Certificate Manager that uses software keys for TLS compatibility
 * This approach generates software keys that can be used in both Android KeyStore operations
 * and standard TLS libraries, avoiding Android KeyStore limitations for mTLS
 */
class HybridCertificateManager(private val context: Context) {
    companion object {
        private const val TAG = "HybridCertManager"
        private const val SOFTWARE_KEY_ALIAS = "software_rsa_key"
    }
    
    /**
     * Generate or load a software-based RSA key pair that's compatible with both
     * Android operations and TLS libraries
     */
    fun getOrCreateSoftwareKeyPair(): KeyPair {
        // First try to load existing software key
        val existingKey = loadSoftwarePrivateKey()
        if (existingKey != null) {
            try {
                // Extract public key from private key
                val keyFactory = KeyFactory.getInstance("RSA")
                val privateRSAKey = existingKey as java.security.interfaces.RSAPrivateKey
                val publicKeySpec = java.security.spec.RSAPublicKeySpec(
                    privateRSAKey.modulus,
                    java.math.BigInteger.valueOf(65537) // Standard RSA public exponent
                )
                val publicKey = keyFactory.generatePublic(publicKeySpec)
                
                Log.d(TAG, "Loaded existing software key pair")
                return KeyPair(publicKey, existingKey)
                
            } catch (e: Exception) {
                Log.w(TAG, "Failed to reconstruct key pair from stored key: ${e.message}")
            }
        }
        
        // Generate new software key pair
        Log.d(TAG, "Generating new software RSA key pair")
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()
        
        // Store the private key securely
        storeSoftwarePrivateKey(keyPair.private)
        
        Log.d(TAG, "Generated and stored new software key pair")
        return keyPair
    }
    
    /**
     * Create a TLS-compatible KeyStore using software keys
     * This resolves Android KeyStore TLS limitations
     */
    fun createTLSKeyStore(certPem: String): KeyStore {
        try {
            Log.d(TAG, "Creating TLS KeyStore with software keys")
            
            // Get software key pair
            val keyPair = getOrCreateSoftwareKeyPair()
            
            // Parse certificate
            val certFactory = CertificateFactory.getInstance("X.509")
            val cert = certFactory.generateCertificate(certPem.byteInputStream()) as X509Certificate
            
            // Create PKCS12 KeyStore
            val keyStore = KeyStore.getInstance("PKCS12")
            keyStore.load(null, null)
            
            // Store certificate and private key
            val certChain = arrayOf(cert)
            keyStore.setKeyEntry(
                SOFTWARE_KEY_ALIAS,
                keyPair.private,
                "".toCharArray(), // Empty password
                certChain
            )
            
            Log.d(TAG, "Successfully created TLS KeyStore")
            return keyStore
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create TLS KeyStore: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Store the software private key securely in encrypted preferences
     * This allows us to reuse the same key for certificate generation and TLS
     */
    private fun storeSoftwarePrivateKey(privateKey: PrivateKey) {
        try {
            val encryptedPrefs = EncryptedSharedPreferences.create(
                "software_private_key",
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            
            val keyBytes = privateKey.encoded
            val base64Key = android.util.Base64.encodeToString(keyBytes, android.util.Base64.NO_WRAP)
            
            encryptedPrefs.edit()
                .putString("private_key", base64Key)
                .apply()
                
            Log.d(TAG, "Software private key stored securely")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store software private key: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Load the software private key from encrypted storage
     */
    private fun loadSoftwarePrivateKey(): PrivateKey? {
        try {
            val encryptedPrefs = EncryptedSharedPreferences.create(
                "software_private_key",
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            
            val base64Key = encryptedPrefs.getString("private_key", null) ?: return null
            val keyBytes = android.util.Base64.decode(base64Key, android.util.Base64.NO_WRAP)
            
            val keyFactory = KeyFactory.getInstance("RSA")
            val keySpec = PKCS8EncodedKeySpec(keyBytes)
            val privateKey = keyFactory.generatePrivate(keySpec)
            
            Log.d(TAG, "Software private key loaded from secure storage")
            return privateKey
            
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load software private key: ${e.message}")
            return null
        }
    }
    
    /**
     * Clear software keys (for environment changes)
     */
    fun clearSoftwareKeys() {
        try {
            val encryptedPrefs = EncryptedSharedPreferences.create(
                "software_private_key",
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            
            encryptedPrefs.edit().clear().apply()
            Log.d(TAG, "Software keys cleared")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear software keys: ${e.message}", e)
        }
    }

}

class CertificateManager(private val context: Context) {
    companion object {
        private const val TAG = "CertificateManager"
        private const val KEY_ALIAS = "artiusid_client_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val CERT_FILE_NAME = "client_cert.pem"
        
        // Enhanced security: Use EncryptedSharedPreferences (like iOS Keychain)
        private const val ENCRYPTED_PREFS_NAME = "certificate_prefs"
        private const val CERT_PEM_KEY = "certificate_pem"
        private const val PRIVATE_KEY_KEY = "private_key_pem"
    }

    /**
     * Generate an RSA keypair using hybrid approach (software keys for TLS compatibility)
     * Returns the KeyPair (public, private) that works with both Android operations and TLS.
     */
    fun generateOrGetKeyPair(): KeyPair {
        Log.d(TAG, "Using hybrid certificate approach for TLS compatibility")
        
        // Check if we have existing certificate and software key
        val existingCertPem = loadCertificatePem()
        val hybridManager = HybridCertificateManager(context)
        
        if (existingCertPem != null) {
            Log.d(TAG, "Certificate exists, loading matching software key pair")
            try {
                val keyPair = hybridManager.getOrCreateSoftwareKeyPair()
                
                // Verify the public key matches the certificate
                val certFactory = java.security.cert.CertificateFactory.getInstance("X.509")
                val cert = certFactory.generateCertificate(existingCertPem.byteInputStream()) as java.security.cert.X509Certificate
                val certPublicKey = cert.publicKey
                
                if (certPublicKey.encoded.contentEquals(keyPair.public.encoded)) {
                    Log.d(TAG, "Successfully loaded matching software key pair")
                    return keyPair
                } else {
                    Log.w(TAG, "Software key doesn't match certificate, regenerating both")
                    removeCertificatePem()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load existing key pair: ${e.message}")
                removeCertificatePem()
            }
        }
        
        // Generate new software key pair
        Log.d(TAG, "Generating new software RSA key pair for TLS compatibility")
        val keyPair = hybridManager.getOrCreateSoftwareKeyPair()
        
        Log.d(TAG, "Successfully generated hybrid key pair")
        return keyPair
    }

    /**
     * Generate a Certificate Signing Request (CSR) using DER encoding
     * This matches the iOS implementation exactly for server compatibility
     */
    fun generateCSR(deviceId: String): String {
        val keyPair = generateOrGetKeyPair()
        
        // Create subject map with all required fields (matching working test CSR)
        val subject = mapOf(
            "C" to "US",
            "ST" to "Arizona", 
            "L" to "Phoenix",
            "O" to "ArtiusID",
            "OU" to "Development",
            "CN" to deviceId
        )
        Log.d(TAG, "Generating CSR with subject: $subject")
        
        // Use DERUtils for exact iOS compatibility
        val csrPem = DERUtils.generateCSRPEM(subject, keyPair.public, keyPair.private)
        Log.d(TAG, "CSR generated successfully using DER encoding")
        
        return csrPem
    }

    /**
     * Store the certificate PEM string in encrypted storage (iOS Keychain equivalent).
     * Also stores in file for backward compatibility.
     */
    fun storeCertificatePem(certPem: String) {
        try {
            // Store in encrypted storage (like iOS Keychain)
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val encryptedPrefs = EncryptedSharedPreferences.create(
                ENCRYPTED_PREFS_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            
            encryptedPrefs.edit()
                .putString(CERT_PEM_KEY, certPem)
                .apply()
                
            Log.d(TAG, "✅ Certificate PEM stored in encrypted storage (iOS Keychain equivalent)")
            
            // Also store in file for backward compatibility
            val file = File(context.filesDir, CERT_FILE_NAME)
            file.writeText(certPem)
            Log.d(TAG, "📁 Certificate PEM also stored in file for compatibility: ${file.absolutePath}")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to store certificate PEM in encrypted storage", e)
            // Fallback to file storage
            val file = File(context.filesDir, CERT_FILE_NAME)
            file.writeText(certPem)
            Log.d(TAG, "📁 Fallback: Certificate PEM stored in file: ${file.absolutePath}")
        }
    }

    /**
     * Load the certificate PEM string from encrypted storage (iOS Keychain equivalent).
     * Falls back to file storage for backward compatibility.
     * Returns null if not found.
     */
    fun loadCertificatePem(): String? {
        try {
            // Try encrypted storage first (like iOS Keychain)
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val encryptedPrefs = EncryptedSharedPreferences.create(
                ENCRYPTED_PREFS_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            
            val encryptedCertPem = encryptedPrefs.getString(CERT_PEM_KEY, null)
            if (encryptedCertPem != null) {
                Log.d(TAG, "✅ Certificate PEM loaded from encrypted storage (iOS Keychain equivalent)")
                return encryptedCertPem
            }
            
            Log.d(TAG, "🔍 No certificate found in encrypted storage, checking file storage...")
            
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Failed to load certificate PEM from encrypted storage, falling back to file", e)
        }
        
        // Fallback to file storage
        val file = File(context.filesDir, CERT_FILE_NAME)
        if (file.exists()) {
            val certPem = file.readText()
            Log.d(TAG, "📁 Certificate PEM loaded from file: ${file.absolutePath}")
            return certPem
        }
        
        Log.d(TAG, "❌ No certificate PEM found in either encrypted or file storage")
        return null
    }

    /**
     * Remove the certificate and private key from both encrypted and file storage.
     */
    fun removeCertificatePem() {
        try {
            // Remove from encrypted storage
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val encryptedPrefs = EncryptedSharedPreferences.create(
                ENCRYPTED_PREFS_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            
            encryptedPrefs.edit()
                .remove(CERT_PEM_KEY)
                .remove(PRIVATE_KEY_KEY)
                .apply()
                
            Log.d(TAG, "✅ Certificate removed from encrypted storage")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to remove certificate from encrypted storage", e)
        }
        
        // Remove from file storage
        val file = File(context.filesDir, CERT_FILE_NAME)
        if (file.exists()) {
            file.delete()
            Log.d(TAG, "📁 Certificate file deleted: ${file.absolutePath}")
        }
        
        // Clear software keys
        val hybridManager = HybridCertificateManager(context)
        hybridManager.clearSoftwareKeys()
    }

    /**
     * Remove the key pair from Android KeyStore.
     */
    fun removeKeyPair() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            keyStore.deleteEntry(KEY_ALIAS)
            Log.d(TAG, "✅ Key pair removed from Android KeyStore")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to remove key pair from Android KeyStore", e)
        }
        
        // Also clear software keys
        val hybridManager = HybridCertificateManager(context)
        hybridManager.clearSoftwareKeys()
    }
} 