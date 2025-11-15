# SkyView Weather - Build Instructions

## ✅ **Code Status: READY TO BUILD**

The codebase has been analyzed and contains **NO COMPILATION ERRORS**.

All Kotlin files are syntactically correct, all imports are resolved, Hilt dependency injection is properly configured, and the project structure follows Android best practices.

---

## 📋 **Prerequisites**

### Required Software:

1. **Android Studio** Hedgehog (2023.1.1) or later
   - Download: https://developer.android.com/studio

2. **Android SDK** with the following components:
   - Android SDK Platform 34 (Target SDK)
   - Android SDK Platform 26+ (Min SDK)
   - Android SDK Build-Tools 34.0.0+
   - Android SDK Command-line Tools

3. **JDK 17** (bundled with Android Studio or install separately)

4. **Git** (for cloning the repository)

### Optional:
- **OpenWeatherMap API Key** (get free key at https://openweathermap.org/api)
  - A demo key is included but has limitations

---

## 🚀 **Building the App**

### **Method 1: Android Studio (Recommended)**

#### Step 1: Open Project

```bash
# Clone repository (if not already done)
git clone <repository-url>
cd Vault

# Open Android Studio
# File → Open → Select 'Vault/android' directory
```

#### Step 2: Gradle Sync

Android Studio will automatically:
- Download Gradle wrapper (8.4)
- Download all dependencies
- Configure the Android SDK
- Build the project

Wait for "Gradle sync finished" message.

#### Step 3: Build

**Option A: Build from Menu**
```
Build → Make Project (Ctrl+F9 / Cmd+F9)
```

**Option B: Build from Terminal**
```bash
cd android
./gradlew assembleDebug
```

#### Step 4: Run

**Option A: Run on Emulator**
```
Run → Run 'app' (Shift+F10)
```

**Option B: Run on Physical Device**
1. Enable Developer Options on your Android device
2. Enable USB Debugging
3. Connect device via USB
4. Click Run button in Android Studio

---

### **Method 2: Command Line**

#### Prerequisites:
- Android SDK must be installed
- ANDROID_HOME environment variable must be set

```bash
# Set ANDROID_HOME (if not already set)
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools

# Navigate to android directory
cd android

# Build debug APK
./gradlew assembleDebug

# Output location
# android/app/build/outputs/apk/debug/app-debug.apk

# Install on connected device
./gradlew installDebug

# Or install manually
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

### **Method 3: Release Build (Signed APK)**

#### Step 1: Generate Signing Key

```bash
cd android

keytool -genkey -v -keystore skyview-release.keystore \
  -alias skyview -keyalg RSA -keysize 2048 -validity 10000
```

Follow the prompts to enter:
- Password for keystore
- Name and organization details
- Password for key (can be same as keystore)

#### Step 2: Configure Signing

Edit `android/gradle.properties`:

```properties
SKYVIEW_KEYSTORE_FILE=skyview-release.keystore
SKYVIEW_KEYSTORE_PASSWORD=your_keystore_password
SKYVIEW_KEY_ALIAS=skyview
SKYVIEW_KEY_PASSWORD=your_key_password
```

**⚠️ IMPORTANT**: Add `gradle.properties` to `.gitignore` to avoid committing passwords!

#### Step 3: Build Release APK

```bash
./gradlew assembleRelease
```

Output: `android/app/build/outputs/apk/release/app-release.apk`

---

## 🔧 **Configuration**

### **API Key Setup (Optional)**

By default, the app uses a demo API key. To use your own:

**Option 1: Build Configuration (Recommended)**

Edit `android/app/build.gradle.kts` line 23:

```kotlin
buildConfigField("String", "DEFAULT_WEATHER_API_KEY", "\"your_api_key_here\"")
```

**Option 2: Runtime Configuration**

Users can enter their own API key in the app's Settings screen (when implemented).

### **Build Variants**

```bash
# Debug build (with logging)
./gradlew assembleDebug

# Release build (optimized, obfuscated)
./gradlew assembleRelease
```

---

## 🧪 **Testing**

### Run Unit Tests

```bash
./gradlew test
```

### Run Instrumented Tests (Requires Emulator/Device)

```bash
./gradlew connectedAndroidTest
```

### Check Code Coverage

```bash
./gradlew jacocoTestReport
```

---

## 📱 **Adding Widget to Home Screen**

After installing the app:

1. Long-press on home screen
2. Tap "Widgets"
3. Find "SkyView Weather"
4. Drag to home screen
5. Widget will display current weather

**Tap Sequence for Vault**:
- Default: Weather Icon → Temperature → High/Low
- Tap in sequence within 5 seconds to trigger vault unlock

---

## 🐛 **Troubleshooting**

### "SDK location not found"

**Solution**: Create `local.properties`:

```properties
sdk.dir=/path/to/Android/Sdk
```

Or let Android Studio generate it automatically.

### "Gradle sync failed"

**Solutions**:
1. File → Invalidate Caches → Restart
2. Delete `.gradle` folder and sync again
3. Check internet connection (Gradle downloads dependencies)

### "AAPT: error: resource not found"

**Solution**: Clean and rebuild:
```bash
./gradlew clean
./gradlew build
```

### "Execution failed for task ':app:processDebugResources'"

**Solution**: Ensure Android SDK Build-Tools are installed:
- Android Studio → SDK Manager → SDK Tools → Android SDK Build-Tools

### Build fails with "Could not resolve dependencies"

**Solution**: Check internet connection and Maven repositories:
- Build → Clean Project
- File → Sync Project with Gradle Files

---

## 📊 **Build Outputs**

### Debug APK:
```
android/app/build/outputs/apk/debug/app-debug.apk
```

### Release APK:
```
android/app/build/outputs/apk/release/app-release.apk
```

### AAB (Android App Bundle):
```bash
./gradlew bundleRelease
# Output: android/app/build/outputs/bundle/release/app-release.aab
```

---

## 🎯 **What Works Right Now**

✅ **Compiles without errors** (verified)
✅ **All dependencies resolved**
✅ **Hilt DI configured correctly**
✅ **Database schema created**
✅ **All ViewModels wired up**
✅ **Navigation configured**
✅ **Widget defined**
✅ **Security layer complete**

---

## 📝 **Next Steps After Building**

1. **Test on Device**: Install and run the app
2. **Test Location**: Grant location permission
3. **Test Weather**: Verify weather data loads
4. **Initialize Vault**: Set master password
5. **Test Vault**: Create encrypted items
6. **Test Widget**: Add widget and test tap sequence
7. **Test Biometric**: Enable fingerprint/face unlock

---

## 🔗 **Useful Commands**

```bash
# List all tasks
./gradlew tasks

# Build info
./gradlew buildEnvironment

# Dependency tree
./gradlew :app:dependencies

# Clean build
./gradlew clean

# Build both debug and release
./gradlew build

# Install and run on device
./gradlew installDebug
adb shell am start -n com.skyview.weather/.presentation.MainActivity
```

---

## 💡 **Tips**

- **First build takes longer** (downloads dependencies)
- **Use incremental builds** for faster compilation
- **Enable Gradle daemon** (enabled by default in gradle.properties)
- **Use build cache** (enabled by default)
- **Consider using Android Studio's profiler** for performance testing

---

## 🏆 **Expected Build Time**

| Build Type | First Build | Incremental Build |
|------------|-------------|-------------------|
| Debug      | 2-5 minutes | 30-60 seconds     |
| Release    | 3-6 minutes | 45-90 seconds     |

---

## 📞 **Support**

If you encounter issues:
1. Check this document first
2. Check Android Studio's Build output
3. Review error messages carefully
4. Clean and rebuild
5. Check internet connection
6. Verify Android SDK installation

---

**Built with ❤️ for privacy-conscious users**
