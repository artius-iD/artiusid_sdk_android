/*
 * File: VerificationResults.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.data.model

import android.content.Context
import com.artiusid.sdk.R

enum class VerificationResults {
    FAILED,
    OCR_ERROR,
    PRD417_ERROR,
    PRE_PROCESS_ERROR,
    MRZ_OCR_ERROR,
    FACE_IMAGE_VALIDATION_ERROR,
    DOCUMENT_VALIDATION_ERROR,
    SUCCESS;

    fun getLocalizedDescription(context: Context): String {
        return when (this) {
            DOCUMENT_VALIDATION_ERROR -> context.getString(R.string.error_document_validation)
            FACE_IMAGE_VALIDATION_ERROR -> context.getString(R.string.error_face_validation)
            FAILED -> context.getString(R.string.error_general)
            OCR_ERROR -> context.getString(R.string.error_ocr)
            PRD417_ERROR -> context.getString(R.string.error_barcode)
            PRE_PROCESS_ERROR -> context.getString(R.string.error_preprocessing)
            MRZ_OCR_ERROR -> context.getString(R.string.error_mrz)
            SUCCESS -> context.getString(R.string.success)
        }
    }

    // Backward compatibility property (deprecated)
    @Deprecated("Use getLocalizedDescription(context) instead", ReplaceWith("getLocalizedDescription(context)"))
    val localizedDescription: String
        get() = when (this) {
            DOCUMENT_VALIDATION_ERROR -> "Document image validation error, please select document type and re-capture document data"
            FACE_IMAGE_VALIDATION_ERROR -> "Face image capture validation error, please re-capture face image"
            FAILED -> "General error has occurred, please GO BACK and try again (400)"
            OCR_ERROR -> "Unable to capture data from the front of your government document."
            PRD417_ERROR -> "Unable to decode Barcode from the image of the document."
            PRE_PROCESS_ERROR -> "Unable to process your document image. Please rescan your passport with better lighting and focus."
            MRZ_OCR_ERROR -> "Unable to capture Machine-Readable Zone (MRZ) data from your document image"
            SUCCESS -> "Success"
        }

    val localizedErrorCode: Int
        get() = when (this) {
            DOCUMENT_VALIDATION_ERROR -> 605
            FACE_IMAGE_VALIDATION_ERROR -> 604
            PRD417_ERROR -> 603
            OCR_ERROR -> 602
            MRZ_OCR_ERROR -> 601
            PRE_PROCESS_ERROR -> 600
            FAILED -> 400
            SUCCESS -> 200
        }

    companion object {
        fun fromHttpStatusCode(statusCode: Int): VerificationResults {
            return when (statusCode) {
                600 -> PRE_PROCESS_ERROR
                601 -> MRZ_OCR_ERROR
                602 -> OCR_ERROR
                603 -> PRD417_ERROR
                604 -> FACE_IMAGE_VALIDATION_ERROR
                605 -> DOCUMENT_VALIDATION_ERROR
                400 -> FAILED
                200 -> SUCCESS
                else -> FAILED
            }
        }
    }
} 