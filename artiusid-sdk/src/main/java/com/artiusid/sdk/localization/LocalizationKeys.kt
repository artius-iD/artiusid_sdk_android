/*
 * File: LocalizationKeys.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 *
 * All available localization keys for SDK strings.
 * Matches iOS SDK LocalizationKeys for cross-platform consistency.
 */

package com.artiusid.sdk.localization

/**
 * Central constants for SDK string resource keys.
 * Use these when building [com.artiusid.sdk.config.SDKConfiguration.localizationOverrides]
 * so the same keys work on iOS and Android.
 */
object LocalizationKeys {

    // Welcome & Intro
    const val WELCOME_TITLE = "welcome_title"
    const val WELCOME_SUBTITLE = "welcome_subtitle"
    const val WELCOME_MESSAGE = "welcome_message"
    const val GET_STARTED_BUTTON = "get_started_button"
    const val START_BUTTON = "start_button"

    // Document Selection
    const val DOCUMENT_SELECTION_TITLE = "document_selection_title"
    const val DOCUMENT_SELECTION_SUBTITLE = "document_selection_subtitle"
    const val SELECT_DOCUMENT_TYPE = "select_document_type"
    const val PASSPORT_OPTION = "passport_option"
    const val STATE_ID_OPTION = "state_id_option"
    const val DRIVER_LICENSE_OPTION = "driver_license_option"

    // Document Scanning
    const val DOCUMENT_SCAN_TITLE = "document_scan_title"
    const val PASSPORT_SCAN_TITLE = "passport_scan_title"
    const val STATE_ID_SCAN_TITLE = "state_id_scan_title"
    const val DRIVER_LICENSE_SCAN_TITLE = "driver_license_scan_title"
    const val DOCUMENT_FRONT_INSTRUCTION = "document_front_instruction"
    const val DOCUMENT_BACK_INSTRUCTION = "document_back_instruction"
    const val PASSPORT_INSTRUCTION = "passport_instruction"
    const val POSITION_DOCUMENT = "position_document"
    const val HOLD_STEADY = "hold_steady"
    const val CAPTURING = "capturing"

    // Face Scanning
    const val FACE_SCAN_INTRO_TITLE = "face_scan_intro_title"
    const val FACE_SCAN_INTRO_SUBTITLE = "face_scan_intro_subtitle"
    const val FACE_SCAN_TITLE = "face_scan_title"
    const val FACE_INSTRUCTION = "face_instruction"
    const val FACE_SCAN_SEARCHING = "face_scan_searching"
    const val FACE_SCAN_PROCESSING = "face_scan_processing"
    const val FACE_SCAN_COMPLETE = "face_scan_complete"
    const val POSITION_FACE = "position_face"
    const val LOOK_AT_CAMERA = "look_at_camera"
    const val PHONE_TOO_CLOSE = "phone_too_close"
    const val PHONE_TOO_FAR = "phone_too_far"
    const val POSITION_YOUR_FACE = "position_your_face"
    const val MOVE_HEAD = "move_head"

    // Face Scan Tips
    const val TIP_NO_GLASSES = "tip_no_glasses"
    const val TIP_NO_HAT = "tip_no_hat"
    const val TIP_NO_MASK = "tip_no_mask"
    const val TIP_GOOD_LIGHTING = "tip_good_lighting"
    const val TIP_NEUTRAL_EXPRESSION = "tip_neutral_expression"
    const val TIP_REMOVE_OBSTRUCTIONS = "tip_remove_obstructions"

    // Liveness Detection
    const val LIVENESS_COMPLETED = "liveness_completed"
    const val PLEASE_BLINK = "liveness_please_blink"

    // NFC Reading
    const val NFC_SCAN_TITLE = "nfc_scan_title"
    const val NFC_SCAN_SUBTITLE = "nfc_scan_subtitle"
    const val NFC_SCAN_INSTRUCTION = "nfc_scan_instruction"
    const val NFC_SCAN_READING = "nfc_scan_reading"
    const val NFC_SCAN_COMPLETE = "nfc_scan_complete"
    const val NFC_SCAN_FAILED = "nfc_scan_failed"
    const val HOLD_PHONE_TO_PASSPORT = "hold_phone_to_passport"
    const val KEEP_PHONE_STEADY = "keep_phone_steady"

