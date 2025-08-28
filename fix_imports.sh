#!/bin/bash

# Script to fix package imports in the SDK
SDK_DIR="/Users/toddbryant/Documents/mobile-sdk-android/artiusid-sdk/src/main/java/com/artiusid/sdk"

echo "Fixing package imports in SDK..."

# Fix all imports from standalone app to SDK
find "$SDK_DIR" -name "*.kt" -exec sed -i '' 's/import com\.artiusid\./import com.artiusid.sdk./g' {} \;

# Fix specific common imports
find "$SDK_DIR" -name "*.kt" -exec sed -i '' 's/import com\.artiusid\.sdk\.R/import com.artiusid.sdk.R/g' {} \;
find "$SDK_DIR" -name "*.kt" -exec sed -i '' 's/import com\.artiusid\.sdk\.MainActivity/import com.artiusid.sdk.ui.activities.SDKMainActivity/g' {} \;

# Fix navigation imports
find "$SDK_DIR" -name "*.kt" -exec sed -i '' 's/import com\.artiusid\.sdk\.navigation\.AppNavigation/import com.artiusid.sdk.navigation.SDKNavigation/g' {} \;

# Fix utils imports
find "$SDK_DIR" -name "*.kt" -exec sed -i '' 's/import com\.artiusid\.sdk\.utils\.ImageStorage/import com.artiusid.sdk.utils.ImageStorage/g' {} \;
find "$SDK_DIR" -name "*.kt" -exec sed -i '' 's/import com\.artiusid\.sdk\.utils\.VerificationDataHolder/import com.artiusid.sdk.utils.VerificationDataHolder/g' {} \;

echo "Package imports fixed!"
