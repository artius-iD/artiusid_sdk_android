#!/bin/bash

# ArtiusID Android SDK GitHub Publisher - Minimal Customer Distribution
# Publishes ONLY the essential files customers need for integration
# Removes internal documentation and implementation details

set -e

echo "🚀 ArtiusID Android SDK Minimal Distribution Publisher"
echo "===================================================="

# Setup paths
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$SCRIPT_DIR/../.."
SDK_DIR="$PROJECT_ROOT/artiusid-sdk"
GRADLE_PROPERTIES="$PROJECT_ROOT/gradle.properties"

# Change to project root directory
cd "$PROJECT_ROOT"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${GREEN}✅${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠️${NC} $1"
}

print_error() {
    echo -e "${RED}❌${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ️${NC} $1"
}

# Function to get current version from gradle.properties
get_current_version() {
    grep "^SDK_VERSION_NAME=" "$GRADLE_PROPERTIES" | cut -d'=' -f2
}

# Function to update version in gradle.properties
update_gradle_version() {
    local new_version=$1
    local new_version_code=$2
    
    # Create backup
    cp "$GRADLE_PROPERTIES" "$GRADLE_PROPERTIES.bak"
    
    # Update version name and code
    sed -i.tmp "s/^SDK_VERSION_NAME=.*/SDK_VERSION_NAME=$new_version/" "$GRADLE_PROPERTIES"
    sed -i.tmp "s/^SDK_VERSION_CODE=.*/SDK_VERSION_CODE=$new_version_code/" "$GRADLE_PROPERTIES"
    sed -i.tmp "s/^PUBLISH_VERSION=.*/PUBLISH_VERSION=$new_version/" "$GRADLE_PROPERTIES"
    
    # Clean up temp files
    rm -f "$GRADLE_PROPERTIES.tmp"
    
    print_status "Updated gradle.properties with version $new_version"
}

# Function to increment version
increment_version() {
    local version=$1
    local type=$2
    
    IFS='.' read -ra VERSION_PARTS <<< "$version"
    local major=${VERSION_PARTS[0]}
    local minor=${VERSION_PARTS[1]}
    local patch=${VERSION_PARTS[2]}
    
    case $type in
        "major")
            major=$((major + 1))
            minor=0
            patch=0
            ;;
        "minor")
            minor=$((minor + 1))
            patch=0
            ;;
        "patch")
            patch=$((patch + 1))
            ;;
    esac
    
    echo "$major.$minor.$patch"
}

# Function to calculate next version code
get_next_version_code() {
    local current_code=$(grep "^SDK_VERSION_CODE=" "$GRADLE_PROPERTIES" | cut -d'=' -f2)
    echo $((current_code + 1))
}

# Mandatory check for uncommitted changes
if [[ -n $(git status --porcelain) ]]; then
    print_warning "Uncommitted changes detected in your source repository!"
    git status
    print_warning "For production builds, you should always commit and push all changes before publishing."
    read -p "Would you like to commit and push these changes now? (y/N): " COMMIT_CHOICE
    if [[ "$COMMIT_CHOICE" =~ ^[Yy]$ ]]; then
        read -p "Enter commit message: " COMMIT_MSG
        git add -A
        git commit -m "$COMMIT_MSG"
        CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
        if [ -z "$CURRENT_BRANCH" ]; then
            print_error "Could not determine current branch."
            exit 1
        fi
        git push origin "$CURRENT_BRANCH"
        print_status "Changes committed and pushed."
    else
        print_warning "Continuing without committing/pushing changes. This is NOT recommended for production releases."
    fi
fi

# Check if we're in the correct directory structure
if [ ! -f "artiusid-sdk/build.gradle" ] || [ ! -f "settings.gradle" ]; then
    print_error "Invalid Android project structure. Could not find required files."
    exit 1
fi

# Check for required tools
if ! command -v gh &> /dev/null; then
    print_warning "Installing GitHub CLI..."
    if command -v brew &> /dev/null; then
        brew install gh
    else
        print_error "Please install GitHub CLI manually: https://cli.github.com/"
        exit 1
    fi
