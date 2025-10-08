# SECURITY NOTICE - Source Code Cleanup

## Issue Resolved
**Date:** $(date)
**Issue:** Source code files were accidentally included in customer distribution
**Resolution:** All source code files and directories have been removed

## Files Removed
- Source code files (ArtiusIDSDK.kt, MainActivity.kt)
- Internal implementation directories (bridge/, security/, etc.)
- Internal documentation and utilities

## Files Retained
- ✅ artiusid-sdk-*.aar (obfuscated SDK packages)
- ✅ consumer-rules.pro (ProGuard rules)
- ✅ Customer documentation and integration guides

## Security Measures Implemented
1. Source code completely removed from customer repository
2. Only obfuscated AAR files remain available
3. Enhanced deployment process to prevent future exposure
4. Repository access reviewed and secured

## Customer Impact
- **No action required** from customers
- All AAR files remain fully functional
- Integration guides and documentation unchanged
- Enhanced security and IP protection

For questions, contact: security@artiusid.com
