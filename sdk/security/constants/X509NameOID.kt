/*
 * File: X509NameOID.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.security.constants

/**
 * X.509 Name Object Identifiers used in certificate subjects
 * Port of iOS X509NameOID constants
 */
object X509NameOID {
    const val COUNTRY_NAME = "2.5.4.6"
    const val STATE_OR_PROVINCE_NAME = "2.5.4.8"
    const val LOCALITY_NAME = "2.5.4.7"
    const val ORGANIZATION_NAME = "2.5.4.10"
    const val ORGANIZATIONAL_UNIT_NAME = "2.5.4.11"
    const val COMMON_NAME = "2.5.4.3"
}