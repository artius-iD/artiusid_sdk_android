#!/bin/bash

# ArtiusID SDK - HILT Setup Script
# This script helps configure HILT for ArtiusID SDK integration

echo "🚀 ArtiusID SDK - HILT Setup Script"
echo "=================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Check if we're in an Android project
if [ ! -f "build.gradle" ] && [ ! -f "build.gradle.kts" ]; then
    print_error "Not in an Android project root directory!"
    exit 1
fi

print_info "Detected Android project"

# Check for app module
if [ ! -d "app" ]; then
    print_error "No 'app' module found!"
    exit 1
fi

APP_BUILD_GRADLE="app/build.gradle"
if [ ! -f "$APP_BUILD_GRADLE" ]; then
    APP_BUILD_GRADLE="app/build.gradle.kts"
    if [ ! -f "$APP_BUILD_GRADLE" ]; then
        print_error "No build.gradle file found in app module!"
        exit 1
    fi
fi

print_info "Found app build file: $APP_BUILD_GRADLE"

# Backup original build.gradle
cp "$APP_BUILD_GRADLE" "${APP_BUILD_GRADLE}.backup"
print_status "Created backup: ${APP_BUILD_GRADLE}.backup"

# Check if HILT plugin is already applied
if grep -q "com.google.dagger.hilt.android" "$APP_BUILD_GRADLE"; then
    print_warning "HILT plugin already applied"
else
    print_info "Adding HILT plugin..."
    
    # Add HILT plugin after the existing plugins
    if [[ "$APP_BUILD_GRADLE" == *.kts ]]; then
        # Kotlin DSL
        sed -i.tmp '/id.*com.android.application/a\
    id("com.google.dagger.hilt.android")' "$APP_BUILD_GRADLE"
    else
        # Groovy DSL
        sed -i.tmp "/id 'com.android.application'/a\\
    id 'com.google.dagger.hilt.android'" "$APP_BUILD_GRADLE"
    fi
    print_status "Added HILT plugin"
fi

# Check if KSP plugin is already applied
if grep -q "com.google.devtools.ksp" "$APP_BUILD_GRADLE"; then
    print_warning "KSP plugin already applied"
else
    print_info "Adding KSP plugin..."
    
    if [[ "$APP_BUILD_GRADLE" == *.kts ]]; then
        sed -i.tmp '/id.*com.google.dagger.hilt.android/a\
    id("com.google.devtools.ksp")' "$APP_BUILD_GRADLE"
    else
        sed -i.tmp "/id 'com.google.dagger.hilt.android'/a\\
    id 'com.google.devtools.ksp'" "$APP_BUILD_GRADLE"
    fi
    print_status "Added KSP plugin"
fi

# Add HILT dependencies if not present
if grep -q "hilt-android" "$APP_BUILD_GRADLE"; then
    print_warning "HILT dependencies already present"
else
    print_info "Adding HILT dependencies..."
    
    # Find the dependencies block and add HILT dependencies
    if [[ "$APP_BUILD_GRADLE" == *.kts ]]; then
        # Kotlin DSL
        cat >> "$APP_BUILD_GRADLE" << 'EOF'

    // HILT - Added by ArtiusID SDK setup script
    val hiltVersion = "2.48"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-android-compiler:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
EOF
    else
        # Groovy DSL
        cat >> "$APP_BUILD_GRADLE" << 'EOF'

    // HILT - Added by ArtiusID SDK setup script
    def hilt_version = "2.48"
    implementation "com.google.dagger:hilt-android:${hilt_version}"
    ksp "com.google.dagger:hilt-android-compiler:${hilt_version}"
    implementation 'androidx.hilt:hilt-navigation-compose:1.1.0'
EOF
    fi
    print_status "Added HILT dependencies"
fi

# Check for Application class
MANIFEST_FILE="app/src/main/AndroidManifest.xml"
if [ -f "$MANIFEST_FILE" ]; then
    if grep -q "android:name=" "$MANIFEST_FILE"; then
        print_warning "Application class already specified in manifest"
    else
        print_info "You need to create an Application class with @HiltAndroidApp"
        echo ""
        echo "Create a file like app/src/main/java/com/yourpackage/YourApplication.kt:"
        echo ""
        cat << 'EOF'
@HiltAndroidApp
class YourApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase if needed
        FirebaseApp.initializeApp(this)
    }
}
EOF
        echo ""
        echo "Then add to AndroidManifest.xml:"
        echo '<application android:name=".YourApplication" ...>'
    fi
fi

# Check project-level build.gradle for HILT classpath
PROJECT_BUILD_GRADLE="build.gradle"
if [ ! -f "$PROJECT_BUILD_GRADLE" ]; then
    PROJECT_BUILD_GRADLE="build.gradle.kts"
fi

if [ -f "$PROJECT_BUILD_GRADLE" ]; then
    if grep -q "hilt-android-gradle-plugin" "$PROJECT_BUILD_GRADLE"; then
        print_warning "HILT classpath already in project build.gradle"
    else
        print_info "You may need to add HILT classpath to project-level build.gradle:"
        echo ""
        echo "In the dependencies block of your project-level build.gradle:"
        echo "classpath 'com.google.dagger:hilt-android-gradle-plugin:2.48'"
    fi
fi

# Clean up temporary files
rm -f "${APP_BUILD_GRADLE}.tmp"

echo ""
print_status "HILT setup completed!"
echo ""
print_info "Next steps:"
echo "1. Sync your project"
echo "2. Create Application class with @HiltAndroidApp if not done"
echo "3. Add @AndroidEntryPoint to activities using the SDK"
echo "4. Initialize ArtiusID SDK in your Application or MainActivity"
echo ""
print_info "For detailed instructions, see HILT_INTEGRATION_GUIDE.md"

# Offer to run diagnostic
echo ""
read -p "Would you like to run HILT diagnostic? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    if command -v ./gradlew &> /dev/null; then
        print_info "Running HILT diagnostic..."
        ./gradlew diagnoseHilt
    else
        print_warning "Gradle wrapper not found. Run './gradlew diagnoseHilt' manually after setup."
    fi
fi

print_status "Setup script completed!"
