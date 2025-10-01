/*
 * File: CertError.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.security.exceptions

/**
 * Certificate-related error types
 * Port of iOS CertError enum
 */
sealed class CertError : Exception() {
    object KeyGenerationFailed : CertError()
    object InvalidCertificate : CertError()
    object RegistrationFailed : CertError()
    object StorageError : CertError()
    object EncodingFailed : CertError()
    object ValidationFailed : CertError()
    
    override val message: String
        get() = when (this) {
            is KeyGenerationFailed -> "Failed to generate cryptographic key"
            is InvalidCertificate -> "Invalid certificate format or data"
            is RegistrationFailed -> "Failed to register certificate with server"
            is StorageError -> "Failed to store certificate or key"
            is EncodingFailed -> "Failed to encode ASN.1 data"
            is ValidationFailed -> "Certificate validation failed"
        }
}