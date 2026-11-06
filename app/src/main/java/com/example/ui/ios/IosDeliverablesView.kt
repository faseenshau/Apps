package com.example.ui.ios

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderEmeraldBright
import com.example.ui.theme.BorderEmeraldSubtle
import com.example.ui.theme.EmeraldOnPrimary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryContainer
import com.example.ui.theme.FocusSurface
import com.example.ui.theme.FocusSurfaceContainer
import com.example.ui.theme.FocusSurfaceContainerHigh
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun IosDeliverablesView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val fileTabs = listOf(
        "Xcode Setup Guide",
        "FocusLockApp.swift",
        "ContentView.swift",
        "StudySessionManager.swift",
        "DeviceActivityMonitor.swift",
        "Entitlements & Config"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("ios_deliverables_view")
    ) {
        // Banner Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = FocusSurfaceContainer),
            border = BorderStroke(1.dp, BorderEmeraldSubtle)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneIphone,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Native iOS Screen Time Architecture",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Complete SwiftUI, FamilyControls & DeviceActivity source deliverables",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = FocusSurface,
            contentColor = EmeraldPrimary,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = EmeraldPrimary
                )
            }
        ) {
            fileTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) EmeraldPrimary else TextMuted
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val currentCode = remember(selectedTab) {
            when (selectedTab) {
                0 -> IOS_SETUP_GUIDE
                1 -> SWIFT_FOCUSLOCK_APP
                2 -> SWIFT_CONTENT_VIEW
                3 -> SWIFT_SESSION_MANAGER
                4 -> SWIFT_DEVICE_ACTIVITY_MONITOR
                else -> IOS_ENTITLEMENTS_AND_CONFIG
            }
        }

        // Code Viewer Card
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF061009)),
            border = BorderStroke(1.dp, BorderEmeraldSubtle)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top bar with file name and copy button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FocusSurfaceContainerHigh)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = fileTabs[selectedTab],
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MintSecondary,
                        fontFamily = FontFamily.Monospace
                    )

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("iOS File Code", currentCode)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied ${fileTabs[selectedTab]} to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = EmeraldOnPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Code", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Scrollable Code Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = currentCode,
                            color = Color(0xFFD4ECD8),
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }
}

// Swift code and setup constants
private const val IOS_SETUP_GUIDE = """//
// FocusLock - Complete iOS Physical iPhone Installation & Setup Guide
//
// 1. SYSTEM REQUIREMENTS:
//    - Xcode 15.0 or later running on macOS Sonoma or Sequoia.
//    - Physical iPhone running iOS 16.0 or later (FamilyControls is NOT fully functional on the simulator).
//    - Active Apple Developer Account (FamilyControls entitlement requires individual or organization membership).
//
// 2. PROJECT CREATION IN XCODE:
//    Step A: Create new Project -> iOS -> App. Name it "FocusLock".
//    Step B: Choose Interface: "SwiftUI", Language: "Swift".
//    Step C: Set Bundle Identifier: "com.yourname.FocusLock".
//
// 3. ADDING THE DEVICE ACTIVITY MONITOR EXTENSION:
//    Step A: In Xcode menu, click File -> New -> Target...
//    Step B: Under iOS targets, select "Device Activity Monitor Extension".
//    Step C: Name the extension target: "FocusLockMonitor".
//
// 4. ENTITLEMENTS CONFIGURATION:
//    For BOTH the main App target AND the Extension target:
//    Step A: Go to Target -> Signing & Capabilities -> + Capability.
//    Step B: Add "Family Controls" capability.
//    Step C: Add "App Groups" capability. Check group: "group.com.yourname.FocusLock".
//
// 5. OFFICIAL APPLE SCREEN TIME HONESTY POLICY:
//    - FocusLock uses Apple's official Screen Time API (FamilyControls, ManagedSettings, DeviceActivity).
//    - It shields designated apps via ManagedSettingsStore without claiming to lock the entire OS.
//    - DeviceActivityMonitor extension triggers automatically on schedule boundaries.
//    - When the study session finishes, the shield is immediately removed.
"""

private const val SWIFT_FOCUSLOCK_APP = """import SwiftUI
import FamilyControls

@main
struct FocusLockApp: App {
    @StateObject private var sessionManager = StudySessionManager.shared
    @Environment(\.scenePhase) private var scenePhase

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(sessionManager)
                .task {
                    // Request FamilyControls authorization on first launch
                    await sessionManager.requestAuthorization()
                }
                .onChange(of: scenePhase) { newPhase in
                    if newPhase == .active {
                        sessionManager.syncActiveSessionState()
                    }
                }
        }
    }
}
"""

