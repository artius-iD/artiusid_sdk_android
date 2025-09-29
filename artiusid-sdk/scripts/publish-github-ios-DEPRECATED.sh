#!/bin/bash

# ArtiusID SDK GitHub Publisher
# Publishes SDK to public GitHub repository for distribution

set -e

echo "🚀 ArtiusID SDK GitHub Publisher"
echo "================================="

# Setup paths
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$SCRIPT_DIR/.."


# Change to project root directory
cd "$PROJECT_ROOT"

# Mandatory check for uncommitted changes in GitLab repo
if [[ -n $(git status --porcelain) ]]; then
    echo "⚠️  Uncommitted changes detected in your source (GitLab) repository!"
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
        echo "✅ Changes committed and pushed to GitLab."
    else
        echo "⚠️  Continuing without committing/pushing changes. This is NOT recommended for production releases."
    fi
fi

# Check if we're in the correct directory structure
if [ ! -f "scripts/build-framework.sh" ] || [ ! -f "Package.swift" ]; then
    echo "❌ Error: Invalid project structure. Could not find required files."
    exit 1
fi

cleanup_build_artifacts() {
    echo "🧹 Cleaning up build artifacts..."
    rm -rf .build 2>/dev/null || true
    rm -rf build/DerivedData-* 2>/dev/null || true
    rm -f build-*.log 2>/dev/null || true
    rm -f *" 2".* 2>/dev/null || true
    rm -rf *" 2" 2>/dev/null || true
    rm -rf artiusid-sdk-ios 2>/dev/null || true
    echo "✅ Build cleanup completed - Kept essential framework"
}

if ! command -v gh &> /dev/null; then
    echo "⚠️  Installing GitHub CLI..."
    brew install gh
fi

if ! gh auth status &> /dev/null; then
    echo "🔐 GitHub authentication required..."
    gh auth login
fi

if ! git remote get-url github &> /dev/null; then
    echo "❌ Error: GitHub remote not found"
    exit 1
fi

echo "📂 Private Source: $(git remote get-url origin)"
echo "📦 Public GitHub: $(git remote get-url github)"

echo ""
echo "🔄 Synchronizing tags..."
# Fetch from GitLab (origin)
if git fetch origin --tags -q 2>/dev/null; then
    echo "✅ GitLab tags synchronized"
else
    echo "⚠️  Could not fetch GitLab tags"
fi

# Temporarily disable exit on error for git fetch
set +e

# Fetch from GitHub if remote exists and is accessible
if git remote get-url github &>/dev/null; then
    echo "🔄 Attempting to fetch from GitHub..."
    GITHUB_FETCH_OUTPUT=$(git fetch github --tags 2>&1)
    FETCH_STATUS=$?
    
    if [ $FETCH_STATUS -eq 0 ]; then
        echo "✅ GitHub tags synchronized"
    else
        if [[ "$GITHUB_FETCH_OUTPUT" == *"would clobber existing tag"* ]]; then
            echo "ℹ️  Tag conflict detected (this is normal)"
            echo "   - Local tags will be used"
            echo "   - New tag will be pushed to both repositories"
        elif [[ "$GITHUB_FETCH_OUTPUT" == *"not found"* ]]; then
            echo "ℹ️  The GitHub repository might not exist yet. This is normal for the first publish."
        elif [[ "$GITHUB_FETCH_OUTPUT" == *"authentication"* ]]; then
            echo "ℹ️  Try running: gh auth login"
        else
            echo "⚠️  Could not fetch GitHub tags"
            echo "🔍 Error details: $GITHUB_FETCH_OUTPUT"
        fi
    fi
fi

# Re-enable exit on error
set -e

echo "📋 Recent tags:"
git tag -l | sort -V | tail -5 || echo "  No tags found"

echo ""
read -p "🏷️  Enter version (e.g., 1.0.4): " VERSION

