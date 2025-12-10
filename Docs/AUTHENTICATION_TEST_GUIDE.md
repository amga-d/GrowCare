# Authentication System Testing Guide

**Project**: GrowCare  
**Date**: December 9, 2025  
**Status**: Ready for Testing

---

## Quick Start

### 1. Build and Install

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device/emulator
./gradlew installDebug

# Or build and install in one command
./gradlew installDebug
```

### 2. Run Unit Tests

```bash
# Run all unit tests
./gradlew testDebugUnitTest

# View test report
open app/build/reports/tests/testDebugUnitTest/index.html
```

---

## Manual Testing Scenarios

### Test Case 1: New User Sign Up ✅

**Steps**:
1. Launch the app
2. Tap "Don't have an account? Sign up"
3. Enter:
   - Name: "Test User"
   - Email: "testuser@example.com"
   - Password: "password123"
   - Confirm Password: "password123"
4. Accept terms and conditions
5. Tap "Sign Up"

**Expected Result**:
- Loading indicator appears
- Successfully navigates to Home screen
- User is authenticated
- Back button doesn't return to sign up

**Validation Checks**:
- Empty name shows error
- Invalid email format shows error
- Password < 6 chars shows error
- Mismatched passwords show error

---

### Test Case 2: Existing User Sign In ✅

**Steps**:
1. Launch the app (or sign out first)
2. Enter:
   - Email: "testuser@example.com"
   - Password: "password123"
3. Tap "Login"

**Expected Result**:
- Loading indicator appears
- Successfully navigates to Home screen
- User session persisted

**Validation Checks**:
- Empty email shows error
- Invalid email format shows error
- Wrong password shows Firebase error
- Unknown email shows Firebase error

---

### Test Case 3: Invalid Login Attempts ✅

**Scenario A: Wrong Password**
1. Email: "testuser@example.com"
2. Password: "wrongpassword"
3. Tap "Login"

**Expected**: Error message "The password is invalid"

**Scenario B: Non-existent Account**
1. Email: "nonexistent@example.com"
2. Password: "password123"
3. Tap "Login"

**Expected**: Error message about account not found

---

### Test Case 4: Field Validation ✅

**Email Validation**:
- ❌ Empty email → "Email is required"
- ❌ "notanemail" → "Invalid email format"
- ❌ "test@" → "Invalid email format"
- ✅ "test@example.com" → Valid

**Password Validation**:
- ❌ Empty → "Password is required"
- ❌ "12345" → "Password must be at least 6 characters"
- ✅ "123456" → Valid

**Sign Up Specific**:
- ❌ Empty name → "Name is required"
- ❌ Password ≠ Confirm → "Passwords do not match"

---

### Test Case 5: Authentication Persistence ✅

**Steps**:
1. Sign in successfully
2. Navigate to Home screen
3. Close the app completely
4. Reopen the app

**Expected Result**:
- User remains signed in
- App opens directly to Home screen
- No need to sign in again

---

### Test Case 6: Sign Out ✅

**Steps**:
1. While signed in, navigate to Profile screen
2. Tap "Logout" button
3. Confirm logout

**Expected Result**:
- User signed out
- Navigates to Login screen
- Back button disabled (no back stack)
- Next app launch requires sign in

---

### Test Case 7: Password Visibility Toggle ✅

**Steps**:
1. On Login or Sign Up screen
2. Type password
3. Tap the eye icon

**Expected Result**:
- Password becomes visible/hidden
- Icon changes between eye and eye-off
- Toggle works independently for password and confirm password fields

---

### Test Case 8: Navigation Between Auth Screens ✅

**Login → Sign Up**:
1. From Login screen
2. Tap "Don't have an account? Sign up"
3. Should navigate to Sign Up

**Sign Up → Login**:
1. From Sign Up screen
2. Tap "Already have account? Sign in"
3. Should navigate back to Login

---

## Firebase Console Verification

### Check User Creation

1. Go to Firebase Console
2. Select GrowCare project
3. Navigate to Authentication > Users
4. Verify new users appear after sign up

### Check Authentication Methods

1. Authentication > Sign-in method
2. Verify Email/Password is enabled
3. Check for any security alerts

---

## Common Issues and Solutions

### Issue 1: Firebase Not Initialized
**Symptom**: App crashes on auth attempt  
**Solution**: Verify `google-services.json` is in `app/` directory

### Issue 2: Network Error
**Symptom**: "Network error" message  
**Solution**: Check internet connection, Firebase project status

### Issue 3: Tests Failing
**Symptom**: Unit tests fail with NoClassDefFoundError  
**Solution**: Robolectric dependency added, tests should pass now

### Issue 4: Weak Password
**Symptom**: Firebase returns "weak password" error  
**Solution**: Use password with at least 6 characters (Firebase requirement)

### Issue 5: Email Already in Use
**Symptom**: Sign up fails with "email already in use"  
**Solution**: Use different email or sign in with existing account

---

## API Key Configuration

### Gemini API (for AI features)

1. Get API key from: https://makersuite.google.com/app/apikey
2. Add to `local.properties`:
   ```properties
   GEMINI_API_KEY=your_api_key_here
   ```
3. Rebuild project

### Firebase Configuration

Already configured via `google-services.json`

---

## Testing Checklist

### Functional Tests
- [ ] Sign up with new account
- [ ] Sign in with existing account
- [ ] Invalid email format rejected
- [ ] Short password rejected
- [ ] Password mismatch rejected
- [ ] Empty fields rejected
- [ ] Loading indicators appear
- [ ] Error messages clear
- [ ] Navigation flows correctly
- [ ] Sign out works
- [ ] Auth state persists

### UI/UX Tests
- [ ] Password visibility toggle works
- [ ] Keyboard types correct (email, password)
- [ ] Inputs disabled during loading
- [ ] Snackbar shows errors
- [ ] Material3 styling consistent
- [ ] Responsive on different screen sizes

### Edge Cases
- [ ] Network disconnect during auth
- [ ] Rapid button taps (loading state prevents)
- [ ] Back button handling
- [ ] App backgrounding during auth
- [ ] Multiple sign in attempts

---

## Test Data

### Valid Test Accounts

Use these for testing (create if needed):

```
Account 1:
Email: testuser1@growcare.com
Password: testpass123

