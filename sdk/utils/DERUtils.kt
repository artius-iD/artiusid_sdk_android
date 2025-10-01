/*
 * File: DERUtils.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

import java.nio.charset.StandardCharsets
import java.security.PrivateKey
import java.security.Signature
import java.util.Base64

object DERUtils {
    // ASN.1 tag constants
    private const val SEQUENCE = 0x30
    private const val SET = 0x31
    private const val INTEGER = 0x02
    private const val OBJECT_ID = 0x06
    private const val UTF8_STRING = 0x0C
    private const val PRINTABLE_STRING = 0x13
    private const val NULL = 0x05
    private const val BIT_STRING = 0x03
    private const val CONTEXT_SPECIFIC = 0xA0

    // RSA Object Identifiers (matching iOS exactly)
    private const val RSA_ENCRYPTION = "1.2.840.113549.1.1.1"
    private const val SHA256_WITH_RSA = "1.2.840.113549.1.1.11"

    // X.509 Name OIDs (matching iOS exactly)
    private const val COUNTRY_NAME = "2.5.4.6"
    private const val STATE_OR_PROVINCE_NAME = "2.5.4.8"
    private const val LOCALITY_NAME = "2.5.4.7"
    private const val ORGANIZATION_NAME = "2.5.4.10"
    private const val ORGANIZATIONAL_UNIT_NAME = "2.5.4.11"
    private const val COMMON_NAME = "2.5.4.3"

    // PEM encoding
    fun encodePEM(base64: String, type: String): String {
        val chunks = base64.chunked(64)
        return buildString {
            appendLine("-----BEGIN $type-----")
            chunks.forEach { chunk ->
                appendLine(chunk)
            }
            appendLine("-----END $type-----")
        }
    }

    // Generate CSR and return DER bytes
    fun generateCSR(subject: Map<String, String>, publicKey: java.security.PublicKey, privateKey: java.security.PrivateKey): ByteArray {
        // Create certification request info sequence (matching iOS structure)
        val certRequestInfo = createCertificationRequestInfo(subject, publicKey)
        val certRequestInfoBytes = certRequestInfo

        // Sign the CSR using SHA256-RSA (matching iOS)
        val signature = createSignature(privateKey, certRequestInfoBytes)

        // Construct final CSR structure (matching iOS exactly)
        val csrBytes = createCSRStructure(certRequestInfoBytes, signature)
        
        // Convert to PEM format
        val base64 = Base64.getEncoder().encodeToString(csrBytes)
        return csrBytes
    }

    // Generate CSR and return PEM string
    fun generateCSRPEM(subject: Map<String, String>, publicKey: java.security.PublicKey, privateKey: java.security.PrivateKey): String {
        val der = generateCSR(subject, publicKey, privateKey)
        val base64Der = java.util.Base64.getEncoder().encodeToString(der)
        return encodePEM(base64Der, "CERTIFICATE REQUEST")
    }

    private fun createCertificationRequestInfo(subject: Map<String, String>, publicKey: java.security.PublicKey): ByteArray {
        val version = encodeInteger(0) // Version 0
        val subjectName = encodeX509Name(subject)
        val publicKeyInfo = encodePublicKeyInfo(publicKey)
        val attributes = encodeAttributes() // Empty attributes [0] IMPLICIT

        // Combine all elements into SEQUENCE
        val content = version + subjectName + publicKeyInfo + attributes
        return encodeTag(SEQUENCE, true, content)
    }

    private fun encodeX509Name(subject: Map<String, String>): ByteArray {
        // Map string keys to OIDs for consistent encoding (matching iOS exactly)
        val keyToOid = mapOf(
            "C" to COUNTRY_NAME,
            "ST" to STATE_OR_PROVINCE_NAME,
            "L" to LOCALITY_NAME,
            "O" to ORGANIZATION_NAME,
            "OU" to ORGANIZATIONAL_UNIT_NAME,
            "CN" to COMMON_NAME
        )
        
        // Define field order for consistent encoding (matching iOS exactly)
        val fieldOrder = listOf(
            "C" to COUNTRY_NAME,
            "ST" to STATE_OR_PROVINCE_NAME,
            "L" to LOCALITY_NAME,
            "O" to ORGANIZATION_NAME,
            "OU" to ORGANIZATIONAL_UNIT_NAME,
            "CN" to COMMON_NAME
        )

        val rdns = mutableListOf<ByteArray>()
        
        for ((key, oid) in fieldOrder) {
            val value = subject[key] ?: continue
            
            // Create AttributeTypeAndValue SEQUENCE (matching iOS exactly)
            val attrTypeAndValue = if (oid == COUNTRY_NAME) {
                // Country name must use PrintableString (matching iOS)
                encodeObjectIdentifier(oid) + encodePrintableString(value)
            } else {
                // Others use UTF8String (matching iOS)
                encodeObjectIdentifier(oid) + encodeUTF8String(value)
            }
            val attrTypeAndValueSequence = encodeTag(SEQUENCE, true, attrTypeAndValue)
            
            // Wrap in SET (matching iOS: SET with single AttributeTypeAndValue)
            val set = encodeTag(SET, true, attrTypeAndValueSequence)
            rdns.add(set)
        }

        // Combine all RDNs into SEQUENCE
        val content = if (rdns.isEmpty()) {
            ByteArray(0)
        } else {
            rdns.reduce { acc, rdn -> acc + rdn }
        }
        return encodeTag(SEQUENCE, true, content)
    }

    private fun encodePublicKeyInfo(publicKey: java.security.PublicKey): ByteArray {
        // Algorithm identifier
        val algorithm = encodeObjectIdentifier(RSA_ENCRYPTION) + encodeNull()
        val algorithmSequence = encodeTag(SEQUENCE, true, algorithm)

        // Public key bit string - use the raw encoded key data (matching iOS SecKeyCopyExternalRepresentation)
        val publicKeyBytes = publicKey.encoded
        // Remove the ASN.1 wrapper that Java adds and use just the raw key data
        val rawKeyData = extractRawPublicKeyData(publicKeyBytes)
        val bitString = encodeBitString(rawKeyData)

        // Combine into SEQUENCE
        val content = algorithmSequence + bitString
        return encodeTag(SEQUENCE, true, content)
    }

    private fun extractRawPublicKeyData(encodedKey: ByteArray): ByteArray {
        // Java's PublicKey.encoded returns a SubjectPublicKeyInfo structure
        // We need to extract just the raw key data (the bit string content)
        // This matches what iOS SecKeyCopyExternalRepresentation returns
        
        // Skip the ASN.1 wrapper and get to the actual key data
        // The structure is: SEQUENCE -> SEQUENCE (algorithm) -> BITSTRING (key data)
        var offset = 0
        
        // Skip outer SEQUENCE tag and length
        if (encodedKey[offset] == SEQUENCE.toByte()) {
            offset += 1
            val length = encodedKey[offset].toInt() and 0xFF
            offset += 1
            if (length and 0x80 != 0) {
                val numLengthBytes = length and 0x7F
                offset += numLengthBytes
            }
        }
        
        // Skip algorithm SEQUENCE
        if (encodedKey[offset] == SEQUENCE.toByte()) {
            offset += 1
            val length = encodedKey[offset].toInt() and 0xFF
            offset += 1
            if (length and 0x80 != 0) {
                val numLengthBytes = length and 0x7F
                offset += numLengthBytes
            }
            offset += length
        }
        
        // Now we're at the BITSTRING, extract the actual key data
        if (encodedKey[offset] == BIT_STRING.toByte()) {
            offset += 1
            val length = encodedKey[offset].toInt() and 0xFF
            offset += 1
            if (length and 0x80 != 0) {
                val numLengthBytes = length and 0x7F
                offset += numLengthBytes
            }
            // Skip the unused bits byte
            offset += 1
            // Return the actual key data
            return encodedKey.copyOfRange(offset, encodedKey.size)
        }
        
        // Fallback: return the original encoded key
        return encodedKey
    }

    private fun encodeAttributes(): ByteArray {
        // Empty attributes [0] IMPLICIT (matching iOS)
        return encodeTag(CONTEXT_SPECIFIC, true, ByteArray(0))
    }

    private fun createCSRStructure(certRequestInfo: ByteArray, signature: ByteArray): ByteArray {
        // Signature algorithm identifier - use exact same OID as iOS
        val signatureAlgorithm = encodeObjectIdentifier(SHA256_WITH_RSA) + encodeNull()
        val signatureAlgorithmSequence = encodeTag(SEQUENCE, true, signatureAlgorithm)

        // Signature value as bit string - ensure proper encoding
        val signatureBitString = encodeBitString(signature)

        // Combine into final SEQUENCE - this is the exact structure iOS uses
        val content = certRequestInfo + signatureAlgorithmSequence + signatureBitString
        return encodeTag(SEQUENCE, true, content)
    }

    private fun createSignature(privateKey: java.security.PrivateKey, data: ByteArray): ByteArray {
        // Use SHA256withRSA to match iOS SecKeyCreateSignature with .rsaSignatureMessagePKCS1v15SHA256
        val signature = java.security.Signature.getInstance("SHA256withRSA")
        signature.initSign(privateKey)
        signature.update(data)
        return signature.sign()
    }

    // ASN.1 encoding methods
    private fun encodeTag(tag: Int, constructed: Boolean, content: ByteArray): ByteArray {
        val tagByte = if (constructed) tag or 0x20 else tag
        val length = encodeLength(content.size)
        return byteArrayOf(tagByte.toByte()) + length + content
    }

    private fun encodeLength(length: Int): ByteArray {
        return when {
            length < 128 -> {
                // Short form - single byte
                byteArrayOf(length.toByte())
            }
            else -> {
                // Long form - first byte indicates number of length bytes
                val lengthBytes = mutableListOf<Byte>()
                var tempLength = length
                while (tempLength > 0) {
                    lengthBytes.add(0, (tempLength and 0xFF).toByte())
                    tempLength = tempLength shr 8
                }
                // Set the high bit to indicate long form
                val firstByte = (0x80 or lengthBytes.size).toByte()
                byteArrayOf(firstByte) + lengthBytes.toByteArray()
            }
        }
    }

    private fun encodeInteger(value: Int): ByteArray {
        val bytes = mutableListOf<Byte>()
        var v = value
        
        if (v == 0) {
            bytes.add(0)
        } else {
            while (v > 0) {
                bytes.add(0, (v and 0xFF).toByte())
                v = v shr 8
            }
        }
        
        // Add leading zero if high bit is set
        if (bytes.isNotEmpty() && bytes[0].toInt() and 0x80 != 0) {
            bytes.add(0, 0)
        }
        
        return encodeTag(INTEGER, false, bytes.toByteArray())
    }

    private fun encodeObjectIdentifier(oid: String): ByteArray {
        val components = oid.split(".").map { it.toInt() }
        require(components.size >= 2) { "Invalid OID" }
        
        val bytes = mutableListOf<Byte>()
        
        // First byte encodes first two components
        val firstByte = (40 * components[0] + components[1]).toByte()
        bytes.add(firstByte)
        
        // Encode remaining components using base 128
        for (i in 2 until components.size) {
            val component = components[i]
            val subIdentifiers = mutableListOf<Byte>()
            var value = component
            
            do {
                subIdentifiers.add(0, (value and 0x7F).toByte())
                value = value shr 7
            } while (value > 0)
            
            // Set continuation bits for all but last byte
            for (j in 0 until subIdentifiers.size - 1) {
                subIdentifiers[j] = (subIdentifiers[j].toInt() or 0x80).toByte()
            }
            
            bytes.addAll(subIdentifiers)
        }
        
        return encodeTag(OBJECT_ID, false, bytes.toByteArray())
    }

    private fun encodeUTF8String(value: String): ByteArray {
        val bytes = value.toByteArray(StandardCharsets.UTF_8)
        return encodeTag(UTF8_STRING, false, bytes)
    }

    private fun encodePrintableString(value: String): ByteArray {
        val bytes = value.toByteArray(StandardCharsets.US_ASCII)
        return encodeTag(PRINTABLE_STRING, false, bytes)
    }

    private fun encodeNull(): ByteArray {
        return encodeTag(NULL, false, ByteArray(0))
    }

    private fun encodeBitString(data: ByteArray): ByteArray {
        // Ensure proper bit string encoding - iOS might handle this differently
        val bitString = ByteArray(data.size + 1)
        bitString[0] = 0 // Unused bits count (0 for byte-aligned data)
        System.arraycopy(data, 0, bitString, 1, data.size)
        return encodeTag(BIT_STRING, false, bitString)
    }
} 