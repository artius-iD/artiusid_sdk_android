#!/bin/bash

# Script to fix package declarations with extra 'sdk' in them
SDK_DIR="/Users/toddbryant/Documents/mobile-sdk-android/artiusid-sdk/src/main/java/com/artiusid/sdk"

echo "🔧 Fixing package declarations with extra 'sdk'..."

# Fix package declarations that have com.artiusid.sdk.sdk.* pattern
find "$SDK_DIR" -name "*.kt" -exec sed -i '' 's/package com\.artiusid\.sdk\.sdk\./package com.artiusid.sdk./g' {} \;

# Fix any remaining package declarations that don't start with com.artiusid.sdk
find "$SDK_DIR" -name "*.kt" -exec sed -i '' 's/^package com\.artiusid\./package com.artiusid.sdk./g' {} \;

echo "✅ Package declarations fixed!"

echo "🔍 Checking for any remaining package issues..."
find "$SDK_DIR" -name "*.kt" -exec grep -H "^package " {} \; | grep -v "package com.artiusid.sdk" | head -10
