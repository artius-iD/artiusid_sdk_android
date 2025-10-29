# ArtiusID SDK - Build Guide

**Complete build instructions for internal developers**

---

## 🏗️ Building the SDK from Source

This guide covers building the ArtiusID Android SDK AAR from source code.

---

## 📋 Prerequisites

### **Required Software**

| Tool | Version | Purpose |
|------|---------|---------|
| **Android Studio** | Jellyfish (2023.3.1)+ | IDE and build tools |
| **JDK** | 17+ | Java compilation |
| **Gradle** | 8.0+ | Build automation |
| **Kotlin** | 1.9.0+ | Language |
| **Git** | Any recent | Version control |

### **System Requirements**

- **OS:** macOS, Linux, or Windows
- **RAM:** 8GB minimum, 16GB recommended
- **Disk:** 10GB free space
- **Network:** Internet connection for dependencies

---

## 🚀 Quick Build

### **Build Release AAR**

```bash
# Navigate to project root
cd mobile-sdk-android

# Clean previous builds
./gradlew clean

# Build release AAR (obfuscated)
./gradlew :artiusid-sdk:assembleRelease

# Output location
ls -lh artiusid-sdk/build/outputs/aar/artiusid-sdk-release.aar
```

**Result:** Obfuscated AAR file (~25MB) ready for distribution.

---

## 🔧 Build Commands

### **SDK Module**

```bash
# Clean build
./gradlew :artiusid-sdk:clean

# Debug build (no obfuscation, includes debug symbols)
./gradlew :artiusid-sdk:assembleDebug

# Release build (obfuscated, optimized)
./gradlew :artiusid-sdk:assembleRelease

# Both debug and release
./gradlew :artiusid-sdk:assemble

# Lint checks
./gradlew :artiusid-sdk:lint

# Run tests
./gradlew :artiusid-sdk:test
```

### **Sample App**

```bash
# Debug build
./gradlew :sample-app:assembleDebug

# Release build
./gradlew :sample-app:assembleRelease

# Customer distribution build (heavily obfuscated)
./gradlew :sample-app:assembleCustomerDistribution

# Install on connected device
./gradlew :sample-app:installDebug
```

### **Entire Project**

```bash
# Clean everything
./gradlew clean

# Build all modules
./gradlew assemble

# Run all tests
./gradlew test

# Lint all modules
./gradlew lint
```

---

## 📦 Build Output

### **SDK AAR Files**

After building, AAR files are located at:

```
artiusid-sdk/build/outputs/aar/
├── artiusid-sdk-debug.aar    # Debug build (~30MB)
└── artiusid-sdk-release.aar  # Release build (~25MB)
```

### **Sample App APKs**

```
sample-app/build/outputs/apk/
├── debug/
│   └── sample-app-debug.apk           # Debug build (~30MB)
├── release/
│   └── sample-app-release.apk         # Release build (~20MB)
└── customerDistribution/
    └── sample-app-customerDistribution.apk  # Client build (~173MB)
```

### **ProGuard Mapping Files**

```
artiusid-sdk/build/outputs/mapping/release/
├── mapping.txt        # Symbol mapping for de-obfuscation
├── seeds.txt          # Entry points (not obfuscated)
├── usage.txt          # Removed code
└── configuration.txt  # ProGuard configuration used
```

**⚠️ Important:** Save `mapping.txt` for each release to decode production crash reports.

---

## 🔐 Build Variants

### **SDK Build Variants**

| Variant | Obfuscation | Debug Symbols | Size | Use Case |
|---------|-------------|---------------|------|----------|
| **debug** | ❌ No | ✅ Yes | ~30MB | Development & testing |
| **release** | ✅ Yes | ❌ No | ~25MB | Production distribution |

### **Sample App Build Variants**

| Variant | Obfuscation | Level | Use Case |
|---------|-------------|-------|----------|
| **debug** | ❌ No | - | Development |
| **release** | ✅ Yes | Standard | Internal testing |
| **customerDistribution** | ✅ Yes | Heavy | Client distribution |

---

## ⚙️ Build Configuration

### **Gradle Properties**

**File:** `gradle.properties`

