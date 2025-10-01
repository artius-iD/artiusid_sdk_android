/**
 * Author: Todd Bryant
 * Company: artius.iD
 * 
 * Verification failure types matching iOS DocumentRecaptureType implementation
 */
package com.artiusid.sdk.data.model

import android.content.Context
import com.artiusid.sdk.R

enum class VerificationFailureType {
    PASSPORT,
    STATE_ID_FRONT,
    STATE_ID_BACK,
    FACE,
    GENERAL;

    fun getTitle(context: Context): String {
        return when (this) {
            PASSPORT -> context.getString(R.string.failure_passport_title)
            STATE_ID_FRONT -> context.getString(R.string.failure_state_id_front_title)
            STATE_ID_BACK -> context.getString(R.string.failure_state_id_back_title)
            FACE -> context.getString(R.string.failure_face_title)
            GENERAL -> context.getString(R.string.failure_general_title)
        }
    }

    fun getMessage(context: Context): String {
        return when (this) {
            PASSPORT -> context.getString(R.string.failure_passport_message)
            STATE_ID_FRONT -> context.getString(R.string.failure_state_id_front_message)
            STATE_ID_BACK -> context.getString(R.string.failure_state_id_back_message)
            FACE -> context.getString(R.string.failure_face_message)
            GENERAL -> context.getString(R.string.failure_general_message)
        }
    }

    fun getButtonText(context: Context): String {
        return when (this) {
            PASSPORT -> context.getString(R.string.failure_passport_button)
            STATE_ID_FRONT -> context.getString(R.string.failure_state_id_front_button)
            STATE_ID_BACK -> context.getString(R.string.failure_state_id_back_button)
            FACE -> context.getString(R.string.failure_face_button)
            GENERAL -> context.getString(R.string.failure_general_button)
        }
    }

    // Backward compatibility properties (deprecated)
    @Deprecated("Use getTitle(context) instead", ReplaceWith("getTitle(context)"))
    val title: String
        get() = when (this) {
            PASSPORT -> "Passport Image Quality Issue"
            STATE_ID_FRONT -> "ID Front Image Quality Issue"
            STATE_ID_BACK -> "ID Back Image Quality Issue"
            FACE -> "Face Image Quality Issue"
            GENERAL -> "Verification Failed"
        }

    @Deprecated("Use getMessage(context) instead", ReplaceWith("getMessage(context)"))
    val message: String
        get() = when (this) {
            PASSPORT -> "We had trouble reading your passport. Please retake the photo ensuring the MRZ (Machine Readable Zone) at the bottom is clearly visible and the passport is well-lit."
            STATE_ID_FRONT -> "We had trouble reading the front of your ID. Please retake the photo ensuring the text is clear, the lighting is good, and the entire ID is within the frame."
            STATE_ID_BACK -> "We had trouble reading the back of your ID. Please retake the photo ensuring the barcode is clearly visible and the lighting is adequate."
            FACE -> "We had trouble processing your face image. Please retake the photo in good lighting without glasses, hat, or mask covering your face."
            GENERAL -> "Verification could not be completed at this time. This may be due to document validation issues or system processing errors. Please try again later or contact support if the problem persists."
        }

    @Deprecated("Use getButtonText(context) instead", ReplaceWith("getButtonText(context)"))
    val buttonText: String
        get() = when (this) {
            PASSPORT -> "Retake Passport Photo"
            STATE_ID_FRONT -> "Retake ID Front Photo"
            STATE_ID_BACK -> "Retake ID Back Photo"
            FACE -> "Retake Face Photo"
            GENERAL -> "Back to Home"
        }
}
