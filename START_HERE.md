# 👋 Welcome to the ArtiusID Android SDK Repository

**This is the internal development repository for the ArtiusID Android SDK**

---

## 🎯 Are You...?

### **📱 A New SDK Developer?**
**Start here:** [QUICKSTART_INTERNAL.md](QUICKSTART_INTERNAL.md)  
Get up and running in 15 minutes, then read [DEVELOPER_README.md](DEVELOPER_README.md)

### **🔨 Building the SDK?**
**Go to:** [BUILD_GUIDE.md](BUILD_GUIDE.md)  
Complete instructions for building from source

### **🤝 Contributing Code?**
**Read:** [CONTRIBUTING.md](CONTRIBUTING.md)  
Guidelines, code style, and workflow

### **👥 A Client Integrating the SDK?**
**See:** [README.md](README.md) and [docs/client/CLIENT_IMPLEMENTATION_GUIDE.md](docs/client/CLIENT_IMPLEMENTATION_GUIDE.md)  
Public documentation and integration guide

### **🔍 Looking for Something?**
**Check:** [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md)  
Complete index of all documentation

### **⚡ Need Quick Reference?**
**Use:** [QUICK_REFERENCE.md](QUICK_REFERENCE.md)  
Essential commands and locations

---

## 📚 Essential Documentation

### **🚀 Getting Started (Read First)**

1. **[QUICKSTART_INTERNAL.md](QUICKSTART_INTERNAL.md)** - 15 minute quick start
   - Clone and set up
   - Run sample app
   - Your first day checklist

2. **[DEVELOPER_README.md](DEVELOPER_README.md)** - Comprehensive guide
   - Repository structure
   - Development workflow
   - Architecture overview
   - Release process

3. **[sample-app/README.md](sample-app/README.md)** - Sample app guide
   - How to run and test
   - Testing checklist
   - Debugging tips

### **🔧 Development Guides**

4. **[BUILD_GUIDE.md](BUILD_GUIDE.md)** - Building from source
5. **[CONTRIBUTING.md](CONTRIBUTING.md)** - Contribution guidelines
6. **[REPOSITORY_ORGANIZATION.md](REPOSITORY_ORGANIZATION.md)** - Repository structure

### **📖 Reference**

7. **[DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md)** - Complete doc index
8. **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** - Quick reference card

---

## 🏗️ Repository Overview

```
mobile-sdk-android/
├── artiusid-sdk/          # SDK source code (you'll work here)
├── sample-app/            # Test app (use this to test SDK)
├── docs/                  # Organized documentation
│   ├── README.md
│   └── client/           # Client-facing documentation
├── [documentation].md     # Development guides (at root level)
└── scripts/              # Build and deployment tools
```

**Full details:** [REPOSITORY_ORGANIZATION.md](REPOSITORY_ORGANIZATION.md)

---

## ⚡ Quick Commands

```bash
# Build SDK
./gradlew :artiusid-sdk:assembleRelease

# Run sample app
./gradlew :sample-app:installDebug

# View logs
adb logcat | grep ArtiusIDSDK

# Lint check
./gradlew lint

# Clean build
./gradlew clean && ./gradlew assemble
```

**More commands:** [QUICK_REFERENCE.md](QUICK_REFERENCE.md)

---

## 🎓 Learning Path

### **Week 1: Get Oriented**
- [ ] Read [QUICKSTART_INTERNAL.md](QUICKSTART_INTERNAL.md)
- [ ] Set up development environment
- [ ] Run the sample app
- [ ] Read [DEVELOPER_README.md](DEVELOPER_README.md)
- [ ] Complete a small bug fix

### **Week 2: Explore**
- [ ] Understand verification flow
- [ ] Understand authentication flow
- [ ] Learn certificate management
- [ ] Test in all environments

### **Week 3: Contribute**
- [ ] Complete a feature
- [ ] Write/improve documentation
- [ ] Review someone's code
- [ ] Help with testing

---

## 💡 Key Concepts

### **The SDK Does:**
- Face liveness detection
- Document scanning (Passport, State ID)
- NFC passport reading
- Biometric authentication
- Approval request system
- mTLS certificate management

### **The Sample App Shows:**
- Complete SDK integration
- Firebase messaging setup
- Environment management
- Theme customization
- Asset overrides
- Error handling

