package com.artiusid.sdk.models

/**
 * Document types supported by the SDK - EXACT STANDALONE MATCH
 */
enum class DocumentType(val value: Int) {
    DRIVERS_LICENSE(1),
    STATE_ID(2),
    PASSPORT(3),
    MILITARY_ID(4),
    TRIBAL_ID(5),
    ID_CARD(6),
    VISA(7),
    OTHER(99);
    
    companion object {
        fun fromValue(value: Int): DocumentType {
            return values().find { it.value == value } ?: OTHER
        }
    }
}