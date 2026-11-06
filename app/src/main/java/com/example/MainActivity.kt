package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.db.FocusDatabase
import com.example.data.repository.FocusSessionManager
import com.example.model.SessionState
import com.example.model.StudySession
import com.example.ui.apps.AppSelectionSheet
import com.example.ui.completion.CompletionScreen
import com.example.ui.ios.IosDeliverablesView
import com.example.ui.stats.StudyStatsView
import com.example.ui.theme.BorderEmeraldBright
import com.example.ui.theme.BorderEmeraldSubtle
import com.example.ui.theme.EmeraldOnPrimary
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryContainer
import com.example.ui.theme.FocusBlack
import com.example.ui.theme.FocusSurface
import com.example.ui.theme.FocusSurfaceContainer
import com.example.ui.theme.FocusSurfaceContainerHigh
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.timer.CircularCountdownTimer
import com.example.ui.timer.TimerControls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize installed and curated app list
        FocusSessionManager.initApps(this)

        setContent {
            MyApplicationTheme {
                MainAppRoot()
            }
        }
    }
}

@Composable
fun MainAppRoot() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Screen navigation tabs: 0 = Timer, 1 = Stats, 2 = iOS / Swift
    var selectedTab by remember { mutableIntStateOf(0) }

    // Sheet states
    var showAppPickerSheet by remember { mutableStateOf(false) }

    // State flows from FocusSessionManager
    val selectedDuration by FocusSessionManager.selectedDurationMinutes.collectAsState()
    val subjectGoal by FocusSessionManager.subjectGoal.collectAsState()
    val sessionState by FocusSessionManager.sessionState.collectAsState()
    val remainingSeconds by FocusSessionManager.remainingSeconds.collectAsState()
    val totalSeconds by FocusSessionManager.totalSessionSeconds.collectAsState()
    val blockedPackages by FocusSessionManager.blockedPackages.collectAsState()
    val availableApps by FocusSessionManager.availableApps.collectAsState()
    val interceptedAttempts by FocusSessionManager.interceptedAttempts.collectAsState()

    // Room Database queries
    val database = remember { FocusDatabase.getDatabase(context) }
    val sessionsList by database.sessionDao().getAllSessions().collectAsState(initial = emptyList())
    val totalFocusMinutes by database.sessionDao().getTotalFocusMinutes().collectAsState(initial = 0)
    val completedCount by database.sessionDao().getCompletedCount().collectAsState(initial = 0)

    // Notification Permission Launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Notifications enabled for study alerts!")
            }
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Handle Back Press
    BackHandler(enabled = selectedTab != 0 || sessionState == SessionState.COMPLETED) {
        if (sessionState == SessionState.COMPLETED) {
            FocusSessionManager.resetToIdle()
        } else if (selectedTab != 0) {
            selectedTab = 0
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("focuslock_scaffold"),
        containerColor = FocusBlack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (sessionState != SessionState.COMPLETED) {
                NavigationBar(
                    containerColor = FocusSurface,
                    contentColor = TextPrimary,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Timer",
                                tint = if (selectedTab == 0) EmeraldPrimary else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = "Study Timer",
                                color = if (selectedTab == 0) EmeraldPrimary else TextMuted,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = EmeraldPrimaryContainer
                        ),
                        modifier = Modifier.testTag("nav_timer_tab")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Stats",
                                tint = if (selectedTab == 1) EmeraldPrimary else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = "Progress",
                                color = if (selectedTab == 1) EmeraldPrimary else TextMuted,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = EmeraldPrimaryContainer
                        ),
                        modifier = Modifier.testTag("nav_stats_tab")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = MintSecondary,
                                        contentColor = FocusBlack
                                    ) {
                                        Text("iOS", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhoneIphone,
                                    contentDescription = "iOS Swift Deliverables",
                                    tint = if (selectedTab == 2) EmeraldPrimary else TextMuted
                                )
                            }
                        },
                        label = {
                            Text(
                                text = "iOS Swift",
                                color = if (selectedTab == 2) EmeraldPrimary else TextMuted,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = EmeraldPrimaryContainer
                        ),
                        modifier = Modifier.testTag("nav_ios_tab")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF030D07),
                            FocusBlack,
                            Color(0xFF020904)
                        )
                    )
                )
        ) {
            if (sessionState == SessionState.COMPLETED) {
                // Rewarding completion screen
                CompletionScreen(
                    durationMinutes = selectedDuration,
                    subjectGoal = subjectGoal,
                    shieldedAppsCount = blockedPackages.size,
                    interceptedAttempts = interceptedAttempts,
                    onStartNewSession = {
                        FocusSessionManager.resetToIdle()
                        FocusSessionManager.startStudySession(context)
                    },
                    onTakeBreak = {
                        FocusSessionManager.resetToIdle()
                        FocusSessionManager.setDuration(5)
                        FocusSessionManager.setSubjectGoal("☕ Rest & Stretch Break")
                    },
                    onSaveNotesAndFinish = { notes ->
                        coroutineScope.launch(Dispatchers.IO) {
                            val lastSession = database.sessionDao().getAllSessions()
                            // Update notes in latest session
                        }
                        FocusSessionManager.resetToIdle()
                        selectedTab = 1 // Switch to stats view
                    }
                )
            } else {
                when (selectedTab) {
                    0 -> {
                        // Study Timer Main Tab
                        TimerMainScreen(
                            remainingSeconds = remainingSeconds,
                            totalSeconds = totalSeconds,
                            selectedDuration = selectedDuration,
                            subjectGoal = subjectGoal,
                            sessionState = sessionState,
                            shieldedAppsCount = blockedPackages.size,
                            onDurationSelected = { mins -> FocusSessionManager.setDuration(mins) },
                            onSubjectGoalChanged = { goal -> FocusSessionManager.setSubjectGoal(goal) },
                            onOpenAppPicker = { showAppPickerSheet = true },
                            onStartStudy = {
                                FocusSessionManager.startStudySession(context)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("🛡️ FocusLock activated! $selectedDuration min session started.")
                                }
                            },
                            onPause = { FocusSessionManager.pauseSession(context) },
                            onResume = { FocusSessionManager.resumeSession(context) },
                            onStopSession = { completed ->
                                FocusSessionManager.endSession(context, completedSuccessfully = completed)
                            }
                        )
                    }
                    1 -> {
                        // Stats & Progress Tab
                        Box(modifier = Modifier.padding(16.dp)) {
                            StudyStatsView(
                                sessions = sessionsList,
                                totalFocusMinutes = totalFocusMinutes ?: 0,
                                completedCount = completedCount,
                                onDeleteSession = { session ->
                                    coroutineScope.launch(Dispatchers.IO) {
                                        database.sessionDao().deleteSession(session)
                                    }
                                }
                            )
                        }
                    }
                    2 -> {
                        // iOS Swift Deliverables Tab
                        Box(modifier = Modifier.padding(16.dp)) {
                            IosDeliverablesView()
                        }
                    }
                }
            }
        }
    }

    // App Selection Modal Sheet
    if (showAppPickerSheet) {
        AppSelectionSheet(
            apps = availableApps,
            blockedPackageNames = blockedPackages,
            onToggleApp = { pkg -> FocusSessionManager.toggleAppBlocked(pkg) },
            onSelectAll = { selectAll -> FocusSessionManager.selectAllApps(selectAll) },
            onDismiss = { showAppPickerSheet = false }
        )
    }
}

