import DeviceActivity
import ManagedSettings
import Foundation

// Device Activity Monitor Extension
// Automatically called by iOS at schedule boundaries
class DeviceActivityMonitorExtension: DeviceActivityMonitor {
    private let store = ManagedSettingsStore()
    private let sharedDefaults = UserDefaults(suiteName: "group.com.focuslock.app")

    override func intervalDidStart(for activity: DeviceActivityName) {
        super.intervalDidStart(for: activity)
        print("DeviceActivityMonitor: Session boundary started")
    }

    override func intervalDidEnd(for activity: DeviceActivityName) {
        super.intervalDidEnd(for: activity)
        // Automatically remove shields when the scheduled timer ends
        store.shield.applications = nil
        store.shield.applicationCategories = nil
        sharedDefaults?.set(false, forKey: "isSessionActive")
        print("DeviceActivityMonitor: Session ended - all restrictions automatically removed")
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
