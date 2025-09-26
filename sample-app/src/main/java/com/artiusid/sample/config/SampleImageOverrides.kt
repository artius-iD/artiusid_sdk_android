/*
 * File: SampleImageOverrides.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sample.config

import com.artiusid.sdk.models.SDKImageOverrides
import com.artiusid.sdk.models.ImageLoadingStrategy

/**
 * Sample App Image Override Configurations
 * Provides predefined image override sets for demonstration and testing
 */
object SampleImageOverrides {
    
    /**
     * No overrides - use default SDK images
     */
    val DEFAULT = SDKImageOverrides()
    
    /**
     * Corporate Images - Professional blue/grey overrides
     */
    val CORPORATE = SDKImageOverrides(
        // Face scan assets - using custom corporate styling
        faceOverlay = "corporate/corporate_face_overlay.png",
        faceUpGif = "corporate/corporate_face_up.gif",
        faceDownGif = "corporate/corporate_face_down.gif",
        phoneUpGif = "corporate/corporate_phone_up.gif",
        phoneDownGif = "corporate/corporate_phone_down.gif",
        
        // Document assets - corporate branding
        passportOverlay = "corporate/corporate_passport_overlay.png",
        stateIdFrontOverlay = "corporate/corporate_stateid_front_overlay.png",
        stateIdBackOverlay = "corporate/corporate_stateid_back_overlay.png",
        passportAnimationGif = "corporate/corporate_passport_animation.gif",
        stateIdAnimationGif = "corporate/corporate_stateid_animation.gif",
        
        // UI icons - corporate color scheme
        backButtonIcon = "corporate/corporate_back_button.png",
        cameraButtonIcon = "corporate/corporate_camera_button.png",
        scanFaceIcon = "corporate/corporate_scan_face.png",
        docScanIcon = "corporate/corporate_doc_scan.png",
        doneIcon = "corporate/corporate_success.png", // Use success icon for completion
        
        // Status icons - professional styling
        successIcon = "corporate/corporate_success.png",
        failedIcon = "corporate/corporate_failed.png",
        errorIcon = "corporate/corporate_error.png",
        systemErrorIcon = "corporate/corporate_error.png", // Reuse error icon
        
        // Approval-specific icons - NEW: Support for approval screens
        approvalIcon = "corporate/corporate_brand_image.png",           // General approval icon
        approvalRequestIcon = "corporate/corporate_brand_image.png",    // Approval request illustration  
        declinedIcon = "corporate/corporate_failed.png",               // Declined/rejected icon
        
        // Processing and verification images
        crossPlatformImage = "corporate/corporate_brand_image.png", // Use brand image for processing
        
        // Instruction icons for face scan intro - use distinct icons
        noGlassesIcon = "corporate/corporate_scan_face.png",    // Face scan for glasses
        noHatIcon = "corporate/corporate_back_button.png",      // Back button for hat (different shape)
        noMaskIcon = "corporate/corporate_error.png",           // Error icon for mask (warning)
        goodLightIcon = "corporate/corporate_success.png",      // Success icon for good lighting
        
        // Brand assets - corporate identity
        brandLogo = "corporate/corporate_logo.png",
        brandImage = "corporate/corporate_brand_image.png",
        
        // Document selection icons
        passportIcon = "corporate/corporate_passport_icon.png",
        stateIdIcon = "corporate/corporate_stateid_icon.png",
        
        // Configuration
        defaultLoadingStrategy = ImageLoadingStrategy.ASSET,
        enableCaching = true,
        enableFallback = true,
        preloadImages = true
    )
    
    /**
     * Modern Theme - Sleek, modern overrides with gradients
     */
    val MODERN = SDKImageOverrides(
        // Face scan assets - modern styling
        faceOverlay = "modern_face_overlay",
        faceUpGif = "modern_face_up",
        faceDownGif = "modern_face_down",
        phoneUpGif = "modern_phone_up",
        phoneDownGif = "modern_phone_down",
        
        // Document assets - modern design
        passportOverlay = "modern_passport_overlay",
        stateIdFrontOverlay = "modern_stateid_front_overlay",
        stateIdBackOverlay = "modern_stateid_back_overlay",
        
        // UI icons - modern flat design
        backButtonIcon = "modern_back_button",
        cameraButtonIcon = "modern_camera_button",
        scanFaceIcon = "modern_scan_face",
        docScanIcon = "modern_doc_scan",
        
        // Status icons - modern styling
        successIcon = "modern_success",
        failedIcon = "modern_failed",
        errorIcon = "modern_error",
        
        // Brand assets
        brandLogo = "modern_logo",
        
        // Document selection icons
        passportIcon = "modern_passport_icon",
        stateIdIcon = "modern_stateid_icon",
        
        // Configuration
        defaultLoadingStrategy = ImageLoadingStrategy.ASSET,
        enableCaching = true,
        enableFallback = true,
        preloadImages = false
    )
    
    /**
     * Custom URL-based overrides for testing web-hosted images
     */
    val URL_BASED = SDKImageOverrides(
        // Example URL-based overrides (these would be real URLs in production)
        faceOverlay = "https://example.com/custom/face_overlay.png",
        brandLogo = "https://example.com/custom/logo.png",
        successIcon = "https://example.com/custom/success.png",
        failedIcon = "https://example.com/custom/failed.png",
        
        // Mixed strategies - some URLs, some assets
        backButtonIcon = "custom_back_button", // Asset
        passportIcon = "https://example.com/custom/passport_icon.png", // URL
        
        // Configuration for URL loading
        defaultLoadingStrategy = ImageLoadingStrategy.AUTO_DETECT,
        enableCaching = true,
        cacheDurationMs = 60 * 60 * 1000L, // 1 hour cache
        enableFallback = true,
        preloadImages = false
    )
    
