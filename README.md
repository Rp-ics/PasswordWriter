# PasswordWriter

## Overview
PasswordWriter is a secure offline password manager application that allows users to store, generate, and manage passwords safely without internet connectivity. All data is encrypted locally using AES-256-GCM, ensuring complete privacy and security.

## Key Features

### 🔐 **Security**
- **AES-256-GCM Encryption**: All passwords are encrypted locally using military-grade encryption
- **Master Password**: Required to unlock the app and access stored passwords
- **Biometric Authentication**: Optional fingerprint/biometric unlock support
- **100% Offline**: No internet connection required, no data collected

### 📱 **Password Management**
- **Secure Storage**: Save unlimited passwords with usernames, URLs, and notes
- **Password Generator**: Generate strong, random passwords with customizable options
- **Categorization**: Organize passwords into custom categories with icons and colors
- **Search Functionality**: Quickly find passwords by name, username, or URL
- **Auto-fill**: Integrated keyboard for secure password entry

### 🌐 **Multi-Language & Theme Support**
- **Languages**: English, Spanish, Italian, Russian
- **Themes**: Dark, Light, Vintage, Blue
- **Adaptive UI**: Automatically adjusts to system theme settings

### 📋 **Backup & Restore**
- **Export**: Create encrypted backup files
- **Import**: Restore backups from encrypted files
- **Password Protection**: Backup files require master password to import

## Installation

### Android
1. Download the APK from the releases page
2. Install the app on your Android device
3. Set up your master password during first launch

### Manual Installation
```bash
# Clone the repository
git clone https://github.com/Rp-ics/PasswordWriter.git

# Navigate to the project directory
cd PasswordWriter

# Build the APK
gradlew assembleDebug
```

## Usage Guide

### First-Time Setup
1. **Initial Setup**: Create a strong master password (minimum 6 characters)
2. **App Lock**: Choose to enable or disable app lock on startup
3. **Language Selection**: Choose your preferred language
4. **Theme Selection**: Choose your preferred visual theme

### Daily Usage
1. **Adding Passwords**: Click "Add Password" and fill in details
2. **Viewing Passwords**: Browse your password list and select categories
3. **Searching**: Use the search functionality to find specific passwords
4. **Exporting Backups**: Create encrypted backups for data security
5. **Importing Backups**: Restore your passwords from encrypted backup files

### Security Best Practices
- Use a strong, unique master password
- Enable biometric unlock if available
- Regularly backup your data
- Never share your master password

## Technical Specifications

- **Encryption**: AES-256-GCM
- **Platform**: Android
- **Language**: Kotlin
- **License**: Proprietary
- **Architecture**: client-side only

## Supported Languages

### 🌍 Available Languages
- **English** (default language)
- **Spanish** (`es`)
- **Italian** (`it`) 
- **Russian** (`ru`)

### 🎨 Available Themes
- **Dark** (default, optimized for low light conditions)
- **Light** (optimized for bright environments)
- **Vintage** (Windows 95 inspired aesthetic)
- **Blue** (modern blue theme)

## Troubleshooting

### Common Issues

1. **App Won't Open**
   - Ensure you have entered the correct master password
   - Check if your device supports biometric authentication
   - Clear app cache if the app is not responding

2. **Backup Issues**
   - Ensure you have sufficient storage space
   - Verify the master password is correct when importing
   - Check file format and integrity

3. **Language Settings**
   - Reboot the app after changing language settings
   - Ensure you have selected the correct language in Settings

### Getting Help
If you encounter issues, please:
1. Check the app documentation
2. Verify your internet connection (for updates only)
3. Contact support through the app if available
4. Review the release notes for known issues

## Release History

### Current Version: 1.0.1
- **Release Date**: [Current Date]
- **Major Features**:
  - Complete multilingual support
  - Enhanced security features
  - Improved UI/UX
  - Blue theme implementation
  - Spanish language support

### Changes in 1.0.1
- ✅ Added Spanish language support
- ✅ English set as default language
- ✅ Added Blue theme
- ✅ Implemented Spanish and Blue theme in settings
- ✅ Removed redundant imports
- ✅ Fixed language and theme handling

## License

This application is proprietary software. All rights reserved. The source code is provided for educational and reference purposes only.

## Contact

For support or feature requests:
- GitHub: https://github.com/Rp-ics/PasswordWriter
- Issues: Report bugs and feature requests through GitHub

## Disclaimer

- This app is for personal and secure password storage only
- The developers are not responsible for any misuse
- Always use strong, unique passwords for different services
- This app does not collect any user data
