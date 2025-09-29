#!/bin/bash

# ArtiusID Android SDK GitHub Publisher
# Publishes Android SDK to public GitHub repository for distribution

set -e

echo "🚀 ArtiusID Android SDK GitHub Publisher"
echo "======================================="

# Setup paths
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$SCRIPT_DIR/../.."
SDK_DIR="$PROJECT_ROOT/artiusid-sdk"

# Change to project root directory
cd "$PROJECT_ROOT"

# Mandatory check for uncommitted changes
if [[ -n $(git status --porcelain) ]]; then
    echo "⚠️  Uncommitted changes detected in your source repository!"
    git status
    echo "⚠️  For production builds, you should always commit and push all changes before publishing."
    read -p "Would you like to commit and push these changes now? (y/N): " COMMIT_CHOICE
    if [[ "$COMMIT_CHOICE" =~ ^[Yy]$ ]]; then
        read -p "Enter commit message: " COMMIT_MSG
        git add -A
        git commit -m "$COMMIT_MSG"
        CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
        if [ -z "$CURRENT_BRANCH" ]; then
            echo "❌ Could not determine current branch."
            exit 1
        fi
        git push origin "$CURRENT_BRANCH"
        echo "✅ Changes committed and pushed."
    else
        echo "⚠️  Continuing without committing/pushing changes. This is NOT recommended for production releases."
    fi
fi

# Check if we're in the correct directory structure
if [ ! -f "artiusid-sdk/build.gradle" ] || [ ! -f "settings.gradle" ]; then
    echo "❌ Error: Invalid Android project structure. Could not find required files."
    exit 1
fi