if ! [[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
    echo "❌ Invalid version format"
    exit 1
fi

if git tag -l | grep -q "^v$VERSION$"; then
    echo "❌ Tag v$VERSION already exists locally"
    exit 1
fi

if git ls-remote --tags github | grep -q "refs/tags/v$VERSION$"; then
    echo "❌ Tag v$VERSION already exists on GitHub"
    exit 1
fi

echo "✅ Version: v$VERSION"

# Check for build artifacts
if [ -f "build/artiusid_sdk_ios.xcframework.zip" ] && [ -f "build/OpenSSL.xcframework.zip" ]; then
    echo "📦 Found existing framework builds"
    read -p "🤔 Use existing builds? (y/N): " USE_EXISTING
    if [[ ! "$USE_EXISTING" =~ ^[Yy]$ ]]; then
        echo "🔨 Building frameworks..."
        chmod +x scripts/build-framework.sh
        ./scripts/build-framework.sh
        cleanup_build_artifacts
    fi
else
    echo "🔨 Building frameworks..."
    chmod +x scripts/build-framework.sh
    ./scripts/build-framework.sh
    cleanup_build_artifacts
fi

# --- Compute Checksums ---
echo "🔐 Computing checksums..."
ARTIUS_CHECKSUM=$(swift package compute-checksum build/artiusid_sdk_ios.xcframework.zip)
ARTIUS_SIZE=$(du -h build/artiusid_sdk_ios.xcframework.zip | cut -f1)
OPENSSL_CHECKSUM=$(swift package compute-checksum build/OpenSSL.xcframework.zip)
OPENSSL_SIZE=$(du -h build/OpenSSL.xcframework.zip | cut -f1)

echo "✅ ArtiusID SDK ready - Size: $ARTIUS_SIZE, Checksum: $ARTIUS_CHECKSUM"
echo "✅ OpenSSL ready - Size: $OPENSSL_SIZE, Checksum: $OPENSSL_CHECKSUM"

# --- Publish to GitHub ---
echo "🚀 Publishing to GitHub..."
TEMP_DIR=$(mktemp -d)
echo "🔄 Working in: $TEMP_DIR"
OLDPWD=$(pwd)
cd "$TEMP_DIR"

echo "🔄 Cloning GitHub repository ..."
git clone --depth 1 https://github.com/artius-iD/sdk.git .



# --- Update version numbers in source files and README after successful build ---
echo "📝 Updating version numbers in source files and README..."
sed -i '' "s/public static let version = \".*\"/public static let version = \"$VERSION\"/" Sources/ArtiusIDSDKWrapper.swift
sed -i '' "s/public static let version = \".*\"/public static let version = \"$VERSION\"/" Sources/SDKResourceBundle.swift
sed -i '' "s/SDK Version: .*/SDK Version: $VERSION/" README.md
git add Sources/ArtiusIDSDKWrapper.swift Sources/SDKResourceBundle.swift README.md


# Create Package.swift for GitHub repo
cat > Package.swift << PACKAGE_EOF
// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "artiusid_sdk_ios",
    platforms: [.iOS(.v13)],
    products: [
        .library(
            name: "ArtiusIDSDK",
            targets: ["ArtiusIDSDKWrapper"]
        )
    ],
    dependencies: [],
    targets: [
        .binaryTarget(
            name: "OpenSSL",
            url: "https://github.com/artius-iD/sdk/releases/download/v$VERSION/OpenSSL.xcframework.zip",
            checksum: "$OPENSSL_CHECKSUM"
        ),
        .binaryTarget(
            name: "artiusid_sdk_ios",
            url: "https://github.com/artius-iD/sdk/releases/download/v$VERSION/artiusid_sdk_ios.xcframework.zip",
            checksum: "$ARTIUS_CHECKSUM"
        ),
        .target(
            name: "ArtiusIDSDKWrapper",
            dependencies: [
                "artiusid_sdk_ios",
                "OpenSSL"
            ],
            path: "Sources",
            sources: [
                "ArtiusIDSDKWrapper.swift",
                "VerificationResult.swift"
            ]
        )
    ]
)
PACKAGE_EOF
echo "✅ Generated Package.swift for v$VERSION"

# --- Prepare Files ---
cp "$OLDPWD/build/artiusid_sdk_ios.xcframework.zip" .
cp "$OLDPWD/build/OpenSSL.xcframework.zip" .
cp "$OLDPWD/README.md" .
# Do NOT copy the local Package.swift; use only the generated manifest
rm -rf Sources
cp -R "$OLDPWD/Sources" .


# --- Git Operations ---
echo "🔄 Committing changes..."
git add Package.swift README.md Sources/ArtiusIDSDKWrapper.swift Sources/SDKResourceBundle.swift artiusid_sdk_ios.xcframework.zip OpenSSL.xcframework.zip
git commit -m "Release v$VERSION"


echo "📌 Creating and pushing tag..."
git tag "v$VERSION"

echo "⬆️ Pushing to GitHub..."
git push origin main --tags

# --- Tag and push to GitLab (origin) ---
echo "📌 Creating and pushing tag to GitLab (origin)..."
git push origin "v$VERSION"

# --- Create GitHub Release ---
echo "🎉 Creating GitHub release..."
RELEASE_NOTES="Release for ArtiusID iOS SDK v$VERSION."
if gh release create "v$VERSION" \
    --title "ArtiusID iOS SDK v$VERSION" \
    --notes "$RELEASE_NOTES"; then
    echo "✅ Release created"
else
    echo "❌ Failed to create GitHub release. It might already exist."
    echo "Please check https://github.com/artius-iD/sdk/releases"
    exit 1
fi

# --- Upload Artifacts ---
echo "📦 Uploading frameworks..."
echo "  ↳ Uploading ArtiusID SDK framework..."
if ! gh release upload "v$VERSION" artiusid_sdk_ios.xcframework.zip; then
    echo "❌ Failed to upload ArtiusID SDK framework"
    exit 1
fi

echo "  ↳ Uploading OpenSSL framework..."
if ! gh release upload "v$VERSION" OpenSSL.xcframework.zip; then
    echo "❌ Failed to upload OpenSSL framework"
    exit 1
fi

echo "✅ Frameworks uploaded successfully"

# --- Update Source Repo ---
cd "$OLDPWD"
echo "📝 Overwriting source Package.swift with generated manifest..."
cp "$TEMP_DIR/Package.swift" ./Package.swift
echo "✅ Source Package.swift updated."
git add Package.swift
git commit -m "Update Package.swift for v$VERSION (GitHub)"
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [ -z "$CURRENT_BRANCH" ]; then
    echo "❌ Could not determine current branch."
    exit 1
fi
git push origin "$CURRENT_BRANCH"

# --- Cleanup ---
rm -rf "$TEMP_DIR"
echo "✅ Cleanup complete."
echo "🚀 Ready for distribution!"