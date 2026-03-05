/*
 * File: DocumentType.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.document

/** Document type (iOS parity: photoID, passport with displayName). */
enum class DocumentType(val displayName: String) {
    ID_CARD("Photo ID"),
    PASSPORT("Passport")
} 