/*
 * File: SampleAppThemes.kt
 * Author: Todd Bryant
 * Company: artius.iD, Inc.
 */

package com.artiusid.sample.theme

import com.artiusid.sdk.models.*

/**
 * Comprehensive Theme Configurations for Sample App
 * Demonstrates the full power of the enhanced theming system
 */
object SampleAppThemes {
    
    /**
     * artius.iD Default Theme - MATCHING iOS EnhancedSDKThemeConfiguration.artiusIDDefault exactly:
     * Dark blue background #22354D, white text, orange #F58220 secondary (Auth button).
     */
    val ARTIUSID_DEFAULT = EnhancedSDKThemeConfiguration(
        brandName = "artius.iD",
        typography = SDKTypography(
            fontFamily = "default",
            headlineLarge = 32f,
            headlineMedium = 28f,
            titleLarge = 22f,
            bodyLarge = 16f,
            bodyMedium = 14f,
            headlineWeight = "bold",
            titleWeight = "medium",
            bodyWeight = "normal"
        ),
        colorScheme = SDKColorScheme(
            primaryColorHex = "#FFFFFF",
            secondaryColorHex = "#F58220",
            backgroundColorHex = "#22354D",
            surfaceColorHex = "#22354D",
            onPrimaryColorHex = "#22354D",
            onSecondaryColorHex = "#FFFFFF",
            onBackgroundColorHex = "#FFFFFF",
            onSurfaceColorHex = "#FFFFFF",
            successColorHex = "#34C759",
            errorColorHex = "#FF3B30",
            warningColorHex = "#FF9500",
            primaryButtonColorHex = "#F58220",
            primaryButtonTextColorHex = "#FFFFFF",
            secondaryButtonColorHex = "#F58220",
            secondaryButtonTextColorHex = "#FFFFFF"
        ),
        iconTheme = SDKIconTheme(
            iconStyle = "filled",
            mediumIconSize = 24f,
            primaryIconColorHex = "#F58220",
            secondaryIconColorHex = "#F58220",
            accentIconColorHex = "#F58220",
            disabledIconColorHex = "#ADB5BD",
            navigationIconColorHex = "#F58220",
            actionIconColorHex = "#F58220",
            instructionIconColorHex = "#FF9500",
            warningIconColorHex = "#FF9500",
            errorIconColorHex = "#FF3B30",
            successIconColorHex = "#34C759",
            documentIconColorHex = "#F58220",
            cameraIconColorHex = "#F58220",
            scanIconColorHex = "#AF52DE",
            biometricIconColorHex = "#34C759",
            securityIconColorHex = "#F58220",
            nfcIconColorHex = "#30B0C0",
            statusActiveIconColorHex = "#34C759",
            statusInactiveIconColorHex = "#ADB5BD",
            statusProcessingIconColorHex = "#FF9500"
        ),
        textContent = SDKTextContent(
            welcomeTitle = "artius.iD Verification",
            welcomeSubtitle = "Secure identity verification powered by artius.iD",
            documentScanTitle = "Scan Your ID",
            passportScanTitle = "Scan Your Passport",
            faceScanTitle = "Face Verification",
            processingTitle = "Processing",
            verificationSuccessTitle = "Verification Complete"
        ),
        componentStyling = SDKComponentStyling(
            buttonCornerRadius = 12f,
            cardCornerRadius = 12f,
            buttonHeight = 48f
        ),
        layoutConfig = SDKLayoutConfig(
            screenPadding = 16f,
            componentSpacing = 16f
        )
    )
    