```properties
# Gradle daemon optimization
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m
org.gradle.parallel=true
org.gradle.caching=true

# Android build optimization
android.useAndroidX=true
android.enableJetifier=false

# Kotlin optimization
kotlin.code.style=official
kotlin.incremental=true
```

### **SDK Build Configuration**

**File:** `artiusid-sdk/build.gradle`

Key settings:

```gradle
android {
    compileSdk = 34
    
    defaultConfig {
        minSdk = 24
        targetSdk = 34
        versionCode = 56
        versionName = "1.2.48"
    }
    
    buildTypes {
        release {
            minifyEnabled = true
            proguardFiles(
                getDefaultProguardFile('proguard-android-optimize.txt'),
                'proguard-rules.pro'
            )
            consumerProguardFiles 'consumer-rules.pro'
        }
    }
    
    buildFeatures {
        compose = true
        viewBinding = true
        dataBinding = true
    }
}
```

---

## 🛡️ ProGuard Configuration

### **SDK ProGuard Rules**

**File:** `artiusid-sdk/proguard-rules.pro`

Protects:
- Public SDK APIs
- Kotlin metadata
- Compose runtime
- Third-party libraries
- ML models

### **Consumer Rules**

**File:** `artiusid-sdk/consumer-rules.pro`

Rules applied to client apps using the SDK:
- Keep SDK public classes
- Preserve callbacks and interfaces
- Protect Hilt components

### **Verify Obfuscation**

```bash
# Build release AAR
./gradlew :artiusid-sdk:assembleRelease

# Check mapping file
cat artiusid-sdk/build/outputs/mapping/release/mapping.txt | head -20

# Inspect AAR contents
unzip -l artiusid-sdk/build/outputs/aar/artiusid-sdk-release.aar
```

---

## 🔍 Build Verification

### **Post-Build Checks**

```bash
# 1. Verify AAR was created
ls -lh artiusid-sdk/build/outputs/aar/artiusid-sdk-release.aar

# 2. Check AAR size (should be ~25MB)
du -h artiusid-sdk/build/outputs/aar/artiusid-sdk-release.aar

# 3. Inspect AAR contents
unzip -l artiusid-sdk/build/outputs/aar/artiusid-sdk-release.aar

# 4. Verify mapping file exists
ls -lh artiusid-sdk/build/outputs/mapping/release/mapping.txt

# 5. Check for compilation errors
./gradlew :artiusid-sdk:assembleRelease --warning-mode all
```

### **Integration Testing**

After building, test with the sample app:

```bash
# Build and install sample app
./gradlew :sample-app:assembleDebug
./gradlew :sample-app:installDebug

# Run on connected device
adb shell am start -n com.artiusid.sampleapp/.BridgeMainActivity

# Watch logs
adb logcat | grep -E "ArtiusIDSDK|Sample"
```

---

## 🚨 Troubleshooting

### **Build Failures**

#### **Out of Memory**

**Error:** `OutOfMemoryError: Java heap space`

**Solution:**
```properties
# In gradle.properties, increase heap size:
org.gradle.jvmargs=-Xmx6144m -XX:MaxMetaspaceSize=1024m
```

#### **Hilt Compilation Error**

**Error:** `Hilt component not found`

**Solution:**
```bash
# Clean and rebuild
./gradlew clean
./gradlew :artiusid-sdk:kaptDebug
./gradlew :artiusid-sdk:assembleRelease
```

#### **Kotlin Compilation Error**

**Error:** `Kotlin version mismatch`

**Solution:**
```bash
# Update Kotlin plugin in project build.gradle
# Ensure consistency across all modules
```

#### **ProGuard Error**

**Error:** `ProGuard configuration error`

**Solution:**
```bash
# Check ProGuard rules syntax
# Verify consumer-rules.pro is valid
# Review mapping file for warnings
```

### **Performance Issues**

#### **Slow Builds**

```bash
# Enable build cache
./gradlew assemble --build-cache

# Use parallel execution
./gradlew assemble --parallel

# Offline mode (if dependencies cached)
./gradlew assemble --offline
```

#### **Gradle Daemon Issues**

```bash
# Stop all Gradle daemons
./gradlew --stop

# Rebuild
./gradlew :artiusid-sdk:assembleRelease
```

---

## 📊 Build Metrics

### **Typical Build Times**

