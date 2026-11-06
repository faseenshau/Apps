import Foundation
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
