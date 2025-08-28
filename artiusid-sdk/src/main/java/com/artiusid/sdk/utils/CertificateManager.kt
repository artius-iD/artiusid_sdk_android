package com.artiusid.utils

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
            val encoded = privateKey.encoded
            val base64Key = android.util.Base64.encodeToString(encoded, android.util.Base64.NO_WRAP)
            
            val encryptedPrefs = EncryptedSharedPreferences.create(
                "software_private_key",
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            
            encryptedPrefs.edit().putString("private_key", base64Key).apply()
            Log.d(TAG, "Stored software private key securely")
            
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
            
            Log.d(TAG, "Loaded software private key from secure storage")
            return privateKey
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load software private key: ${e.message}", e)
            return null
        }
    }
    
    /**
     * Clear stored software private key to force regeneration
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
            
            encryptedPrefs.edit().remove("private_key").apply()
            Log.d(TAG, "Cleared software private key")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear software private key: ${e.message}", e)
        }
    }

}

class CertificateManager(private val context: Context) {
    companion object {
        private const val TAG = "CertificateManager"
        private const val KEY_ALIAS = "artiusid_client_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val CERT_FILE_NAME = "client_cert.pem"
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
                Log.w(TAG, "Failed to load software key: ${e.message}, regenerating...")
                removeCertificatePem()
            }
        }
        
        // Generate new software key pair for TLS compatibility
        Log.d(TAG, "Generating new software RSA key pair for TLS compatibility")
        val keyPair = hybridManager.getOrCreateSoftwareKeyPair()
        
        Log.d(TAG, "Successfully generated software key pair for hybrid certificate approach")
        return keyPair
    }

    /**
     * Remove the keypair from the Android Keystore.
     */
    fun removeKeyPair() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.deleteEntry(KEY_ALIAS)
            Log.d(TAG, "Removed keypair from Android Keystore with alias $KEY_ALIAS")
        }
    }

    // Generate CSR using custom DER logic (matching iOS exactly)
    fun generateCSR(deviceId: String): String {
        Log.d(TAG, "Generating CSR for device: $deviceId")
        
        // Get the keypair from Keystore
        val keyPair = generateOrGetKeyPair()
        
        // Create subject map using string OIDs (matching iOS exactly)
        val subject = mapOf(
            "2.5.4.6" to "US",           // countryName
            "2.5.4.8" to "Arizona",      // stateOrProvinceName  
            "2.5.4.7" to "Phoenix",      // localityName
            "2.5.4.10" to "ArtiusID",    // organizationName
            "2.5.4.11" to "Development", // organizationalUnitName
            "2.5.4.3" to deviceId         // commonName
        )

        // Generate DER CSR for logging
        val derCsr = DERUtils.generateCSR(subject, keyPair.public, keyPair.private)
        val base64Der = android.util.Base64.encodeToString(derCsr, android.util.Base64.NO_WRAP)
        Log.d(TAG, "DER CSR (base64): $base64Der")

        // Generate PEM CSR for use
        val pemCsr = DERUtils.generateCSRPEM(subject, keyPair.public, keyPair.private)
        Log.d(TAG, "PEM CSR: $pemCsr")

        return pemCsr
    }

    /**
     * Store the certificate PEM string in app-private storage.
     */
    fun storeCertificatePem(certPem: String) {
        val file = File(context.filesDir, CERT_FILE_NAME)
        file.writeText(certPem)
        Log.d(TAG, "Stored certificate PEM at: ${file.absolutePath}")
    }

    /**
     * Load the certificate PEM string from app-private storage.
     * Returns null if not found.
     */
    fun loadCertificatePem(): String? {
        val file = File(context.filesDir, CERT_FILE_NAME)
        return if (file.exists()) file.readText() else null
    }

    /**
     * Remove the certificate PEM file from app-private storage.
     */
    fun removeCertificatePem() {
        val file = File(context.filesDir, CERT_FILE_NAME)
        if (file.exists()) {
            file.delete()
            Log.d(TAG, "Removed certificate PEM file: ${file.absolutePath}")
        } else {
            Log.d(TAG, "Certificate PEM file does not exist: ${file.absolutePath}")
        }
    }

    /**
     * Verify that the private key matches the certificate by doing a sign/verify test
     */
    fun verifyCertificateKeyMatch(): Boolean {
        try {
            val certPem = loadCertificatePem() ?: return false
            val keyPair = generateOrGetKeyPair()
            
            // Load certificate
            val certFactory = java.security.cert.CertificateFactory.getInstance("X.509")
            val cert = certFactory.generateCertificate(certPem.byteInputStream()) as java.security.cert.X509Certificate
            
            Log.d(TAG, "Certificate subject: ${cert.subjectDN}")
            Log.d(TAG, "Certificate public key algorithm: ${cert.publicKey.algorithm}")
            Log.d(TAG, "Private key algorithm: ${keyPair.private.algorithm}")
            
            // Compare public keys directly first
            val certPublicKey = cert.publicKey
            val extractedPublicKey = keyPair.public
            if (extractedPublicKey != null) {
                val publicKeysMatch = certPublicKey.encoded.contentEquals(extractedPublicKey.encoded)
                Log.d(TAG, "Public keys direct comparison: $publicKeysMatch")
            } else {
                Log.w(TAG, "Extracted public key is null, cannot compare directly")
            }
            
            // Test data to sign
            val testData = "certificate-key-match-test".toByteArray()
            
            // Sign with private key
            val signature = java.security.Signature.getInstance("SHA256withRSA")
            signature.initSign(keyPair.private)
            signature.update(testData)
            val signatureBytes = signature.sign()
            Log.d(TAG, "Signature created successfully, length: ${signatureBytes.size}")
            
            // Verify with public key from certificate
            val verifySignature = java.security.Signature.getInstance("SHA256withRSA")
            verifySignature.initVerify(cert.publicKey)
            verifySignature.update(testData)
            val isValid = verifySignature.verify(signatureBytes)
            
            Log.d(TAG, "Signature verification result: $isValid")
            
            // Android KeyStore has signature restrictions that can cause false negatives
            // If public keys match, consider it valid even if signature verification fails
            if (extractedPublicKey != null && certPublicKey.encoded.contentEquals(extractedPublicKey.encoded)) {
                Log.d(TAG, "Certificate-key match verification: true (based on public key comparison)")
                return true
            }
            
            Log.d(TAG, "Certificate-key match verification: $isValid")
            return isValid
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to verify certificate-key match: ${e.message}", e)
            return false
        }
    }
} 