package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.data.repository.FocusSessionManager
import com.example.model.SessionState
import com.example.ui.shield.ShieldActivity

class FocusShieldAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkgName = event.packageName?.toString() ?: return

            if (pkgName != packageName &&
                FocusSessionManager.sessionState.value == SessionState.RUNNING &&
                FocusSessionManager.isPackageBlocked(pkgName)
            ) {
                FocusSessionManager.registerInterceptedAttempt()
                val intent = Intent(this, ShieldActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("BLOCKED_PACKAGE", pkgName)
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {}
}