private const val SWIFT_CONTENT_VIEW = """import SwiftUI
import FamilyControls
import DeviceActivity

struct ContentView: View {
    @EnvironmentObject var sessionManager: StudySessionManager
    @State private var showingAppPicker = false
    @State private var showingGiveUpAlert = false
    
    // Preset timer durations
    let presetDurations = [15, 25, 30, 45, 60, 90]

    var body: some View {
        ZStack {
            // Obsidian dark green gradient background
            LinearGradient(
                colors: [Color(hex: "0x040D07"), Color(hex: "0x08160E"), Color.black],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 20) {
                // Header
                HStack {
                    HStack(spacing: 8) {
                        Image(systemName: "lock.shield.fill")
                            .foregroundColor(Color(hex: "0x00E676"))
                        Text("FocusLock")
                            .font(.title3.bold())
                            .foregroundColor(.white)
                    }
                    Spacer()
                    
                    // FamilyActivityPicker Button
                    Button {
                        showingAppPicker = true
                    } label: {
                        HStack(spacing: 6) {
                            Image(systemName: "apps.iphone")
                            Text("\(sessionManager.selectedApps.applicationTokens.count) Apps")
                        }
                        .font(.caption.bold())
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(Color(hex: "0x142B1E"))
                        .foregroundColor(Color(hex: "0x00E676"))
                        .clipShape(Capsule())
                    }
                }
                .padding(.horizontal)

                Spacer()

                // Large Circular Countdown Timer
                ZStack {
                    // Track
                    Circle()
                        .stroke(Color(hex: "0x10271B"), lineWidth: 16)
                        .frame(width: 270, height: 270)

                    // Glowing Progress Arc
                    Circle()
                        .trim(from: 0.0, to: sessionManager.progress)
                        .stroke(
                            AngularGradient(
                                colors: [Color(hex: "0x00E676"), Color(hex: "0x1DE9B6"), Color(hex: "0x00E676")],
                                center: .center
                            ),
                            style: StrokeStyle(lineWidth: 16, lineCap: .round)
                        )
                        .rotationEffect(.degrees(-90))
                        .frame(width: 270, height: 270)
                        .animation(.easeInOut(duration: 0.5), value: sessionManager.progress)

                    // Center Content
                    VStack(spacing: 8) {
                        Text(sessionManager.sessionState == .running ? "FOCUS ACTIVE" : "STUDY SESSION")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(Color(hex: "0x00E676"))
                            .padding(.horizontal, 10)
                            .padding(.vertical, 4)
                            .background(Color(hex: "0x0A3822"))
                            .clipShape(Capsule())

                        Text(sessionManager.timeRemainingString)
                            .font(.system(size: 52, weight: .heavy, design: .monospaced))
                            .foregroundColor(.white)

                        Text(sessionManager.studyGoal.isEmpty ? "Deep Work Session" : sessionManager.studyGoal)
                            .font(.subheadline)
                            .foregroundColor(Color(hex: "0x9EBAAA"))
                            .lineLimit(1)
                    }
                }

                Spacer()

                if sessionManager.sessionState == .idle {
                    // Goal Input
                    VStack(alignment: .leading, spacing: 6) {
                        Text("STUDY GOAL / SUBJECT")
                            .font(.caption2.bold())
                            .foregroundColor(Color(hex: "0x6B8777"))

                        TextField("e.g. Master Organic Chem Chapter 4", text: ${'$'}sessionManager.studyGoal)
                            .padding()
                            .background(Color(hex: "0x142B1E"))
                            .cornerRadius(12)
                            .foregroundColor(.white)
                    }
                    .padding(.horizontal)

                    // Duration Presets (15, 25, 30, 45, 60, 90)
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 10) {
                            ForEach(presetDurations, id: \.self) { mins in
                                Button {
                                    sessionManager.setDuration(minutes: mins)
                                } label: {
                                    VStack(spacing: 2) {
                                        Text("\(mins)")
                                            .font(.headline.bold())
                                        Text("min")
                                            .font(.caption2)
                                    }
                                    .frame(width: 58, height: 58)
                                    .background(sessionManager.selectedDurationMinutes == mins ? Color(hex: "0x00E676") : Color(hex: "0x142B1E"))
                                    .foregroundColor(sessionManager.selectedDurationMinutes == mins ? .black : .white)
                                    .cornerRadius(14)
                                }
                            }
                        }
                        .padding(.horizontal)
                    }

                    // START STUDY Button
                    Button {
                        sessionManager.startStudySession()
                    } label: {
                        HStack {
                            Image(systemName: "play.fill")
                            Text("START STUDY • \(sessionManager.selectedDurationMinutes) MIN")
                                .fontWeight(.bold)
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(Color(hex: "0x00E676"))
                        .foregroundColor(.black)
                        .cornerRadius(16)
                    }
                    .padding(.horizontal)
                } else {
                    // Running Controls
                    HStack(spacing: 14) {
                        Button {
                            if sessionManager.sessionState == .running {
                                sessionManager.pauseSession()
                            } else {
                                sessionManager.resumeSession()
                            }
                        } label: {
                            Text(sessionManager.sessionState == .running ? "Pause" : "Resume")
                                .frame(maxWidth: .infinity)
                                .frame(height: 52)
                                .background(Color(hex: "0x1C3A2A"))
                                .foregroundColor(.white)
                                .cornerRadius(14)
                        }

                        Button {
                            showingGiveUpAlert = true
                        } label: {
                            Text("End Early")
                                .frame(maxWidth: .infinity)
                                .frame(height: 52)
                                .background(Color.red.opacity(0.8))
                                .foregroundColor(.white)
                                .cornerRadius(14)
                        }
                    }
                    .padding(.horizontal)
                }
            }
            .padding(.vertical)
        }
        .sheet(isPresented: ${'$'}showingAppPicker) {
            FamilyActivityPicker(selection: ${'$'}sessionManager.selectedApps)
        }
        .alert("End Study Session?", isPresented: ${'$'}showingGiveUpAlert) {
            Button("Keep Focusing", role: .cancel) {}
            Button("End Early", role: .destructive) {
                sessionManager.stopStudySession(completed: false)
            }
        } message: {
            Text("Ending the session early will lift the app restrictions.")
        }
    }
}
"""

