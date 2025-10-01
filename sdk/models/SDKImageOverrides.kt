/*
 * File: SDKImageOverrides.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * Comprehensive SDK Image Override Configuration
 * Allows host applications to override any image or GIF asset within the SDK
 */
@Parcelize
data class SDKImageOverrides(
    // === FACE SCAN ASSETS ===
    val faceOverlay: String? = null,                    // Face outline overlay
    val faceUpGif: String? = null,                      // Face positioning up GIF
    val faceDownGif: String? = null,                    // Face positioning down GIF
    val phoneUpGif: String? = null,                     // Phone positioning up GIF
    val phoneDownGif: String? = null,                   // Phone positioning down GIF
    val faceRotationGif: String? = null,                // Face rotation animation
    
    // === DOCUMENT SCAN ASSETS ===
    val passportOverlay: String? = null,                // Passport scan overlay
    val stateIdFrontOverlay: String? = null,            // State ID front overlay
    val stateIdBackOverlay: String? = null,             // State ID back overlay
    val passportAnimationGif: String? = null,           // Passport scan animation
    val stateIdAnimationGif: String? = null,            // State ID scan animation
    
    // === UI NAVIGATION ICONS ===
    val backButtonIcon: String? = null,                 // Back navigation button
    val cameraButtonIcon: String? = null,               // Camera capture button
    val doneIcon: String? = null,                       // Completion/done icon
    val documentRightArrow: String? = null,             // Document selection arrow
    
    // === SCAN & VERIFICATION ICONS ===
    val scanFaceIcon: String? = null,                   // Face scan step icon
    val docScanIcon: String? = null,                    // Document scan step icon
    val passportIcon: String? = null,                   // Passport selection icon
    val stateIdIcon: String? = null,                    // State ID selection icon
    val focusIcon: String? = null,                      // Camera focus icon
    
    // === STATUS & FEEDBACK ICONS ===
    val successIcon: String? = null,                    // Success/approval icon
    val failedIcon: String? = null,                     // Failure/error icon
    val errorIcon: String? = null,                      // System error icon
    val systemErrorIcon: String? = null,                // System error illustration
    val approvalIcon: String? = null,                   // Approval request icon
    val approvalRequestIcon: String? = null,            // Approval request illustration
    val declinedIcon: String? = null,                   // Declined/rejected icon
    val informationalIcon: String? = null,              // Information/help icon
    
    // === BRAND & IDENTITY ASSETS ===
    val brandLogo: String? = null,                      // Primary brand logo
    val brandImage: String? = null,                     // Brand illustration (artius.iD)
    val introHomeImage: String? = null,                 // Home screen illustration
    val accountIcon: String? = null,                    // User account icon
    val lockIcon: String? = null,                       // Security/lock icon
    
    // === INSTRUCTION & GUIDANCE ICONS ===
    val noGlassesIcon: String? = null,                  // Remove glasses instruction
    val noHatIcon: String? = null,                      // Remove hat instruction
    val noMaskIcon: String? = null,                     // Remove mask instruction
    val goodLightIcon: String? = null,                  // Good lighting instruction
    val layFlatIcon: String? = null,                    // Lay document flat instruction
    val noGlareIcon: String? = null,                    // Avoid glare instruction
    
    // === BACKGROUND & DECORATIVE ASSETS ===
    val scanBackground01: String? = null,               // Scan background variant 1
    val scanBackground02: String? = null,               // Scan background variant 2
    val scanBackground03: String? = null,               // Scan background variant 3
    
    // === CROSS-PLATFORM & FEATURE ILLUSTRATIONS ===
    val crossPlatformImage: String? = null,             // Cross-platform illustration
    val crossDeviceImage: String? = null,               // Cross-device illustration
    val searchImage: String? = null,                    // Search illustration
    val groupImage: String? = null,                     // Group/team illustration
    val group254x353Image: String? = null,              // Specific group illustration
    val group243Image: String? = null,                  // Another group illustration
    
    // === VECTOR & GRAPHIC ELEMENTS ===
    val vector1Gray902: String? = null,                 // Vector graphic element
    val vector1Gray903: String? = null,                 // Vector graphic element
    val vector1Gray904: String? = null,                 // Vector graphic element
    val vector1: String? = null,                        // Primary vector element
    
    // === EXTENSIBILITY ===
    /**
     * Custom override map for additional assets not covered by specific fields
     * Key: Asset identifier (e.g., "custom_animation", "special_overlay")
     * Value: Override source (URL, asset name, file path, etc.)
     */
    val customOverrides: Map<String, String> = emptyMap(),
    
    // === OVERRIDE CONFIGURATION ===
    /**
     * Default loading strategy for overrides
     * - URL: Load from web URL
     * - ASSET: Load from host app assets
     * - FILE: Load from file system
     * - RESOURCE: Use resource ID (requires additional configuration)
     */
    val defaultLoadingStrategy: ImageLoadingStrategy = ImageLoadingStrategy.AUTO_DETECT,
    
    /**
     * Enable caching for override images
     */
    val enableCaching: Boolean = true,
    
    /**
     * Cache duration in milliseconds (default: 24 hours)
     */
    val cacheDurationMs: Long = 24 * 60 * 60 * 1000L,
    
    /**
     * Enable fallback to default SDK assets if override fails to load
     */
    val enableFallback: Boolean = true,
    
    /**
     * Preload override images on SDK initialization
     */
    val preloadImages: Boolean = false
) : Parcelable