    /**
     * Corporate Blue Theme - MATCHING iOS SampleAppThemes.CORPORATE_BLUE
     */
    val CORPORATE_BLUE = EnhancedSDKThemeConfiguration(
        brandName = "Corporate",
        typography = SDKTypography(
            fontFamily = "sans-serif",
            headlineLarge = 30f,
            headlineMedium = 26f,
            titleLarge = 20f,
            bodyLarge = 16f,
            bodyMedium = 14f,
            headlineWeight = "bold",
            titleWeight = "medium",
            bodyWeight = "normal",
            letterSpacing = 0.5f
        ),
        colorScheme = SDKColorScheme(
            primaryColorHex = "#1565C0",
            secondaryColorHex = "#42A5F5",
            backgroundColorHex = "#F5F5F5",
            surfaceColorHex = "#FFFFFF",
            onPrimaryColorHex = "#FFFFFF",
            onSecondaryColorHex = "#FFFFFF",
            onBackgroundColorHex = "#000000",
            onSurfaceColorHex = "#000000",
            successColorHex = "#34C759",
            errorColorHex = "#FF3B30",
            warningColorHex = "#FF9500",
            primaryButtonColorHex = "#1565C0",
            primaryButtonTextColorHex = "#FFFFFF",
            secondaryButtonColorHex = "#42A5F5",
            secondaryButtonTextColorHex = "#FFFFFF",
            faceDetectionOverlayColorHex = "#42A5F5",
            documentScanOverlayColorHex = "#42A5F5"
        ),
        
        iconTheme = SDKIconTheme(
            iconStyle = "outlined",
            mediumIconSize = 24f,
            primaryIconColorHex = "#1565C0",
            secondaryIconColorHex = "#42A5F5",
            accentIconColorHex = "#42A5F5",
            disabledIconColorHex = "#BDBDBD",
            navigationIconColorHex = "#1565C0",
            actionIconColorHex = "#42A5F5",
            instructionIconColorHex = "#42A5F5",
            warningIconColorHex = "#FF9500",
            errorIconColorHex = "#FF3B30",
            successIconColorHex = "#34C759",
            documentIconColorHex = "#42A5F5",
            cameraIconColorHex = "#1565C0",
            scanIconColorHex = "#42A5F5",
            biometricIconColorHex = "#42A5F5",
            securityIconColorHex = "#1565C0",
            nfcIconColorHex = "#42A5F5",
            statusActiveIconColorHex = "#34C759",
            statusInactiveIconColorHex = "#757575",
            statusProcessingIconColorHex = "#42A5F5"
        ),
        textContent = SDKTextContent(
            welcomeTitle = "Secure Verification",
            welcomeSubtitle = "Professional corporate theme",
            documentScanTitle = "Document Verification",
            passportScanTitle = "Passport Verification",
            faceScanTitle = "Biometric Verification",
            processingTitle = "Processing Securely",
            verificationSuccessTitle = "Verification Complete",
            getStartedButton = "Start Verification",
            continueButton = "Continue",
            tryAgainButton = "Try Again"
        ),
        
        componentStyling = SDKComponentStyling(
            buttonCornerRadius = 4f, // More rectangular for corporate look
            cardCornerRadius = 8f,
            buttonHeight = 52f, // Slightly taller buttons
            buttonElevation = 2f
        ),
        
        layoutConfig = SDKLayoutConfig(
            screenPadding = 20f,
            componentSpacing = 20f,
            largeSpacing = 32f
        )
    )
    
    /**
     * Dark Theme - MATCHING iOS SampleAppThemes.DARK_THEME
     */
    val DARK_THEME = EnhancedSDKThemeConfiguration(
        brandName = "Dark Mode",
        typography = SDKTypography(
            fontFamily = "default",
            headlineLarge = 32f,
            headlineMedium = 28f,
            titleLarge = 22f,
            bodyLarge = 16f,
            bodyMedium = 14f,
            headlineWeight = "medium",
            titleWeight = "medium",
            bodyWeight = "normal",
            letterSpacing = 0.25f,
            lineHeight = 1.6f
        ),
        colorScheme = SDKColorScheme(
            primaryColorHex = "#BB86FC",
            secondaryColorHex = "#03DAC6",
            backgroundColorHex = "#121212",
            surfaceColorHex = "#1E1E1E",
            onPrimaryColorHex = "#000000",
            onSecondaryColorHex = "#000000",
            onBackgroundColorHex = "#FFFFFF",
            onSurfaceColorHex = "#FFFFFF",
            successColorHex = "#34C759",
            errorColorHex = "#FF3B30",
            warningColorHex = "#FF9500",
            primaryButtonColorHex = "#BB86FC",
            primaryButtonTextColorHex = "#000000",
            secondaryButtonColorHex = "#03DAC6",
            secondaryButtonTextColorHex = "#000000",
            faceDetectionOverlayColorHex = "#03DAC6",
            documentScanOverlayColorHex = "#03DAC6"
        ),
        iconTheme = SDKIconTheme(
            iconStyle = "rounded",
            mediumIconSize = 24f,
            primaryIconColorHex = "#BB86FC",
            secondaryIconColorHex = "#03DAC6",
            accentIconColorHex = "#03DAC6",
            disabledIconColorHex = "#B0BEC5",
            navigationIconColorHex = "#BB86FC",
            actionIconColorHex = "#03DAC6",
            instructionIconColorHex = "#03DAC6",
            warningIconColorHex = "#FF9500",
            errorIconColorHex = "#FF3B30",
            successIconColorHex = "#34C759",
            documentIconColorHex = "#03DAC6",
            cameraIconColorHex = "#BB86FC",
            scanIconColorHex = "#03DAC6",
            biometricIconColorHex = "#03DAC6",
            securityIconColorHex = "#34C759",
            nfcIconColorHex = "#03DAC6",
            statusActiveIconColorHex = "#34C759",
            statusInactiveIconColorHex = "#B0BEC5",
            statusProcessingIconColorHex = "#03DAC6"
        ),
        textContent = SDKTextContent(
            welcomeTitle = "Professional Verification",
            welcomeSubtitle = "Dark mode for low-light",
            documentScanTitle = "Document Verification",
            passportScanTitle = "Passport Verification",
            faceScanTitle = "Biometric Verification",
            processingTitle = "Processing Verification",
            verificationSuccessTitle = "Successfully Verified",
            getStartedButton = "Begin Process",
            continueButton = "Continue",
            tryAgainButton = "Retry"
        ),
        
        componentStyling = SDKComponentStyling(
            buttonCornerRadius = 12f, // More rounded for modern look
            cardCornerRadius = 16f,
            buttonHeight = 48f,
            buttonElevation = 8f, // Higher elevation for dark theme
            cardElevation = 12f
        ),
        
        layoutConfig = SDKLayoutConfig(
            screenPadding = 16f,
            componentSpacing = 16f,
            smallSpacing = 12f
        )
    )
    
