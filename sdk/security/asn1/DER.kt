/*
 * File: DER.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.security.asn1

import android.util.Base64
import android.util.Log
import com.artiusid.sdk.security.constants.ASN1Tag
import com.artiusid.sdk.security.constants.RSAOID
import com.artiusid.sdk.security.constants.X509NameOID
import com.artiusid.sdk.security.enums.ASN1TagClass
import com.artiusid.sdk.security.exceptions.CertError
import java.nio.ByteBuffer
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.io.ByteArrayInputStream

/**
 * DER implements Distinguished Encoding Rules (DER) encoding for ASN.1 data structures
 * Port of iOS DER.swift
 */
object DER {
    private const val TAG = "DER"
    
    // MARK: - Core DER Encoding Methods
    
    /**
     * Encode ASN.1 SEQUENCE
     */
    @Throws(CertError::class)
    fun encodeSequence(sequence: ByteArray): ByteArray {
        return encodeTag(ASN1Tag.SEQUENCE, constructed = true, content = sequence)
    }
    
    /**
     * Encode ASN.1 SET
     */
    @Throws(CertError::class)
    fun encodeSet(set: ByteArray): ByteArray {
        return encodeTag(ASN1Tag.SET, constructed = true, content = set)
    }
    
    /**
     * Encode ASN.1 INTEGER
     */
    @Throws(CertError::class)
    fun encodeInteger(integer: UInt): ByteArray {
        var value = integer
        val bytes = mutableListOf<UByte>()
        
        // Handle special case for zero
        if (value == 0u) {
            bytes.add(0x00u)
        } else {
            // Convert integer to bytes
            while (value > 0u) {
                bytes.add(0, (value and 0xFFu).toUByte())
                value = value shr 8
            }
        }
        
        // Add leading zero if high bit is set to ensure positive integer
        if (bytes.isNotEmpty() && (bytes.first().toInt() and 0x80) != 0) {
            bytes.add(0, 0x00u)
        }
        
        return encodeTag(ASN1Tag.INTEGER, content = bytes.map { it.toByte() }.toByteArray())
    }
    
    /**
     * Encode ASN.1 OBJECT IDENTIFIER
     */
    @Throws(CertError::class)
    fun encodeObjectIdentifier(objectIdentifier: String): ByteArray {
        val oidComponents = objectIdentifier.split(".").mapNotNull { it.toUIntOrNull() }
        if (oidComponents.size < 2) {
            throw CertError.EncodingFailed
        }
        
        // First byte encodes first two OID components
        val firstByte = (40u * oidComponents[0] + oidComponents[1]).toUByte()
        val encoded = mutableListOf<UByte>(firstByte)
        
        // Encode remaining components using base 128 with continuation bits
        for (component in oidComponents.drop(2)) {
            val subIdentifiers = mutableListOf<UByte>()
            var value = component
            do {
                subIdentifiers.add(0, (value and 0x7Fu).toUByte())
                value = value shr 7
            } while (value > 0u)
            
            // Set continuation bits for all but last byte
            for (i in 0 until subIdentifiers.size - 1) {
                subIdentifiers[i] = (subIdentifiers[i].toUInt() or 0x80u).toUByte()
            }
            encoded.addAll(subIdentifiers)
        }
        
        return encodeTag(ASN1Tag.OBJECT_ID, content = encoded.map { it.toByte() }.toByteArray())
    }
    
    // MARK: - String Encoding Methods
    
    /**
     * Encode ASN.1 UTF8String
     */
    @Throws(CertError::class)
    fun encodeUTF8String(content: ByteArray): ByteArray {
        return encodeTag(ASN1Tag.UTF8_STRING, content = content)
    }
    
    /**
     * Encode ASN.1 PrintableString (ASCII subset)
     */
    @Throws(CertError::class)
    fun encodePrintableString(content: ByteArray): ByteArray {
        return encodeTag(ASN1Tag.PRINTABLE_STRING, content = content)
    }
    