cleanup_build_artifacts() {
    echo "🧹 Cleaning up build artifacts..."
    rm -rf artiusid-sdk/build/outputs/aar/*.aar 2>/dev/null || true
    rm -rf artiusid-sdk/build/intermediates 2>/dev/null || true
    rm -rf sample-app/build 2>/dev/null || true
    echo "✅ Build cleanup completed"
}

# Check for GitHub CLI
if ! command -v gh &> /dev/null; then
    echo "⚠️  Installing GitHub CLI..."
    if command -v brew &> /dev/null; then
        brew install gh
    else
        echo "❌ Please install GitHub CLI manually: https://cli.github.com/"
        exit 1
    fi
fi

# Check GitHub authentication
if ! gh auth status &> /dev/null; then
    echo "🔐 GitHub authentication required..."
    gh auth login
fi

# Set up GitHub remote if it doesn't exist
GITHUB_REPO="https://github.com/toddbryant1966/artiusid_sdk_android.git"
if ! git remote get-url github &> /dev/null; then
    echo "🔗 Adding GitHub remote..."
    git remote add github "$GITHUB_REPO"
fi

echo "📂 Source Repository: $(git remote get-url origin 2>/dev/null || echo "Local repository")"
echo "📦 GitHub Repository: $GITHUB_REPO"

echo ""
echo "🔄 Synchronizing tags..."
# Fetch existing tags
git fetch --tags 2>/dev/null || true

echo "📋 Recent tags:"
git tag -l | sort -V | tail -5 || echo "  No tags found"

echo ""
read -p "🏷️  Enter version (e.g., 1.0.0): " VERSION

if ! [[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "❌ Invalid version format. Use semantic versioning (e.g., 1.0.0)"
    exit 1
fi

if git tag -l | grep -q "^v$VERSION$"; then
    echo "❌ Tag v$VERSION already exists locally"
    exit 1
fi

echo "✅ Version: v$VERSION"

# Build the Android SDK
echo "🔨 Building Android SDK..."
./gradlew :artiusid-sdk:clean
./gradlew :artiusid-sdk:assembleRelease

# Check if AAR was built successfully
AAR_FILE="artiusid-sdk/build/outputs/aar/artiusid-sdk-release.aar"
if [ ! -f "$AAR_FILE" ]; then
    echo "❌ Failed to build AAR file: $AAR_FILE"
    exit 1
fi

AAR_SIZE=$(du -h "$AAR_FILE" | cut -f1)
echo "✅ Android SDK AAR built - Size: $AAR_SIZE"

# Create temporary directory for GitHub repository
TEMP_DIR=$(mktemp -d)
echo "🔄 Working in: $TEMP_DIR"
OLDPWD=$(pwd)
cd "$TEMP_DIR"

# Clone or initialize GitHub repository
echo "🔄 Setting up GitHub repository..."
if gh repo view toddbryant1966/artiusid_sdk_android &> /dev/null; then
    echo "📥 Cloning existing repository..."
    git clone "$GITHUB_REPO" .
else
    echo "🆕 Initializing new repository..."
    git init
    git remote add origin "$GITHUB_REPO"
fi

# Create directory structure for Android SDK
mkdir -p sdk
mkdir -p docs
mkdir -p sample

# Copy SDK files
echo "📦 Copying SDK files..."
cp "$OLDPWD/$AAR_FILE" sdk/artiusid-sdk-$VERSION.aar
cp -r "$OLDPWD/artiusid-sdk/src/main/java/com/artiusid/sdk"/* sdk/ 2>/dev/null || true

# Copy documentation
cp "$OLDPWD/README.md" . 2>/dev/null || echo "# ArtiusID Android SDK v$VERSION" > README.md
cp "$OLDPWD/SDK_DISTRIBUTION_SECURITY.md" docs/ 2>/dev/null || true
cp "$OLDPWD/Image_Override_System_Documentation.md" docs/ 2>/dev/null || true

# Copy sample app (without build artifacts)
cp -r "$OLDPWD/sample-app/src" sample/ 2>/dev/null || true
cp "$OLDPWD/sample-app/build.gradle" sample/ 2>/dev/null || true

# Create build.gradle for SDK distribution
cat > build.gradle << 'GRADLE_EOF'
// ArtiusID Android SDK
// Add this to your app's build.gradle dependencies:
// implementation files('path/to/artiusid-sdk-VERSION.aar')

apply plugin: 'com.android.library'

android {
    compileSdkVersion 34
    
    defaultConfig {
        minSdkVersion 21
        targetSdkVersion 34
        versionCode 1
        versionName "VERSION_PLACEHOLDER"
    }
    
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}

dependencies {
    // Add your dependencies here
}
GRADLE_EOF

# Replace version placeholder
sed -i.bak "s/VERSION_PLACEHOLDER/$VERSION/g" build.gradle && rm build.gradle.bak

# Update README with version info
cat >> README.md << README_EOF

## Installation

### Gradle
Add the AAR file to your project:

1. Copy \`artiusid-sdk-$VERSION.aar\` to your app's \`libs\` directory
2. Add to your app's \`build.gradle\`:

\`\`\`gradle
dependencies {
    implementation files('libs/artiusid-sdk-$VERSION.aar')
}
\`\`\`

## Version
Current version: **$VERSION**

## Documentation
- [Security Guide](docs/SDK_DISTRIBUTION_SECURITY.md)
- [Image Override System](docs/Image_Override_System_Documentation.md)

README_EOF

# Git operations
echo "🔄 Committing changes..."
git add .
git commit -m "Release Android SDK v$VERSION"

echo "📌 Creating and pushing tag..."
git tag "v$VERSION"

echo "⬆️ Pushing to GitHub..."
git push origin main --tags 2>/dev/null || git push origin master --tags

# Create GitHub Release
echo "🎉 Creating GitHub release..."
RELEASE_NOTES="Release for ArtiusID Android SDK v$VERSION

## What's New
- Android SDK AAR package
- Complete source code
- Documentation and samples

## Installation
Download the \`artiusid-sdk-$VERSION.aar\` file and add it to your Android project."

if gh release create "v$VERSION" \
    --title "ArtiusID Android SDK v$VERSION" \
    --notes "$RELEASE_NOTES" \
    --repo toddbryant1966/artiusid_sdk_android; then
    echo "✅ Release created"
else
    echo "❌ Failed to create GitHub release."
    exit 1
fi

# Upload AAR file
echo "📦 Uploading AAR file..."
if ! gh release upload "v$VERSION" "sdk/artiusid-sdk-$VERSION.aar" --repo toddbryant1966/artiusid_sdk_android; then
    echo "❌ Failed to upload AAR file"
    exit 1
fi

echo "✅ AAR uploaded successfully"

# Cleanup
cd "$OLDPWD"
rm -rf "$TEMP_DIR"
echo "✅ Cleanup complete."

echo ""
echo "🚀 Android SDK v$VERSION published successfully!"
echo "📦 Repository: $GITHUB_REPO"
echo "🔗 Releases: https://github.com/toddbryant1966/artiusid_sdk_android/releases"
echo ""
echo "📋 Next steps:"
echo "   1. Update your project documentation"
echo "   2. Notify users of the new release"
echo "   3. Update integration guides with new version"