private const val SWIFT_SESSION_MANAGER = """import Foundation
import FamilyControls
import ManagedSettings
import DeviceActivity
import UserNotifications

enum SessionStatus {
    case idle, running, paused, completed
}

class StudySessionManager: ObservableObject {
    static let shared = StudySessionManager()

    @Published var selectedDurationMinutes: Int = 30
    @Published var studyGoal: String = "Master Chapter Review"
    @Published var selectedApps = FamilyActivitySelection()
    @Published var sessionState: SessionStatus = .idle
    @Published var remainingSeconds: Int = 30 * 60
    @Published var totalSeconds: Int = 30 * 60

    private let managedSettingsStore = ManagedSettingsStore()
    private let deviceActivityCenter = DeviceActivityCenter()
    private var timer: Timer?

    // Shared App Group storage for extension synchronization
    private let sharedDefaults = UserDefaults(suiteName: "group.com.focuslock.app")

    var progress: CGFloat {
        guard totalSeconds > 0 else { return 1.0 }
        return CGFloat(remainingSeconds) / CGFloat(totalSeconds)
    }

    var timeRemainingString: String {
        let mins = remainingSeconds / 60
        let secs = remainingSeconds % 60
        return String(format: "%02d:%02d", mins, secs)
    }

    func requestAuthorization() async {
        do {
            try await AuthorizationCenter.shared.requestAuthorization(for: .individual)
            print("FamilyControls authorization granted")
        } catch {
            print("FamilyControls authorization failed: \(error.localizedDescription)")
        }
    }

    func setDuration(minutes: Int) {
        guard sessionState == .idle else { return }
        selectedDurationMinutes = minutes
        totalSeconds = minutes * 60
        remainingSeconds = totalSeconds
    }

    func startStudySession() {
        let duration = selectedDurationMinutes * 60
        totalSeconds = duration
        remainingSeconds = duration
        sessionState = .running

        // 1. Immediately activate ManagedSettings restrictions on selected apps
        activateAppRestrictions()

        // 2. Schedule DeviceActivity boundaries
        scheduleDeviceActivitySession(durationSeconds: duration)

        // 3. Start local countdown timer
        startTimer()

        // 4. Schedule local notification for completion
        scheduleCompletionNotification(inSeconds: TimeInterval(duration))
    }

    private func activateAppRestrictions() {
        // Shield selected apps using ManagedSettings
        let tokens = selectedApps.applicationTokens
        let categoryTokens = selectedApps.categoryTokens

        managedSettingsStore.shield.applications = tokens.isEmpty ? nil : tokens
        managedSettingsStore.shield.applicationCategories = categoryTokens.isEmpty ? nil : .specific(categoryTokens)

        // Save active state to shared app group
        sharedDefaults?.set(true, forKey: "isSessionActive")
        sharedDefaults?.set(Date().timeIntervalSince1970, forKey: "sessionStartTime")
    }

    func deactivateAppRestrictions() {
        // Immediately remove shields
        managedSettingsStore.shield.applications = nil
        managedSettingsStore.shield.applicationCategories = nil
        sharedDefaults?.set(false, forKey: "isSessionActive")
    }

    private func scheduleDeviceActivitySession(durationSeconds: Int) {
        let now = Calendar.current.dateComponents([.hour, .minute, .second], from: Date())
        let endDate = Date().addingTimeInterval(TimeInterval(durationSeconds))
        let endComponents = Calendar.current.dateComponents([.hour, .minute, .second], from: endDate)

        let schedule = DeviceActivitySchedule(
            intervalStart: now,
            intervalEnd: endComponents,
            repeats: false
        )

        let activityName = DeviceActivityName("FocusLockStudySession")
        do {
            try deviceActivityCenter.startMonitoring(activityName, during: schedule)
        } catch {
            print("DeviceActivity scheduling error: \(error)")
        }
    }

    private func startTimer() {
        timer?.invalidate()
        timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] _ in
            guard let self = self else { return }
            if self.sessionState == .running && self.remainingSeconds > 0 {
                self.remainingSeconds -= 1
                if self.remainingSeconds <= 0 {
                    self.stopStudySession(completed: true)
                }
            }
        }
    }

    func pauseSession() {
        sessionState = .paused
    }

    func resumeSession() {
        sessionState = .running
    }

    func stopStudySession(completed: Bool) {
        timer?.invalidate()
        timer = nil
        deviceActivityCenter.stopMonitoring([DeviceActivityName("FocusLockStudySession")])
        deactivateAppRestrictions()
        sessionState = completed ? .completed : .idle
        if !completed {
            remainingSeconds = selectedDurationMinutes * 60
        }
    }

    func syncActiveSessionState() {
        // Sync state if returned from background
    }

    private func scheduleCompletionNotification(inSeconds: TimeInterval) {
        let content = UNMutableNotificationContent()
        content.title = "🎉 Study Session Complete!"
        content.body = "Great focus on \(studyGoal.isEmpty ? "your goals" : studyGoal). Distracting apps are now unlocked."
        content.sound = .default

        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: inSeconds, repeats: false)
        let request = UNNotificationRequest(identifier: "FocusLockCompletion", content: content, trigger: trigger)
        UNUserNotificationCenter.current().add(request)
    }
}
"""