| Task | Duration | Machine |
|------|----------|---------|
| Clean build (debug) | ~45s | M1 Mac |
| Incremental build | ~10s | M1 Mac |
| Release build (full) | ~75s | M1 Mac |
| ProGuard processing | ~15s | M1 Mac |

### **Artifact Sizes**

| Artifact | Size | Components |
|----------|------|------------|
| **SDK AAR (debug)** | ~30MB | Code + symbols |
| **SDK AAR (release)** | ~25MB | Obfuscated code |
| **ML Models** | ~15MB | Face detection/recognition |
| **Resources** | ~5MB | Images, fonts, strings |
| **Code** | ~5MB | Compiled Kotlin/Java |

---

## 🔄 Continuous Integration

### **CI Build Script**

```bash
#!/bin/bash
# ci-build.sh

set -e

echo "🧹 Cleaning..."
./gradlew clean

echo "🔨 Building SDK..."
./gradlew :artiusid-sdk:assembleRelease

echo "🧪 Running tests..."
./gradlew :artiusid-sdk:test

echo "🔍 Running lint..."
./gradlew :artiusid-sdk:lint

echo "✅ Build complete!"

# Save artifacts
cp artiusid-sdk/build/outputs/aar/artiusid-sdk-release.aar ./artifacts/
cp artiusid-sdk/build/outputs/mapping/release/mapping.txt ./artifacts/
```

### **Local CI Simulation**

```bash
# Make executable
chmod +x ci-build.sh

# Run
./ci-build.sh
```

---

## 📦 Packaging for Distribution

### **1. Build Release AAR**

```bash
./gradlew :artiusid-sdk:assembleRelease
```

### **2. Copy to Distribution Directory**

```bash
# Create distribution directory
mkdir -p distribution/v1.2.48

# Copy AAR
cp artiusid-sdk/build/outputs/aar/artiusid-sdk-release.aar \
   distribution/v1.2.48/artiusid-sdk-1.2.48.aar

# Copy consumer rules
cp artiusid-sdk/consumer-rules.pro \
   distribution/v1.2.48/

# Save mapping file
cp artiusid-sdk/build/outputs/mapping/release/mapping.txt \
   distribution/v1.2.48/mapping-1.2.48.txt
```

### **3. Create Archive**

```bash
cd distribution
tar -czf artiusid-sdk-1.2.48.tar.gz v1.2.48/
```

---

## 🔐 Security Considerations

### **ProGuard Obfuscation**

- ✅ All internal classes obfuscated
- ✅ Public SDK APIs preserved
- ✅ String encryption enabled
- ✅ Debug info removed

### **Code Signing**

For production releases, sign the AAR:

```bash
# Sign AAR (if applicable)
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore release.keystore \
  artiusid-sdk-release.aar \
  your-key-alias
```

### **Mapping File Storage**

```bash
# Store mapping file securely
mkdir -p ~/.artiusid/mappings
cp artiusid-sdk/build/outputs/mapping/release/mapping.txt \
   ~/.artiusid/mappings/mapping-v1.2.48.txt
```

---

## 🎯 Best Practices

### **Before Building**

- [ ] Update version numbers
- [ ] Review recent code changes
- [ ] Run lint checks
- [ ] Run unit tests
- [ ] Update documentation

### **During Build**

- [ ] Monitor build output for warnings
- [ ] Check ProGuard warnings
- [ ] Verify all dependencies resolved
- [ ] Watch for deprecation warnings

### **After Building**

- [ ] Verify AAR size (~25MB)
- [ ] Test with sample app
- [ ] Check mapping file created
- [ ] Run integration tests
- [ ] Store mapping file securely

---

## 📞 Support

For build issues:
- Check this guide's troubleshooting section
- Review Gradle output for specific errors
- Consult the SDK team
- Check GitLab issues for known problems

---

## 📚 Related Documentation

- **[DEVELOPER_README.md](DEVELOPER_README.md)** - Development guide
- **[sample-app/README.md](sample-app/README.md)** - Sample app documentation
- **[SDK_DEPENDENCY_REQUIREMENTS.md](SDK_DEPENDENCY_REQUIREMENTS.md)** - Dependencies

---

**Last Updated:** October 29, 2025  
**Maintained By:** ArtiusID SDK Team

