/*
 * File: RSAOID.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.security.constants

/**
 * RSA Object Identifiers used in X.509 certificates
 * Port of iOS RSAOID struct
 */
object RSAOID {
    // RSA encryption algorithm identifier
    const val ENCRYPTION = "1.2.840.113549.1.1.1"
    // SHA256 with RSA signature algorithm identifier
    const val SHA256_WITH_RSA = "1.2.840.113549.1.1.11"
}