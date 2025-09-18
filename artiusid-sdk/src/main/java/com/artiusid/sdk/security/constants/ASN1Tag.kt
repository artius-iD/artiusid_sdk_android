/*
 * File: ASN1Tag.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.security.constants

/**
 * ASN.1 Tag constants
 * Port of iOS ASN1Tag.swift
 */
object ASN1Tag {
    const val SEQUENCE: UByte = 0x30u
    const val SET: UByte = 0x31u
    const val INTEGER: UByte = 0x02u
    const val BIT_STRING: UByte = 0x03u
    const val NULL: UByte = 0x05u
    const val OBJECT_ID: UByte = 0x06u
    const val UTF8_STRING: UByte = 0x0Cu
    const val PRINTABLE_STRING: UByte = 0x13u // For country name encoding
}