/*
 * File: Constants.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.utils

object Constants {
    object X509NameOID {
        const val COUNTRY_NAME = "2.5.4.6"
        const val STATE_OR_PROVINCE_NAME = "2.5.4.8"
        const val LOCALITY_NAME = "2.5.4.7"
        const val ORGANIZATION_NAME = "2.5.4.10"
        const val ORGANIZATIONAL_UNIT_NAME = "2.5.4.11"
        const val COMMON_NAME = "2.5.4.3"
    }

    object RSAOID {
        const val ENCRYPTION = "1.2.840.113549.1.1.1"
        const val SHA256_WITH_RSA = "1.2.840.113549.1.1.11"
    }

    object ASN1Tag {
        const val SEQUENCE = 0x30.toByte()
        const val SET = 0x31.toByte()
        const val INTEGER = 0x02.toByte()
        const val BIT_STRING = 0x03.toByte()
        const val NULL = 0x05.toByte()
        const val OBJECT_ID = 0x06.toByte()
        const val UTF8_STRING = 0x0C.toByte()
        const val PRINTABLE_STRING = 0x13.toByte()
    }

    object ASN1TagClass {
        const val UNIVERSAL = 0x00.toByte()
        const val APPLICATION = 0x40.toByte()
        const val CONTEXT_SPECIFIC = 0x80.toByte()
        const val PRIVATE_USE = 0xC0.toByte()
    }
} 