private const val SWIFT_DEVICE_ACTIVITY_MONITOR = """import DeviceActivity
import ManagedSettings
import Foundation

// Device Activity Monitor Extension
// This extension automatically triggers when the scheduled session boundaries hit,
// ensuring restrictions are safely removed even if the app is suspended.
class DeviceActivityMonitorExtension: DeviceActivityMonitor {
    private let store = ManagedSettingsStore()
    private let sharedDefaults = UserDefaults(suiteName: "group.com.focuslock.app")

    override func intervalDidStart(for activity: DeviceActivityName) {
        super.intervalDidStart(for: activity)
        print("DeviceActivity intervalDidStart: Shields active")
    }

    override func intervalDidEnd(for activity: DeviceActivityName) {
        super.intervalDidEnd(for: activity)
        // Automatically remove shields when the scheduled timer ends
        store.shield.applications = nil
        store.shield.applicationCategories = nil
        sharedDefaults?.set(false, forKey: "isSessionActive")
        print("DeviceActivity intervalDidEnd: All app restrictions automatically removed")
    }

    override func eventDidReachThreshold(for activity: DeviceActivityName, event: DeviceActivityEvent.Name) {
        super.eventDidReachThreshold(for: activity, event: event)
    }

    override func intervalWillStartWarning(for activity: DeviceActivityName) {
        super.intervalWillStartWarning(for: activity)
    }

    override func intervalWillEndWarning(for activity: DeviceActivityName) {
        super.intervalWillEndWarning(for: activity)
    }
}
"""

private const val IOS_ENTITLEMENTS_AND_CONFIG = """<!-- FocusLock.entitlements (Main Target) -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>com.apple.developer.family-controls</key>
    <true/>
    <key>com.apple.security.application-groups</key>
    <array>
        <string>group.com.focuslock.app</string>
    </array>
</dict>
</plist>

<!-- FocusLockMonitor.entitlements (Extension Target) -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>com.apple.developer.family-controls</key>
    <true/>
    <key>com.apple.security.application-groups</key>
    <array>
        <string>group.com.focuslock.app</string>
    </array>
</dict>
</plist>
"""
