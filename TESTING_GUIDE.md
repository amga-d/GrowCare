# Camera Integration Testing Guide

## 🚀 Quick Start

### Prerequisites

1. ✅ Android device or emulator with camera support
2. ✅ USB debugging enabled (for physical device)
3. ✅ Android Studio installed
4. ✅ Build successful (verified)

---

## 📱 Testing the Camera Features

### 1. Build and Install

```bash
# Connect device via USB or start emulator
adb devices

# Build and install
cd /home/amgad/Desktop/projects/GrowCare
./gradlew installDebug
```

### 2. Test Disease Detection Flow

**Steps:**

1. Open GrowCare app
2. From Home screen, tap **"Disease Detection"** button
3. You should see **"Disease Scan"** screen with instructions
4. Tap **"Capture Image"** button
5. **Camera permission dialog** should appear → Tap "Allow"
6. Camera preview should open with:
   - Live camera feed
   - Flip camera button (top right)
   - Close button (X, top left)
   - Capture button (bottom center, white circle)
7. Point camera at a plant leaf
8. Tap **capture button** (white circle)
9. Screen returns to scan screen with image preview
10. **"Analyzing plant disease..."** loading indicator appears
11. After analysis (2 seconds mock delay), navigates to result screen

**Expected Result Screen:**

- Disease name (e.g., "Leaf Spot")
- Confidence percentage
- Severity indicator (color-coded)
- Symptoms list
- Treatment recommendations
- Prevention measures

**What to Test:**

- ✅ Permission request appears
- ✅ Camera preview works
- ✅ Flip camera switches between front/back
- ✅ Capture saves image
- ✅ Image preview displays correctly
- ✅ Loading indicator shows during analysis
- ✅ Result screen displays mock data

---

### 3. Test Seed Quality Flow

**Steps:**

1. From Home screen, tap **"Seed Scanner"** button
2. You should see **"Seed Scan"** screen
3. Tap **"Capture Image"** button
4. Camera permission should already be granted
5. Camera opens immediately
6. Place seeds on contrasting surface
7. Tap **capture button**
8. Returns to scan screen with seed image
9. **"Analyzing seed quality..."** loading appears
10. After 2 seconds, navigates to result screen

**Expected Result Screen:**

- Large quality score (0-100) with color background
- Quality label (Excellent/Good/Fair/Poor)
- "Recommended for Use" badge (if score ≥ 60)
- Metrics grid:
  - Size assessment
  - Color consistency
  - Damage percentage
  - Germination potential
- Recommendations list
- Storage advice

**What to Test:**

- ✅ Camera opens without permission dialog (already granted)
- ✅ Seed image captured successfully
- ✅ Analysis loading indicator
- ✅ Quality score display with correct color
- ✅ All metrics populate
- ✅ Recommendations show

---

### 4. Test Error Handling

#### Test Camera Permission Denial

**Steps:**

1. Go to Android Settings → Apps → GrowCare → Permissions
2. Disable Camera permission
3. Open app → Disease Detection
4. Tap "Capture Image"
5. Should see **"Camera Permission Required"** screen
6. Tap **"Grant Permission"** button
7. Permission dialog appears again

**Expected:** Graceful handling with retry option

#### Test Back Button Behavior

**Steps:**

1. Open camera in Disease Detection
2. Tap **X (Close)** button in top-left
3. Should return to scan screen
4. Camera should be released (not running in background)

**Expected:** Proper camera lifecycle management

#### Test Camera Flip

**Steps:**

1. Open camera
2. Tap **flip icon** (top-right)
3. Camera should switch to front camera
4. Tap flip again
5. Should switch back to rear camera

**Expected:** Smooth camera switching without crashes

---

## 🧪 Test Scenarios

### Scenario 1: Full Disease Detection Workflow

```
Home → Disease Detection → Capture → Analyze → View Results → Back
```

**Time:** ~30 seconds **Purpose:** Verify complete feature flow

### Scenario 2: Multiple Captures

```
Disease Detection → Capture 1 → Clear → Capture 2 → Clear → Capture 3
```

**Time:** ~60 seconds **Purpose:** Test state management and memory handling

### Scenario 3: Rapid Camera Open/Close

```
Open Camera → Close → Open → Close → Open → Capture
```

**Time:** ~20 seconds **Purpose:** Test camera lifecycle and resource management

### Scenario 4: Seed Quality End-to-End

```
Home → Seed Scanner → Capture → Analyze → View Results → Storage Advice Check
```

**Time:** ~40 seconds **Purpose:** Verify seed quality feature completeness

### Scenario 5: Navigation Flow

```
Home → Disease Detection → Back → Seed Scanner → Back → Home
```

**Time:** ~15 seconds **Purpose:** Test navigation and back stack behavior

---

## 📊 Verification Checklist

### Camera Functionality