    // Processing
    const val PROCESSING_TITLE = "processing_title"
    const val PROCESSING_MESSAGE = "processing_message"
    const val VERIFICATION_PROCESSING = "verification_processing"
    const val ANALYZING_DOCUMENT = "analyzing_document"
    const val ANALYZING_FACE = "analyzing_face"
    const val PLEASE_WAIT = "please_wait"
    const val PLEASE_DO_NOT_CLOSE_APP = "please_do_not_close_app"
    const val DEVELOPER_MODE = "developer_mode"

    // Success
    const val VERIFICATION_SUCCESS_TITLE = "verification_success_title"
    const val VERIFICATION_SUCCESS_MESSAGE = "verification_success_message"
    const val AUTHENTICATION_SUCCESS_TITLE = "authentication_success_title"
    const val AUTHENTICATION_SUCCESS_MESSAGE = "authentication_success_message"
    const val SUCCESS_TITLE = "success_title"
    const val SUCCESS_MESSAGE = "success_message"
    const val DOCUMENT_CAPTURE_FRONT_SUCCESS = "document_capture_front_success"
    const val DOCUMENT_CAPTURE_BACK_SUCCESS = "document_capture_back_success"
    const val DOCUMENT_CAPTURE_PASSPORT_SUCCESS = "document_capture_passport_success"

    // Error & Retry
    const val ERROR_TITLE = "error_title"
    const val ERROR_MESSAGE = "error_message"
    const val NETWORK_ERROR = "network_error"
    const val VERIFICATION_FAILED = "verification_failed"
    const val AUTHENTICATION_FAILED = "authentication_failed"
    const val DOCUMENT_VALIDATION_ERROR = "document_validation_error"
    const val FACE_VALIDATION_ERROR = "face_validation_error"
    const val NFC_READ_ERROR = "nfc_read_error"
    const val DOCUMENT_NOT_CLEAR = "document_not_clear"
    const val DOCUMENT_NOT_RECOGNIZED = "document_not_recognized"
    const val FACE_NOT_DETECTED = "face_not_detected"
    const val FACE_NOT_CLEAR = "face_not_clear"

    // Authentication View
    const val AUTH_VIEW_HEADER = "auth_view_header"
    const val AUTH_BIOMETRIC_REQUIRED_TITLE = "auth_biometric_required_title"
    const val AUTH_BIOMETRIC_REQUIRED_DESCRIPTION = "auth_biometric_required_description"
    const val AUTH_FAILED_TITLE = "auth_failed_title"
    const val AUTH_SUCCESSFUL_TITLE = "auth_successful_title"
    const val AUTH_TRY_AGAIN = "auth_try_again"
    const val AUTH_BACK_HOME = "auth_back_home"
    const val AUTH_BIOMETRIC_NOT_AVAILABLE = "auth_biometric_not_available"
    const val AUTH_BIOMETRIC_NOT_ENROLLED = "auth_biometric_not_enrolled"
    const val AUTH_BIOMETRIC_LOCKED = "auth_biometric_locked"
    const val AUTH_BIOMETRIC_FAILED = "auth_biometric_failed"

    // Image Quality & Lighting Validation
    const val IMAGE_TOO_DARK = "validation_image_too_dark"
    const val IMAGE_TOO_BRIGHT = "validation_image_too_bright"
    const val LIGHTING_ACCEPTABLE = "validation_lighting_acceptable"
    const val PLEASE_HOLD_PHONE_STEADY = "validation_blur_detected"
    const val GLARE_DETECTED_ADJUST_LIGHTING = "validation_glare_detected"
    const val IMAGE_QUALITY_ACCEPTABLE = "validation_quality_acceptable"
    const val INVALID_IMAGE_FOR_ANALYSIS = "invalid_image_for_analysis"
    const val IMAGE_ANALYSIS_FAILED = "validation_image_analysis_failed"
    const val IMAGE_TOO_SMALL = "validation_image_too_small"

    // Document Validation Messages
    const val INVALID_IMAGE_CAPTURED = "validation_invalid_image"
    const val NO_DOCUMENT_DETECTED = "validation_no_document"
    const val PLEASE_MOVE_CLOSER_TO_ID = "validation_move_closer"
    const val PART_OF_ID_OUTSIDE_CAMERA_VIEW = "validation_outside_frame"
    const val FAILED_TO_ANALYZE_IMAGE = "validation_analyze_failed"
    const val VALID_DOCUMENT = "validation_success"

