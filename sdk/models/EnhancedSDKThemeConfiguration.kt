/*
 * File: EnhancedSDKThemeConfiguration.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sdk.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * Comprehensive SDK Theme Configuration
 * Allows complete customization of fonts, colors, icons, and verbiage
 */
@Parcelize
data class EnhancedSDKThemeConfiguration(
    // === BRAND IDENTITY ===
    val brandName: String = "ArtiusID",
    val brandLogoUrl: String? = null,
    val brandLogoResourceName: String? = null, // For local resources
    
    // === TYPOGRAPHY ===
    val typography: SDKTypography = SDKTypography(),
    
    // === COLOR SCHEME ===
    val colorScheme: SDKColorScheme = SDKColorScheme(),
    
    // === ICON THEMING ===
    val iconTheme: SDKIconTheme = SDKIconTheme(),
    
    // === TEXT/VERBIAGE CUSTOMIZATION ===
    val textContent: SDKTextContent = SDKTextContent(),
    
    // === COMPONENT STYLING ===
    val componentStyling: SDKComponentStyling = SDKComponentStyling(),
    
    // === LAYOUT & SPACING ===
    val layoutConfig: SDKLayoutConfig = SDKLayoutConfig(),
    
    // === ANIMATION & TRANSITIONS ===
    val animationConfig: SDKAnimationConfig = SDKAnimationConfig()
) : Parcelable

/**
 * Typography Configuration
 */
@Parcelize
data class SDKTypography(
    // Font Family (system font names or custom font resource names)
    val fontFamily: String = "default", // "roboto", "custom_font", etc.
    val customFontResourcePrefix: String? = null, // For custom fonts: "my_font" -> my_font_regular.ttf
    
    // Font Sizes (in SP)
    val headlineLarge: Float = 32f,
    val headlineMedium: Float = 28f,
    val headlineSmall: Float = 24f,
    val titleLarge: Float = 22f,
    val titleMedium: Float = 16f,
    val titleSmall: Float = 14f,
    val bodyLarge: Float = 16f,
    val bodyMedium: Float = 14f,
    val bodySmall: Float = 12f,
    val labelLarge: Float = 14f,
    val labelMedium: Float = 12f,
    val labelSmall: Float = 11f,
    
    // Font Weights
    val headlineWeight: String = "bold", // "normal", "bold", "light", "medium"
    val titleWeight: String = "medium",
    val bodyWeight: String = "normal",
    val labelWeight: String = "medium",
    
    // Letter Spacing
    val letterSpacing: Float = 0f,
    val lineHeight: Float = 1.5f
) : Parcelable

/**
 * Color Scheme Configuration
 */
@Parcelize
data class SDKColorScheme(
    // === PRIMARY COLORS ===
    val primaryColorHex: String = "#263238",
    val onPrimaryColorHex: String = "#FFFFFF",
    val primaryContainerColorHex: String = "#37474F",
    val onPrimaryContainerColorHex: String = "#FFFFFF",
    
    // === SECONDARY COLORS ===
    val secondaryColorHex: String = "#F57C00",
    val onSecondaryColorHex: String = "#263238",
    val secondaryContainerColorHex: String = "#FFE0B2",
    val onSecondaryContainerColorHex: String = "#263238",
    
    // === SURFACE COLORS ===
    val backgroundColorHex: String = "#263238",
    val onBackgroundColorHex: String = "#FFFFFF",
    val surfaceColorHex: String = "#37474F",
    val onSurfaceColorHex: String = "#FFFFFF",
    val surfaceVariantColorHex: String = "#455A64",
    val onSurfaceVariantColorHex: String = "#FFFFFF",
    
    // === STATUS COLORS ===
    val successColorHex: String = "#4CAF50",
    val onSuccessColorHex: String = "#FFFFFF",
    val errorColorHex: String = "#D32F2F",
    val onErrorColorHex: String = "#FFFFFF",
    val warningColorHex: String = "#FF9800",
    val onWarningColorHex: String = "#000000",
    val infoColorHex: String = "#2196F3",
    val onInfoColorHex: String = "#FFFFFF",
    
    // === VERIFICATION SPECIFIC ===
    val faceDetectionOverlayColorHex: String = "#4CAF50",
    val documentScanOverlayColorHex: String = "#F57C00",
    val nfcScanColorHex: String = "#2196F3",
    val processingColorHex: String = "#FF9800",
    
    // === STEP INDICATOR COLORS ===
    val pendingStepColorHex: String = "#9E9E9E",
    val activeStepColorHex: String = "#F57C00",
    val completedStepColorHex: String = "#4CAF50",
    
    // === BUTTON COLORS ===
    val primaryButtonColorHex: String = "#F57C00",
    val primaryButtonTextColorHex: String = "#FFFFFF",
    val secondaryButtonColorHex: String = "#37474F",
    val secondaryButtonTextColorHex: String = "#FFFFFF",
    val disabledButtonColorHex: String = "#9E9E9E",
    val disabledButtonTextColorHex: String = "#616161",
    
    // === BORDER & OUTLINE COLORS ===
    val outlineColorHex: String = "#616161",
    val outlineVariantColorHex: String = "#9E9E9E",
    
    // === OVERLAY & SCRIM ===
    val scrimColorHex: String = "#000000",
    val overlayColorHex: String = "#000000"
) : Parcelable