    /**
     * Banking Theme - MATCHING iOS SampleAppThemes.BANKING_THEME
     */
    val BANKING_THEME = EnhancedSDKThemeConfiguration(
        brandName = "Banking",
        typography = SDKTypography(
            fontFamily = "serif",
            headlineLarge = 28f,
            headlineMedium = 24f,
            titleLarge = 20f,
            bodyLarge = 16f,
            bodyMedium = 14f,
            headlineWeight = "bold",
            titleWeight = "bold",
            bodyWeight = "normal",
            letterSpacing = 0f,
            lineHeight = 1.4f
        ),
        colorScheme = SDKColorScheme(
            primaryColorHex = "#004D40",
            secondaryColorHex = "#00796B",
            backgroundColorHex = "#FAFAFA",
            surfaceColorHex = "#FFFFFF",
            onPrimaryColorHex = "#FFFFFF",
            onSecondaryColorHex = "#FFFFFF",
            onBackgroundColorHex = "#004D40",
            onSurfaceColorHex = "#004D40",
            successColorHex = "#34C759",
            errorColorHex = "#FF3B30",
            warningColorHex = "#FF9500",
            primaryButtonColorHex = "#004D40",
            primaryButtonTextColorHex = "#FFFFFF",
            secondaryButtonColorHex = "#00796B",
            secondaryButtonTextColorHex = "#FFFFFF",
            faceDetectionOverlayColorHex = "#00796B",
            documentScanOverlayColorHex = "#00796B"
        ),
        
        iconTheme = SDKIconTheme(
            iconStyle = "filled",
            mediumIconSize = 22f,
            primaryIconColorHex = "#004D40",
            secondaryIconColorHex = "#00796B",
            accentIconColorHex = "#00796B",
            disabledIconColorHex = "#BDBDBD",
            navigationIconColorHex = "#004D40",
            actionIconColorHex = "#00796B",
            instructionIconColorHex = "#00796B",
            warningIconColorHex = "#FF9500",
            errorIconColorHex = "#FF3B30",
            successIconColorHex = "#34C759",
            documentIconColorHex = "#00796B",
            cameraIconColorHex = "#004D40",
            scanIconColorHex = "#00796B",
            biometricIconColorHex = "#00796B",
            securityIconColorHex = "#34C759",
            nfcIconColorHex = "#00796B",
            statusActiveIconColorHex = "#34C759",
            statusInactiveIconColorHex = "#757575",
            statusProcessingIconColorHex = "#00796B"
        ),
        textContent = SDKTextContent(
            welcomeTitle = "Trusted Verification",
            welcomeSubtitle = "Financial institution theme",
            documentScanTitle = "Document Verification",
            passportScanTitle = "Passport Verification",
            faceScanTitle = "Identity Verification",
            processingTitle = "Processing Verification",
            verificationSuccessTitle = "Verification Complete",
            getStartedButton = "Get Started",
            continueButton = "Continue",
            tryAgainButton = "Try Again",
            documentFrontInstruction = "Please position your government-issued ID within the frame",
            documentBackInstruction = "Please position the back of your ID within the frame",
            faceInstruction = "Please position your face within the circle for biometric verification"
        ),
        
        componentStyling = SDKComponentStyling(
            buttonCornerRadius = 6f, // Conservative rounded corners
            cardCornerRadius = 8f,
            buttonHeight = 50f, // Taller for easier touch
            buttonElevation = 1f, // Minimal elevation for conservative look
            cardElevation = 2f
        ),
        
        layoutConfig = SDKLayoutConfig(
            screenPadding = 24f, // More padding for spacious feel
            componentSpacing = 20f,
            largeSpacing = 32f
        )
    )
    