    // Passport Validation Messages
    const val NO_PHOTO_FOUND = "validation_no_photo"
    const val CAPTURE_IS_INVALID = "validation_capture_invalid"
    const val NO_DOCUMENT_FOUND_PASSPORT = "no_document_found_passport"
    const val OCR_FAILED = "validation_ocr_failed"
    const val LOW_OCR_CONFIDENCE = "validation_low_confidence"
    const val POSITION_MRZ = "validation_position_mrz"

    // Buttons
    const val CONTINUE_BUTTON = "continue_button"
    const val TRY_AGAIN_BUTTON = "try_again_button"
    const val RETRY_BUTTON = "retry_button"
    const val CANCEL_BUTTON = "cancel_button"
    const val DONE_BUTTON = "done_button"
    const val NEXT_BUTTON = "next_button"
    const val BACK_BUTTON = "back_button"
    const val SKIP_BUTTON = "skip_button"
    const val SUBMIT_BUTTON = "submit_button"
    const val BUTTON_APPROVE = "button_approve"
    const val BUTTON_DENY = "button_deny"

    // Permissions
    const val PERMISSION_CAMERA_TITLE = "permission_camera_title"
    const val PERMISSION_CAMERA_MESSAGE = "permission_camera_message"
    const val PERMISSION_NFC_TITLE = "permission_nfc_title"
    const val PERMISSION_NFC_MESSAGE = "permission_nfc_message"
    const val PERMISSION_SETTINGS_BUTTON = "permission_settings_button"
    const val PERMISSION_DENIED = "permission_denied"

    // Approval Requests
    const val APPROVAL_REQUEST_TITLE = "approval_request_title"
    const val APPROVAL_REQUEST_MESSAGE = "approval_request_message"
    const val APPROVAL_PENDING = "approval_pending"
    const val APPROVAL_APPROVED = "approval_approved"
    const val APPROVAL_DECLINED = "approval_declined"
    const val WAITING_FOR_APPROVAL = "waiting_for_approval"
    const val SUBMITTING_RESPONSE = "approval_submitting_response"

    // Verification Steps
    const val STEP_DOCUMENT_SCAN = "step_document_scan"
    const val STEP_FACE_SCAN = "step_face_scan"
    const val STEP_NFC_READ = "step_nfc_read"
    const val STEP_REVIEW = "step_review"
    const val STEP_COMPLETE = "step_complete"

    // Document Types
    const val PASSPORT = "passport"
    const val STATE_ID = "state_id"
    const val DRIVER_LICENSE = "driver_license"
    const val IDENTITY_CARD = "identity_card"

    // Approval response screen (iOS parity)
    const val APPROVAL_RESPONSE_APPROVED_TITLE = "approval_response_approved_title"
    const val APPROVAL_RESPONSE_DECLINED_TITLE = "approval_response_declined_title"

    // Settings (sample app parity with iOS)
    const val SETTINGS_TITLE = "settings_title"
    const val SETTINGS_LANGUAGE = "settings_language"
    const val SETTINGS_ENVIRONMENT = "settings_environment"
    const val SETTINGS_THEME = "settings_theme"
    const val SETTINGS_IMAGE_OVERRIDES = "settings_imageOverrides"
    const val SETTINGS_CONFIG_INFO = "settings_config_info"
    const val SETTINGS_ACCOUNT_NUMBER = "settings_account_number"
    const val SETTINGS_FCM_TOKEN = "settings_fcm_token"
    const val SETTINGS_DEVICE_ID = "settings_device_id"
    const val SETTINGS_SDK_VERSION = "settings_sdk_version"
    const val SETTINGS_APP_VERSION = "settings_app_version"
    const val SETTINGS_CERTIFICATE = "settings_certificate"
    const val SETTINGS_STATUS = "settings_status"
    const val SETTINGS_LOADED_DATE = "settings_loaded_date"
    const val SETTINGS_DEVICE = "settings_device"
    const val SETTINGS_MODEL = "settings_model"
    const val SETTINGS_ANDROID_VERSION = "settings_android_version"

    // Sample app
    const val SAMPLE_DONE = "sample_done"
    const val SAMPLE_THEME_PREVIEW = "sample_theme_preview"
    const val SAMPLE_PRIMARY_COLOR = "sample_primary_color"
    const val SAMPLE_SECONDARY_COLOR = "sample_secondary_color"
    const val SAMPLE_BACKGROUND_COLOR = "sample_background_color"
    const val SAMPLE_VERIFICATION = "sample_verification"
    const val SAMPLE_AUTHENTICATION = "sample_authentication"
    const val SAMPLE_APPROVAL = "sample_approval"
    const val SAMPLE_APPROVAL_REQUEST_RESULT = "sample_approval_request_result"
    const val SAMPLE_CLEAR_RESULT = "sample_clear_result"
    const val SAMPLE_OKTA_ID = "sample_okta_id"
    const val SAMPLE_LOGIN_WITH_OKTA = "sample_login_with_okta"
    const val SAMPLE_RELOGIN = "sample_relogin"