/**
 * Icon Theme Configuration
 */
@Parcelize
data class SDKIconTheme(
    // Icon Style
    val iconStyle: String = "default", // "default", "outlined", "filled", "rounded", "sharp"
    val customIconResourcePrefix: String? = null, // For custom icons: "my_icon" -> my_icon_camera.xml
    
    // Icon Sizes (in DP)
    val smallIconSize: Float = 16f,
    val mediumIconSize: Float = 24f,
    val largeIconSize: Float = 32f,
    val extraLargeIconSize: Float = 48f,
    
    // General Icon Colors
    val primaryIconColorHex: String = "#FFFFFF",
    val secondaryIconColorHex: String = "#9E9E9E",
    val accentIconColorHex: String = "#F57C00",
    val disabledIconColorHex: String = "#616161",
    
    // Navigation & UI Icons
    val navigationIconColorHex: String = "#FFFFFF", // Back buttons, close buttons
    val actionIconColorHex: String = "#F57C00", // Action buttons, confirm icons
    
    // Instruction & Guide Icons
    val instructionIconColorHex: String = "#F57C00", // Tips, guides, info icons
    val warningIconColorHex: String = "#FF9800", // Warning icons
    val errorIconColorHex: String = "#D32F2F", // Error icons
    val successIconColorHex: String = "#4CAF50", // Success, checkmark icons
    
    // Document & Verification Icons
    val documentIconColorHex: String = "#F57C00", // Document-related icons
    val cameraIconColorHex: String = "#FFFFFF", // Camera icons
    val scanIconColorHex: String = "#F57C00", // Scanning overlay icons
    
    // Biometric & Security Icons
    val biometricIconColorHex: String = "#F57C00", // Face scan, fingerprint icons
    val securityIconColorHex: String = "#4CAF50", // Security, lock icons
    val nfcIconColorHex: String = "#F57C00", // NFC-related icons
    
    // Status Icons
    val statusActiveIconColorHex: String = "#4CAF50", // Active/connected status
    val statusInactiveIconColorHex: String = "#9E9E9E", // Inactive/disconnected status
    val statusProcessingIconColorHex: String = "#F57C00", // Processing/loading status
    
    // Custom Icon Mappings (resource names for specific icons)
    val customIcons: @RawValue Map<String, String> = emptyMap()
    // Example: mapOf(
    //   "camera" to "my_custom_camera_icon",
    //   "face" to "my_custom_face_icon",
    //   "document" to "my_custom_document_icon"
    // )
) : Parcelable

/**
 * Text Content Customization
 */
