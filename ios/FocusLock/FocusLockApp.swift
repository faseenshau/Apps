import SwiftUI
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
