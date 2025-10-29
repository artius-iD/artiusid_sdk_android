# Contributing to ArtiusID Android SDK

**Guidelines for internal developers**

---

## 👋 Welcome

Thank you for contributing to the ArtiusID Android SDK! This document provides guidelines and best practices for internal developers working on the SDK.

---

## 🚀 Getting Started

### **1. Set Up Your Development Environment**

Follow the setup guide in [DEVELOPER_README.md](DEVELOPER_README.md):

```bash
# Clone repository
git clone git@gitlab.com:artiusid1/mobile-sdk-android.git
cd mobile-sdk-android

# Open in Android Studio
# File → Open → Select project directory

# Build project
./gradlew assemble
```

### **2. Understand the Codebase**

- **Read:** [DEVELOPER_README.md](DEVELOPER_README.md) - Development guide
- **Review:** [sample-app/README.md](sample-app/README.md) - Sample app documentation
- **Explore:** `artiusid-sdk/src/main/java/com/artiusid/sdk/` - SDK source code

### **3. Run the Sample App**

```bash
# Build and run
./gradlew :sample-app:installDebug

# Or use Android Studio run configuration
```

---

## 🌿 Branching Strategy

### **Branch Types**

| Branch Type | Naming | Purpose | Example |
|------------|--------|---------|---------|
| **main** | `main` | Production-ready code | `main` |
| **develop** | `develop` | Active development | `develop` |
| **feature** | `feature/description` | New features | `feature/add-iris-scanning` |
| **fix** | `fix/description` | Bug fixes | `fix/certificate-expiry` |
| **hotfix** | `hotfix/description` | Critical production fixes | `hotfix/crash-on-launch` |
| **release** | `release/vX.X.XX` | Release preparation | `release/v1.2.49` |

### **Branch Workflow**

```bash
# Create feature branch from develop
git checkout develop
git pull origin develop
git checkout -b feature/my-feature

# Work on feature
# ... make changes ...

# Commit changes
git add .
git commit -m "[feat] Add my feature"

# Push to GitLab
git push origin feature/my-feature

# Create merge request to develop
```

---

## 💻 Code Style

### **Kotlin Conventions**

Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html):

```kotlin
// ✅ Good
class VerificationManager @Inject constructor(
    private val apiService: ApiService,
    private val certificateManager: CertificateManager
) {
    fun startVerification(userId: String): Flow<VerificationState> {
        return flow {
            emit(VerificationState.Loading)
            val result = apiService.verify(userId)
            emit(VerificationState.Success(result))
        }
    }
}

// ❌ Bad
class VerificationManager @Inject constructor(private val apiService:ApiService,private val certificateManager:CertificateManager){
fun startVerification(userId:String):Flow<VerificationState>{
return flow{
emit(VerificationState.Loading)
val result=apiService.verify(userId)
emit(VerificationState.Success(result))
}}}
```

### **Formatting Rules**

- **Indentation:** 4 spaces (no tabs)
- **Line length:** 120 characters max
- **Blank lines:** One between functions
- **Imports:** Remove unused, organize alphabetically

### **Naming Conventions**

| Type | Convention | Example |
|------|------------|---------|
| **Class** | PascalCase | `VerificationManager` |
| **Function** | camelCase | `startVerification()` |
| **Variable** | camelCase | `userId` |
| **Constant** | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| **Private** | camelCase with _ prefix | `_privateField` |
| **Composable** | PascalCase | `VerificationScreen()` |

### **Documentation**

Document public APIs with KDoc:

```kotlin
/**
 * Starts the verification flow for a user.
 *
 * @param userId The unique identifier for the user
 * @return A [Flow] emitting [VerificationState] updates
 * @throws NetworkException if network is unavailable
 */
fun startVerification(userId: String): Flow<VerificationState> {
    // Implementation
}
```

---

## 📝 Commit Messages

### **Format**

```
[type] Brief description (50 chars or less)

Detailed description if needed (wrap at 72 characters).
Explain what changed and why.

- Bullet points for multiple items
- Use present tense: "Add feature" not "Added feature"
```

### **Types**

| Type | Description | Example |
|------|-------------|---------|
| **feat** | New feature | `[feat] Add iris scanning support` |
| **fix** | Bug fix | `[fix] Resolve certificate expiry crash` |
| **docs** | Documentation | `[docs] Update API documentation` |
| **refactor** | Code restructuring | `[refactor] Simplify certificate manager` |
| **test** | Add/update tests | `[test] Add unit tests for verification` |
| **chore** | Maintenance | `[chore] Update dependencies` |
| **perf** | Performance improvement | `[perf] Optimize image processing` |

### **Examples**

```bash
# Good commits
git commit -m "[feat] Add environment-specific credential storage"
git commit -m "[fix] Resolve member ID display after environment change"
git commit -m "[docs] Update client implementation guide"

# Bad commits
git commit -m "Fixed stuff"
git commit -m "WIP"
git commit -m "Update"
```

---

## 🧪 Testing

### **Before Submitting**

Run these tests before creating a merge request:

```bash
# 1. Lint checks
./gradlew lint

# 2. Unit tests
./gradlew test

# 3. Build SDK
./gradlew :artiusid-sdk:assembleRelease

# 4. Build sample app
./gradlew :sample-app:assembleDebug

# 5. Install and test manually
./gradlew :sample-app:installDebug
```

### **Manual Testing**

