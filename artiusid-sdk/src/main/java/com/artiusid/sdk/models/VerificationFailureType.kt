package com.artiusid.sdk.models

/**
 * Types of verification failures that can occur during the verification process
 */
enum class VerificationFailureType {
    FAILED_FACE_MATCH,
    FAILED_DOCUMENT_QUALITY,
    FAILED_NFC_VERIFICATION,
    FAILED_GENERAL,
    PROCESSING_ERROR;
    
    fun getTitle(): String {
        return when (this) {
            FAILED_FACE_MATCH -> "Face Verification Failed"
            FAILED_DOCUMENT_QUALITY -> "Document Quality Issue"
            FAILED_NFC_VERIFICATION -> "NFC Reading Failed"
            FAILED_GENERAL -> "Verification Failed"
            PROCESSING_ERROR -> "Processing Error"
        }
    }
    
    fun getMessage(): String {
        return when (this) {
            FAILED_FACE_MATCH -> "The face in your selfie doesn't match the document photo. Please try again."
            FAILED_DOCUMENT_QUALITY -> "The document image quality is too low. Please retake the photo."
            FAILED_NFC_VERIFICATION -> "Unable to read the NFC chip. Please try again."
            FAILED_GENERAL -> "Verification failed. Please try again."
            PROCESSING_ERROR -> "An error occurred during processing. Please try again."
        }
    }
    
    fun getButtonText(): String {
        return when (this) {
            FAILED_FACE_MATCH -> "Retake Selfie"
            FAILED_DOCUMENT_QUALITY -> "Retake Document"
            FAILED_NFC_VERIFICATION -> "Try NFC Again"
            FAILED_GENERAL -> "Try Again"
            PROCESSING_ERROR -> "Retry"
        }
    }
    
    companion object {
        val GENERAL = FAILED_GENERAL
        val FACE = FAILED_FACE_MATCH
        val PASSPORT = FAILED_NFC_VERIFICATION
        val STATE_ID_FRONT = FAILED_DOCUMENT_QUALITY
        val STATE_ID_BACK = FAILED_DOCUMENT_QUALITY
    }
}
