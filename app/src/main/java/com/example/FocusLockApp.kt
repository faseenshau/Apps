package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class FocusLockApp : Application() {

    companion object {
        const val FOCUS_CHANNEL_ID = "focus_lock_session_channel"
        const val COMPLETION_CHANNEL_ID = "focus_lock_completion_channel"
        lateinit var instance: FocusLockApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val sessionChannel = NotificationChannel(
                FOCUS_CHANNEL_ID,
                getString(R.string.focus_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.focus_channel_desc)
                setShowBadge(false)
            }

            val completionChannel = NotificationChannel(
                COMPLETION_CHANNEL_ID,
                "Focus Session Completed",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when a study timer completes"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(sessionChannel)
            notificationManager.createNotificationChannel(completionChannel)
        }
    }
}
