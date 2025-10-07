# ArtiusID Android SDK Deployment Guide

## Overview

This guide covers the enhanced deployment process for the ArtiusID Android SDK, including automated versioning and secure AAR distribution to GitHub.

## 🚀 Quick Start

### Option 1: Automated Deployment (Recommended)
```bash
# Run the enhanced deployment script
./artiusid-sdk/scripts/publish-android-github-improved.sh
```

### Option 2: Manual Version Management
```bash
# Manage versions separately
./artiusid-sdk/scripts/version-manager.sh

# Then deploy
./artiusid-sdk/scripts/publish-android-github-improved.sh
```

## 📋 What's New in the Enhanced Script

### ✅ **Improvements Made:**

1. **Automated Versioning**
   - Auto-increment patch/minor/major versions
   - Reads from `gradle.properties`
   - Updates all version references automatically
   - Commits version changes to git

2. **Enhanced IP Protection**
   - **REMOVED** source code copying (was a security risk!)
   - Only distributes obfuscated AAR files
   - Validates AAR obfuscation before deployment
   - Creates comprehensive integration guides instead

3. **Better Build Validation**
   - Ensures release AAR is built (not debug)
   - Validates ProGuard obfuscation is working
   - Checks AAR file integrity

4. **Improved Documentation**
   - Auto-generates integration guides
   - Creates comprehensive README files
   - Includes security documentation
   - Provides sample configuration files

5. **Enhanced GitHub Integration**
   - Better release notes with markdown formatting
   - Proper asset uploading
   - Comprehensive changelog generation

## 🏷️ Version Management

### Current Version Configuration
All versions are managed through `gradle.properties`:

```properties
SDK_VERSION_NAME=1.0.0
SDK_VERSION_CODE=1
SDK_MIN_SDK_VERSION=24
SDK_TARGET_SDK_VERSION=34
SDK_COMPILE_SDK_VERSION=34
PUBLISH_VERSION=1.0.0
```

### Version Manager Tool
Use the version manager for easy version updates:

```bash
./artiusid-sdk/scripts/version-manager.sh
```

**Features:**
- Interactive version selection
- Automatic version code increment
- Updates all configuration files
- Git commit integration

### Versioning Options

1. **Patch Version** (1.0.0 → 1.0.1)
   - Bug fixes
   - Minor improvements
   - No breaking changes

2. **Minor Version** (1.0.0 → 1.1.0)
   - New features
   - Enhancements
   - Backward compatible

3. **Major Version** (1.0.0 → 2.0.0)
   - Breaking changes
   - Major feature additions
   - API changes

## 🔒 Security Features

### IP Protection Measures

1. **Source Code Protection**
   - ❌ **OLD**: Source code was copied to public GitHub
   - ✅ **NEW**: Only obfuscated AAR is distributed

2. **Build Validation**
   - Ensures release build (not debug)
   - Validates ProGuard obfuscation
   - Checks for obfuscated class names (`a/`, `b/`, etc.)

3. **AAR Verification**
   ```bash
   # The script automatically validates:
   # - AAR contains obfuscated classes
   # - No readable source code
   # - Proper ProGuard configuration
   ```

### What Gets Distributed

✅ **Included:**
- Obfuscated AAR file (`artiusid-sdk-VERSION.aar`)
- Integration documentation
- Security guides
- Sample configuration files
- Build requirements

❌ **NOT Included:**
- Source code files
- Internal implementation details
- Debug builds
- Development tools

## 📦 Deployment Process

### Pre-Deployment Checklist

1. **Code Quality**
   - [ ] All tests passing
   - [ ] Code reviewed and approved
   - [ ] No uncommitted changes (or commit them)

2. **Version Management**
   - [ ] Version number decided
   - [ ] Changelog prepared
   - [ ] Documentation updated

3. **Build Validation**
   - [ ] Release build successful
   - [ ] AAR file generated
   - [ ] ProGuard obfuscation verified

### Deployment Steps

1. **Run Enhanced Script**
   ```bash
   ./artiusid-sdk/scripts/publish-android-github-improved.sh
   ```

2. **Select Version Option**
   - Auto-increment (patch/minor/major)
   - Manual version entry
   - Use current version

3. **Automated Process**
   - Updates `gradle.properties`
   - Commits version changes
   - Builds release AAR
   - Validates obfuscation
   - Creates GitHub release
   - Uploads AAR file
   - Generates documentation

4. **Verification**
   - Check GitHub release page
   - Verify AAR file upload
   - Test integration guide

## 🛠️ Troubleshooting

### Common Issues

1. **Build Failures**
   ```bash
   # Clean and rebuild
   ./gradlew :artiusid-sdk:clean
   ./gradlew :artiusid-sdk:assembleRelease
   ```

2. **Version Conflicts**
   ```bash
   # Check existing tags
   git tag -l | sort -V
   
   # Use version manager to set new version
   ./artiusid-sdk/scripts/version-manager.sh
   ```

3. **GitHub Authentication**
   ```bash
   # Login to GitHub CLI
   gh auth login
   
   # Check status
   gh auth status
   ```

4. **Obfuscation Validation Fails**
   - Check ProGuard configuration in `proguard-rules.pro`
   - Ensure `minifyEnabled true` in release build
   - Verify R8/ProGuard is working correctly

### Script Debugging

Enable verbose output:
```bash
# Add debug flag to script
bash -x ./artiusid-sdk/scripts/publish-android-github-improved.sh
```

## 📊 Comparison: Old vs New Process

| Feature | Old Script | New Enhanced Script |
|---------|------------|-------------------|
| **Versioning** | Manual input every time | Automated with options |
| **IP Protection** | ❌ Exposed source code | ✅ AAR only |
| **Build Validation** | Basic AAR check | Full obfuscation validation |
| **Documentation** | Minimal | Comprehensive guides |
| **Version Management** | Manual | Integrated with gradle.properties |
| **Security** | Medium | High |
| **User Experience** | Basic | Enhanced with colors/status |

## 🔄 Migration from Old Script

1. **Backup Current Process**
   ```bash
   cp artiusid-sdk/scripts/publish-android-github.sh artiusid-sdk/scripts/publish-android-github-old.sh
   ```

2. **Use New Script**
   ```bash
   # The new script is ready to use
   ./artiusid-sdk/scripts/publish-android-github-improved.sh
   ```

3. **Update CI/CD** (if applicable)
   - Update any automated deployment pipelines
   - Use new script path and parameters

## 📞 Support

For issues with the deployment process:

1. **Check Logs**: Review script output for error messages
2. **Validate Environment**: Ensure GitHub CLI and tools are installed
3. **Test Locally**: Run version manager and build separately
4. **Contact Team**: Reach out for deployment support

## 🎯 Best Practices

1. **Always use release builds** for distribution
2. **Test AAR integration** in a sample project before release
3. **Keep version history** in changelog
4. **Validate obfuscation** is working properly
5. **Review security documentation** before each release
6. **Use semantic versioning** consistently
7. **Commit version changes** separately from feature changes