    /**
     * FinTech Theme - MATCHING iOS SampleAppThemes.FINTECH_THEME
     */
    val FINTECH_THEME = EnhancedSDKThemeConfiguration(
        brandName = "FinTech",
        typography = SDKTypography(
            fontFamily = "sans-serif",
            headlineLarge = 36f,
            headlineMedium = 30f,
            titleLarge = 24f,
            bodyLarge = 18f,
            bodyMedium = 16f,
            headlineWeight = "light",
            titleWeight = "normal",
            bodyWeight = "normal",
            letterSpacing = 1f,
            lineHeight = 1.5f
        ),
        colorScheme = SDKColorScheme(
            primaryColorHex = "#6200EA",
            secondaryColorHex = "#00BFA5",
            backgroundColorHex = "#FFFFFF",
            surfaceColorHex = "#FFFFFF",
            onPrimaryColorHex = "#FFFFFF",
            onSecondaryColorHex = "#FFFFFF",
            onBackgroundColorHex = "#000000",
            onSurfaceColorHex = "#000000",
            successColorHex = "#34C759",
            errorColorHex = "#FF3B30",
            warningColorHex = "#FF9500",
            primaryButtonColorHex = "#6200EA",
            primaryButtonTextColorHex = "#FFFFFF",
            secondaryButtonColorHex = "#00BFA5",
            secondaryButtonTextColorHex = "#FFFFFF",
            faceDetectionOverlayColorHex = "#00BFA5",
            documentScanOverlayColorHex = "#00BFA5"
        ),
        
        iconTheme = SDKIconTheme(
            iconStyle = "sharp",
            mediumIconSize = 26f,
            primaryIconColorHex = "#6200EA",
            secondaryIconColorHex = "#00BFA5",
            accentIconColorHex = "#00BFA5",
            disabledIconColorHex = "#ADB5BD",
            navigationIconColorHex = "#6200EA",
            actionIconColorHex = "#00BFA5",
            instructionIconColorHex = "#00BFA5",
            warningIconColorHex = "#FF9500",
            errorIconColorHex = "#FF3B30",
            successIconColorHex = "#34C759",
            documentIconColorHex = "#00BFA5",
            cameraIconColorHex = "#6200EA",
            scanIconColorHex = "#00BFA5",
            biometricIconColorHex = "#00BFA5",
            securityIconColorHex = "#34C759",
            nfcIconColorHex = "#00BFA5",
            statusActiveIconColorHex = "#34C759",
            statusInactiveIconColorHex = "#ADB5BD",
            statusProcessingIconColorHex = "#00BFA5"
        ),
        textContent = SDKTextContent(
            welcomeTitle = "Modern Verification",
            welcomeSubtitle = "Modern fintech theme",
            documentScanTitle = "Document Verification",
            passportScanTitle = "Passport Verification",
            faceScanTitle = "Biometric Verification",
            processingTitle = "Processing",
            verificationSuccessTitle = "Verification Complete",
            getStartedButton = "Begin",
            continueButton = "Continue",
            tryAgainButton = "Retry",
            processingMessage = "Processing your verification..."
        ),
        
        componentStyling = SDKComponentStyling(
            buttonCornerRadius = 16f, // Very rounded for modern look
            cardCornerRadius = 20f,
            buttonHeight = 56f, // Taller modern buttons
            buttonElevation = 6f,
            cardElevation = 8f
        ),
        
        layoutConfig = SDKLayoutConfig(
            screenPadding = 20f,
            componentSpacing = 24f,
            largeSpacing = 40f
        ),
        
        animationConfig = SDKAnimationConfig(
            enablePageTransitions = true,
            enableButtonAnimations = true,
            enableProgressAnimations = true,
            mediumAnimationDuration = 300,
            longAnimationDuration = 500
        )
    )
}

/**
 * Theme Options Enum for Sample App - MATCHING iOS ThemeOption display names and descriptions
 */
enum class EnhancedThemeOption(
    val displayName: String,
    val description: String,
    val themeConfig: EnhancedSDKThemeConfiguration
) {
    ARTIUSID_DEFAULT(
        "artius.iD Default",
        "Default artius.iD brand colors",
        SampleAppThemes.ARTIUSID_DEFAULT
    ),
    CORPORATE_BLUE(
        "Corporate Blue",
        "Professional corporate theme",
        SampleAppThemes.CORPORATE_BLUE
    ),
    DARK_THEME(
        "Dark Theme",
        "Dark mode for low-light",
        SampleAppThemes.DARK_THEME
    ),
    BANKING_THEME(
        "Banking Theme",
        "Financial institution theme",
        SampleAppThemes.BANKING_THEME
    ),
    FINTECH_THEME(
        "FinTech Theme",
        "Modern fintech theme",
        SampleAppThemes.FINTECH_THEME
    )
}
