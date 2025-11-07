#!/bin/bash

# publish-android-github-essential.sh
# Script to compile SDK and publish to GitHub
# Usage: ./publish-android-github-essential.sh

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}ArtiusID SDK GitHub Publisher${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Navigate to project root
cd "$(dirname "$0")/../.."

# Get version from gradle.properties
VERSION=$(grep "PUBLISH_VERSION=" gradle.properties | cut -d'=' -f2)
echo -e "${GREEN}📦 Version: ${VERSION}${NC}"
echo ""

# Step 1: Clean build
echo -e "${BLUE}🧹 Cleaning previous builds...${NC}"
./gradlew clean

# Step 2: Build release AAR
echo -e "${BLUE}🔨 Building release AAR...${NC}"
./gradlew :artiusid-sdk:assembleRelease

# Step 3: Find the AAR file
AAR_FILE=$(find artiusid-sdk/build/outputs/aar -name "*-release.aar" | head -n 1)

if [ ! -f "$AAR_FILE" ]; then
    echo -e "${RED}❌ Error: AAR file not found${NC}"
    exit 1
fi

echo -e "${GREEN}✅ AAR built: ${AAR_FILE}${NC}"
echo ""

# Step 4: Copy AAR to root with versioned name
RELEASE_AAR="artiusid-sdk-${VERSION}.aar"
cp "$AAR_FILE" "$RELEASE_AAR"
echo -e "${GREEN}✅ Copied AAR to: ${RELEASE_AAR}${NC}"
echo ""

# Step 5: Create/update GitHub tag
echo -e "${BLUE}🏷️  Creating Git tag v${VERSION}...${NC}"
git tag -a "v${VERSION}" -m "Release v${VERSION}" || echo "Tag already exists"

# Step 6: Push to GitHub
echo -e "${BLUE}📤 Pushing to GitHub...${NC}"
git push origin "v${VERSION}" || echo "Tag already pushed"

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}✅ SDK Published Successfully!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${BLUE}📝 Next steps:${NC}"
echo "1. Go to GitHub: https://github.com/artius-iD/artiusid_sdk_android"
echo "2. Create a new release from tag v${VERSION}"
echo "3. Upload ${RELEASE_AAR} to the release"
echo "4. Add release notes from docs/client/RELEASE_NOTES_v${VERSION}.md"
echo ""

