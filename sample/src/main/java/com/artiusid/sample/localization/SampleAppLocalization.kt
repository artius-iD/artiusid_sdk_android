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
        
        // List of all possible SDK string names that can be overridden
        val possibleStringNames = listOf(
            // Common/Generic Strings
            "ok", "cancel", "retry", "back", "next", "done", "error", "success", "warning", 
            "info", "help", "settings", "close", "menu", "loading", "processing", "please_wait",
            
            // Button Labels
            "button_verify_now", "button_authenticate", "button_approve", "button_deny", 
            "button_try_again", "button_back_home", "button_go_back", "button_start_now", 
            "button_continue", "button_skip",
            
            // Home Screen
            "welcome_to", "home_intro_title_1", "home_intro_title_2", "home_intro_title_3", 
            "home_intro_title_4",
            
            // Verification Steps Screen
            "verification_steps_title", "verification_steps_subtitle", "step_document_scan", 
            "step_face_scan", "step_processing",
            
            // Document Selection
            "select_document_title", "select_document_subtitle", "document_type_state_id", 
            "document_type_passport",
            
            // Document Scan Instructions
            "document_scan_title", "document_scan_subtitle", "document_scan_front_title", 
            "document_scan_back_title", "passport_scan_title", "passport_scan_subtitle",
            
            // Document Scan Tips
            "tip_good_lighting", "tip_avoid_glare", "tip_fill_frame", "tip_hold_steady",
            
            // Face Scan Instructions
            "face_scan_title", "face_scan_subtitle", "face_scan_intro_title", "face_scan_intro_subtitle",
            
            // Face Scan Tips
            "face_tip_no_glasses", "face_tip_no_hat", "face_tip_no_mask", "face_tip_good_light",
            
            // Face Scan Status
            "face_scan_searching", "face_scan_found", "face_scan_align", "face_scan_hold_still", 
            "face_scan_capturing", "face_scan_complete", "face_blink_to_complete", 
            "face_liveness_completed", "face_position_in_circle", "face_centered_taking_selfie",
            
            // NFC Passport Scan
            "nfc_scan_title", "nfc_scan_subtitle", "nfc_scan_instruction", "nfc_scan_searching", 
            "nfc_scan_found", "nfc_scan_reading", "nfc_scan_complete", "nfc_scan_failed", 
            "nfc_scan_retry",
            
            // Processing Screen
            "verification_processing", "verification_uploading", "verification_analyzing", 
            "verification_complete", "verification_successful", "verification_failed",
            
            // Results Screen
            "verification_success_description", "verification_account_details", 
            "verify_details_member_label", "verify_details_first_name_label", 
            "verify_details_last_name_label", "verify_details_document_score_label", 
            "verify_details_face_score_label", "verify_details_overall_score_label",
            
            // Error Messages
            "error_document_validation", "error_face_validation", "error_general", "error_ocr", 
            "error_barcode", "error_preprocessing", "error_mrz",
            
            // Permission Messages
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