    /**
     * File-based overrides for testing local file system images
     */
    val FILE_BASED = SDKImageOverrides(
        // File-based overrides using android_asset paths
        faceOverlay = "file:///android_asset/custom_images/face_overlay.png",
        brandLogo = "file:///android_asset/custom_images/logo.png",
        successIcon = "file:///android_asset/custom_images/success.png",
        
        // Configuration
        defaultLoadingStrategy = ImageLoadingStrategy.FILE,
        enableCaching = true,
        enableFallback = true,
        preloadImages = false
    )
    
    /**
     * Custom overrides with extensibility example
     */
    val CUSTOM_EXTENDED = SDKImageOverrides(
        // Standard overrides
        faceOverlay = "custom_face_overlay",
        brandLogo = "custom_logo",
        
        // Custom overrides using the extensibility map
        customOverrides = mapOf(
            "special_animation" to "special_custom_animation",
            "holiday_theme_overlay" to "holiday_face_overlay",
            "branded_background" to "company_background_pattern",
            "custom_loading_spinner" to "branded_loading_animation"
        ),
        
        // Configuration
        defaultLoadingStrategy = ImageLoadingStrategy.ASSET,
        enableCaching = true,
        enableFallback = true,
        preloadImages = true
    )
}

/**
 * Image Override Option for UI selection
 */
enum class ImageOverrideOption(
    val displayName: String,
    val description: String,
    val overrides: SDKImageOverrides
) {
    DEFAULT(
        displayName = "SDK Default",
        description = "Use default SDK images and animations",
        overrides = SampleImageOverrides.DEFAULT
    ),
    
    CORPORATE(
        displayName = "Corporate Images",
        description = "Professional styling with Freepik Special Lineal icons",
        overrides = SampleImageOverrides.CORPORATE
    )
}

/**
 * Helper functions for image override management
 */
object ImageOverrideHelper {
    
    /**
     * Get override statistics for debugging
     */
    fun getOverrideStats(overrides: SDKImageOverrides): Map<String, Any> {
        var activeOverrides = 0
        var totalFields = 0
        
        // Count active overrides (non-null fields)
        if (overrides.faceOverlay != null) activeOverrides++
        if (overrides.faceUpGif != null) activeOverrides++
        if (overrides.faceDownGif != null) activeOverrides++
        if (overrides.phoneUpGif != null) activeOverrides++
        if (overrides.phoneDownGif != null) activeOverrides++
        if (overrides.passportOverlay != null) activeOverrides++
        if (overrides.stateIdFrontOverlay != null) activeOverrides++
        if (overrides.stateIdBackOverlay != null) activeOverrides++
        if (overrides.passportAnimationGif != null) activeOverrides++
        if (overrides.stateIdAnimationGif != null) activeOverrides++
        if (overrides.backButtonIcon != null) activeOverrides++
        if (overrides.cameraButtonIcon != null) activeOverrides++
        if (overrides.scanFaceIcon != null) activeOverrides++
        if (overrides.docScanIcon != null) activeOverrides++
        if (overrides.passportIcon != null) activeOverrides++
        if (overrides.stateIdIcon != null) activeOverrides++
        if (overrides.successIcon != null) activeOverrides++
        if (overrides.failedIcon != null) activeOverrides++
        if (overrides.errorIcon != null) activeOverrides++
        if (overrides.brandLogo != null) activeOverrides++
        if (overrides.brandImage != null) activeOverrides++
        
        activeOverrides += overrides.customOverrides.size
        totalFields = 21 + overrides.customOverrides.size // Base fields + custom
        
        return mapOf(
            "activeOverrides" to activeOverrides,
            "totalFields" to totalFields,
            "overridePercentage" to if (totalFields > 0) (activeOverrides * 100 / totalFields) else 0,
            "loadingStrategy" to overrides.defaultLoadingStrategy.name,
            "cachingEnabled" to overrides.enableCaching,
            "fallbackEnabled" to overrides.enableFallback,
            "preloadEnabled" to overrides.preloadImages,
            "customOverrideCount" to overrides.customOverrides.size
        )
    }
    
    /**
     * Validate override configuration
     */
    fun validateOverrides(overrides: SDKImageOverrides): List<String> {
        val issues = mutableListOf<String>()
        
        // Check for potential URL format issues
        listOf(
            overrides.faceOverlay,
            overrides.brandLogo,
            overrides.successIcon,
            overrides.failedIcon
        ).filterNotNull().forEach { override ->
            if (override.startsWith("http://")) {
                issues.add("HTTP URL detected (consider HTTPS): $override")
            }
            if (override.contains(" ")) {
                issues.add("URL contains spaces: $override")
            }
        }
        
        // Check cache configuration
        if (overrides.enableCaching && overrides.cacheDurationMs < 1000) {
            issues.add("Cache duration is very short (${overrides.cacheDurationMs}ms)")
        }
        
        // Check preload configuration
        if (overrides.preloadImages && overrides.customOverrides.size > 10) {
            issues.add("Preloading enabled with many custom overrides (${overrides.customOverrides.size}) - may impact startup time")
        }
        
        return issues
    }
}
