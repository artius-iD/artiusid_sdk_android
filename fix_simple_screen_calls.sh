#!/bin/bash

# Script to update all SimpleScreen function calls in SDKNavigation.kt
SDK_NAV_FILE="/Users/toddbryant/Documents/mobile-sdk-android/artiusid-sdk/src/main/java/com/artiusid/sdk/navigation/SDKNavigation.kt"

echo "🔧 Updating SimpleScreen function calls in SDKNavigation.kt..."

# Update all the screen function calls
sed -i '' 's/FaceScanScreen(/SimpleFaceScanScreen(/g' "$SDK_NAV_FILE"
sed -i '' 's/SelectDocumentTypeScreen(/SimpleSelectDocumentTypeScreen(/g' "$SDK_NAV_FILE"
sed -i '' 's/DocumentScanIntroScreen(/SimpleDocumentScanIntroScreen(/g' "$SDK_NAV_FILE"
sed -i '' 's/DocumentScanScreen(/SimpleDocumentScanScreen(/g' "$SDK_NAV_FILE"
sed -i '' 's/DocumentScanBackIntroScreen(/SimpleDocumentScanBackIntroScreen(/g' "$SDK_NAV_FILE"
sed -i '' 's/PassportScanIntroScreen(/SimplePassportScanIntroScreen(/g' "$SDK_NAV_FILE"
sed -i '' 's/PassportScanScreen(/SimplePassportScanScreen(/g' "$SDK_NAV_FILE"
sed -i '' 's/PassportChipIntroScreen(/SimplePassportChipIntroScreen(/g' "$SDK_NAV_FILE"
sed -i '' 's/PassportChipScanScreen(/SimplePassportChipScanScreen(/g' "$SDK_NAV_FILE"
sed -i '' 's/VerificationProcessingScreen(/SimpleVerificationProcessingScreen(/g' "$SDK_NAV_FILE"
sed -i '' 's/VerificationResultsScreen(/SimpleVerificationResultsScreen(/g' "$SDK_NAV_FILE"
sed -i '' 's/VerificationFailureScreen(/SimpleVerificationFailureScreen(/g' "$SDK_NAV_FILE"
sed -i '' 's/AuthenticationScreen(/SimpleAuthenticationScreen(/g' "$SDK_NAV_FILE"
sed -i '' 's/AuthenticatedScreen(/SimpleAuthenticatedScreen(/g' "$SDK_NAV_FILE"

echo "✅ SimpleScreen function calls updated!"
