package com.artiusid.sdk.models

/**
 * Verification failure types - EXACT STANDALONE MATCH
 */
enum class VerificationFailureType {
    GENERAL,
    FAILED_FACE_MATCH,
    FAILED_DOCUMENT_QUALITY,
    FAILED_NFC_VERIFICATION,
    FAILED_GENERAL,
    PROCESSING_ERROR,
    PROCESSING_FAILED,
    NETWORK_ERROR,
    CAMERA_ERROR,
    FACE_NOT_DETECTED,
    LIVENESS_FAILED,
    DOCUMENT_NOT_DETECTED,
    MRZ_READ_FAILED,
    NFC_ERROR,
    API_ERROR,
    TIMEOUT,
    USER_CANCELLED,
    UNKNOWN_ERROR,
    
    // Additional failure types referenced in UI
    FACE,
    PASSPORT,
    STATE_ID_FRONT,
    STATE_ID_BACK;
    
    fun getTitle(): String {
        return when (this) {
            GENERAL -> "Verification Failed"
            FAILED_FACE_MATCH -> "Face Match Failed"
            FAILED_DOCUMENT_QUALITY -> "Document Quality Issue"
            FAILED_NFC_VERIFICATION -> "NFC Verification Failed"
            FAILED_GENERAL -> "General Failure"
            PROCESSING_ERROR -> "Processing Error"
            PROCESSING_FAILED -> "Processing Failed"
            NETWORK_ERROR -> "Network Error"
            CAMERA_ERROR -> "Camera Error"
            FACE_NOT_DETECTED -> "Face Not Detected"
            LIVENESS_FAILED -> "Liveness Check Failed"
            DOCUMENT_NOT_DETECTED -> "Document Not Detected"
            MRZ_READ_FAILED -> "MRZ Read Failed"
            NFC_ERROR -> "NFC Error"
            API_ERROR -> "API Error"
            TIMEOUT -> "Timeout"
            USER_CANCELLED -> "Cancelled"
            UNKNOWN_ERROR -> "Unknown Error"
            FACE -> "Face Verification Failed"
            PASSPORT -> "Passport Verification Failed"
            STATE_ID_FRONT -> "State ID Front Failed"
            STATE_ID_BACK -> "State ID Back Failed"
        }
    }
    
    fun getMessage(): String {
        return when (this) {
            GENERAL -> "The verification process encountered an error. Please try again."
            FAILED_FACE_MATCH -> "The face in your document doesn't match your selfie. Please try again."
            FAILED_DOCUMENT_QUALITY -> "The document image quality is too low. Please ensure good lighting and try again."
            FAILED_NFC_VERIFICATION -> "Unable to verify the NFC chip in your document. Please try again."
            FAILED_GENERAL -> "Verification failed. Please check your documents and try again."
            PROCESSING_ERROR -> "An error occurred while processing your verification. Please try again."
            PROCESSING_FAILED -> "Processing failed. Please try again with better lighting."
            NETWORK_ERROR -> "Network connection error. Please check your internet connection and try again."
            CAMERA_ERROR -> "Camera access error. Please check camera permissions and try again."
            FACE_NOT_DETECTED -> "No face detected in the image. Please ensure your face is clearly visible and try again."
            LIVENESS_FAILED -> "Liveness check failed. Please follow the on-screen instructions and try again."
            DOCUMENT_NOT_DETECTED -> "Document not detected. Please ensure the document is clearly visible and try again."
            MRZ_READ_FAILED -> "Unable to read the machine readable zone. Please ensure the document is clearly visible."
            NFC_ERROR -> "NFC reading error. Please ensure NFC is enabled and try again."
            API_ERROR -> "Server error. Please try again later."
            TIMEOUT -> "The verification process timed out. Please try again."
            USER_CANCELLED -> "Verification was cancelled by user."
            UNKNOWN_ERROR -> "An unknown error occurred. Please try again."
            FACE -> "Face verification failed. Please try again with better lighting."
            PASSPORT -> "Passport verification failed. Please ensure the document is clearly visible."
            STATE_ID_FRONT -> "State ID front verification failed. Please try again."
            STATE_ID_BACK -> "State ID back verification failed. Please try again."
        }
    }
    
    fun getButtonText(): String {
        return when (this) {
            USER_CANCELLED -> "Continue"
            TIMEOUT -> "Try Again"
            else -> "Retry Verification"
        }
    }
}