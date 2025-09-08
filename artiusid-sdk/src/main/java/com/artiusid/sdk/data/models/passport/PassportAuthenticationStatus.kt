package com.artiusid.sdk.data.models.passport

/**
 * Passport NFC authentication status - EXACT STANDALONE MATCH
 */
enum class PassportAuthenticationStatus {
    NOT_AUTHENTICATED,
    AUTHENTICATED,
    FAILED_BAC,
    FAILED_PACE,
    FAILED_ACTIVE_AUTHENTICATION,
    FAILED_CHIP_AUTHENTICATION,
    INVALID_CERTIFICATE,
    EXPIRED_CERTIFICATE,
    REVOKED_CERTIFICATE,
    UNKNOWN_ERROR,
    SUCCESS,
    FAILED,
    NOT_DONE
}
