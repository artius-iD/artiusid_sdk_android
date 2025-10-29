# ArtiusID SDK Documentation

**Documentation Index for Internal Developers**

---

## 📚 Documentation Structure

This directory contains all documentation for the ArtiusID Android SDK, organized by audience and purpose.

---

## 🔧 For Internal Developers

### **Getting Started**
- **[../DEVELOPER_README.md](../DEVELOPER_README.md)** - **START HERE** - Complete internal development guide
  - Repository structure
  - Development workflow
  - Build process
  - Release process
  - Testing guidelines

### **SDK Development**
- **[../artiusid-sdk/src/main/java/com/artiusid/sdk/documentation/](../artiusid-sdk/src/main/java/com/artiusid/sdk/documentation/)** - Technical documentation
  - Architecture documents
  - Implementation guides
  - API documentation

### **Sample Application**
- **[../sample-app/README.md](../sample-app/README.md)** - Sample app documentation
  - How to run and test
  - Testing checklist
  - Debugging guide
  - Build variants

---

## 👥 For External Clients

### **Integration Documentation**
Located in `client/`:

- **[client/CLIENT_IMPLEMENTATION_GUIDE.md](client/CLIENT_IMPLEMENTATION_GUIDE.md)** - **Primary guide for clients**
  - Firebase architecture setup
  - Environment configuration
  - Notification requirements
  - Migration guide
  - Testing checklist

### **Release Information**
- **[client/RELEASE_NOTES_v1.2.48.md](client/RELEASE_NOTES_v1.2.48.md)** - Latest release notes
- **[client/DEPLOYMENT_SUMMARY_v1.2.48.md](client/DEPLOYMENT_SUMMARY_v1.2.48.md)** - Deployment information
- **[client/TRINET_DEPLOYMENT_EMAIL.md](client/TRINET_DEPLOYMENT_EMAIL.md)** - Client communication example

### **Client-Facing README**
- **[../README.md](../README.md)** - Public-facing README (also for clients)
  - Quick start guide
  - Integration examples
  - Download instructions

---

## 🛠️ Technical Documentation

### **Dependencies & Setup**
- **[../SDK_DEPENDENCY_REQUIREMENTS.md](../SDK_DEPENDENCY_REQUIREMENTS.md)** - Required dependencies
- **[../HILT_INTEGRATION_GUIDE.md](../HILT_INTEGRATION_GUIDE.md)** - HILT setup guide
- **[../README_HILT_SETUP.md](../README_HILT_SETUP.md)** - Quick HILT reference

### **Customization**
- **[../sample-app/LOCALIZATION_GUIDE.md](../sample-app/LOCALIZATION_GUIDE.md)** - String customization
- **[../sample-app/src/main/assets/README.md](../sample-app/src/main/assets/README.md)** - Asset overrides

---

## 📋 Internal Reference Documents

### **Architecture & Design**
- **[../TRINET_CERTIFICATE_ARCHITECTURE_UPDATE.md](../TRINET_CERTIFICATE_ARCHITECTURE_UPDATE.md)** - Certificate architecture
- **[../TRINET_COMMUNICATION_v1.2.15.md](../TRINET_COMMUNICATION_v1.2.15.md)** - Historical communication

---

## 🔍 Quick Reference

### **I want to...**

| Goal | Document |
|------|----------|
| **Set up development environment** | [DEVELOPER_README.md](../DEVELOPER_README.md) |
| **Run the sample app** | [sample-app/README.md](../sample-app/README.md) |
| **Build a release** | [DEVELOPER_README.md](../DEVELOPER_README.md) - Release Process |
| **Integrate SDK as a client** | [client/CLIENT_IMPLEMENTATION_GUIDE.md](client/CLIENT_IMPLEMENTATION_GUIDE.md) |
| **Customize SDK theme** | [sample-app/README.md](../sample-app/README.md) - Theme Customization |
| **Override SDK assets** | [sample-app/src/main/assets/README.md](../sample-app/src/main/assets/README.md) |
| **Set up HILT** | [HILT_INTEGRATION_GUIDE.md](../HILT_INTEGRATION_GUIDE.md) |
| **Understand dependencies** | [SDK_DEPENDENCY_REQUIREMENTS.md](../SDK_DEPENDENCY_REQUIREMENTS.md) |
| **Debug SDK issues** | [sample-app/README.md](../sample-app/README.md) - Debugging |
| **Review latest changes** | [client/RELEASE_NOTES_v1.2.48.md](client/RELEASE_NOTES_v1.2.48.md) |

---

## 📝 Documentation Guidelines

### **For Internal Developers**

When creating or updating documentation:

1. **Choose the right location:**
   - Internal/development docs → Root or `docs/` directory
   - Client-facing docs → `docs/client/` directory
   - Technical architecture → `artiusid-sdk/src/main/java/com/artiusid/sdk/documentation/`
   - Sample app docs → `sample-app/` directory

2. **Use clear formatting:**
   - Use markdown (.md) format
   - Include table of contents for long documents
   - Use code blocks with syntax highlighting
   - Add diagrams where helpful

3. **Keep docs updated:**
   - Update docs when changing functionality
   - Maintain version-specific release notes
   - Archive old deployment summaries
   - Review docs during release process

4. **Target your audience:**
   - Internal docs: Assume SDK development knowledge
   - Client docs: Assume Android development knowledge
   - Sample app: Assume testing/QA perspective

---

## 🗂️ Directory Organization

```
docs/
├── README.md                          # This file
├── client/                            # Client-facing documentation
│   ├── CLIENT_IMPLEMENTATION_GUIDE.md
│   ├── DEPLOYMENT_SUMMARY_v1.2.48.md
│   ├── RELEASE_NOTES_v1.2.48.md
│   └── TRINET_DEPLOYMENT_EMAIL.md
└── [future expansion]

Root level (../)
├── DEVELOPER_README.md                # Internal developer guide
├── README.md                          # Public/client README
├── SDK_DEPENDENCY_REQUIREMENTS.md     # Dependency documentation
├── HILT_INTEGRATION_GUIDE.md          # HILT setup
└── README_HILT_SETUP.md              # Quick HILT reference

artiusid-sdk/src/.../documentation/    # Technical architecture docs
sample-app/                            # Sample app documentation
├── README.md                          # Sample app guide
├── LOCALIZATION_GUIDE.md             # String customization
└── src/main/assets/README.md         # Asset overrides
```

---

## 🔄 Documentation Workflow

### **When Releasing a New Version:**

1. **Create release notes:**
   - `docs/client/RELEASE_NOTES_vX.X.XX.md`
   - Include all changes, fixes, and breaking changes

2. **Create deployment summary:**
   - `docs/client/DEPLOYMENT_SUMMARY_vX.X.XX.md`
   - Document deployment process and results

3. **Update guides if needed:**
   - Update `CLIENT_IMPLEMENTATION_GUIDE.md` for breaking changes
   - Update `DEVELOPER_README.md` for new development processes

4. **Update version references:**
   - Main README.md
   - DEVELOPER_README.md
   - Sample app documentation

---

## 📞 Documentation Support

### **Questions or Improvements**
- Create an issue in GitLab with label `documentation`
- Discuss with the SDK team
- Submit merge request with improvements

### **Documentation Standards**
- Follow existing document structure
- Use consistent markdown formatting
- Include code examples where relevant
- Keep language clear and concise

---

**Last Updated:** October 29, 2025  
**Maintained By:** ArtiusID SDK Team

