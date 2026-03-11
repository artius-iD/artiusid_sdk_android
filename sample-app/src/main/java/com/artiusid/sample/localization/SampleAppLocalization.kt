/*
 * File: SampleAppLocalization.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sample.localization

import android.content.Context
import androidx.annotation.StringRes
import com.artiusid.sample.R

/**
 * Sample App Localization Manager
 * 
 * This class collects all string overrides from the sample app's strings.xml
 * and provides them to the SDK for localization customization.
 */
object SampleAppLocalization {
    
    /**
     * Get all string overrides from the sample app to pass to the SDK
     */
    fun getStringOverrides(context: Context): Map<String, String> {
        val overrides = mutableMapOf<String, String>()
        
        // List of all possible SDK and app string names that can be overridden (iOS parity + SDK keys)
        val possibleStringNames = listOf(
            // SDK VerificationStepsScreen and common
            "verification_steps_title", "verification_steps_subtitle", "step_face_scan", "step_face_scan_description",
            "step_document_scan", "step_document_scan_description", "step_processing", "step_processing_description",
            "button_start_now", "button_approve", "button_deny", "button_back_home", "button_go_back",
            // iOS-aligned keys (sample app + SDK override)
            "approval_request_title", "intro_verifyMe", "intro_authenticate", "intro_authNeeded", "intro_biometricMFA",
            "intro_requestApproval", "intro_requestApprovalSending", "intro_requestApprovalVerifyFirst",
            "okta_whyLoginRequired", "okta_provisioningMessage", "verification_successful", "verification_failed",
            "verify_steps_navTitle", "verify_steps_viewText", "verify_process_success_viewTitle", "verify_process_success_viewText",
            "verify_process_success_backButtonLabel", "verify_process_fail_viewText", "verify_process_fail_backButtonLabel",
            "document_intro_viewTitle", "document_intro_viewText", "document_intro_nextButtonLabel",
            "document_front_bullet", "document_back_viewTitle", "document_back_viewText", "document_back_nextButtonLabel",
            "face_scan_intro_title", "face_scan_intro_subtitle", "face_scan_title", "faceScan_nextButtonLabel",
            "passport_intro_viewTitle", "passport_intro_viewText", "passport_intro_nextButtonLabel",
            "gen_processing", "gen_doNotCloseApp", "gen_contactSupport", "gen_somethingWrong",
            "alert_cancel", "alert_ok", "view_continue", "view_back", "view_done",
            "settings_themeSelection", "settings_languageSelection", "settings_environmentalSettings",
            "settings_includeOktaID", "settings_clearVerification", "settings_verificationCleared", "settings_verificationClearedMessage",
            "settings_resetOktaUserID", "settings_clearOktaAndReregister", "settings_forceFcmRegistration", "settings_forceApnsRegistration",
            "biometric_authenticate", "biometric_success", "biometric_failed", "biometric_failedInitial",
            "auth_approved", "auth_denied", "auth_failed", "auth_cancelled", "approval_requestApproved", "approval_requestDenied",
            "sample_approval_request_result", "sample_approved", "sample_declined",
            // Common/Generic
            "ok", "cancel", "retry", "back", "next", "done", "error", "success", "warning",
            "info", "help", "settings", "close", "menu", "loading", "processing", "please_wait",
            "button_verify_now", "button_authenticate", "button_try_again", "button_continue", "button_skip",
            "welcome_to", "home_intro_title_1", "home_intro_title_2", "home_intro_title_3", "home_intro_title_4",
            "select_document_title", "select_document_subtitle", "document_type_state_id", "document_type_passport",
            "document_scan_title", "document_scan_subtitle", "document_scan_front_title", "document_scan_back_title",
            "passport_scan_title", "passport_scan_subtitle", "tip_good_lighting", "tip_avoid_glare", "tip_fill_frame", "tip_hold_steady",
            "face_scan_subtitle", "face_tip_no_glasses", "face_tip_no_hat", "face_tip_no_mask", "face_tip_good_light",
            "face_scan_searching", "face_scan_found", "face_scan_align", "face_scan_hold_still", "face_scan_capturing",
            "face_scan_complete", "face_blink_to_complete", "face_liveness_completed", "face_position_in_circle", "face_centered_taking_selfie",
            "nfc_scan_title", "nfc_scan_subtitle", "nfc_scan_instruction", "nfc_scan_searching", "nfc_scan_found",
            "nfc_scan_reading", "nfc_scan_complete", "nfc_scan_failed", "nfc_scan_retry",
            "verification_processing", "verification_uploading", "verification_analyzing", "verification_complete",
            "verification_success_description", "verification_account_details", "verify_details_member_label",
            "verify_details_first_name_label", "verify_details_last_name_label", "verify_details_document_score_label",
            "verify_details_face_score_label", "verify_details_overall_score_label",
            "error_document_validation", "error_face_validation", "error_general", "error_ocr", "error_barcode", "error_preprocessing", "error_mrz",
            "permission_camera_title", "permission_camera_message", "permission_camera_grant"
        )
        
        // Check each string name and add to overrides if it exists in sample app
        possibleStringNames.forEach { stringName ->
            try {
                // Check if the string resource exists in the sample app
                val resId = context.resources.getIdentifier(stringName, "string", context.packageName)
                if (resId != 0) {
                    val stringValue = context.getString(resId)
                    overrides[stringName] = stringValue
                    android.util.Log.d("SampleAppLocalization", "✅ Override: $stringName = $stringValue")
                }
            } catch (e: Exception) {
                // String not defined in sample app, skip
                android.util.Log.d("SampleAppLocalization", "⏭️ Skip: $stringName (not defined)")
            }
        }
        
        return overrides
    }
    
    /**
     * Check if a string resource is actually defined in the sample app
     * (not just falling back to the SDK's string resource)
     */
    private fun isStringDefinedInSampleApp(context: Context, resourceName: String): Boolean {
        return try {
            // Try to get the resource ID for this string name in the sample app
            val resId = context.resources.getIdentifier(resourceName, "string", context.packageName)
            resId != 0 // Returns 0 if not found
        } catch (e: Exception) {
            false
        }
    }
}
