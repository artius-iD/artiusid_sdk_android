/*
 * File: ASN1Encoder.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.security.asn1

import com.artiusid.sdk.security.constants.ASN1Tag
import com.artiusid.sdk.security.constants.RSAOID
import com.artiusid.sdk.security.enums.ASN1TagClass
import com.artiusid.sdk.security.exceptions.CertError
import java.nio.ByteBuffer

/**
 * ASN.1 Encoder for DER encoding
 * Port of iOS ASN1Encoder.swift
 */

// MARK: - ASN.1 Base Interface

/**
 * Interface defining ASN.1 encodable types
 */
interface ASN1Encodable {
    /**
     * Encodes the value according to ASN.1 DER rules
     */
    @Throws(CertError::class)
    fun encode(): ByteArray
}

// MARK: - Core ASN.1 Types

/**
 * Represents a basic ASN.1 type with tag class, number and encoding rules
 */
data class ASN1(
    val tagClass: ASN1TagClass,
    val tagNumber: UByte,
    val constructed: Boolean,
    val data: ByteArray
) : ASN1Encodable {
    
    /**
     * Creates an ASN.1 SEQUENCE
     */
    constructor(data: ByteArray) : this(
        tagClass = ASN1TagClass.UNIVERSAL,
        tagNumber = ASN1Tag.SEQUENCE,
        constructed = true,
        data = data
    )
    
    override fun encode(): ByteArray {
        // Combine tag class and number into single byte
        val tagByte = (tagClass.value or 
                      (if (constructed) 0x20u else 0x00u) or 
                      (tagNumber and 0x1Fu)).toByte()
        
        val encoded = ByteBuffer.allocate(1 + data.size + 10) // Estimate size
        encoded.put(tagByte)
        
        // Add length and content
        val length = DER.encodeLength(data.size)
        encoded.put(length)
        encoded.put(data)
        
        // Return only the used portion
        val result = ByteArray(encoded.position())
        encoded.rewind()
        encoded.get(result)
        return result
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as ASN1
        
        if (tagClass != other.tagClass) return false
        if (tagNumber != other.tagNumber) return false
        if (constructed != other.constructed) return false
        if (!data.contentEquals(other.data)) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        var result = tagClass.hashCode()
        result = 31 * result + tagNumber.hashCode()
        result = 31 * result + constructed.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}

/**
 * Represents an ASN.1 SEQUENCE - ordered collection of values
 */
data class ASNSequence(
    val elements: List<ASN1Encodable>
) : ASN1Encodable {
    
    override fun encode(): ByteArray {
        // Encode all elements and combine
        val content = ByteBuffer.allocate(8192) // Estimate size
        for (element in elements) {
            content.put(element.encode())
        }
        
        // Get the actual content
        val contentArray = ByteArray(content.position())
        content.rewind()
        content.get(contentArray)
        
        return DER.encodeSequence(contentArray)
    }
}

/**
 * Represents an ASN.1 SET - unordered collection of values
 */
data class ASNSet(
    val elements: List<ASN1Encodable>
) : ASN1Encodable {
    
    override fun encode(): ByteArray {
        // Encode all elements and combine
        val content = ByteBuffer.allocate(8192) // Estimate size
        for (element in elements) {
            content.put(element.encode())
        }
        
        // Get the actual content
        val contentArray = ByteArray(content.position())
        content.rewind()
        content.get(contentArray)
        
        return DER.encodeSet(contentArray)
    }
}

/**
 * Represents an ASN.1 INTEGER
 */
data class ASNInteger(
    val value: UInt
) : ASN1Encodable {
    
    override fun encode(): ByteArray {
        return DER.encodeInteger(value)
    }
}

/**
 * Represents an ASN.1 OBJECT IDENTIFIER
 */
data class ObjectIdentifier(
    val oid: String
) : ASN1Encodable {
    
    override fun encode(): ByteArray {
        return DER.encodeObjectIdentifier(oid)
    }
}

/**
 * Represents an ASN.1 UTF8String
 */
data class UTF8String(
    val string: String
) : ASN1Encodable {
    
    override fun encode(): ByteArray {
        val content = string.toByteArray(Charsets.UTF_8)
        return DER.encodeUTF8String(content)
    }
}

/**
 * Represents an ASN.1 PrintableString (subset of ASCII)
 */
data class PrintableString(
    val string: String
) : ASN1Encodable {
    
    override fun encode(): ByteArray {
        // Ensure string contains only PrintableString characters (ASCII subset)
        if (!string.all { it.isAscii() }) {
            throw CertError.EncodingFailed
        }
        val content = string.toByteArray(Charsets.US_ASCII)
        return DER.encodePrintableString(content)
    }
}

/**
 * Represents an ASN.1 NULL value
 */
object ASNNull : ASN1Encodable {
    override fun encode(): ByteArray {
        return DER.encodeNull()
    }
}

/**
 * Represents an ASN.1 BIT STRING
 */
data class BitString(
    val data: ByteArray
) : ASN1Encodable {
    
    override fun encode(): ByteArray {
        return DER.encodeBitString(data)
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as BitString
        
        if (!data.contentEquals(other.data)) return false
        
        return true
    }
    
    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}

// Extension function to check if character is ASCII
private fun Char.isAscii(): Boolean = code <= 127