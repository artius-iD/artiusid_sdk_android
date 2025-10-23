#!/bin/bash

# GitHub Release Script for SDK v1.2.38 - CRITICAL BUG FIX
# Date: October 23, 2025
# Priority: CRITICAL

set -e

# Configuration
GITHUB_TOKEN="${GITHUB_TOKEN}"
REPO_OWNER="artiusid1"
REPO_NAME="mobile-sdk-android"
VERSION="1.2.38"
TAG_NAME="v${VERSION}"
AAR_FILE="artiusid-sdk-${VERSION}.aar"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚨 GitHub Release Script - SDK v${VERSION} CRITICAL BUG FIX${NC}"
echo -e "${BLUE}=================================================${NC}"

# Check if GitHub token is set
if [ -z "$GITHUB_TOKEN" ]; then
    echo -e "${RED}❌ Error: GITHUB_TOKEN environment variable is not set${NC}"
    echo -e "${YELLOW}Please set your GitHub token: export GITHUB_TOKEN=your_token_here${NC}"
    exit 1
fi

# Check if AAR file exists
if [ ! -f "$AAR_FILE" ]; then
    echo -e "${RED}❌ Error: AAR file not found: $AAR_FILE${NC}"
    echo -e "${YELLOW}Please build the SDK first: ./gradlew :artiusid-sdk:assembleRelease${NC}"
    exit 1
fi

# Get AAR file size
AAR_SIZE=$(ls -lh "$AAR_FILE" | awk '{print $5}')
echo -e "${GREEN}📦 AAR file found: $AAR_FILE ($AAR_SIZE)${NC}"

# Create release notes
RELEASE_NOTES="## 🚨 CRITICAL BUG FIX - EncryptedSharedPreferences Corruption Recovery

**Priority:** CRITICAL  
**Release Date:** October 23, 2025  
**Version:** 1.2.38

### 🐛 Critical Issue Fixed

