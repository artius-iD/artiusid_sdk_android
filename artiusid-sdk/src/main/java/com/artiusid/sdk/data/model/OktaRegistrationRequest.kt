/*
 * File: OktaRegistrationRequest.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 *
 * Request structure for Okta user profile registration (iOS parity: OktaRegistrationRequest).
 * Maps to the OktaRegistrationFunction Lambda parameters.
 */

package com.artiusid.sdk.data.model

/**
 * Request body for Okta registration API. All fields sent as strings to match Lambda expectations.
 */
data class OktaRegistrationRequest(
    val userId: String? = null,
    val userEmail: String? = null,
    val phoneNumber: String? = null,
    val memberId: String
) {
    /** Convert to map for API request body (iOS toEncodableBody). */
    fun toEncodableBody(): Map<String, String> {
        val body = mutableMapOf<String, String>("memberId" to memberId)
        userId?.takeIf { it.isNotBlank() }?.let { body["userId"] = it }
        userEmail?.takeIf { it.isNotBlank() }?.let { body["userEmail"] = it }
        phoneNumber?.takeIf { it.isNotBlank() }?.let { body["phoneNumber"] = it }
        return body
    }
}