    /**
     * Encode ASN.1 NULL value
     */
    @Throws(CertError::class)
    fun encodeNull(): ByteArray {
        return encodeTag(ASN1Tag.NULL, content = ByteArray(0))
    }
    
    /**
     * Encode ASN.1 BIT STRING
     */
    @Throws(CertError::class)
    fun encodeBitString(bitString: ByteArray): ByteArray {
        val encodedData: ByteArray = when {
            bitString.isEmpty() -> byteArrayOf(0x00) // Empty bit string
            bitString[0] == 0x00.toByte() -> bitString // Already has unused bits byte
            bitString[0] > 0x00.toByte() && bitString[0] < 0x08.toByte() -> bitString // Has valid unused bits count
            else -> byteArrayOf(0x00) + bitString // Add unused bits byte
        }
        
        return encodeTag(ASN1Tag.BIT_STRING, content = encodedData)
    }
    
    // MARK: - Private Helper Methods
    
    /**
     * Encode DER tag with content
     */
    @Throws(CertError::class)
    private fun encodeTag(tag: UByte, constructed: Boolean = false, content: ByteArray): ByteArray {
        var tagByte = tag.toUInt()
        if (constructed) {
            tagByte = tagByte or 0x20u // Set constructed bit
        }
        
        val encoded = ByteBuffer.allocate(1 + content.size + 10) // Estimate size
        encoded.put(tagByte.toByte())
        val length = encodeLength(content.size)
        encoded.put(length)
        encoded.put(content)
        
        // Return only the used portion
        val result = ByteArray(encoded.position())
        encoded.rewind()
        encoded.get(result)
        return result
    }
    
    /**
     * Encode DER length field
     */
    @Throws(CertError::class)
    fun encodeLength(length: Int): ByteArray {
        if (length < 128) {
            return byteArrayOf(length.toByte()) // Short form
        }
        
        // Long form encoding
        var len = length
        val bytes = mutableListOf<Byte>()
        while (len > 0) {
            bytes.add(0, (len and 0xFF).toByte())
            len = len shr 8
        }
        
        if (bytes.size > 126) {
            throw CertError.EncodingFailed
        }
        
        return byteArrayOf((0x80 or bytes.size).toByte()) + bytes.toByteArray()
    }
    
    // MARK: - PEM Encoding
    
    /**
     * Encodes data as PEM format with specified type
     */
    fun encodePEM(base64: String, type: String): String {
        // Split into exactly 64-char chunks for PEM format requirements
        val chunks = mutableListOf<String>()
        var index = 0
        
        while (index < base64.length) {
            val endIndex = minOf(index + 64, base64.length)
            chunks.add(base64.substring(index, endIndex))
            index += 64
        }
        
        // Format according to AWS PCA requirements
        return listOf(
            "-----BEGIN $type-----",
            chunks.joinToString("\n"),
            "-----END $type-----"
        ).joinToString("\n")
    }
    
    // MARK: - CSR Generation
    
    /**
     * Generate Certificate Signing Request (CSR)
     */
    @Throws(CertError::class)
    fun generateCSR(subject: Map<String, String>, publicKey: PublicKey, privateKey: PrivateKey): ByteArray {
        Log.d(TAG, "Generating CSR with subject: $subject")
        
        // Create certification request info sequence
        val certRequestInfo = ASNSequence(listOf(
            ASNInteger(0u), // Version
            encodeX509Name(subject),
            encodePublicKeyInfo(publicKey),
            ASN1( // Attributes [0] IMPLICIT
                tagClass = ASN1TagClass.CONTEXT_SPECIFIC,
                tagNumber = 0u,
                constructed = true,
                data = ByteArray(0)
            )
        ))
        
        // Sign the CSR
        val csrBytes = certRequestInfo.encode()
        val signature = encodeSignature(privateKey, csrBytes)
        
        // Construct and return final CSR structure
        return ASNSequence(listOf(
            certRequestInfo,
            ASNSequence(listOf( // SignatureAlgorithm
                ObjectIdentifier(RSAOID.SHA256_WITH_RSA),
                ASNNull
            )),
            BitString(signature) // Signature value
        )).encode()
    }
    