fi

# Check GitHub authentication
if ! gh auth status &> /dev/null; then
    print_info "GitHub authentication required..."
    gh auth login
fi

# Get current version
CURRENT_VERSION=$(get_current_version)
print_info "Current version: $CURRENT_VERSION"

# Fetch existing tags
git fetch --tags 2>/dev/null || true

echo ""
print_info "Recent tags:"
git tag -l | sort -V | tail -5 || echo "  No tags found"

echo ""
echo "🏷️  Version Management Options:"
echo "1. Auto-increment patch (${CURRENT_VERSION} → $(increment_version "$CURRENT_VERSION" "patch"))"
echo "2. Auto-increment minor (${CURRENT_VERSION} → $(increment_version "$CURRENT_VERSION" "minor"))"
echo "3. Auto-increment major (${CURRENT_VERSION} → $(increment_version "$CURRENT_VERSION" "major"))"
echo "4. Manual version entry"
echo "5. Use current version ($CURRENT_VERSION)"

read -p "Choose option (1-5): " VERSION_CHOICE

case $VERSION_CHOICE in
    1)
        NEW_VERSION=$(increment_version "$CURRENT_VERSION" "patch")
        ;;
    2)
        NEW_VERSION=$(increment_version "$CURRENT_VERSION" "minor")
        ;;
    3)
        NEW_VERSION=$(increment_version "$CURRENT_VERSION" "major")
        ;;
    4)
        read -p "Enter version (e.g., 1.0.0): " NEW_VERSION
        ;;
    5)
        NEW_VERSION=$CURRENT_VERSION
        ;;
    *)
        print_error "Invalid choice. Exiting."
        exit 1
        ;;
esac

