package com.artiusid.sdk.models

/**
 * AAMVA (American Association of Motor Vehicle Administrators) data - EXACT STANDALONE MATCH
 */
data class AAMVAData(
    val firstName: String? = null,
    val lastName: String? = null,
    val middleName: String? = null,
    val dateOfBirth: String? = null,
    val licenseNumber: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String? = null,
    val expirationDate: String? = null,
    val issueDate: String? = null,
    val sex: String? = null,
    val height: String? = null,
    val weight: String? = null,
    val eyeColor: String? = null,
    val hairColor: String? = null,
    val restrictions: String? = null,
    val endorsements: String? = null,
    val vehicleClass: String? = null,
    val rawData: Map<String, String> = emptyMap()
)
