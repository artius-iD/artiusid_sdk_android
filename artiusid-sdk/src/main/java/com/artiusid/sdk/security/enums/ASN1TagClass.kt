/*
 * File: ASN1TagClass.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.security.enums

/**
 * ASN.1 Tag Class definitions
 * Port of iOS ASN1TagClass.swift
 */
enum class ASN1TagClass(val value: UByte) {
    UNIVERSAL(0x00u),
    APPLICATION(0x40u),
    CONTEXT_SPECIFIC(0x80u),
    PRIVATE_USE(0xC0u)
}