#!/bin/bash

# ArtiusID Android SDK GitHub Publisher - Enhanced Version
# Publishes Android SDK to public GitHub repository for distribution
# Features: Automated versioning, IP protection, build validation

set -e

echo "🚀 ArtiusID Android SDK GitHub Publisher (Enhanced)"
echo "=================================================="

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

# Create directory structure for Android SDK
mkdir -p sdk
mkdir -p docs
mkdir -p sample

# Copy ONLY the AAR file (NO SOURCE CODE for IP protection)
print_info "Copying SDK AAR (IP-protected)..."
cp "$OLDPWD/$AAR_FILE" sdk/artiusid-sdk-$NEW_VERSION.aar

# Copy documentation only
print_info "Copying documentation..."
cp "$OLDPWD/README.md" . 2>/dev/null || echo "# ArtiusID Android SDK v$NEW_VERSION" > README.md
cp "$OLDPWD/SDK_DISTRIBUTION_SECURITY.md" docs/ 2>/dev/null || true
cp "$OLDPWD/Image_Override_System_Documentation.md" docs/ 2>/dev/null || true
cp "$OLDPWD/SDK_SECURITY_GUIDE.md" docs/ 2>/dev/null || true

# Copy sample app configuration (without source code)
print_info "Copying sample configuration..."
if [ -f "$OLDPWD/sample-app/build.gradle" ]; then
    cp "$OLDPWD/sample-app/build.gradle" sample/
fi

# Create integration guide
cat > INTEGRATION_GUIDE.md << 'INTEGRATION_EOF'
# ArtiusID Android SDK Integration Guide

## Quick Start

### 1. Add AAR to Your Project

Download the latest AAR file and add it to your project:

```gradle
dependencies {
    implementation files('libs/artiusid-sdk-VERSION.aar')
    
    // Required dependencies
    implementation 'androidx.compose.ui:ui:1.5.8'
    implementation 'androidx.compose.material3:material3:1.1.2'
    implementation 'androidx.camera:camera-camera2:1.3.1'
    implementation 'androidx.camera:camera-lifecycle:1.3.1'
    implementation 'com.google.mlkit:face-detection:16.1.5'
    implementation 'com.google.dagger:hilt-android:2.48'
}
```

### 2. Initialize SDK

```kotlin
import com.artiusid.sdk.ArtiusIDSDK
import com.artiusid.sdk.config.SDKConfiguration

val config = SDKConfiguration.Builder()
    .setEnvironment(SDKConfiguration.Environment.PRODUCTION)
    .build()

ArtiusIDSDK.initialize(context, config)
```

### 3. Start Verification

```kotlin
ArtiusIDSDK.startVerification(
    activity = this,
    callback = object : VerificationCallback {
        override fun onSuccess(result: VerificationResult) {
            // Handle success
        }
        
        override fun onError(error: SDKError) {
            // Handle error
        }
    }
)
```

## Security Requirements

- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 34 (Android 14)
- ProGuard: Enabled (recommended)
- Network Security: HTTPS only

## Support

For technical support, please contact: support@artiusid.com
INTEGRATION_EOF

# Replace version placeholder in integration guide
sed -i.bak "s/VERSION/$NEW_VERSION/g" INTEGRATION_GUIDE.md && rm INTEGRATION_GUIDE.md.bak

# Create build.gradle for reference (not for building)
cat > build.gradle << 'GRADLE_EOF'
// ArtiusID Android SDK - Reference Configuration
// This file is for reference only. Use the AAR file in your project.

// Minimum requirements for host applications:
android {
    compileSdk 34
    
    defaultConfig {
        minSdk 24
        targetSdk 34
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = '1.8'
    }
}

// Required dependencies (add these to your app's build.gradle)
dependencies {
    // Core Android
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'
    implementation 'androidx.activity:activity-compose:1.8.2'
    
    // Compose
    implementation platform('androidx.compose:compose-bom:2023.10.01')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.material3:material3'
    
    // Camera
    implementation 'androidx.camera:camera-core:1.3.1'
    implementation 'androidx.camera:camera-camera2:1.3.1'
    implementation 'androidx.camera:camera-lifecycle:1.3.1'
    
    // ML Kit
    implementation 'com.google.mlkit:face-detection:16.1.5'
    implementation 'com.google.mlkit:text-recognition:16.0.0'
    
    // Dependency Injection
    implementation 'com.google.dagger:hilt-android:2.48'
    kapt 'com.google.dagger:hilt-android-compiler:2.48'
}
GRADLE_EOF

# Update README with version info
cat >> README.md << README_EOF

## Installation

### Gradle
Add the AAR file to your project:

1. Download \`artiusid-sdk-$NEW_VERSION.aar\` from the releases page
2. Copy it to your app's \`libs\` directory
3. Add to your app's \`build.gradle\`:

\`\`\`gradle
dependencies {
    implementation files('libs/artiusid-sdk-$NEW_VERSION.aar')
}
\`\`\`

## Version
Current version: **$NEW_VERSION**

## Documentation
- [Integration Guide](INTEGRATION_GUIDE.md)
- [Security Guide](docs/SDK_DISTRIBUTION_SECURITY.md)
- [Image Override System](docs/Image_Override_System_Documentation.md)

## Changelog
### v$NEW_VERSION
- Latest release with enhanced security and performance improvements
- Fully obfuscated AAR for IP protection
- Comprehensive documentation and integration guides

README_EOF

# Git operations
print_info "Committing changes..."
git add .
git commit -m "Release Android SDK v$NEW_VERSION

- AAR package with full obfuscation
- Updated documentation and integration guides
- Enhanced security features"

print_info "Creating and pushing tag..."
git tag "v$NEW_VERSION"

print_info "Pushing to GitHub..."
git push origin main --tags 2>/dev/null || git push origin master --tags

# Create GitHub Release
print_info "Creating GitHub release..."
RELEASE_NOTES="# ArtiusID Android SDK v$NEW_VERSION

## 🚀 What's New
- **Secure AAR Distribution**: Fully obfuscated AAR package for maximum IP protection
- **Enhanced Documentation**: Complete integration guides and security documentation
- **Production Ready**: Optimized for production deployment with ProGuard obfuscation

## 📦 Installation
1. Download \`artiusid-sdk-$NEW_VERSION.aar\` from this release
2. Add it to your Android project's \`libs\` directory
3. Update your \`build.gradle\` dependencies

## 🔒 Security Features
- Code obfuscation with ProGuard
- Certificate pinning support
- Secure data handling
- No source code exposure

## 📚 Documentation
- [Integration Guide](https://github.com/toddbryant1966/artiusid_sdk_android/blob/main/INTEGRATION_GUIDE.md)
- [Security Guide](https://github.com/toddbryant1966/artiusid_sdk_android/blob/main/docs/SDK_DISTRIBUTION_SECURITY.md)

## 🛠️ Requirements
- Minimum SDK: API 24 (Android 7.0)
- Target SDK: API 34 (Android 14)
- Kotlin support required"

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
echo "🎉 Android SDK v$NEW_VERSION published successfully!"
echo "📦 Repository: $GITHUB_REPO"
echo "🔗 Releases: https://github.com/artius-iD/artiusid_sdk_android/releases"
echo "📋 AAR File: artiusid-sdk-$NEW_VERSION.aar"
echo ""
echo "📋 Next steps:"
echo "   1. Update your project documentation"
echo "   2. Notify users of the new release"
echo "   3. Update integration guides with new version"
echo "   4. Test the AAR in a sample project"
