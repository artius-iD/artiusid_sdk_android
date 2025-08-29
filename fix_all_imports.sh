#!/bin/bash

# Comprehensive script to fix all remaining import issues in the SDK
SDK_DIR="/Users/toddbryant/Documents/mobile-sdk-android/artiusid-sdk/src/main/java/com/artiusid/sdk"

echo "🔧 Fixing all remaining import issues..."

# Fix package declarations that have wrong paths
echo "📦 Fixing package declarations..."
find "$SDK_DIR" -name "*.kt" -exec sed -i '' 's/package com\.artiusid\./package com.artiusid.sdk./g' {} \;

# Fix import statements with wrong paths
echo "📥 Fixing import statements..."
find "$SDK_DIR" -name "*.kt" -exec sed -i '' 's/import com\.artiusid\.sdk\.sdk\./import com.artiusid.sdk./g' {} \;
find "$SDK_DIR" -name "*.kt" -exec sed -i '' 's/import com\.artiusid\.data\./import com.artiusid.sdk.data./g' {} \;
find "$SDK_DIR" -name "*.kt" -exec sed -i '' 's/import com\.artiusid\.presentation\./import com.artiusid.sdk.presentation./g' {} \;
find "$SDK_DIR" -name "*.kt" -exec sed -i '' 's/import com\.artiusid\.services\./import com.artiusid.sdk.services./g' {} \;
find "$SDK_DIR" -name "*.kt" -exec sed -i '' 's/import com\.artiusid\.utils\./import com.artiusid.sdk.utils./g' {} \;

# Fix specific model imports
echo "🏗️ Fixing model imports..."
find "$SDK_DIR" -name "*.kt" -exec sed -i '' 's/import com\.artiusid\.sdk\.data\.model\./import com.artiusid.sdk.models./g' {} \;
find "$SDK_DIR" -name "*.kt" -exec sed -i '' 's/import com\.artiusid\.sdk\.models\.models\./import com.artiusid.sdk.models./g' {} \;

# Add missing imports for commonly used classes
echo "➕ Adding missing imports..."

# Add imports for UI utilities
find "$SDK_DIR/ui/screens" -name "*.kt" -exec grep -l "getRelativeWidthDp\|getRelativeHeightDp\|getRelativeFontSize" {} \; | while read file; do
    if ! grep -q "import com.artiusid.sdk.ui.utils" "$file"; then
        sed -i '' '/^package /a\
\
import com.artiusid.sdk.ui.utils.*
' "$file"
    fi
done

# Add imports for UI components
find "$SDK_DIR/ui/screens" -name "*.kt" -exec grep -l "GradientBackground\|AppTopBar\|LoadingIndicator\|CustomInfoButton" {} \; | while read file; do
    if ! grep -q "import com.artiusid.sdk.ui.components" "$file"; then
        sed -i '' '/^package /a\
\
import com.artiusid.sdk.ui.components.*
' "$file"
    fi
done

# Add imports for models
find "$SDK_DIR" -name "*.kt" -exec grep -l "PassportNFCData\|ProcessingStage\|DocumentRecaptureType\|VerificationResults" {} \; | while read file; do
    if ! grep -q "import com.artiusid.sdk.models" "$file"; then
        sed -i '' '/^package /a\
\
import com.artiusid.sdk.models.*
' "$file"
    fi
done

# Add imports for services
find "$SDK_DIR" -name "*.kt" -exec grep -l "BarcodeScanManager\|DocumentScanManager\|FaceDetectionManager" {} \; | while read file; do
    if ! grep -q "import com.artiusid.sdk.services" "$file"; then
        sed -i '' '/^package /a\
\
import com.artiusid.sdk.services.*
' "$file"
    fi
done

# Add imports for utilities
find "$SDK_DIR" -name "*.kt" -exec grep -l "LogManager\|ImageUtils\|AAMVABarcodeParser" {} \; | while read file; do
    if ! grep -q "import com.artiusid.sdk.utils" "$file"; then
        sed -i '' '/^package /a\
\
import com.artiusid.sdk.utils.*
' "$file"
    fi
done

echo "✅ Import fixes completed!"