# Validate version format
if ! [[ "$NEW_VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    print_error "Invalid version format. Use semantic versioning (e.g., 1.0.0)"
    exit 1
fi

# Check if tag already exists
if git tag -l | grep -q "^v$NEW_VERSION$"; then
    print_error "Tag v$NEW_VERSION already exists locally"
    exit 1
fi

print_status "Selected version: v$NEW_VERSION"

# Update gradle.properties if version changed
if [ "$NEW_VERSION" != "$CURRENT_VERSION" ]; then
    NEW_VERSION_CODE=$(get_next_version_code)
    update_gradle_version "$NEW_VERSION" "$NEW_VERSION_CODE"
    
    # Commit version update
    git add "$GRADLE_PROPERTIES"
    git commit -m "Bump version to $NEW_VERSION"
    print_status "Version updated and committed"
fi

# Clean previous builds
print_info "Cleaning previous builds..."
./gradlew :artiusid-sdk:clean

# Build the Android SDK (RELEASE VERSION ONLY)
print_info "Building Android SDK (Release)..."
./gradlew :artiusid-sdk:assembleRelease

# Verify AAR was built successfully
AAR_FILE="artiusid-sdk/build/outputs/aar/artiusid-sdk-release.aar"
if [ ! -f "$AAR_FILE" ]; then
    print_error "Failed to build AAR file: $AAR_FILE"
    exit 1
fi

# Validate it's the release AAR (check for obfuscation)
print_info "Validating AAR is properly obfuscated..."
TEMP_CHECK_DIR=$(mktemp -d)
cd "$TEMP_CHECK_DIR"
unzip -q "$PROJECT_ROOT/$AAR_FILE"
if jar -tf classes.jar | grep -q "^a/.*\.class$"; then
    print_status "AAR is properly obfuscated (found obfuscated classes)"
else
    print_warning "AAR may not be properly obfuscated - verify ProGuard settings"
fi
cd "$PROJECT_ROOT"
rm -rf "$TEMP_CHECK_DIR"

AAR_SIZE=$(du -h "$AAR_FILE" | cut -f1)
print_status "Android SDK AAR built successfully - Size: $AAR_SIZE"

# Set up GitHub repository configuration
GITHUB_REPO="https://github.com/artius-iD/artiusid_sdk_android.git"
if ! git remote get-url github &> /dev/null; then
    print_info "Adding GitHub remote..."
    git remote add github "$GITHUB_REPO"
fi

print_info "Source Repository: $(git remote get-url origin 2>/dev/null || echo "Local repository")"
print_info "GitHub Repository: $GITHUB_REPO"

# Create temporary directory for GitHub repository
TEMP_DIR=$(mktemp -d)
print_info "Working in: $TEMP_DIR"
OLDPWD=$(pwd)
cd "$TEMP_DIR"

# Clone or initialize GitHub repository
print_info "Setting up GitHub repository..."
if gh repo view artius-iD/artiusid_sdk_android &> /dev/null; then
    print_info "Cloning existing repository..."
    git clone "$GITHUB_REPO" .
else
    print_info "Initializing new repository..."
    git init
    git remote add origin "$GITHUB_REPO"
fi

# Create minimal directory structure
mkdir -p sdk
mkdir -p sample

print_info "📦 Creating MINIMAL customer distribution package..."

# 1. Copy ONLY the AAR file (NO SOURCE CODE for IP protection)
print_info "✅ Adding: SDK AAR file (obfuscated)"
cp "$OLDPWD/$AAR_FILE" sdk/artiusid-sdk-$NEW_VERSION.aar

# 2. Copy consumer ProGuard rules (required for customer apps)
print_info "✅ Adding: Consumer ProGuard rules"
cp "$OLDPWD/artiusid-sdk/consumer-rules.pro" sdk/

# 3. Create minimal integration guide (PUBLIC API ONLY)
print_info "✅ Creating: Public integration guide"
cat > INTEGRATION_GUIDE.md << 'INTEGRATION_EOF'
# ArtiusID Android SDK Integration Guide

## Quick Start

### 1. Add AAR to Your Project

1. Download `artiusid-sdk-VERSION.aar` from the releases page
2. Copy it to your app's `libs` directory
3. Add to your app's `build.gradle`:

```gradle
android {
    compileSdk 34
    
    defaultConfig {
        minSdk 24
        targetSdk 34
    }
    
    buildFeatures {
        compose true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion '1.5.3'
    }
}

dependencies {
    // Add the SDK AAR
    implementation files('libs/artiusid-sdk-VERSION.aar')
    
    // Required dependencies
    def hilt_version = "2.48"
    implementation "com.google.dagger:hilt-android:${hilt_version}"
    kapt "com.google.dagger:hilt-android-compiler:${hilt_version}"
    implementation 'androidx.hilt:hilt-navigation-compose:1.1.0'
    
    // Compose
    implementation platform('androidx.compose:compose-bom:2023.10.01')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.material3:material3'
    
    // Image loading (required for SDK animations)
    implementation 'io.coil-kt:coil-compose:2.5.0'
    implementation 'io.coil-kt:coil-gif:2.5.0'
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    
    // Firebase (required for FCM functionality)
    implementation platform('com.google.firebase:firebase-bom:32.7.2')
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-messaging:23.4.1'
    
    // Biometric authentication
    implementation 'androidx.biometric:biometric:1.1.0'
}
```

### 2. Add Required Plugins

```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'com.google.dagger.hilt.android'
    id 'com.google.devtools.ksp'
    id 'com.google.gms.google-services'
}
```

### 3. Application Class Setup

```kotlin
import dagger.hilt.android.HiltAndroidApp
import android.app.Application

@HiltAndroidApp
class YourApplication : Application() {
    // Your application setup
}
```

### 4. Initialize SDK

```kotlin
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.config.SDKConfiguration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SDK
        val config = SDKConfiguration.Builder()
            .setEnvironment(SDKConfiguration.Environment.PRODUCTION)
            .build()
            
        ArtiusIDSDK.initialize(this, config)
    }
}
```

### 5. Start Verification

```kotlin
import com.artiusid.sdk.callbacks.VerificationCallback
import com.artiusid.sdk.models.VerificationResult
import com.artiusid.sdk.models.SDKError

ArtiusIDSDK.startVerificationFlow(
    activity = this,
    config = verificationConfig,
    callback = object : VerificationCallback {
        override fun onSuccess(result: VerificationResult) {
            // Handle successful verification
        }
        
        override fun onError(error: SDKError) {
            // Handle error
        }
        
        override fun onCancelled() {
            // Handle cancellation
        }
    }
)
```

## Requirements

- **Minimum SDK**: Android 7.0 (API level 24)
- **Target SDK**: Android 14 (API level 34)
- **Kotlin**: 1.9.0+
- **Gradle**: 8.0+
- **Firebase Project**: Required for authentication and messaging

## ProGuard Configuration

The SDK includes consumer ProGuard rules that are automatically applied to your app. No additional configuration needed.

## Support

For technical support, please contact: support@artiusid.com
INTEGRATION_EOF

# Replace version placeholder
sed -i.bak "s/VERSION/$NEW_VERSION/g" INTEGRATION_GUIDE.md && rm INTEGRATION_GUIDE.md.bak

# 4. Build and include obfuscated sample app
print_info "✅ Building: Obfuscated sample app for customer distribution"

# Build the obfuscated sample app first
cd "$OLDPWD"
print_info "Building obfuscated sample app..."
./gradlew :sample-app:assembleCustomerDistribution

# Check if the obfuscated APK was built
SAMPLE_APK="sample-app/build/outputs/apk/customerDistribution/sample-app-customerDistribution.apk"
if [ ! -f "$SAMPLE_APK" ]; then
    print_error "Failed to build obfuscated sample app: $SAMPLE_APK"
    exit 1
fi

SAMPLE_APK_SIZE=$(du -h "$SAMPLE_APK" | cut -f1)
print_status "Obfuscated sample app built successfully - Size: $SAMPLE_APK_SIZE"

# Return to temp directory
cd "$TEMP_DIR"

# Create sample directory and copy the obfuscated APK
print_info "✅ Adding: Obfuscated functional sample app (IP protected)"
mkdir -p sample-app
cp "$OLDPWD/$SAMPLE_APK" sample-app/ArtiusID-Sample-App-Functional.apk

# 5. Create integration template files (for reference only)
print_info "✅ Creating: Integration template files"
mkdir -p integration-template

# Create minimal build.gradle reference
cat > integration-template/build.gradle << 'BUILD_EOF'
// ArtiusID SDK - Integration Template
// Copy this configuration to your app's build.gradle

plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'com.google.dagger.hilt.android'
    id 'com.google.devtools.ksp'
    id 'com.google.gms.google-services'
}

android {
    namespace 'com.example.yourapp'
    compileSdk 34

    defaultConfig {
        applicationId "com.example.yourapp"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }

    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = '1.8'
    }
    
    buildFeatures {
        compose true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion '1.5.3'
    }
}