    // MARK: - Signature Operations
    
    /**
     * Create PKCS#1 SHA256-RSA signature
     */
    @Throws(CertError::class)
    private fun encodeSignature(privateKey: PrivateKey, data: ByteArray): ByteArray {
        return try {
            val signature = Signature.getInstance("SHA256withRSA")
            signature.initSign(privateKey)
            signature.update(data)
            signature.sign()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create signature", e)
            throw CertError.EncodingFailed
        }
    }
    
    // MARK: - X.509 Name Handling
    
    /**
     * Encode X.509 name
     */
    @Throws(CertError::class)
    private fun encodeX509Name(subject: Map<String, String>): ASNSequence {
        if (subject.isEmpty()) {
            throw CertError.EncodingFailed
        }
        
        // Define field order for consistent encoding
        val fieldOrder = listOf(
            X509NameOID.COUNTRY_NAME,
            X509NameOID.STATE_OR_PROVINCE_NAME,
            X509NameOID.LOCALITY_NAME,
            X509NameOID.ORGANIZATION_NAME,
            X509NameOID.ORGANIZATIONAL_UNIT_NAME,
            X509NameOID.COMMON_NAME
        )
        
        // Build RelativeDistinguishedNames (RDNs)
        val rdns = mutableListOf<ASN1Encodable>()
        for (oid in fieldOrder) {
            val value = subject[oid] ?: continue
            
            // Country name must use PrintableString, others use UTF8String
            val attr: ASN1Encodable = if (oid == X509NameOID.COUNTRY_NAME) {
                ASNSequence(listOf(
                    ObjectIdentifier(oid),
                    PrintableString(value)
                ))
            } else {
                ASNSequence(listOf(
                    ObjectIdentifier(oid),
                    UTF8String(value)
                ))
            }
            
            rdns.add(ASNSet(listOf(attr)))
        }
        
        return ASNSequence(rdns)
    }
    
    /**
     * Encodes SubjectPublicKeyInfo ASN.1 structure
     */
    @Throws(CertError::class)
    private fun encodePublicKeyInfo(publicKey: PublicKey): ASNSequence {
        val publicKeyData = publicKey.encoded
        
        // Create algorithm identifier
        val algorithm = ASNSequence(listOf(
            ObjectIdentifier(RSAOID.ENCRYPTION),
            ASNNull
        ))
        
        // Construct SubjectPublicKeyInfo
        return ASNSequence(listOf(
            algorithm,
            BitString(publicKeyData)
        ))
    }
    
    /**
     * Convert PEM formatted certificate to DER
     */
    @Throws(CertError::class)
    fun convertPEMToDER(pemData: ByteArray): ByteArray {
        val pemString = String(pemData, Charsets.UTF_8)
        return convertPEMToDER(pemString)
    }
    
    /**
     * Convert PEM formatted certificate to DER
     */
    @Throws(CertError::class)
    fun convertPEMToDER(pemString: String): ByteArray {
        val lines = pemString.split("\n")
        var base64String = ""
        var certFound = false
        
        for (line in lines) {
            when {
                line.contains("-----BEGIN CERTIFICATE-----") -> {
                    certFound = true
                    continue
                }
                line.contains("-----END CERTIFICATE-----") -> break
                certFound -> base64String += line
            }
        }
        
        return try {
            Base64.decode(base64String.trim(), Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode base64 certificate", e)
            throw CertError.InvalidCertificate
        }
    }
    
    /**
     * Validate certificate data
     */
    fun validateCertificate(certificateData: ByteArray): Boolean {
        return try {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val certificate = certificateFactory.generateCertificate(
                ByteArrayInputStream(certificateData)
            ) as X509Certificate
            certificate.checkValidity()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Certificate validation failed", e)
            false
        }
    }
}