@Parcelize
data class SDKTextContent(
    // === WELCOME & INTRO ===
    val welcomeTitle: String = "Identity Verification",
    val welcomeSubtitle: String = "Secure and fast identity verification",
    val getStartedButton: String = "Get Started",
    
    // === DOCUMENT SCANNING ===
    val documentScanTitle: String = "Scan Your ID",
    val documentFrontInstruction: String = "Position your ID card in the frame",
    val documentBackInstruction: String = "Position the back of your ID card in the frame",
    val documentCaptureSuccess: String = "Document captured successfully",
    val documentRetakeButton: String = "Retake Photo",
    val documentContinueButton: String = "Continue",
    
    // === PASSPORT SCANNING ===
    val passportScanTitle: String = "Scan Your Passport",
    val passportInstruction: String = "Position your passport in the frame",
    val passportMrzInstruction: String = "Ensure the MRZ (bottom text) is clearly visible",
    val passportCaptureSuccess: String = "Passport captured successfully",
    
    // === NFC SCANNING ===
    val nfcScanTitle: String = "NFC Chip Reading",
    val nfcInstruction: String = "Hold your device near the passport",
    val nfcReadyToScan: String = "Ready to Scan",
    val nfcScanning: String = "Reading NFC chip...",
    val nfcSuccess: String = "NFC data read successfully",
    val nfcError: String = "Failed to read NFC chip",
    val nfcRetryButton: String = "Try Again",
    
    // === FACE SCANNING ===
    val faceScanTitle: String = "Face Verification",
    val faceInstruction: String = "Position your face in the circle",
    val faceHoldStill: String = "Hold still...",
    val faceSuccess: String = "Face captured successfully",
    val faceRetakeButton: String = "Retake Photo",
    
    // === PROCESSING ===
    val processingTitle: String = "Processing",
    val processingMessage: String = "Verifying your identity...",
    val processingPleaseWait: String = "Please wait while we process your information",
    
    // === RESULTS ===
    val verificationSuccessTitle: String = "Verification Successful",
    val verificationSuccessMessage: String = "Your identity has been verified",
    val verificationFailedTitle: String = "Verification Failed",
    val verificationFailedMessage: String = "Please try again or contact support",
    val continueButton: String = "Continue",
    val tryAgainButton: String = "Try Again",
    val contactSupportButton: String = "Contact Support",
    
    // === ERROR MESSAGES ===
    val cameraPermissionTitle: String = "Camera Permission Required",
    val cameraPermissionMessage: String = "Please grant camera permission to continue",
    val networkErrorTitle: String = "Connection Error",
    val networkErrorMessage: String = "Please check your internet connection",
    val genericErrorTitle: String = "Something went wrong",
    val genericErrorMessage: String = "Please try again later",
    
    // === BUTTONS & ACTIONS ===
    val backButton: String = "Back",
    val nextButton: String = "Next",
    val skipButton: String = "Skip",
    val cancelButton: String = "Cancel",
    val doneButton: String = "Done",
    val closeButton: String = "Close",
    val grantPermissionButton: String = "Grant Permission",
    
    // === VALIDATION MESSAGES ===
    val documentTooFar: String = "Move closer to the document",
    val documentTooClose: String = "Move further from the document",
    val documentNotCentered: String = "Center the document in the frame",
    val documentBlurry: String = "Hold steady for a clear image",
    val faceNotDetected: String = "Face not detected",
    val faceTooFar: String = "Move closer to the camera",
    val faceTooClose: String = "Move further from the camera",
    val faceNotCentered: String = "Center your face in the circle",
    val multipleFacesDetected: String = "Multiple faces detected"
) : Parcelable

/**
 * Component Styling Configuration
 */
@Parcelize
data class SDKComponentStyling(
    // === BUTTONS ===
    val buttonCornerRadius: Float = 8f,
    val buttonElevation: Float = 4f,
    val buttonHeight: Float = 48f,
    val buttonMinWidth: Float = 120f,
    
    // === CARDS ===
    val cardCornerRadius: Float = 12f,
    val cardElevation: Float = 8f,
    
    // === INPUT FIELDS ===
    val inputFieldCornerRadius: Float = 8f,
    val inputFieldHeight: Float = 56f,
    
    // === OVERLAYS ===
    val overlayCornerRadius: Float = 16f,
    val overlayOpacity: Float = 0.8f,
    
    // === BORDERS ===
    val borderWidth: Float = 1f,
    val focusedBorderWidth: Float = 2f,
    
    // === SHADOWS ===
    val shadowBlurRadius: Float = 8f,
    val shadowOffsetX: Float = 0f,
    val shadowOffsetY: Float = 4f
) : Parcelable

/**
 * Layout Configuration
 */
@Parcelize
data class SDKLayoutConfig(
    // === PADDING & MARGINS ===
    val screenPadding: Float = 16f,
    val componentSpacing: Float = 16f,
    val smallSpacing: Float = 8f,
    val largeSpacing: Float = 24f,
    
    // === CONTENT SIZING ===
    val maxContentWidth: Float = 400f,
    val minTouchTarget: Float = 48f,
    
    // === CAMERA OVERLAY ===
    val documentOverlayAspectRatio: Float = 1.6f, // Standard ID card ratio
    val faceOverlaySize: Float = 200f,
    val overlayStrokeWidth: Float = 4f
) : Parcelable

/**
 * Animation Configuration
 */
@Parcelize
data class SDKAnimationConfig(
    // === TIMING ===
    val shortAnimationDuration: Int = 200,
    val mediumAnimationDuration: Int = 400,
    val longAnimationDuration: Int = 600,
    
    // === TRANSITIONS ===
    val enablePageTransitions: Boolean = true,
    val enableButtonAnimations: Boolean = true,
    val enableProgressAnimations: Boolean = true,
    val enableSuccessAnimations: Boolean = true,
    
    // === EASING ===
    val animationEasing: String = "ease_in_out" // "linear", "ease_in", "ease_out", "ease_in_out"
) : Parcelable