dependencies {
    // ArtiusID SDK
    implementation files('libs/artiusid-sdk-VERSION.aar')
    
    // Required dependencies
    def hilt_version = "2.48"
    implementation "com.google.dagger:hilt-android:${hilt_version}"
    ksp "com.google.dagger:hilt-android-compiler:${hilt_version}"
    implementation 'androidx.hilt:hilt-navigation-compose:1.1.0'
    
    // Core Android
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'
    implementation 'androidx.activity:activity-compose:1.8.2'
    
    // Compose
    implementation platform('androidx.compose:compose-bom:2023.10.01')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.material3:material3'
    
    // Image loading (required for SDK)
    implementation 'io.coil-kt:coil-compose:2.5.0'
    implementation 'io.coil-kt:coil-gif:2.5.0'
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    
    // Firebase (required for SDK)
    implementation platform('com.google.firebase:firebase-bom:32.7.2')
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-messaging:23.4.1'
    
    // Biometric authentication
    implementation 'androidx.biometric:biometric:1.1.0'
}
BUILD_EOF

# Create minimal MainActivity template
cat > integration-template/MainActivity.kt << 'TEMPLATE_EOF'
package com.example.yourapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.config.SDKConfiguration
import com.artiusid.sdk.callbacks.VerificationCallback
import com.artiusid.sdk.models.VerificationResult
import com.artiusid.sdk.models.SDKError

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SDK
        val config = SDKConfiguration.Builder()
            .setEnvironment(SDKConfiguration.Environment.PRODUCTION)
            .build()
            
        ArtiusIDSDK.initialize(this, config)
        
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
    
    @Composable
    fun MainScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { startVerification() }
            ) {
                Text("Start Identity Verification")
            }
        }
    }
    
    private fun startVerification() {
        ArtiusIDSDK.startVerificationFlow(
            activity = this,
            callback = object : VerificationCallback {
                override fun onSuccess(result: VerificationResult) {
                    // Handle successful verification
                    println("Verification successful: ${result.verificationId}")
                }
                
                override fun onError(error: SDKError) {
                    // Handle error
                    println("Verification error: ${error.message}")
                }
                
                override fun onCancelled() {
                    // Handle cancellation
                    println("Verification cancelled")
                }
            }
        )
    }
}
TEMPLATE_EOF

