#!/bin/bash

# ArtiusID SDK GitHub Repository Cleanup
# Removes internal documentation files that shouldn't be in customer-facing repository

set -e

echo "🧹 ArtiusID SDK Repository Cleanup"
echo "=================================="

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

# Check for required tools
if ! command -v gh &> /dev/null; then
    print_error "GitHub CLI not found. Please install: https://cli.github.com/"
    exit 1
fi

# Check GitHub authentication
if ! gh auth status &> /dev/null; then
    print_info "GitHub authentication required..."
    gh auth login
fi

GITHUB_REPO="artius-iD/artiusid_sdk_android"

print_info "Repository: $GITHUB_REPO"

# Create temporary directory for cleanup
TEMP_DIR=$(mktemp -d)
print_info "Working in: $TEMP_DIR"
cd "$TEMP_DIR"

# Clone the repository
print_info "Cloning repository for cleanup..."
git clone "https://github.com/$GITHUB_REPO.git" .

print_info "Current repository contents:"
echo "📁 Root files:"
ls -la | grep -E '^-' | awk '{print "   " $9}'
echo "📁 Docs directory:"
ls -la docs/ 2>/dev/null | grep -E '^-' | awk '{print "   " $9}' || echo "   (empty or missing)"

echo ""
print_warning "🗑️  Files to be DELETED (internal documentation):"
echo "   ❌ docs/SDK_DISTRIBUTION_SECURITY.md"
echo "   ❌ docs/SDK_SECURITY_GUIDE.md" 
echo "   ❌ docs/Image_Override_System_Documentation.md"

echo ""
print_info "✅ Files to be KEPT (customer-facing):"
echo "   ✅ README.md"
echo "   ✅ INTEGRATION_GUIDE.md"
echo "   ✅ LICENSE.txt"
echo "   ✅ sdk/artiusid-sdk-*.aar"
echo "   ✅ sdk/consumer-rules.pro"
echo "   ✅ sample/"

echo ""
read -p "❓ Proceed with cleanup? This will permanently delete internal documentation files. (y/N): " CONFIRM

if [[ ! "$CONFIRM" =~ ^[Yy]$ ]]; then
    print_info "Cleanup cancelled."
    cd /
    rm -rf "$TEMP_DIR"
    exit 0
fi

print_info "🗑️  Removing internal documentation files..."

# Remove internal documentation files
if [ -f "docs/SDK_DISTRIBUTION_SECURITY.md" ]; then
    git rm "docs/SDK_DISTRIBUTION_SECURITY.md"
    print_status "Removed SDK_DISTRIBUTION_SECURITY.md"
else
    print_warning "SDK_DISTRIBUTION_SECURITY.md not found"
fi

if [ -f "docs/SDK_SECURITY_GUIDE.md" ]; then
    git rm "docs/SDK_SECURITY_GUIDE.md"
    print_status "Removed SDK_SECURITY_GUIDE.md"
else
    print_warning "SDK_SECURITY_GUIDE.md not found"
fi

if [ -f "docs/Image_Override_System_Documentation.md" ]; then
    git rm "docs/Image_Override_System_Documentation.md"
    print_status "Removed Image_Override_System_Documentation.md"
else
    print_warning "Image_Override_System_Documentation.md not found"
fi

# Check if docs directory is empty and remove it
if [ -d "docs" ] && [ -z "$(ls -A docs)" ]; then
    git rm -r docs/
    print_status "Removed empty docs directory"
fi

# Update README to remove references to deleted documentation
print_info "📝 Updating README to remove internal documentation references..."

if [ -f "README.md" ]; then
    # Remove lines that reference internal documentation
    sed -i.bak '/Security Guide.*docs\/SDK_DISTRIBUTION_SECURITY.md/d' README.md
    sed -i.bak '/Image Override System.*docs\/Image_Override_System_Documentation.md/d' README.md
    sed -i.bak '/docs\/SDK_SECURITY_GUIDE.md/d' README.md
    rm -f README.md.bak
    
    git add README.md
    print_status "Updated README.md to remove internal documentation links"
fi

# Commit the cleanup
print_info "💾 Committing cleanup changes..."
git commit -m "Security cleanup: Remove internal documentation from customer repository

- Removed SDK_DISTRIBUTION_SECURITY.md (internal security details)
- Removed SDK_SECURITY_GUIDE.md (internal implementation details)  
- Removed Image_Override_System_Documentation.md (internal system docs)
- Updated README.md to remove internal documentation references

This ensures customers only receive public API documentation and integration guides."

# Push the changes
print_info "🚀 Pushing cleanup to GitHub..."
git push origin main 2>/dev/null || git push origin master

print_status "Repository cleanup completed successfully!"

echo ""
print_info "📋 Summary of changes:"
echo "   🗑️  Removed 3 internal documentation files"
echo "   📝 Updated README.md references"
echo "   🔒 Repository now contains only customer-facing files"

echo ""
print_info "✅ Current customer-only repository structure:"
echo "   📦 sdk/artiusid-sdk-*.aar (obfuscated SDK files)"
echo "   📄 INTEGRATION_GUIDE.md (public API documentation)"
echo "   📄 LICENSE.txt (usage agreement)"
echo "   📁 sample/ (minimal integration examples)"
echo "   📄 README.md (customer documentation)"

# Cleanup
cd /
rm -rf "$TEMP_DIR"
print_status "Temporary files cleaned up"

echo ""
echo "🎉 Repository cleanup completed!"
echo "🔗 Repository: https://github.com/$GITHUB_REPO"
echo ""
echo "📋 Next steps:"
echo "   1. Verify the repository only contains customer files"
echo "   2. Update any external links that referenced deleted documentation"
echo "   3. Inform team about the cleanup"