Use the [sample app testing checklist](sample-app/README.md#-testing-checklist):

- [ ] Verification flow works
- [ ] Authentication flow works
- [ ] Approval requests work
- [ ] Environment switching works
- [ ] No crashes or errors in logs

### **Test in All Environments**

- [ ] Sandbox
- [ ] Development
- [ ] Staging

---

## 🔍 Code Review

### **Before Creating Merge Request**

- [ ] Code follows style guidelines
- [ ] All tests pass
- [ ] Documentation updated
- [ ] No console warnings or errors
- [ ] Manual testing completed
- [ ] Lint checks pass

### **Merge Request Description**

```markdown
## What Changed
Brief description of changes

## Why
Reason for changes

## Testing
- [ ] Manual testing completed
- [ ] Verified in Sandbox
- [ ] Verified in Development
- [ ] Sample app tested

## Screenshots (if UI changes)
[Add screenshots]

## Breaking Changes
[List any breaking changes]
```

### **Review Process**

1. **Create MR** → Assign to reviewer
2. **Review** → Reviewer provides feedback
3. **Address feedback** → Make requested changes
4. **Approval** → Reviewer approves MR
5. **Merge** → Merge to target branch

---

## 📦 Release Process

### **Version Numbering**

Follow semantic versioning: `MAJOR.MINOR.PATCH`

- **MAJOR:** Breaking changes (1.0.0 → 2.0.0)
- **MINOR:** New features, backwards compatible (1.2.0 → 1.3.0)
- **PATCH:** Bug fixes (1.2.48 → 1.2.49)

### **Creating a Release**

See [DEVELOPER_README.md - Release Process](DEVELOPER_README.md#-release-process) for detailed steps.

**Quick summary:**

```bash
# 1. Update version
./artiusid-sdk/scripts/version-manager.sh

# 2. Build release
./gradlew clean
./gradlew :artiusid-sdk:assembleRelease

# 3. Create documentation
# - RELEASE_NOTES_vX.X.XX.md
# - DEPLOYMENT_SUMMARY_vX.X.XX.md

# 4. Commit and tag
git commit -am "Release v1.2.XX"
git tag -a v1.2.XX -m "Release v1.2.XX"
git push origin main v1.2.XX

# 5. Publish to GitHub
cd artiusid-sdk/scripts
./publish-android-github-essential.sh
```

---

## 🐛 Bug Reports

### **Reporting Bugs**

When you find a bug:

1. **Check existing issues** in GitLab
2. **Reproduce the bug** consistently
3. **Create issue** with template below

### **Bug Report Template**

```markdown
## Bug Description
Clear description of the bug

## Steps to Reproduce
1. Step one
2. Step two
3. Step three

## Expected Behavior
What should happen

## Actual Behavior
What actually happens

## Environment
- SDK Version: 1.2.48
- Android Version: 14
- Device: Pixel 6
- Environment: Sandbox

## Logs
```
[Paste relevant logs]
```

## Screenshots
[Add screenshots if applicable]
```

---

## 💡 Feature Requests

### **Proposing Features**

1. **Discuss with team** before implementing
2. **Create issue** with proposal
3. **Wait for approval** before starting work

### **Feature Proposal Template**

```markdown
## Feature Description
What feature do you want to add?

## Use Case
Why is this feature needed?

## Proposed Implementation
How would this be implemented?

## Alternatives Considered
What other approaches were considered?

## Breaking Changes
Will this break existing integrations?
```

---

## 🔒 Security

### **Reporting Security Issues**

**DO NOT** create public issues for security vulnerabilities.

**Instead:**
1. Email: security@artiusid.com
2. Include: Detailed description and reproduction steps
3. Allow time for fix before public disclosure

### **Security Best Practices**

- ✅ Never commit API keys or secrets
- ✅ Use environment variables for sensitive data
- ✅ Validate all user input
- ✅ Use ProGuard obfuscation for releases
- ✅ Store credentials in Android Keystore
- ✅ Use mTLS for API communication

---

## 📚 Resources

### **Documentation**
- [DEVELOPER_README.md](DEVELOPER_README.md) - Main development guide
- [BUILD_GUIDE.md](BUILD_GUIDE.md) - Build instructions
- [sample-app/README.md](sample-app/README.md) - Sample app guide

### **External Resources**
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Android Developer Guides](https://developer.android.com/guide)
- [Hilt Documentation](https://dagger.dev/hilt/)

---

## 🤝 Team Communication

### **GitLab**
- **Issues:** Bug reports, feature requests
- **Merge Requests:** Code reviews
- **Wiki:** Technical documentation

### **Best Practices**
- Respond to code reviews within 24 hours
- Keep merge requests small and focused
- Ask questions if something is unclear
- Share knowledge with the team

---

## ✅ Checklist

Before submitting any contribution:

- [ ] Code follows style guidelines
- [ ] Documentation updated
- [ ] Tests added/updated
- [ ] All tests pass
- [ ] Lint checks pass
- [ ] Manual testing completed
- [ ] Commit messages follow format
- [ ] Branch follows naming convention
- [ ] No console warnings
- [ ] ProGuard rules updated (if needed)

---

## 📞 Questions?

- **Technical questions:** Ask the SDK team
- **Code reviews:** Tag reviewer in MR
- **GitLab issues:** Create issue with question label

---

**Thank you for contributing to the ArtiusID SDK! 🎉**

---

**Last Updated:** October 29, 2025  
**Maintained By:** ArtiusID SDK Team