@Composable
fun TimerMainScreen(
    remainingSeconds: Int,
    totalSeconds: Int,
    selectedDuration: Int,
    subjectGoal: String,
    sessionState: SessionState,
    shieldedAppsCount: Int,
    onDurationSelected: (Int) -> Unit,
    onSubjectGoalChanged: (String) -> Unit,
    onOpenAppPicker: () -> Unit,
    onStartStudy: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStopSession: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(EmeraldPrimary.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                        .border(1.dp, BorderEmeraldBright.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Logo",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "FocusLock",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Student Focus Shield",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            // Quick App Picker pill
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenAppPicker() },
                color = FocusSurfaceContainer,
                border = BorderStroke(1.dp, BorderEmeraldSubtle)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$shieldedAppsCount Apps Shielded",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Hero illustration banner (if idle)
        if (sessionState == SessionState.IDLE) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = FocusSurfaceContainer),
                border = BorderStroke(1.dp, BorderEmeraldSubtle)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.focus_study_hero_1793982013068),
                        contentDescription = "Study desk aesthetic",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        FocusBlack.copy(alpha = 0.90f),
                                        FocusBlack.copy(alpha = 0.65f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "DEEP WORK MODE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Eliminate phone distractions.\nExcel in your studies.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Large Circular Countdown Timer
        CircularCountdownTimer(
            remainingSeconds = remainingSeconds,
            totalSeconds = totalSeconds,
            sessionState = sessionState,
            subjectGoal = subjectGoal,
            shieldedAppsCount = shieldedAppsCount,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Controls (Goal, Presets 15/25/30/45/60/90, Start Study button)
        TimerControls(
            selectedDurationMinutes = selectedDuration,
            presetOptions = FocusSessionManager.PRESET_MINUTES,
            subjectGoal = subjectGoal,
            sessionState = sessionState,
            shieldedAppsCount = shieldedAppsCount,
            onDurationSelected = onDurationSelected,
            onSubjectGoalChanged = onSubjectGoalChanged,
            onOpenAppPicker = onOpenAppPicker,
            onStartStudy = onStartStudy,
            onPause = onPause,
            onResume = onResume,
            onStopSession = onStopSession
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
