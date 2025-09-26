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
     * artius.iD Default Theme - REBUILT from iOS Screenshots
     * Based on the actual iOS standalone app screenshots provided
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
            // CORRECTED FROM iOS SCREENSHOTS
            primaryColorHex = "#FFFFFF", // White - primary color should be white, not dark blue
            secondaryColorHex = "#F58220", // Orange from iOS screenshots - used for "artius.iD", "Face Scan", buttons, icons
            backgroundColorHex = "#22354D", // Dark blue background from iOS screenshots
            surfaceColorHex = "#22354D", // Dark blue surface matching background
            onPrimaryColorHex = "#22354D", // Dark blue text on white primary
            onSecondaryColorHex = "#FFFFFF", // White text on orange secondary
            onBackgroundColorHex = "#FFFFFF", // White text on dark blue background
            onSurfaceColorHex = "#FFFFFF", // White text on dark blue surface
            successColorHex = "#4CAF50",
            errorColorHex = "#D32F2F",
            warningColorHex = "#FF9800",
            // BUTTON COLORS - Use secondary color (orange) for buttons
            primaryButtonColorHex = "#F58220", // Orange button background - use secondary color
            primaryButtonTextColorHex = "#FFFFFF", // White text on orange button
            secondaryButtonColorHex = "#F58220", // Orange button background - use secondary color
            secondaryButtonTextColorHex = "#FFFFFF" // White text on orange button
        ),
        
        iconTheme = SDKIconTheme(
            iconStyle = "default",
            mediumIconSize = 24f,
            // ICONS USE SECONDARY COLOR (ORANGE)
            primaryIconColorHex = "#F58220", // Orange - use secondary color for icons
            secondaryIconColorHex = "#F58220", // Orange - use secondary color for icons
            accentIconColorHex = "#F58220", // Orange - use secondary color for icons
            disabledIconColorHex = "#ADB5BD", // Light gray for disabled
            
            // Navigation & UI Icons - USE SECONDARY COLOR (ORANGE)
            navigationIconColorHex = "#F58220", // Orange - use secondary color for icons
            actionIconColorHex = "#F58220", // Orange - use secondary color for icons
            
            // Instruction & Guide Icons - USE SECONDARY COLOR (ORANGE)
            instructionIconColorHex = "#F58220", // Orange - use secondary color for icons
            warningIconColorHex = "#F58220", // Orange - use secondary color for icons
            errorIconColorHex = "#F58220", // Orange - use secondary color for icons
            successIconColorHex = "#F58220", // Orange - use secondary color for icons
            
            // Document & Verification Icons - USE SECONDARY COLOR (ORANGE)
            documentIconColorHex = "#F58220", // Orange - use secondary color for icons
            cameraIconColorHex = "#F58220", // Orange - use secondary color for icons
            scanIconColorHex = "#F58220", // Orange - use secondary color for icons
            
            // Biometric & Security Icons - USE SECONDARY COLOR (ORANGE)
            biometricIconColorHex = "#F58220", // Orange - use secondary color for icons
            securityIconColorHex = "#F58220", // Orange - use secondary color for icons
            nfcIconColorHex = "#F58220", // Orange - use secondary color for icons
            
            // Status Icons - USE SECONDARY COLOR (ORANGE)
            statusActiveIconColorHex = "#F58220", // Orange - use secondary color for icons
            statusInactiveIconColorHex = "#F58220", // Orange - use secondary color for icons
            statusProcessingIconColorHex = "#F58220", // Orange - use secondary color for icons
            
            // Custom Icon Mappings for Authentication Screens
            customIcons = mapOf(
                "auth_success" to "approval", // Success screen image - high quality approval icon
                "auth_processing" to "img_processing" // Processing screen image (if needed)
            )
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
            buttonCornerRadius = 8f,
            cardCornerRadius = 12f,
            buttonHeight = 48f
        ),
        
        layoutConfig = SDKLayoutConfig(
            screenPadding = 16f,
            componentSpacing = 16f
        )
    )
    
    /**
     * Sample 1 Theme - Professional enterprise look
     */
    val CORPORATE_BLUE = EnhancedSDKThemeConfiguration(
        brandName = "Sample 1",
        
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
            primaryColorHex = "#315C2B", // Hunter Green
            secondaryColorHex = "#60712F", // Fern Green
            backgroundColorHex = "#F8F9F8", // Very light green tint
            surfaceColorHex = "#FFFFFF",
            onPrimaryColorHex = "#FFFFFF",
            onSecondaryColorHex = "#FFFFFF",
            onBackgroundColorHex = "#181F1C", // Eerie Black
            onSurfaceColorHex = "#181F1C", // Eerie Black
            successColorHex = "#9EA93F", // Apple Green
            errorColorHex = "#D32F2F", // Red 700
            warningColorHex = "#F57C00", // Orange 700
            primaryButtonColorHex = "#315C2B", // Hunter Green
            primaryButtonTextColorHex = "#FFFFFF",
            secondaryButtonColorHex = "#E8F5E8", // Light green tint
            secondaryButtonTextColorHex = "#315C2B", // Hunter Green
            faceDetectionOverlayColorHex = "#60712F", // Fern Green
            documentScanOverlayColorHex = "#60712F" // Fern Green
        ),
        
        iconTheme = SDKIconTheme(
            iconStyle = "outlined",
            mediumIconSize = 24f,
            primaryIconColorHex = "#315C2B", // Hunter Green
            secondaryIconColorHex = "#60712F", // Fern Green
            accentIconColorHex = "#9EA93F", // Apple Green
            disabledIconColorHex = "#BDBDBD",
            
            // Navigation & UI Icons
            navigationIconColorHex = "#315C2B", // Hunter Green
            actionIconColorHex = "#60712F", // Fern Green
            
            // Instruction & Guide Icons
            instructionIconColorHex = "#60712F", // Fern Green
            warningIconColorHex = "#FF9800",
            errorIconColorHex = "#D32F2F",
            successIconColorHex = "#9EA93F", // Apple Green
            
            // Document & Verification Icons
            documentIconColorHex = "#60712F", // Fern Green
            cameraIconColorHex = "#315C2B", // Hunter Green
            scanIconColorHex = "#60712F", // Fern Green
            
            // Biometric & Security Icons
            biometricIconColorHex = "#60712F", // Fern Green
            securityIconColorHex = "#9EA93F", // Apple Green
            nfcIconColorHex = "#60712F", // Fern Green
            
            // Status Icons
            statusActiveIconColorHex = "#9EA93F", // Apple Green
            statusInactiveIconColorHex = "#757575",
            statusProcessingIconColorHex = "#60712F" // Fern Green
        ),
        
        textContent = SDKTextContent(
            welcomeTitle = "Secure Verification",
            welcomeSubtitle = "Natural and secure identity verification",
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
     * Sample 2 Theme - Modern dark UI
     */
    val DARK_PROFESSIONAL = EnhancedSDKThemeConfiguration(
        brandName = "Sample 2",
        
        typography = SDKTypography(
            fontFamily = "default",
            headlineLarge = 32f,
            headlineMedium = 28f,
            titleLarge = 22f,
            bodyLarge = 16f,
            bodyMedium = 14f,
            headlineWeight = "medium", // Slightly lighter for dark theme
            titleWeight = "medium",
            bodyWeight = "normal",
            letterSpacing = 0.25f,
            lineHeight = 1.6f
        ),
        
        colorScheme = SDKColorScheme(
            primaryColorHex = "#3E517A", // YInMn Blue (dark, professional)
            secondaryColorHex = "#70CAD1", // Tiffany Blue
            backgroundColorHex = "#F0F8FF", // Very light blue tint
            surfaceColorHex = "#FFFFFF",
            onPrimaryColorHex = "#FFFFFF",
            onSecondaryColorHex = "#000000",
            onBackgroundColorHex = "#2C3E50", // Dark blue-gray text
            onSurfaceColorHex = "#2C3E50", // Dark blue-gray text
            successColorHex = "#4CAF50",
            errorColorHex = "#D32F2F",
            warningColorHex = "#FF9800",
            primaryButtonColorHex = "#3E517A", // YInMn Blue
            primaryButtonTextColorHex = "#FFFFFF",
            secondaryButtonColorHex = "#E3F2FD", // Light blue tint
            secondaryButtonTextColorHex = "#3E517A", // YInMn Blue
            faceDetectionOverlayColorHex = "#70CAD1", // Tiffany Blue
            documentScanOverlayColorHex = "#70CAD1", // Tiffany Blue
            surfaceVariantColorHex = "#F5F9FF", // Very light blue
            outlineColorHex = "#B08EA2" // Rose Quartz for subtle accents
        ),
        
        iconTheme = SDKIconTheme(
            iconStyle = "rounded",
            mediumIconSize = 24f,
            primaryIconColorHex = "#3E517A", // YInMn Blue
            secondaryIconColorHex = "#70CAD1", // Tiffany Blue
            accentIconColorHex = "#8EE3F5", // Non Photo Blue
            disabledIconColorHex = "#B0BEC5",
            
            // Navigation & UI Icons
            navigationIconColorHex = "#3E517A", // YInMn Blue
            actionIconColorHex = "#70CAD1", // Tiffany Blue
            
            // Instruction & Guide Icons
            instructionIconColorHex = "#70CAD1", // Tiffany Blue
            warningIconColorHex = "#FF9800",
            errorIconColorHex = "#D32F2F",
            successIconColorHex = "#4CAF50",
            
            // Document & Verification Icons
            documentIconColorHex = "#70CAD1", // Tiffany Blue
            cameraIconColorHex = "#3E517A", // YInMn Blue
            scanIconColorHex = "#70CAD1", // Tiffany Blue
            
            // Biometric & Security Icons
            biometricIconColorHex = "#8EE3F5", // Non Photo Blue
            securityIconColorHex = "#4CAF50",
            nfcIconColorHex = "#70CAD1", // Tiffany Blue
            
            // Status Icons
            statusActiveIconColorHex = "#4CAF50",
            statusInactiveIconColorHex = "#B08EA2", // Rose Quartz
            statusProcessingIconColorHex = "#8EE3F5" // Non Photo Blue
        ),
        
        textContent = SDKTextContent(
            welcomeTitle = "Professional Verification",
            welcomeSubtitle = "Elegant and secure identity verification",
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
     * Sample 3 Theme - Conservative and trustworthy
     */
    val BANKING_THEME = EnhancedSDKThemeConfiguration(
        brandName = "Sample 3",
        
        typography = SDKTypography(
            fontFamily = "serif", // More traditional for banking
            headlineLarge = 28f, // Slightly smaller for conservative look
            headlineMedium = 24f,
            titleLarge = 20f,
            bodyLarge = 16f,
            bodyMedium = 14f,
            headlineWeight = "bold",
            titleWeight = "bold", // Bolder for trust
            bodyWeight = "normal",
            letterSpacing = 0f,
            lineHeight = 1.4f
        ),
        
        colorScheme = SDKColorScheme(
            primaryColorHex = "#233D4D", // Charcoal (dark, professional)
            secondaryColorHex = "#619B8A", // Zomp (calming teal-green)
            backgroundColorHex = "#FDF8F0", // Very light warm tint
            surfaceColorHex = "#FFFFFF",
            onPrimaryColorHex = "#FFFFFF",
            onSecondaryColorHex = "#FFFFFF",
            onBackgroundColorHex = "#233D4D", // Charcoal text
            onSurfaceColorHex = "#233D4D", // Charcoal text
            successColorHex = "#A1C181", // Olivine (soft green)
            errorColorHex = "#D32F2F", // Keep standard red
            warningColorHex = "#FE7F2D", // Pumpkin (warm orange)
            primaryButtonColorHex = "#233D4D", // Charcoal
            primaryButtonTextColorHex = "#FFFFFF",
            secondaryButtonColorHex = "#F0F4F2", // Light teal tint
            secondaryButtonTextColorHex = "#233D4D", // Charcoal
            faceDetectionOverlayColorHex = "#619B8A", // Zomp
            documentScanOverlayColorHex = "#619B8A" // Zomp
        ),
        
        iconTheme = SDKIconTheme(
            iconStyle = "filled",
            mediumIconSize = 22f, // Slightly smaller for conservative look
            primaryIconColorHex = "#233D4D", // Charcoal
            secondaryIconColorHex = "#619B8A", // Zomp
            accentIconColorHex = "#FCCA46", // Sunglow (bright accent)
            disabledIconColorHex = "#BDBDBD",
            
            // Navigation & UI Icons
            navigationIconColorHex = "#233D4D", // Charcoal
            actionIconColorHex = "#619B8A", // Zomp
            
            // Instruction & Guide Icons
            instructionIconColorHex = "#619B8A", // Zomp
            warningIconColorHex = "#FE7F2D", // Pumpkin
            errorIconColorHex = "#D32F2F", // Standard red
            successIconColorHex = "#A1C181", // Olivine
            
            // Document & Verification Icons
            documentIconColorHex = "#619B8A", // Zomp
            cameraIconColorHex = "#233D4D", // Charcoal
            scanIconColorHex = "#619B8A", // Zomp
            
            // Biometric & Security Icons
            biometricIconColorHex = "#FCCA46", // Sunglow (bright for attention)
            securityIconColorHex = "#A1C181", // Olivine
            nfcIconColorHex = "#619B8A", // Zomp
            
            // Status Icons
            statusActiveIconColorHex = "#A1C181", // Olivine
            statusInactiveIconColorHex = "#757575",
            statusProcessingIconColorHex = "#FCCA46" // Sunglow
        ),
        
        textContent = SDKTextContent(
            welcomeTitle = "Trusted Verification",
            welcomeSubtitle = "Warm and secure identity verification experience",
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
     * Sample 4 Theme - Modern and innovative
     */
    val FINTECH_THEME = EnhancedSDKThemeConfiguration(
        brandName = "Sample 4",
        
        typography = SDKTypography(
            fontFamily = "sans-serif",
            headlineLarge = 36f, // Larger for impact
            headlineMedium = 30f,
            titleLarge = 24f,
            bodyLarge = 18f,
            bodyMedium = 16f,
            headlineWeight = "light", // Modern thin headings
            titleWeight = "normal",
            bodyWeight = "normal",
            letterSpacing = 1f, // More spacing for modern look
            lineHeight = 1.5f
        ),
        
        colorScheme = SDKColorScheme(
            primaryColorHex = "#50514F", // Davys Gray (sophisticated dark)
            secondaryColorHex = "#247BA0", // Cerulean (vibrant blue accent)
            backgroundColorHex = "#FFFCFF", // Snow (pure, clean white)
            surfaceColorHex = "#FFFFFF",
            onPrimaryColorHex = "#FFFFFF",
            onSecondaryColorHex = "#FFFFFF",
            onBackgroundColorHex = "#50514F", // Davys Gray text
            onSurfaceColorHex = "#50514F", // Davys Gray text
            successColorHex = "#4CAF50", // Keep standard green
            errorColorHex = "#D32F2F", // Keep standard red
            warningColorHex = "#FF9800", // Keep standard orange
            primaryButtonColorHex = "#50514F", // Davys Gray
            primaryButtonTextColorHex = "#FFFFFF",
            secondaryButtonColorHex = "#CBD4C2", // Ash Gray (soft background)
            secondaryButtonTextColorHex = "#50514F", // Davys Gray
            faceDetectionOverlayColorHex = "#247BA0", // Cerulean
            documentScanOverlayColorHex = "#247BA0" // Cerulean
        ),
        
        iconTheme = SDKIconTheme(
            iconStyle = "sharp", // Sharp, modern icons
            mediumIconSize = 26f,
            primaryIconColorHex = "#50514F", // Davys Gray
            secondaryIconColorHex = "#247BA0", // Cerulean
            accentIconColorHex = "#C3B299", // Khaki (warm neutral accent)
            disabledIconColorHex = "#CBD4C2", // Ash Gray
            
            // Navigation & UI Icons
            navigationIconColorHex = "#50514F", // Davys Gray
            actionIconColorHex = "#247BA0", // Cerulean
            
            // Instruction & Guide Icons
            instructionIconColorHex = "#247BA0", // Cerulean
            warningIconColorHex = "#FF9800", // Standard orange
            errorIconColorHex = "#D32F2F", // Standard red
            successIconColorHex = "#4CAF50", // Standard green
            
            // Document & Verification Icons
            documentIconColorHex = "#247BA0", // Cerulean
            cameraIconColorHex = "#50514F", // Davys Gray
            scanIconColorHex = "#247BA0", // Cerulean
            
            // Biometric & Security Icons
            biometricIconColorHex = "#C3B299", // Khaki (warm, approachable)
            securityIconColorHex = "#4CAF50", // Standard green
            nfcIconColorHex = "#247BA0", // Cerulean
            
            // Status Icons
            statusActiveIconColorHex = "#4CAF50", // Standard green
            statusInactiveIconColorHex = "#CBD4C2", // Ash Gray
            statusProcessingIconColorHex = "#247BA0" // Cerulean
        ),
        
        textContent = SDKTextContent(
            welcomeTitle = "Modern Verification",
            welcomeSubtitle = "Sophisticated and streamlined identity verification",
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
 * Theme Options Enum for Sample App
 */
enum class EnhancedThemeOption(
    val displayName: String,
    val description: String,
    val themeConfig: EnhancedSDKThemeConfiguration
) {
    ARTIUSID_DEFAULT(
        "artius.iD",
        "Default theme matching the standalone application",
        SampleAppThemes.ARTIUSID_DEFAULT
    ),
    
    CORPORATE_BLUE(
        "Sample 1",
        "Natural green theme with earthy tones",
        SampleAppThemes.CORPORATE_BLUE
    ),
    
    DARK_PROFESSIONAL(
        "Sample 2",
        "Professional blue theme with rose quartz accents",
        SampleAppThemes.DARK_PROFESSIONAL
    ),
    
    BANKING_THEME(
        "Sample 3",
        "Warm earth tone theme with charcoal and teal accents",
        SampleAppThemes.BANKING_THEME
    ),
    
    FINTECH_MODERN(
        "Sample 4",
        "Sophisticated neutral theme with cerulean blue accents",
        SampleAppThemes.FINTECH_THEME
    )
}