# Replace version placeholder in build.gradle
sed -i.bak "s/VERSION/$NEW_VERSION/g" integration-template/build.gradle && rm integration-template/build.gradle.bak

# 6. Create LICENSE file
print_info "✅ Creating: License agreement"
cat > LICENSE.txt << 'LICENSE_EOF'
ArtiusID Android SDK License Agreement

Copyright © 2024 artius.iD, Inc. All rights reserved.

This software development kit (SDK) is provided under license and may only be used 
in accordance with the terms of the license agreement between you and artius.iD, Inc.

PERMITTED USES:
- Integration into licensed applications
- Development and testing purposes
- Distribution as part of licensed applications

RESTRICTIONS:
- No reverse engineering or decompilation
- No redistribution of SDK components
- No modification of SDK functionality
- Use only with authorized applications

For full license terms and conditions, please contact: legal@artiusid.com
LICENSE_EOF

# 7. Create minimal README
print_info "✅ Creating: Customer README"
cat > README.md << README_EOF
# ArtiusID Android SDK v$NEW_VERSION

A secure Android SDK for identity verification, face liveness detection, document scanning, and NFC passport reading.

## 📦 Installation

1. Download \`artiusid-sdk-$NEW_VERSION.aar\` from the [releases page](https://github.com/artius-iD/artiusid_sdk_android/releases)
2. Copy it to your app's \`libs\` directory
3. Follow the [Integration Guide](INTEGRATION_GUIDE.md)

## 🚀 Quick Start

\`\`\`gradle
dependencies {
    implementation files('libs/artiusid-sdk-$NEW_VERSION.aar')
    // See INTEGRATION_GUIDE.md for complete dependency list
}
\`\`\`

## 📚 Documentation

- [Integration Guide](INTEGRATION_GUIDE.md) - Quick start and basic setup
- [Integration Template](integration-template/) - Code templates for integration
- [Sample Application](sample-app/ArtiusID-Sample-App-Functional.apk) - Functional obfuscated demo app
- [License Agreement](LICENSE.txt) - Usage terms

## 🔒 Security Features

- Fully obfuscated AAR for IP protection
- Hardware-backed encryption
- Certificate pinning support
- Anti-tampering protection

## 📋 Requirements

- **Minimum SDK**: Android 7.0 (API level 24)
- **Target SDK**: Android 14 (API level 34)
- **Kotlin**: 1.9.0+
- **Firebase Project**: Required

## 📞 Support

For technical support: support@artiusid.com
For licensing questions: legal@artiusid.com

---
**Version**: $NEW_VERSION  
**Release Date**: $(date +"%Y-%m-%d")  
**Package Size**: $AAR_SIZE
README_EOF

print_status "📦 Secure customer distribution package created successfully!"
print_info "Package contents:"
echo "   ✅ sdk/artiusid-sdk-$NEW_VERSION.aar (obfuscated SDK)"
echo "   ✅ sdk/consumer-rules.pro (ProGuard rules)"
echo "   ✅ INTEGRATION_GUIDE.md (public API documentation)"
echo "   ✅ sample-app/ArtiusID-Sample-App-Functional.apk (obfuscated functional demo - $SAMPLE_APK_SIZE)"
echo "   ✅ integration-template/MainActivity.kt (integration code template)"
echo "   ✅ integration-template/build.gradle (build configuration template)"
echo "   ✅ LICENSE.txt (usage agreement)"
echo "   ✅ README.md (customer documentation)"

# Git operations
print_info "Committing changes..."
git add .
git commit -m "Release Android SDK v$NEW_VERSION - Secure Customer Distribution

- Obfuscated AAR package only
- Public API integration guide
- Obfuscated functional sample app (IP protected)
- Integration code templates
- Consumer ProGuard rules
- License agreement"

print_info "Creating and pushing tag..."
git tag "v$NEW_VERSION"

print_info "Pushing to GitHub..."
git push origin main --tags 2>/dev/null || git push origin master --tags

# Create GitHub Release
print_info "Creating GitHub release..."
RELEASE_NOTES="# ArtiusID Android SDK v$NEW_VERSION

## 🚀 Minimal Customer Distribution

This release contains **only the essential files** needed for customer integration:

### 📦 What's Included
- ✅ **artiusid-sdk-$NEW_VERSION.aar** - Obfuscated SDK package
- ✅ **Integration Guide** - Public API documentation
- ✅ **Functional Sample App** - Obfuscated demo application (IP protected)
- ✅ **Integration Templates** - Code templates for easy integration
- ✅ **Consumer ProGuard Rules** - Automatic security configuration
- ✅ **License Agreement** - Usage terms

### 🔒 Security Features
- Fully obfuscated AAR for maximum IP protection
- No source code exposure
- Hardware-backed encryption support
- Anti-tampering protection

### 📋 Requirements
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 34 (Android 14)
- Kotlin 1.9.0+
- Firebase project required

### 🚀 Quick Start
1. Download \`artiusid-sdk-$NEW_VERSION.aar\`
2. Add to your app's \`libs\` directory
3. Follow the integration guide
4. Initialize SDK and start verification

### 📞 Support
- Technical: support@artiusid.com
- Licensing: legal@artiusid.com"

if gh release create "v$NEW_VERSION" \
    --title "ArtiusID Android SDK v$NEW_VERSION" \
    --notes "$RELEASE_NOTES" \
    --repo artius-iD/artiusid_sdk_android; then
    print_status "Release created successfully"
else
    print_error "Failed to create GitHub release."
    exit 1
fi

# Upload AAR file
print_info "Uploading AAR file..."
if ! gh release upload "v$NEW_VERSION" "sdk/artiusid-sdk-$NEW_VERSION.aar" --repo artius-iD/artiusid_sdk_android; then
    print_error "Failed to upload AAR file"
    exit 1
fi

print_status "AAR uploaded successfully"

# Cleanup
cd "$OLDPWD"
rm -rf "$TEMP_DIR"
print_status "Cleanup complete"

echo ""
echo "🎉 Minimal Android SDK v$NEW_VERSION published successfully!"
echo "📦 Repository: $GITHUB_REPO"
echo "🔗 Releases: https://github.com/artius-iD/artiusid_sdk_android/releases"
echo "📋 AAR File: artiusid-sdk-$NEW_VERSION.aar"
echo ""
echo "📦 Customer receives ONLY:"
echo "   ✅ Obfuscated AAR file ($AAR_SIZE)"
echo "   ✅ Public integration guide"
echo "   ✅ Obfuscated functional sample app ($SAMPLE_APK_SIZE)"
echo "   ✅ Integration code templates"
echo "   ✅ Consumer ProGuard rules"
echo "   ✅ License agreement"
echo ""
echo "🔒 IP Protection:"
echo "   ❌ No source code exposure"
echo "   ❌ No internal documentation"
echo "   ❌ No build configurations"
echo "   ❌ No Firebase configs"
echo "   ✅ Sample app fully obfuscated"
echo ""
echo "📋 Next steps:"
echo "   1. Test AAR integration in sample project"
echo "   2. Notify customers of new release"
echo "   3. Update customer documentation"