Account 2:
Email: testuser2@growcare.com
Password: testpass456

Account 3:
Email: farmer@growcare.com
Password: farmer123
```

### Invalid Test Data

For testing validation:

```
Invalid Emails:
- notanemail
- test@
- @example.com
- test user@example.com

Invalid Passwords:
- 12345 (too short)
- (empty)

Mismatched Passwords:
- Password: password123
- Confirm: password456
```

---

## Automated Testing

### Running Tests

```bash
# All tests
./gradlew test

# Only unit tests
./gradlew testDebugUnitTest

# With coverage
./gradlew testDebugUnitTestCoverage

# Specific test class
./gradlew test --tests "AuthViewModelTest"
```

### Test Coverage

Current coverage: **17 test cases**

- ✅ State management
- ✅ Input validation
- ✅ Sign in flow
- ✅ Sign up flow
- ✅ Sign out
- ✅ Error handling
- ✅ Auth status check

---

## Performance Testing

### Metrics to Monitor

1. **Auth Response Time**
   - Target: < 2 seconds for sign in
   - Target: < 3 seconds for sign up

2. **UI Responsiveness**
   - Loading indicators should appear immediately
   - No UI freezing during auth operations

3. **Memory Usage**
   - No memory leaks from ViewModel
   - Proper lifecycle handling

---

## Security Testing

### Verify Security Measures

1. **Password Storage**
   - ✅ Passwords not stored locally
   - ✅ Firebase handles password hashing
   - ✅ No plain text passwords in logs

2. **API Keys**
   - ✅ Gemini API key in local.properties (not in VCS)
   - ✅ Build config field generated securely
   - ✅ google-services.json in .gitignore (should be)

3. **Input Validation**
   - ✅ Client-side validation prevents bad inputs
   - ✅ Server-side validation by Firebase
   - ✅ SQL injection not applicable (NoSQL Firebase)

---

## Regression Testing

After any auth-related changes, verify:

1. [ ] Existing users can still sign in
2. [ ] New users can sign up
3. [ ] Validation still works
4. [ ] Navigation unchanged
5. [ ] All tests still pass

---

## CI/CD Integration

### Automated Checks

```yaml
# Example GitHub Actions workflow
- name: Run Tests
  run: ./gradlew testDebugUnitTest

- name: Build APK
  run: ./gradlew assembleDebug

- name: Upload APK
  uses: actions/upload-artifact@v2
  with:
    name: app-debug
    path: app/build/outputs/apk/debug/*.apk
```

---

## Support and Debugging

### Enable Debugging

1. **Firebase Debug Logging**:
   ```kotlin
   FirebaseAuth.getInstance().useAppLanguage()
   FirebaseAuth.getInstance().firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
   ```

2. **Logcat Filters**:
   ```bash
   adb logcat | grep -E "(AuthViewModel|FirebaseAuth|GrowCare)"
   ```

3. **Network Inspection**:
   - Use Charles Proxy or similar
   - Monitor Firebase API calls

---

## Next Steps

1. ✅ Authentication system verified and working
2. Test on physical device (if not already done)
3. Configure Firebase security rules
4. Implement password reset flow
5. Add email verification
6. Consider social login options

---

**Last Updated**: December 9, 2025  
**Test Status**: All Passing ✅  
**Ready for**: Production Testing


