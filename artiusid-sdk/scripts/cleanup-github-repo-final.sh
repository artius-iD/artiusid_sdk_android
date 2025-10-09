#!/bin/bash

# Final GitHub Repository Cleanup Script
# Removes all non-essential files from the customer-facing repository

set -e

echo "🧹 Final GitHub Repository Cleanup"
echo "=================================="

REPO_OWNER="artius-iD"
REPO_NAME="artiusid_sdk_android"
BRANCH="main"

# Create a temporary directory for cloning
TEMP_DIR=$(mktemp -d)
echo "Working in: $TEMP_DIR"
cd "$TEMP_DIR"

# Clone the repository
echo "Cloning repository..."
git clone "https://github.com/$REPO_OWNER/$REPO_NAME.git" .
git checkout "$BRANCH"

echo "Current repository contents:"
ls -la

# Files and directories to remove (should NOT be in customer distribution)
declare -a ITEMS_TO_REMOVE=(
    "SECURITY_NOTICE.md"
    "build.gradle"
    "sample/"
)

echo ""
echo "Removing non-essential files..."

# Remove specified items
for item in "${ITEMS_TO_REMOVE[@]}"; do
    if [ -e "$item" ]; then
        echo "Removing: $item"
        git rm -rf "$item"
    else
        echo "Not found (OK): $item"
    fi
done

echo ""
echo "Final repository contents should only include:"
echo "✅ INTEGRATION_GUIDE.md"
echo "✅ LICENSE.txt" 
echo "✅ README.md"
echo "✅ integration-template/"
echo "✅ sample-app/ (with README.md only)"
echo "✅ sdk/ (with AAR files and consumer-rules.pro only)"

echo ""
echo "Actual contents after cleanup:"
ls -la

# Commit and push changes
if git diff --cached --quiet; then
    echo "No changes to commit."
else
    git commit -m "FINAL CLEANUP: Remove internal files from customer distribution

- Remove SECURITY_NOTICE.md (internal security document)
- Remove build.gradle (internal build configuration)
- Remove sample/ directory (contained source code)

Repository now contains ONLY customer-essential files:
- SDK AAR files and consumer ProGuard rules
- Public integration guide and templates
- License and README
- Sample app download instructions"

    echo "Pushing changes to GitHub..."
    git push origin "$BRANCH"
    echo "✅ Cleanup complete!"
fi

# Clean up temporary directory
cd - > /dev/null
rm -rf "$TEMP_DIR"

echo ""
echo "🎉 GitHub repository cleaned successfully!"
echo "Repository now contains ONLY essential customer files."
