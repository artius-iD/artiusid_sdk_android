#!/bin/bash

# Script to fix missing import statements in SDK screens
SDK_DIR="/Users/toddbryant/Documents/mobile-sdk-android/artiusid-sdk/src/main/java/com/artiusid/sdk"

echo "Adding missing import statements..."

# Add missing UI component imports
find "$SDK_DIR" -name "*.kt" -exec sed -i '' '/^import com\.artiusid\.sdk\.ui\.components$/a\
import com.artiusid.sdk.ui.components.GradientBackground\
import com.artiusid.sdk.ui.components.AppTopBar\
import com.artiusid.sdk.ui.components.LoadingIndicator\
import com.artiusid.sdk.ui.components.CustomInfoButton\
import com.artiusid.sdk.ui.components.DocumentRecaptureNotificationView
' {} \;

# Add missing UI utils imports
find "$SDK_DIR" -name "*.kt" -exec sed -i '' '/^import com\.artiusid\.sdk\.ui\.utils$/a\
import com.artiusid.sdk.ui.utils.getRelativeWidthDp\
import com.artiusid.sdk.ui.utils.getRelativeHeightDp\
import com.artiusid.sdk.ui.utils.getRelativeFontSize
' {} \;

# Add missing models imports
find "$SDK_DIR" -name "*.kt" -exec sed -i '' '/^import com\.artiusid\.sdk\.models$/a\
import com.artiusid.sdk.models.PassportNFCData\
import com.artiusid.sdk.models.PassportAuthenticationStatus\
import com.artiusid.sdk.models.ProcessingStage\
import com.artiusid.sdk.models.PassportScanningState\
import com.artiusid.sdk.models.DocumentRecaptureType\
import com.artiusid.sdk.models.VerificationModels.*\
import com.artiusid.sdk.models.AAMVAData
' {} \;

# Add missing utils imports
find "$SDK_DIR" -name "*.kt" -exec sed -i '' '/^import com\.artiusid\.sdk\.utils$/a\
import com.artiusid.sdk.utils.LogManager\
import com.artiusid.sdk.utils.ImageUtils\
import com.artiusid.sdk.utils.PassportTextAnalyzer\
import com.artiusid.sdk.utils.AAMVABarcodeParser
' {} \;

echo "Import statements added!"