/**
 * Image loading strategy enumeration
 */
@Parcelize
enum class ImageLoadingStrategy : Parcelable {
    /**
     * Automatically detect based on override string format
     * - URLs: http://, https://
     * - Files: file://, /android_asset/
     * - Assets: Plain names without prefixes
     */
    AUTO_DETECT,
    
    /**
     * Force load from web URLs
     */
    URL,
    
    /**
     * Force load from host application assets
     */
    ASSET,
    
    /**
     * Force load from file system paths
     */
    FILE,
    
    /**
     * Use Android resource IDs (requires special handling)
     */
    RESOURCE
}

/**
 * Image override result for internal use
 */
@Parcelize
data class ImageOverrideResult(
    val source: @RawValue Any,          // Can be URL, resource ID, file path, etc.
    val strategy: ImageLoadingStrategy,
    val isFallback: Boolean = false
) : Parcelable

/**
 * Extension functions for easy override checking
 */
fun SDKImageOverrides.hasOverride(key: String): Boolean {
    return when (key) {
        "face_overlay" -> faceOverlay != null
        "face_up_gif" -> faceUpGif != null
        "face_down_gif" -> faceDownGif != null
        "phone_up_gif" -> phoneUpGif != null
        "phone_down_gif" -> phoneDownGif != null
        "face_rotation_gif" -> faceRotationGif != null
        "passport_overlay" -> passportOverlay != null
        "state_id_front_overlay" -> stateIdFrontOverlay != null
        "state_id_back_overlay" -> stateIdBackOverlay != null
        "passport_animation_gif" -> passportAnimationGif != null
        "state_id_animation_gif" -> stateIdAnimationGif != null
        "back_button_icon" -> backButtonIcon != null
        "camera_button_icon" -> cameraButtonIcon != null
        "done_icon" -> doneIcon != null
        "document_right_arrow" -> documentRightArrow != null
        "scan_face_icon" -> scanFaceIcon != null
        "doc_scan_icon" -> docScanIcon != null
        "passport_icon" -> passportIcon != null
        "state_id_icon" -> stateIdIcon != null
        "focus_icon" -> focusIcon != null
        "success_icon" -> successIcon != null
        "failed_icon" -> failedIcon != null
        "error_icon" -> errorIcon != null
        "system_error_icon" -> systemErrorIcon != null
        "approval_icon" -> approvalIcon != null
        "approval_request_icon" -> approvalRequestIcon != null
        "declined_icon" -> declinedIcon != null
        "informational_icon" -> informationalIcon != null
        "brand_logo" -> brandLogo != null
        "brand_image" -> brandImage != null
        "intro_home_image" -> introHomeImage != null
        "account_icon" -> accountIcon != null
        "lock_icon" -> lockIcon != null
        "no_glasses_icon" -> noGlassesIcon != null
        "no_hat_icon" -> noHatIcon != null
        "no_mask_icon" -> noMaskIcon != null
        "good_light_icon" -> goodLightIcon != null
        "lay_flat_icon" -> layFlatIcon != null
        "no_glare_icon" -> noGlareIcon != null
        "scan_background_01" -> scanBackground01 != null
        "scan_background_02" -> scanBackground02 != null
        "scan_background_03" -> scanBackground03 != null
        "cross_platform_image" -> crossPlatformImage != null
        "cross_device_image" -> crossDeviceImage != null
        "search_image" -> searchImage != null
        "group_image" -> groupImage != null
        "group_254x353_image" -> group254x353Image != null
        "group_243_image" -> group243Image != null
        "vector1_gray_902" -> vector1Gray902 != null
        "vector1_gray_903" -> vector1Gray903 != null
        "vector1_gray_904" -> vector1Gray904 != null
        "vector1" -> vector1 != null
        else -> customOverrides.containsKey(key)
    }
}