Fixed critical bug where **EncryptedSharedPreferences became permanently corrupted** after clearing certificates, causing \`AEADBadTagException\` on all subsequent certificate registration attempts.

**Before Fix:** Users had to clear app data (\`adb shell pm clear\`) to recover  
**After Fix:** Automatic corruption detection and recovery - seamless user experience

### ✅ What's Fixed

- **Automatic Corruption Detection** - Detects \`AEADBadTagException\` in exception chains
- **Safe Cleanup Process** - Removes corrupted SharedPreferences and master keys
- **Automatic Recovery** - Recreates fresh EncryptedSharedPreferences
- **Production-Ready Error Handling** - Comprehensive logging and graceful fallbacks

### 🔧 Technical Implementation

**New Component:** \`EncryptedPreferencesManager\`
- \`safePutString()\` - Store with corruption recovery
- \`safeGetString()\` - Retrieve with corruption recovery  
- \`safeRemove()\` - Remove with corruption recovery

**Updated Components:**
- All certificate managers now use corruption-safe methods
- Comprehensive logging for debugging
- Exception chaining preservation

### 📊 Testing Results

| Scenario | Before (v1.2.37) | After (v1.2.38) |
|----------|-------------------|------------------|
| Clear certificate → Re-verify | ❌ AEADBadTagException | ✅ Success with auto-recovery |
| Multiple clear/verify cycles | ❌ Permanent failure | ✅ Always recovers |
| App restart after clear | ❌ Still broken | ✅ Works seamlessly |

### 🚀 Deployment

**Files Modified:**
- **NEW:** \`EncryptedPreferencesManager.kt\` - Core corruption recovery utility
- \`CertificateManager.kt\` (utils) - Updated to use safe methods
- \`CertificateManager.kt\` (security) - Updated to use safe methods
- \`gradle.properties\` - Version bump to 1.2.38

**Build Status:** ✅ SUCCESS  
**Package Size:** $AAR_SIZE

### 🎯 Impact

- **Users:** Can clear and re-register certificates without data loss
- **Apps:** \"Clear Certificate\" feature now works reliably  
- **Support:** No more permanent certificate registration failures

### 📞 Upgrade Priority

**CRITICAL** - Immediate upgrade recommended for all apps using certificate management features.

---

## 📦 Installation

### Gradle (Recommended)
\`\`\`gradle
dependencies {
    implementation 'com.artiusid:artiusid-sdk:1.2.38'
}
\`\`\`

### Manual Installation
1. Download \`artiusid-sdk-1.2.38.aar\`
2. Place in \`app/libs/\` directory
3. Add to \`build.gradle\`:
\`\`\`gradle
dependencies {
    implementation files('libs/artiusid-sdk-1.2.38.aar')
}
\`\`\`

## 🔍 Verification

After upgrading, test the fix:
1. Complete initial verification (certificate registers)
2. Clear certificate via app settings
3. Attempt verification again
4. **Expected:** Success with recovery logs in Logcat

## 📋 Changelog

### v1.2.38 (October 23, 2025) - CRITICAL FIX
- 🚨 **CRITICAL:** Fixed EncryptedSharedPreferences corruption after certificate clearing
- ✅ Added automatic corruption detection and recovery
- ✅ New EncryptedPreferencesManager utility class
- ✅ Updated all certificate managers with safe methods
- ✅ Comprehensive logging and error handling
- ✅ Production-ready recovery mechanisms

### Previous Versions
- v1.2.37 - Configurable client ID feature
- v1.2.36 - VerificationGuard timeout fix
- v1.2.35 - UI guard flag reset fix"

echo -e "${YELLOW}📝 Creating GitHub release...${NC}"

# Create the release
RESPONSE=$(curl -s -X POST \
  -H "Authorization: token $GITHUB_TOKEN" \
  -H "Accept: application/vnd.github.v3+json" \
  -d "{
    \"tag_name\": \"$TAG_NAME\",
    \"target_commitish\": \"main\",
    \"name\": \"🚨 SDK v$VERSION - CRITICAL FIX: EncryptedSharedPreferences Corruption Recovery\",
    \"body\": $(echo "$RELEASE_NOTES" | jq -R -s .),
    \"draft\": false,
    \"prerelease\": false
  }" \
  "https://api.github.com/repos/$REPO_OWNER/$REPO_NAME/releases")

# Check if release was created successfully
RELEASE_ID=$(echo "$RESPONSE" | jq -r '.id // empty')
if [ -z "$RELEASE_ID" ]; then
    echo -e "${RED}❌ Failed to create release${NC}"
    echo -e "${RED}Response: $RESPONSE${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Release created successfully (ID: $RELEASE_ID)${NC}"

# Upload the AAR file
echo -e "${YELLOW}📤 Uploading AAR file...${NC}"

UPLOAD_RESPONSE=$(curl -s -X POST \
  -H "Authorization: token $GITHUB_TOKEN" \
  -H "Content-Type: application/octet-stream" \
  --data-binary @"$AAR_FILE" \
  "https://uploads.github.com/repos/$REPO_OWNER/$REPO_NAME/releases/$RELEASE_ID/assets?name=$AAR_FILE")

# Check if upload was successful
ASSET_ID=$(echo "$UPLOAD_RESPONSE" | jq -r '.id // empty')
if [ -z "$ASSET_ID" ]; then
    echo -e "${RED}❌ Failed to upload AAR file${NC}"
    echo -e "${RED}Response: $UPLOAD_RESPONSE${NC}"
    exit 1
fi

echo -e "${GREEN}✅ AAR file uploaded successfully (Asset ID: $ASSET_ID)${NC}"

# Get release URL
RELEASE_URL=$(echo "$RESPONSE" | jq -r '.html_url')

echo -e "${BLUE}=================================================${NC}"
echo -e "${GREEN}🎉 SDK v$VERSION CRITICAL FIX RELEASED SUCCESSFULLY!${NC}"
echo -e "${BLUE}=================================================${NC}"
echo -e "${GREEN}📦 Package: $AAR_FILE ($AAR_SIZE)${NC}"
echo -e "${GREEN}🔗 Release URL: $RELEASE_URL${NC}"
echo -e "${GREEN}🏷️  Tag: $TAG_NAME${NC}"
echo -e "${GREEN}📅 Date: $(date)${NC}"
echo -e "${BLUE}=================================================${NC}"
echo -e "${YELLOW}🚨 CRITICAL FIX: EncryptedSharedPreferences corruption recovery${NC}"
echo -e "${YELLOW}📞 Notify app teams to upgrade immediately${NC}"
echo -e "${BLUE}=================================================${NC}"