    // Common Terms
    const val LOADING = "loading"
    const val CANCEL = "cancel"
    const val OK = "ok"
    const val YES = "yes"
    const val NO = "no"
    const val CLOSE = "close"
    const val RETRY = "retry"
    const val SKIP = "skip"

    // Time & Status
    const val JUST_NOW = "just_now"
    const val MINUTES_AGO = "minutes_ago"
    const val HOURS_AGO = "hours_ago"
    const val DAYS_AGO = "days_ago"
    const val PENDING = "pending"
    const val APPROVED = "approved"
    const val DECLINED = "declined"
    const val PROCESSING = "processing"
    const val COMPLETE = "complete"

    /**
     * All localization keys for iteration or validation.
     */
    @JvmStatic
    fun allKeys(): List<String> = listOf(
        WELCOME_TITLE, WELCOME_SUBTITLE, WELCOME_MESSAGE, GET_STARTED_BUTTON, START_BUTTON,
        DOCUMENT_SELECTION_TITLE, DOCUMENT_SELECTION_SUBTITLE, SELECT_DOCUMENT_TYPE,
        PASSPORT_OPTION, STATE_ID_OPTION, DRIVER_LICENSE_OPTION,
        DOCUMENT_SCAN_TITLE, PASSPORT_SCAN_TITLE, STATE_ID_SCAN_TITLE, DRIVER_LICENSE_SCAN_TITLE,
        DOCUMENT_FRONT_INSTRUCTION, DOCUMENT_BACK_INSTRUCTION, PASSPORT_INSTRUCTION,
        POSITION_DOCUMENT, HOLD_STEADY, CAPTURING,
        FACE_SCAN_INTRO_TITLE, FACE_SCAN_INTRO_SUBTITLE, FACE_SCAN_TITLE, FACE_INSTRUCTION,
        FACE_SCAN_SEARCHING, FACE_SCAN_PROCESSING, FACE_SCAN_COMPLETE, POSITION_FACE, LOOK_AT_CAMERA,
        TIP_NO_GLASSES, TIP_NO_HAT, TIP_NO_MASK, TIP_GOOD_LIGHTING, TIP_NEUTRAL_EXPRESSION, TIP_REMOVE_OBSTRUCTIONS,
        LIVENESS_COMPLETED, PLEASE_BLINK,
        NFC_SCAN_TITLE, NFC_SCAN_SUBTITLE, NFC_SCAN_INSTRUCTION, NFC_SCAN_READING, NFC_SCAN_COMPLETE,
        NFC_SCAN_FAILED, HOLD_PHONE_TO_PASSPORT, KEEP_PHONE_STEADY,
        PROCESSING_TITLE, PROCESSING_MESSAGE, VERIFICATION_PROCESSING, ANALYZING_DOCUMENT,
        ANALYZING_FACE, PLEASE_WAIT, PLEASE_DO_NOT_CLOSE_APP, DEVELOPER_MODE,
        VERIFICATION_SUCCESS_TITLE, VERIFICATION_SUCCESS_MESSAGE, AUTHENTICATION_SUCCESS_TITLE,
        AUTHENTICATION_SUCCESS_MESSAGE, SUCCESS_TITLE, SUCCESS_MESSAGE, DOCUMENT_CAPTURE_FRONT_SUCCESS,
        DOCUMENT_CAPTURE_BACK_SUCCESS, DOCUMENT_CAPTURE_PASSPORT_SUCCESS,
        ERROR_TITLE, ERROR_MESSAGE, NETWORK_ERROR, VERIFICATION_FAILED, AUTHENTICATION_FAILED,
        DOCUMENT_VALIDATION_ERROR, FACE_VALIDATION_ERROR, NFC_READ_ERROR,
        DOCUMENT_NOT_CLEAR, DOCUMENT_NOT_RECOGNIZED, FACE_NOT_DETECTED, FACE_NOT_CLEAR,
        IMAGE_TOO_DARK, IMAGE_TOO_BRIGHT, LIGHTING_ACCEPTABLE, PLEASE_HOLD_PHONE_STEADY,
        GLARE_DETECTED_ADJUST_LIGHTING, IMAGE_QUALITY_ACCEPTABLE, INVALID_IMAGE_FOR_ANALYSIS,
        IMAGE_ANALYSIS_FAILED, IMAGE_TOO_SMALL,
        INVALID_IMAGE_CAPTURED, NO_DOCUMENT_DETECTED, PLEASE_MOVE_CLOSER_TO_ID,
        PART_OF_ID_OUTSIDE_CAMERA_VIEW, FAILED_TO_ANALYZE_IMAGE, VALID_DOCUMENT,
        NO_PHOTO_FOUND, CAPTURE_IS_INVALID, NO_DOCUMENT_FOUND_PASSPORT, OCR_FAILED, LOW_OCR_CONFIDENCE, POSITION_MRZ,
        AUTH_VIEW_HEADER, AUTH_BIOMETRIC_REQUIRED_TITLE, AUTH_BIOMETRIC_REQUIRED_DESCRIPTION,
        AUTH_FAILED_TITLE, AUTH_SUCCESSFUL_TITLE, AUTH_TRY_AGAIN, AUTH_BACK_HOME,
        AUTH_BIOMETRIC_NOT_AVAILABLE, AUTH_BIOMETRIC_NOT_ENROLLED, AUTH_BIOMETRIC_LOCKED, AUTH_BIOMETRIC_FAILED,
        CONTINUE_BUTTON, TRY_AGAIN_BUTTON, RETRY_BUTTON, CANCEL_BUTTON, DONE_BUTTON,
        NEXT_BUTTON, BACK_BUTTON, SKIP_BUTTON, SUBMIT_BUTTON, BUTTON_APPROVE, BUTTON_DENY,
        PERMISSION_CAMERA_TITLE, PERMISSION_CAMERA_MESSAGE, PERMISSION_NFC_TITLE,
        PERMISSION_NFC_MESSAGE, PERMISSION_SETTINGS_BUTTON, PERMISSION_DENIED,
        APPROVAL_REQUEST_TITLE, APPROVAL_REQUEST_MESSAGE, APPROVAL_PENDING, APPROVAL_APPROVED,
        APPROVAL_DECLINED, WAITING_FOR_APPROVAL, SUBMITTING_RESPONSE,
        STEP_DOCUMENT_SCAN, STEP_FACE_SCAN, STEP_NFC_READ, STEP_REVIEW, STEP_COMPLETE,
        PASSPORT, STATE_ID, DRIVER_LICENSE, IDENTITY_CARD,
        APPROVAL_RESPONSE_APPROVED_TITLE, APPROVAL_RESPONSE_DECLINED_TITLE,
        SETTINGS_TITLE, SETTINGS_LANGUAGE, SETTINGS_ENVIRONMENT, SETTINGS_THEME,
        SETTINGS_IMAGE_OVERRIDES, SETTINGS_CONFIG_INFO, SETTINGS_ACCOUNT_NUMBER,
        SETTINGS_FCM_TOKEN, SETTINGS_DEVICE_ID, SETTINGS_SDK_VERSION, SETTINGS_APP_VERSION,
        SETTINGS_CERTIFICATE, SETTINGS_STATUS, SETTINGS_LOADED_DATE, SETTINGS_DEVICE,
        SETTINGS_MODEL, SETTINGS_ANDROID_VERSION,
        SAMPLE_DONE, SAMPLE_THEME_PREVIEW, SAMPLE_PRIMARY_COLOR, SAMPLE_SECONDARY_COLOR,
        SAMPLE_BACKGROUND_COLOR, SAMPLE_VERIFICATION, SAMPLE_AUTHENTICATION, SAMPLE_APPROVAL,
        SAMPLE_APPROVAL_REQUEST_RESULT, SAMPLE_CLEAR_RESULT, SAMPLE_OKTA_ID,
        SAMPLE_LOGIN_WITH_OKTA, SAMPLE_RELOGIN,
        LOADING, CANCEL, OK, YES, NO, CLOSE, RETRY, SKIP,
        JUST_NOW, MINUTES_AGO, HOURS_AGO, DAYS_AGO,
        PENDING, APPROVED, DECLINED, PROCESSING, COMPLETE
    )

    @JvmStatic
    fun isValidKey(key: String): Boolean = allKeys().contains(key)
}
