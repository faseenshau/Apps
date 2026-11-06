package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.example.data.db.FocusDatabase
import com.example.model.AppInfo
import com.example.model.SessionState
import com.example.model.StudySession
import com.example.service.FocusSessionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object FocusSessionManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Presets required: 15, 25, 30, 45, 60, 90 (default 30)
    val PRESET_MINUTES = listOf(15, 25, 30, 45, 60, 90)

    private val _selectedDurationMinutes = MutableStateFlow(30)
    val selectedDurationMinutes: StateFlow<Int> = _selectedDurationMinutes.asStateFlow()

    private val _subjectGoal = MutableStateFlow("Master Chapter Review")
    val subjectGoal: StateFlow<String> = _subjectGoal.asStateFlow()

    private val _sessionState = MutableStateFlow(SessionState.IDLE)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(30 * 60)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _totalSessionSeconds = MutableStateFlow(30 * 60)
    val totalSessionSeconds: StateFlow<Int> = _totalSessionSeconds.asStateFlow()

    private val _blockedPackages = MutableStateFlow<Set<String>>(
        setOf(
            "com.instagram.android",
            "com.google.android.youtube",
            "com.zhiliaoapp.musically", // TikTok
            "com.twitter.android",
            "com.reddit.frontpage",
            "com.snapchat.android",
            "com.discord",
            "com.netflix.mediaclient"
        )
    )
    val blockedPackages: StateFlow<Set<String>> = _blockedPackages.asStateFlow()

    private val _availableApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val availableApps: StateFlow<List<AppInfo>> = _availableApps.asStateFlow()

    private val _interceptedAttempts = MutableStateFlow(0)
    val interceptedAttempts: StateFlow<Int> = _interceptedAttempts.asStateFlow()

    private val _sessionCompletedEvent = MutableSharedFlow<StudySession>()
    val sessionCompletedEvent: SharedFlow<StudySession> = _sessionCompletedEvent.asSharedFlow()

    private var currentSessionStartTime = 0L

    fun initApps(context: Context) {
        scope.launch(Dispatchers.IO) {
            val pm = context.packageManager
            val installedApps = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getInstalledApplications(PackageManager.GET_META_DATA)
                }
            } catch (e: Exception) {
                emptyList<ApplicationInfo>()
            }

            // Built-in curated popular student distractions
            val curatedPresets = listOf(
                AppInfo("com.instagram.android", "Instagram", isShielded = true, category = "Social"),
                AppInfo("com.google.android.youtube", "YouTube", isShielded = true, category = "Video"),
                AppInfo("com.zhiliaoapp.musically", "TikTok", isShielded = true, category = "Social"),
                AppInfo("com.twitter.android", "X (Twitter)", isShielded = true, category = "Social"),
                AppInfo("com.reddit.frontpage", "Reddit", isShielded = true, category = "Forums"),
                AppInfo("com.snapchat.android", "Snapchat", isShielded = true, category = "Social"),
                AppInfo("com.discord", "Discord", isShielded = true, category = "Chat"),
                AppInfo("com.netflix.mediaclient", "Netflix", isShielded = true, category = "Streaming"),
                AppInfo("com.supercell.clashofclans", "Clash of Clans", isShielded = false, category = "Games"),
                AppInfo("com.mojang.minecraftpe", "Minecraft", isShielded = false, category = "Games"),
                AppInfo("com.facebook.katana", "Facebook", isShielded = false, category = "Social"),
                AppInfo("com.spotify.music", "Spotify", isShielded = false, category = "Audio")
            )

            val systemPackageNames = setOf(
                context.packageName,
                "com.android.settings",
                "com.android.systemui",
                "com.google.android.dialer",
                "com.android.launcher3",
                "com.google.android.apps.nexuslauncher"
            )

            val deviceApps = installedApps
                .filter { it.packageName !in systemPackageNames && (it.flags and ApplicationInfo.FLAG_SYSTEM == 0) }
                .map { app ->
                    val appName = pm.getApplicationLabel(app).toString()
                    val isShielded = _blockedPackages.value.contains(app.packageName)
                    AppInfo(
                        packageName = app.packageName,
                        appName = appName,
                        isShielded = isShielded,
                        isSystemApp = false,
                        category = "Installed"
                    )
                }

            // Merge curated and installed apps, deduplicating by package name
            val mergedMap = mutableMapOf<String, AppInfo>()
            curatedPresets.forEach { mergedMap[it.packageName] = it }
            deviceApps.forEach { mergedMap[it.packageName] = it }

            // Sync with current blocked set
            val list = mergedMap.values.map { app ->
                app.copy(isShielded = _blockedPackages.value.contains(app.packageName))
            }.sortedWith(compareBy({ !it.isShielded }, { it.appName }))

            _availableApps.value = list
        }
    }

    fun setDuration(minutes: Int) {
        if (_sessionState.value == SessionState.IDLE) {
            _selectedDurationMinutes.value = minutes
            val secs = minutes * 60
            _selectedDurationMinutes.value = minutes
            _totalSessionSeconds.value = secs
            _remainingSeconds.value = secs
        }
    }

    fun setSubjectGoal(goal: String) {
        _subjectGoal.value = goal
    }

    fun toggleAppBlocked(packageName: String) {
        val current = _blockedPackages.value.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        _blockedPackages.value = current

        _availableApps.value = _availableApps.value.map { app ->
            if (app.packageName == packageName) {
                app.copy(isShielded = current.contains(packageName))
            } else app
        }
    }

    fun selectAllApps(shield: Boolean) {
        val current = _blockedPackages.value.toMutableSet()
        if (shield) {
            _availableApps.value.forEach { current.add(it.packageName) }
        } else {
            current.clear()
        }
        _blockedPackages.value = current
        _availableApps.value = _availableApps.value.map { it.copy(isShielded = shield) }
    }

    fun isPackageBlocked(packageName: String): Boolean {
        return _sessionState.value == SessionState.RUNNING && _blockedPackages.value.contains(packageName)
    }

    fun registerInterceptedAttempt() {
        _interceptedAttempts.value = _interceptedAttempts.value + 1
    }

    fun startStudySession(context: Context) {
        val duration = _selectedDurationMinutes.value
        val totalSecs = duration * 60
        _totalSessionSeconds.value = totalSecs
        _remainingSeconds.value = totalSecs
        _interceptedAttempts.value = 0
        _sessionState.value = SessionState.RUNNING
        currentSessionStartTime = System.currentTimeMillis()

        // Start Foreground Service
        val serviceIntent = Intent(context, FocusSessionService::class.java).apply {
            action = FocusSessionService.ACTION_START
            putExtra(FocusSessionService.EXTRA_DURATION_SECONDS, totalSecs)
            putExtra(FocusSessionService.EXTRA_GOAL, _subjectGoal.value)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    fun pauseSession(context: Context) {
        if (_sessionState.value == SessionState.RUNNING) {
            _sessionState.value = SessionState.PAUSED
            val serviceIntent = Intent(context, FocusSessionService::class.java).apply {
                action = FocusSessionService.ACTION_PAUSE
            }
            context.startService(serviceIntent)
        }
    }

    fun resumeSession(context: Context) {
        if (_sessionState.value == SessionState.PAUSED) {
            _sessionState.value = SessionState.RUNNING
            val serviceIntent = Intent(context, FocusSessionService::class.java).apply {
                action = FocusSessionService.ACTION_RESUME
            }
            context.startService(serviceIntent)
        }
    }

    fun updateRemainingSeconds(seconds: Int) {
        _remainingSeconds.value = seconds
    }

    fun endSession(context: Context, completedSuccessfully: Boolean, notes: String = "") {
        val durationSecs = _totalSessionSeconds.value
        val actualSecs = durationSecs - _remainingSeconds.value
        val endTime = System.currentTimeMillis()

        _sessionState.value = if (completedSuccessfully) SessionState.COMPLETED else SessionState.IDLE

        // Stop foreground service
        val serviceIntent = Intent(context, FocusSessionService::class.java).apply {
            action = FocusSessionService.ACTION_STOP
        }
        context.startService(serviceIntent)

        // Save session record to Room database
        val session = StudySession(
            startTimeMillis = if (currentSessionStartTime > 0) currentSessionStartTime else endTime - (actualSecs * 1000L),
            endTimeMillis = endTime,
            durationMinutes = _selectedDurationMinutes.value,
            actualSecondsFocused = actualSecs,
            subjectGoal = _subjectGoal.value.ifBlank { "Study Session" },
            blockedAppsCount = _blockedPackages.value.size,
            completedSuccessfully = completedSuccessfully,
            notes = notes
        )

        scope.launch(Dispatchers.IO) {
            FocusDatabase.getDatabase(context).sessionDao().insertSession(session)
            if (completedSuccessfully) {
                _sessionCompletedEvent.emit(session)
            }
        }
    }

    fun resetToIdle() {
        _sessionState.value = SessionState.IDLE
        val secs = _selectedDurationMinutes.value * 60
        _remainingSeconds.value = secs
        _totalSessionSeconds.value = secs
        _interceptedAttempts.value = 0
    }
}