### **We Support:**
- **Android:** API 24+ (Android 7.0+)
- **Kotlin:** 1.9.0+
- **Compose:** Modern declarative UI
- **Hilt:** Dependency injection

---

## 🎯 Your First Task

### **Option 1: Quick Win (1-2 hours)**
1. Fix a typo in documentation
2. Improve code comments
3. Add a test case

### **Option 2: Feature Task (1-2 days)**
1. Look for `good-first-issue` in GitLab
2. Ask team lead for starter task
3. Pick from backlog

### **Option 3: Explore (1 day)**
1. Run sample app
2. Complete all test flows
3. Read architecture docs
4. Document your learnings

---

## 🐛 Common First-Day Issues

### **App won't build**
```bash
./gradlew clean
./gradlew assemble
```

### **App crashes on startup**
```bash
# Clear app data
adb shell pm clear com.artiusid.sampleapp

# Check google-services.json exists
ls sample-app/google-services.json
```

### **Can't find something**
Check: [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md)

### **Still stuck?**
Ask the team! Don't spend more than 30 minutes stuck.

---

## 🤝 Team Collaboration

### **GitLab**
- **Repository:** git@gitlab.com:artiusid1/mobile-sdk-android.git
- **Issues:** Bug reports, features
- **Merge Requests:** Code reviews

### **Communication**
- **Quick questions:** Team chat
- **Technical discussions:** GitLab issues
- **Code reviews:** Merge requests
- **Documentation:** Update and improve

---

## 📞 Getting Help

### **Questions About...**

| Topic | Resource |
|-------|----------|
| **Setup** | [QUICKSTART_INTERNAL.md](QUICKSTART_INTERNAL.md) |
| **Building** | [BUILD_GUIDE.md](BUILD_GUIDE.md) |
| **Contributing** | [CONTRIBUTING.md](CONTRIBUTING.md) |
| **Architecture** | [DEVELOPER_README.md](DEVELOPER_README.md#-sdk-architecture) |
| **Testing** | [sample-app/README.md](sample-app/README.md#-testing-flows) |
| **Anything else** | Ask your team lead |

---

## ✅ Before You Start Coding

Make sure you have:
- [ ] Cloned the repository
- [ ] Opened in Android Studio
- [ ] Built the project successfully
- [ ] Run the sample app
- [ ] Read [QUICKSTART_INTERNAL.md](QUICKSTART_INTERNAL.md)
- [ ] Read [CONTRIBUTING.md](CONTRIBUTING.md)
- [ ] Joined team communication channels
- [ ] Know who your mentor/lead is

---

## 🎉 Ready to Start!

**You're all set!** Pick a task and start contributing.

**Remember:**
- Read the docs (they're comprehensive!)
- Ask questions (team is here to help)
- Test thoroughly (use the sample app)
- Commit often (small commits are better)
- Have fun! 🚀

---

## 📋 Handy Links

| Resource | Link |
|----------|------|
| **Main Guide** | [DEVELOPER_README.md](DEVELOPER_README.md) |
| **Quick Start** | [QUICKSTART_INTERNAL.md](QUICKSTART_INTERNAL.md) |
| **Build Guide** | [BUILD_GUIDE.md](BUILD_GUIDE.md) |
| **Contributing** | [CONTRIBUTING.md](CONTRIBUTING.md) |
| **Sample App** | [sample-app/README.md](sample-app/README.md) |
| **Quick Ref** | [QUICK_REFERENCE.md](QUICK_REFERENCE.md) |
| **Doc Index** | [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md) |
| **Repository** | [REPOSITORY_ORGANIZATION.md](REPOSITORY_ORGANIZATION.md) |

---

## 🗺️ Documentation Roadmap

**Start Here:**
```
START_HERE.md (you are here!)
    ↓
QUICKSTART_INTERNAL.md (15 min setup)
    ↓
DEVELOPER_README.md (comprehensive guide)
    ↓
Other docs as needed
```

**Reference When Needed:**
- BUILD_GUIDE.md
- CONTRIBUTING.md
- QUICK_REFERENCE.md
- DOCUMENTATION_INDEX.md

---

**Welcome to the team! Let's build something great together! 🎉**

---

**Last Updated:** October 29, 2025  
**Maintained By:** ArtiusID SDK Team

**Questions?** Ask your team lead or check [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md)

