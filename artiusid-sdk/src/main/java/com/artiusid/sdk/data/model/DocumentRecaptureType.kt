/**
 * Author: Todd Bryant
 * Company: artius.iD
 * 
 * Android equivalent of iOS DocumentRecaptureType enum
 * Represents different types of document recapture scenarios
 */
package com.artiusid.sdk.data.model

/**
 * Represents different reasons why a document needs to be recaptured
 * Maps to various API error codes and validation failures
 */
enum class DocumentRecaptureType(
    val title: String,
    val message: String,
    val actionText: String = "Recapture Document"
) {
    PASSPORT_MRZ_ERROR(
        title = "Passport MRZ Issue",
        message = "Unable to read passport MRZ clearly. Please ensure the text lines at the bottom are clearly visible and try again.",
        actionText = "Recapture Passport"
    ),
    
    PASSPORT_OCR_ERROR(
        title = "Passport Text Issue", 
        message = "Unable to read passport text clearly. Please ensure good lighting and try again.",
        actionText = "Recapture Passport"
    ),
    
    STATE_ID_FRONT_ERROR(
        title = "State ID Front Issue",
        message = "Unable to read the front of your State ID clearly. Please ensure all text is visible and try again.",
        actionText = "Recapture Front"
    ),
    
    STATE_ID_BACK_ERROR(
        title = "State ID Back Issue",
        message = "Unable to read the back of your State ID clearly. Please ensure the barcode is clearly visible and try again.",
        actionText = "Recapture Back"
    ),
    
    STATE_ID_BARCODE_ERROR(
        title = "Barcode Read Issue",
        message = "Unable to read the barcode on your State ID. Please ensure the barcode is clearly visible and try again.",
        actionText = "Recapture Back"
    ),
    
    IMAGE_QUALITY_ERROR(
        title = "Image Quality Issue",
        message = "The image quality is too poor for verification. Please ensure good lighting and try again.",
        actionText = "Retake Photo"
    ),
    
    GENERAL_API_ERROR(
        title = "Verification Issue",
        message = "There was an issue processing your document. Please try capturing again.",
        actionText = "Try Again"
    ),
    
    NFC_TIMEOUT_ERROR(
        title = "NFC Read Issue",
        message = "Unable to read passport chip data. Continuing with image verification only.",
        actionText = "Continue"
    );
    
    companion object {
        /**
         * Determine recapture type from HTTP error code
         */
        fun fromHttpErrorCode(errorCode: Int, isPassport: Boolean = false): DocumentRecaptureType {
            return when (errorCode) {
                600 -> if (isPassport) PASSPORT_OCR_ERROR else STATE_ID_FRONT_ERROR
                601 -> if (isPassport) PASSPORT_MRZ_ERROR else STATE_ID_BARCODE_ERROR
                602 -> STATE_ID_BACK_ERROR
                603 -> IMAGE_QUALITY_ERROR
                604 -> NFC_TIMEOUT_ERROR
                else -> GENERAL_API_ERROR
            }
        }
        
        /**
         * Determine recapture type from error message content
         */
        fun fromErrorMessage(errorMessage: String, isPassport: Boolean = false): DocumentRecaptureType {
            val message = errorMessage.lowercase()
            return when {
                message.contains("mrz") -> PASSPORT_MRZ_ERROR
                message.contains("ocr") && isPassport -> PASSPORT_OCR_ERROR
                message.contains("barcode") -> STATE_ID_BARCODE_ERROR
                message.contains("front") -> STATE_ID_FRONT_ERROR
                message.contains("back") -> STATE_ID_BACK_ERROR
                message.contains("quality") || message.contains("blur") -> IMAGE_QUALITY_ERROR
                message.contains("nfc") || message.contains("chip") -> NFC_TIMEOUT_ERROR
                else -> GENERAL_API_ERROR
            }
        }
    }
}