- [ ] Camera opens without crashes
- [ ] Preview displays correctly
- [ ] Capture button responds
- [ ] Image saves to cache
- [ ] Camera closes properly
- [ ] Permission flow works
- [ ] Flip camera works
- [ ] No memory leaks after multiple captures

### UI/UX

- [ ] Loading indicators show during analysis
- [ ] Image previews display correctly
- [ ] Result screens are scrollable
- [ ] Text is readable and properly formatted
- [ ] Colors match design system
- [ ] Animations are smooth
- [ ] Back button works everywhere
- [ ] No UI freezing or lag

### State Management

- [ ] Image state persists on configuration change
- [ ] Loading state clears after analysis
- [ ] Error states display correctly
- [ ] Retry button works after error
- [ ] Clear button resets state properly

### Error Handling

- [ ] Permission denial shows retry option
- [ ] Network errors handled gracefully (when real API added)
- [ ] Invalid images show error message
- [ ] Camera errors don't crash app

---

## 🔍 Debugging Tips

### View Logs

```bash
# Filter GrowCare logs
adb logcat | grep "GrowCare"

# Filter ViewModel logs
adb logcat | grep "ViewModel"

# Filter camera errors
adb logcat | grep "CameraX"
```

### Check Image Files

```bash
# View captured images in cache
adb shell ls /data/data/com.example.growCare/cache/

# Pull image to computer
adb pull /data/data/com.example.growCare/cache/JPEG_<timestamp>.jpg
```

### Memory Profiling

1. Open Android Studio
2. Run → Profile 'app'
3. Select Memory Profiler
4. Capture multiple images
5. Force garbage collection
6. Check for retained objects

---

## ⚠️ Known Issues

### Issue 1: Result Screen Shows No Data

**Symptom:** Result screen appears but shows empty fields **Cause:** Navigation
doesn't pass analysis data yet **Status:** Known limitation (navigation args
TODO) **Workaround:** Use shared ViewModel or implement JSON serialization

### Issue 2: Deprecation Warnings

**Symptom:** Build shows LocalLifecycleOwner deprecation **Cause:**
CameraCapture uses old import **Impact:** None (still works) **Fix:** Will
update in future Compose version

### Issue 3: Mock Data in Results

**Symptom:** Results show placeholder/mock data **Cause:** Use cases not
connected to real Gemini AI **Status:** Expected (AI integration pending)
**Next:** Implement GeminiAIService

---

## 📱 Emulator Setup

### Recommended Emulator Settings

- **Device:** Pixel 5 or newer
- **API Level:** 33 (Android 13) or higher
- **Camera:** Back camera enabled
- **RAM:** 2048 MB minimum
- **Storage:** 8 GB minimum

### Enable Virtual Camera

1. Open AVD Manager
2. Edit your emulator
3. Show Advanced Settings
4. Set "Camera" → Back: "VirtualScene"
5. Set "Camera" → Front: "Emulated"

---

## 🎯 Test Results Template

```markdown
## Test Session: [Date]

### Device Information

- Device: [e.g., Pixel 5, Samsung Galaxy S21]
- Android Version: [e.g., Android 13]
- Build Type: Debug
- App Version: 1.0.0

### Feature Tests

#### Disease Detection

- [ ] Camera opens: PASS/FAIL
- [ ] Image capture: PASS/FAIL
- [ ] Analysis flow: PASS/FAIL
- [ ] Result display: PASS/FAIL
- Issues: [describe any issues]

#### Seed Quality

- [ ] Camera opens: PASS/FAIL
- [ ] Image capture: PASS/FAIL
- [ ] Quality score: PASS/FAIL
- [ ] Metrics display: PASS/FAIL
- Issues: [describe any issues]

#### Error Handling

- [ ] Permission denial: PASS/FAIL
- [ ] Camera error: PASS/FAIL
- [ ] Retry functionality: PASS/FAIL
- Issues: [describe any issues]

### Performance

- App launch time: [X seconds]
- Camera open time: [X seconds]
- Analysis time: [X seconds]
- Memory usage: [X MB]

### Notes

[Additional observations]
```

---

## 🚨 Report Issues

If you encounter bugs, please create an issue with:

1. **Device info** (model, Android version)
2. **Steps to reproduce**
3. **Expected behavior**
4. **Actual behavior**
5. **Logs** (adb logcat output)
6. **Screenshots** (if applicable)

---

## 📝 Next Steps After Testing

1. ✅ Verify all camera functionality works
2. ✅ Test on multiple devices (min SDK 24 - 36)
3. 🔄 Implement navigation argument passing
4. 🔄 Connect to real Gemini AI API
5. 🔄 Add unit tests for ViewModels
6. 🔄 Performance optimization
7. 🔄 Add analytics tracking

---

**Happy Testing! 🎉**

For questions or issues, refer to CAMERA_INTEGRATION_COMPLETE.md
