#!/bin/bash
# Build ArtiusID SDK release AAR, tag the version, push to GitHub, and create a GitHub Release.
# Requires: git, and optionally 'gh' (GitHub CLI) for creating the release with AAR asset.
# Usage: ./upload-build-to-github.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Version from gradle.properties
SDK_VERSION=$(grep -E "^SDK_VERSION_NAME=" gradle.properties | cut -d= -f2)
TAG_NAME="v${SDK_VERSION}"

# Warn if working tree has uncommitted changes (tag would point to current HEAD)
if ! git diff-index --quiet HEAD -- 2>/dev/null; then
  echo "Warning: You have uncommitted changes. The tag will point to the current commit (HEAD)."
  echo "         Commit and push to origin first if you want the tag to include the latest changes."
  if [ -t 0 ]; then
    read -p "Continue anyway? [y/N] " -n 1 -r; echo
    if [[ ! $REPLY =~ ^[yY]$ ]]; then exit 1; fi
  fi
fi

echo "=========================================="
echo " ArtiusID SDK - Build, Tag & Release"
echo " Version: ${SDK_VERSION}  Tag: ${TAG_NAME}"
echo "=========================================="

# 1. Build release AAR
echo ""
echo "Building release AAR..."
export JAVA_HOME="${JAVA_HOME:-/Applications/Android Studio.app/Contents/jbr/Contents/Home}"
./gradlew :artiusid-sdk:assembleRelease

# 2. Copy AAR to root with version name
AAR_SRC="artiusid-sdk/build/outputs/aar/artiusid-sdk-release.aar"
AAR_DEST="artiusid-sdk-${SDK_VERSION}.aar"
if [ -f "$AAR_SRC" ]; then
  cp -f "$AAR_SRC" "$AAR_DEST"
  echo "Copied AAR to $AAR_DEST"
else
  echo "Error: AAR not found at $AAR_SRC" >&2
  exit 1
fi

# 3. Tag this commit if the tag doesn't already exist
if git rev-parse "$TAG_NAME" >/dev/null 2>&1; then
  echo ""
  echo "Tag $TAG_NAME already exists. Skipping tag creation."
else
  echo ""
  echo "Creating tag $TAG_NAME..."
  git tag -a "$TAG_NAME" -m "Release ${SDK_VERSION}"
fi

# 4. Push main and tags to GitHub
echo ""
echo "Pushing main and tags to GitHub (remote: github)..."
git push github main
git push github "$TAG_NAME"

# 5. Create GitHub Release with AAR asset (if gh is installed)
if command -v gh >/dev/null 2>&1; then
  if gh release view "$TAG_NAME" --repo artius-iD/artiusid_sdk_android >/dev/null 2>&1; then
    echo "Release $TAG_NAME already exists. Uploading AAR as asset..."
    gh release upload "$TAG_NAME" "$AAR_DEST" --repo artius-iD/artiusid_sdk_android --clobber
  else
    echo "Creating GitHub Release $TAG_NAME..."
    gh release create "$TAG_NAME" "$AAR_DEST" \
      --repo artius-iD/artiusid_sdk_android \
      --title "ArtiusID Android SDK ${SDK_VERSION}" \
      --notes "ArtiusID Android SDK release ${SDK_VERSION}. Attached: artiusid-sdk-${SDK_VERSION}.aar"
  fi
else
  echo ""
  echo "GitHub CLI (gh) not found. Tag pushed; create the release manually at:"
  echo "  https://github.com/artius-iD/artiusid_sdk_android/releases/new?tag=${TAG_NAME}"
  echo "  and attach: $AAR_DEST"
fi

echo ""
echo "Done. main and $TAG_NAME pushed to https://github.com/artius-iD/artiusid_sdk_android"
