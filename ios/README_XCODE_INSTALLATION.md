# FocusLock - iOS Installation & Xcode Configuration Guide

FocusLock helps students concentrate by blocking distracting iPhone apps during a scheduled study session using Apple's official Screen Time APIs (**FamilyControls**, **ManagedSettings**, and **DeviceActivity**).

---

## 1. Prerequisites
- **Mac** with macOS Sonoma (14.0+) or macOS Sequoia (15.0+).
- **Xcode 15.0+** or **Xcode 16.0+**.
- **Physical iPhone** running **iOS 16.0 or later**. *(Note: Apple's `FamilyControls` framework requires a physical device with an authenticated Apple ID; it does not work in the Xcode Simulator).*
- An active **Apple Developer Program membership** (required by Apple for the `Family Controls` entitlement).

---

## 2. Step-by-Step Xcode Project Setup

### Step 1: Create the Main Xcode Project
1. Open Xcode and select **File > New > Project...**
2. Choose **iOS > App** and click **Next**.
3. Fill in the project details:
   - **Product Name:** `FocusLock`
   - **Team:** Select your Apple Developer Team.
   - **Organization Identifier:** `com.yourcompany` (e.g. `com.focuslock`)
   - **Bundle Identifier:** `com.yourcompany.FocusLock`
   - **Interface:** `SwiftUI`
   - **Language:** `Swift`
4. Click **Create**.

### Step 2: Add the Device Activity Monitor Extension
1. In Xcode, choose **File > New > Target...**
2. In the template sheet, select **iOS > Device Activity Monitor Extension**.
3. Name the product: `FocusLockMonitor`.
4. Ensure the container is set to `FocusLock` and language is `Swift`.
5. Click **Finish**. (If prompted to activate scheme, choose "Activate" or "Cancel").

### Step 3: Add Capabilities & Entitlements
#### For the Main App Target (`FocusLock`):
1. Select the project root in the Project Navigator.
2. Under **Targets**, click **FocusLock**.
3. Click the **Signing & Capabilities** tab.
4. Click **+ Capability** and add:
   - **Family Controls** (Distribution requires requesting FamilyControls entitlement in Apple Developer portal).
   - **App Groups**: Click `+` under App Groups and enter `group.com.focuslock.app` (replace with your identifier).

#### For the Extension Target (`FocusLockMonitor`):
1. Under **Targets**, click **FocusLockMonitor**.
2. Click **Signing & Capabilities**.
3. Click **+ Capability** and add:
   - **Family Controls**
   - **App Groups**: Select the exact same group (`group.com.focuslock.app`).

### Step 4: Add the Source Code Files
Replace the placeholder files in Xcode with the provided Swift source files from this directory:
- `ios/FocusLock/FocusLockApp.swift` -> `FocusLockApp.swift`
- `ios/FocusLock/ContentView.swift` -> `ContentView.swift`
- `ios/FocusLock/StudySessionManager.swift` -> `StudySessionManager.swift`
- `ios/FocusLockMonitor/DeviceActivityMonitorExtension.swift` -> `DeviceActivityMonitorExtension.swift`

### Step 5: Test on Physical iPhone
1. Connect your physical iPhone to your Mac via USB cable.
2. In Xcode's destination dropdown, choose your physical iPhone.
3. Trust your developer profile on the iPhone under **Settings > General > VPN & Device Management**.
4. Press **Cmd + R** to Build & Run.
5. On initial launch, FocusLock prompts for Screen Time authorization. Tap **Continue** to approve FamilyControls.
6. Select distracting apps via the native `FamilyActivityPicker`.
7. Choose your timer duration (15, 25, 30, 45, 60, or 90 minutes), enter your study goal, and tap **START STUDY**!

---

## 3. Apple Screen Time API Architecture & Honesty
- **No Full OS Lock Claim:** In accordance with Apple guidelines, FocusLock only restricts applications selected by the student.
- **Fail-safe:** If timer scheduling fails, `deactivateAppRestrictions()` ensures shields are immediately removed.
- **Automatic Boundary Trigger:** The `DeviceActivityMonitorExtension` listens to interval expiration and automatically removes shields even if the app was terminated.
