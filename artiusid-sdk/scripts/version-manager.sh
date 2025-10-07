#!/bin/bash

# ArtiusID SDK Version Manager
# Utility for managing SDK versions across all configuration files

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$SCRIPT_DIR/../.."
GRADLE_PROPERTIES="$PROJECT_ROOT/gradle.properties"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_status() { echo -e "${GREEN}✅${NC} $1"; }
print_info() { echo -e "${BLUE}ℹ️${NC} $1"; }
print_warning() { echo -e "${YELLOW}⚠️${NC} $1"; }

# Get current version
get_current_version() {
    grep "^SDK_VERSION_NAME=" "$GRADLE_PROPERTIES" | cut -d'=' -f2
}

get_current_version_code() {
    grep "^SDK_VERSION_CODE=" "$GRADLE_PROPERTIES" | cut -d'=' -f2
}

# Increment version
increment_version() {
    local version=$1
    local type=$2
    
    IFS='.' read -ra VERSION_PARTS <<< "$version"
    local major=${VERSION_PARTS[0]}
    local minor=${VERSION_PARTS[1]}
    local patch=${VERSION_PARTS[2]}
    
    case $type in
        "major") echo "$((major + 1)).0.0" ;;
        "minor") echo "$major.$((minor + 1)).0" ;;
        "patch") echo "$major.$minor.$((patch + 1))" ;;
    esac
}

# Update all version references
update_all_versions() {
    local new_version=$1
    local new_version_code=$2
    
    print_info "Updating gradle.properties..."
    sed -i.bak "s/^SDK_VERSION_NAME=.*/SDK_VERSION_NAME=$new_version/" "$GRADLE_PROPERTIES"
    sed -i.bak "s/^SDK_VERSION_CODE=.*/SDK_VERSION_CODE=$new_version_code/" "$GRADLE_PROPERTIES"
    sed -i.bak "s/^PUBLISH_VERSION=.*/PUBLISH_VERSION=$new_version/" "$GRADLE_PROPERTIES"
    rm -f "$GRADLE_PROPERTIES.bak"
    
    print_status "All versions updated to $new_version (code: $new_version_code)"
}

# Show current version info
show_version_info() {
    local current_version=$(get_current_version)
    local current_code=$(get_current_version_code)
    
    echo "📋 Current Version Information:"
    echo "   Version Name: $current_version"
    echo "   Version Code: $current_code"
    echo ""
    echo "🔄 Available Actions:"
    echo "   Patch: $current_version → $(increment_version "$current_version" "patch")"
    echo "   Minor: $current_version → $(increment_version "$current_version" "minor")"
    echo "   Major: $current_version → $(increment_version "$current_version" "major")"
}

# Main menu
main() {
    echo "🏷️  ArtiusID SDK Version Manager"
    echo "================================"
    echo ""
    
    show_version_info
    
    echo ""
    echo "Choose an action:"
    echo "1. Increment patch version"
    echo "2. Increment minor version" 
    echo "3. Increment major version"
    echo "4. Set custom version"
    echo "5. Show version info only"
    echo "6. Exit"
    
    read -p "Enter choice (1-6): " choice
    
    local current_version=$(get_current_version)
    local current_code=$(get_current_version_code)
    local new_version=""
    local new_code=$((current_code + 1))
    
    case $choice in
        1) new_version=$(increment_version "$current_version" "patch") ;;
        2) new_version=$(increment_version "$current_version" "minor") ;;
        3) new_version=$(increment_version "$current_version" "major") ;;
        4) 
            read -p "Enter new version (e.g., 2.1.0): " new_version
            if ! [[ "$new_version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
                print_warning "Invalid version format. Use semantic versioning (e.g., 1.0.0)"
                exit 1
            fi
            ;;
        5) exit 0 ;;
        6) exit 0 ;;
        *) print_warning "Invalid choice"; exit 1 ;;
    esac
    
    if [ -n "$new_version" ]; then
        echo ""
        print_info "Updating version: $current_version → $new_version"
        read -p "Confirm update? (y/N): " confirm
        
        if [[ "$confirm" =~ ^[Yy]$ ]]; then
            update_all_versions "$new_version" "$new_code"
            
            # Commit changes if in git repo
            if git rev-parse --git-dir > /dev/null 2>&1; then
                cd "$PROJECT_ROOT"
                git add "$GRADLE_PROPERTIES"
                git commit -m "Bump version to $new_version"
                print_status "Version committed to git"
            fi
        else
            print_info "Version update cancelled"
        fi
    fi
}

main "$@"
