#!/bin/bash

# Script to fix all parameter names in SDKNavigation.kt to match SimpleScreens
SDK_NAV_FILE="/Users/toddbryant/Documents/mobile-sdk-android/artiusid-sdk/src/main/java/com/artiusid/sdk/navigation/SDKNavigation.kt"

echo "🔧 Fixing navigation parameter names to match SimpleScreens..."

# Replace all the incorrect parameter names with the correct ones
sed -i '' 's/onNavigateToFaceScan/onContinue/g' "$SDK_NAV_FILE"
sed -i '' 's/onNavigateToDocumentScan/onContinue/g' "$SDK_NAV_FILE"
sed -i '' 's/onNavigateBack/onBack/g' "$SDK_NAV_FILE"
sed -i '' 's/onFaceScanComplete/onContinue/g' "$SDK_NAV_FILE"
sed -i '' 's/onDocumentScanComplete/onContinue/g' "$SDK_NAV_FILE"
sed -i '' 's/onDocumentTypeSelected/onContinue/g' "$SDK_NAV_FILE"
sed -i '' 's/onPassportScanComplete/onContinue/g' "$SDK_NAV_FILE"
sed -i '' 's/onNFCComplete/onContinue/g' "$SDK_NAV_FILE"
sed -i '' 's/onProcessingComplete/onContinue/g' "$SDK_NAV_FILE"
sed -i '' 's/onVerificationComplete/onContinue/g' "$SDK_NAV_FILE"
sed -i '' 's/onAuthenticationComplete/onContinue/g' "$SDK_NAV_FILE"
sed -i '' 's/onRetry/onContinue/g' "$SDK_NAV_FILE"
sed -i '' 's/onCancel/onBack/g' "$SDK_NAV_FILE"

echo "✅ Navigation parameter names fixed!"