/**
 * Get override value by key
 */
fun SDKImageOverrides.getOverride(key: String): String? {
    return when (key) {
        "face_overlay" -> faceOverlay
        "face_up_gif" -> faceUpGif
        "face_down_gif" -> faceDownGif
        "phone_up_gif" -> phoneUpGif
        "phone_down_gif" -> phoneDownGif
        "face_rotation_gif" -> faceRotationGif
        "passport_overlay" -> passportOverlay
        "state_id_front_overlay" -> stateIdFrontOverlay
        "state_id_back_overlay" -> stateIdBackOverlay
        "passport_animation_gif" -> passportAnimationGif
        "state_id_animation_gif" -> stateIdAnimationGif
        "back_button_icon" -> backButtonIcon
        "camera_button_icon" -> cameraButtonIcon
        "done_icon" -> doneIcon
        "document_right_arrow" -> documentRightArrow
        "scan_face_icon" -> scanFaceIcon
        "doc_scan_icon" -> docScanIcon
        "passport_icon" -> passportIcon
        "state_id_icon" -> stateIdIcon
        "focus_icon" -> focusIcon
        "success_icon" -> successIcon
        "failed_icon" -> failedIcon
        "error_icon" -> errorIcon
        "system_error_icon" -> systemErrorIcon
        "approval_icon" -> approvalIcon
        "approval_request_icon" -> approvalRequestIcon
        "declined_icon" -> declinedIcon
        "informational_icon" -> informationalIcon
        "brand_logo" -> brandLogo
        "brand_image" -> brandImage
        "intro_home_image" -> introHomeImage
        "account_icon" -> accountIcon
        "lock_icon" -> lockIcon
        "no_glasses_icon" -> noGlassesIcon
        "no_hat_icon" -> noHatIcon
        "no_mask_icon" -> noMaskIcon
        "good_light_icon" -> goodLightIcon
        "lay_flat_icon" -> layFlatIcon
        "no_glare_icon" -> noGlareIcon
        "scan_background_01" -> scanBackground01
        "scan_background_02" -> scanBackground02
        "scan_background_03" -> scanBackground03
        "cross_platform_image" -> crossPlatformImage
        "cross_device_image" -> crossDeviceImage
        "search_image" -> searchImage
        "group_image" -> groupImage
        "group_254x353_image" -> group254x353Image
        "group_243_image" -> group243Image
        "vector1_gray_902" -> vector1Gray902
        "vector1_gray_903" -> vector1Gray903
        "vector1_gray_904" -> vector1Gray904
        "vector1" -> vector1
        else -> customOverrides[key]
    }
}
