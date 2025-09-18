/*
 * File: VerificationResult.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.presentation.screens.document

import android.graphics.Bitmap

data class VerificationResult(
    val documentType: DocumentType,
    val documentImage: Bitmap,
    val isVerified: Boolean,
    val extractedInfo: Map<String, String>? = null,
    val errorMessage: String? = null
) 