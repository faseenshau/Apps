package com.example.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.FocusLockApp
import com.example.MainActivity
import com.example.R
import com.example.data.repository.FocusSessionManager
import com.example.model.SessionState
import com.example.ui.shield.ShieldActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FocusSessionService : Service() {

    companion object {
        const val NOTIFICATION_ID = 1001
        const val COMPLETION_NOTIFICATION_ID = 1002

        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"

        const val EXTRA_DURATION_SECONDS = "EXTRA_DURATION_SECONDS"
        const val EXTRA_GOAL = "EXTRA_GOAL"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var timerJob: Job? = null
    private var monitorJob: Job? = null
    private var totalSeconds = 30 * 60
    private var remainingSeconds = 30 * 60
    private var goalTitle = "Study Session"
    private var isPaused = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                totalSeconds = intent.getIntExtra(EXTRA_DURATION_SECONDS, 30 * 60)
                remainingSeconds = totalSeconds
                goalTitle = intent.getStringExtra(EXTRA_GOAL) ?: "Study Session"
                isPaused = false
                startForeground(NOTIFICATION_ID, buildOngoingNotification())
                startTimer()
                startDistractionMonitor()
            }
            ACTION_PAUSE -> {
                isPaused = true
                updateNotification()
            }
            ACTION_RESUME -> {
                isPaused = false
                updateNotification()
            }
            ACTION_STOP -> {
                stopTimer()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive && remainingSeconds > 0) {
                delay(1000L)
                if (!isPaused) {
                    remainingSeconds--
                    FocusSessionManager.updateRemainingSeconds(remainingSeconds)

                    // Update notification every 5 seconds or when under 1 minute
                    if (remainingSeconds % 5 == 0 || remainingSeconds <= 60) {
                        updateNotification()
                    }

                    if (remainingSeconds <= 0) {
                        onSessionCompleted()
                        break
                    }
                }
            }
        }
    }

    private fun startDistractionMonitor() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            while (isActive) {
                delay(1500L)
                if (!isPaused && FocusSessionManager.sessionState.value == SessionState.RUNNING) {
                    checkForegroundAppAndShield()
                }
            }
        }
    }

    private fun checkForegroundAppAndShield() {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return
        val time = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(time - 3000, time)
        val event = UsageEvents.Event()
        var currentForegroundPackage: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                currentForegroundPackage = event.packageName
            }
        }

        currentForegroundPackage?.let { pkg ->
            if (pkg != packageName && FocusSessionManager.isPackageBlocked(pkg)) {
                FocusSessionManager.registerInterceptedAttempt()
                val shieldIntent = Intent(this, ShieldActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("BLOCKED_PACKAGE", pkg)
                }
                startActivity(shieldIntent)
            }
        }
    }

    private fun onSessionCompleted() {
        triggerHapticAndVibrate()
        showCompletionNotification()
        FocusSessionManager.endSession(applicationContext, completedSuccessfully = true)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun triggerHapticAndVibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 500), -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 500), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(500)
                }
            }
        } catch (_: Exception) {}
    }

    private fun buildOngoingNotification(): Notification {
        val mins = remainingSeconds / 60
        val secs = remainingSeconds % 60
        val timeFormatted = String.format("%02d:%02d", mins, secs)

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentTitle = if (isPaused) "FocusLock Paused: $timeFormatted" else "FocusLock Active: $timeFormatted"
        val contentText = "Goal: $goalTitle • ${FocusSessionManager.blockedPackages.value.size} apps shielded"

        return NotificationCompat.Builder(this, FocusLockApp.FOCUS_CHANNEL_ID)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(NOTIFICATION_ID, buildOngoingNotification())
    }

    private fun showCompletionNotification() {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            1,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, FocusLockApp.COMPLETION_CHANNEL_ID)
            .setContentTitle("🎉 Focus Session Finished!")
            .setContentText("Awesome concentration! You completed your goal: $goalTitle")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(COMPLETION_NOTIFICATION_ID, notification)
    }

    private fun stopTimer() {
        timerJob?.cancel()
        monitorJob?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
