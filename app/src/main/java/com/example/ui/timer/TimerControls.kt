package com.example.ui.timer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SessionState
import com.example.ui.theme.BorderEmeraldBright
import com.example.ui.theme.BorderEmeraldSubtle
import com.example.ui.theme.DangerOnContainer
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerRedContainer
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
fun TimerControls(
    selectedDurationMinutes: Int,
    presetOptions: List<Int>,
    subjectGoal: String,
    sessionState: SessionState,
    shieldedAppsCount: Int,
    onDurationSelected: (Int) -> Unit,
    onSubjectGoalChanged: (String) -> Unit,
    onOpenAppPicker: () -> Unit,
    onStartStudy: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStopSession: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showGiveUpDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("timer_controls"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (sessionState == SessionState.IDLE) {
            // 1. Goal / Subject Input
            GoalInputSection(
                subjectGoal = subjectGoal,
                onSubjectGoalChanged = onSubjectGoalChanged
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Preset Duration Selector (15, 25, 30, 45, 60, 90 mins)
            PresetDurationSelector(
                presets = presetOptions,
                selected = selectedDurationMinutes,
                onSelect = onDurationSelected
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Shielded Apps Trigger Card
            ShieldedAppsBanner(
                count = shieldedAppsCount,
                onClick = onOpenAppPicker
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Primary "START STUDY" Button
            Button(
                onClick = onStartStudy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .testTag("start_study_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = EmeraldOnPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "START STUDY • $selectedDurationMinutes MIN",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        } else {
            // Active Session Controls (RUNNING or PAUSED)
            ActiveSessionActionButtons(
                sessionState = sessionState,
                onPause = onPause,
                onResume = onResume,
                onGiveUpRequest = { showGiveUpDialog = true }
            )
        }
    }

    // Give Up Confirmation Dialog
    if (showGiveUpDialog) {
        AlertDialog(
            onDismissRequest = { showGiveUpDialog = false },
            containerColor = FocusSurfaceContainerHigh,
            title = {
                Text(
                    text = "End Study Session?",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "You're in the middle of a focus block. Ending early will release the app shields and record an incomplete session.",
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showGiveUpDialog = false
                        onStopSession(false)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("End Session", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGiveUpDialog = false }) {
                    Text("Keep Focusing", color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

@Composable
fun GoalInputSection(
    subjectGoal: String,
    onSubjectGoalChanged: (String) -> Unit
) {
    val quickGoals = listOf(
        "🔬 Chemistry",
        "📐 Calculus",
        "💻 Coding",
        "📝 Essay",
        "📖 Reading",
        "🧠 Exam Prep"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "STUDY GOAL / SUBJECT",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )

        OutlinedTextField(
            value = subjectGoal,
            onValueChange = onSubjectGoalChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("goal_input_field"),
            placeholder = { Text("e.g. Master Organic Chem Chapter 4", color = TextMuted) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (subjectGoal.isNotEmpty()) {
                    IconButton(onClick = { onSubjectGoalChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = BorderEmeraldSubtle,
                focusedContainerColor = FocusSurfaceContainer,
                unfocusedContainerColor = FocusSurfaceContainer,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Pick Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickGoals.forEach { goalTag ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (subjectGoal == goalTag) EmeraldPrimaryContainer else FocusSurfaceContainer,
                    border = BorderStroke(
                        1.dp,
                        if (subjectGoal == goalTag) BorderEmeraldBright else BorderEmeraldSubtle
                    ),
                    modifier = Modifier.clickable {
                        onSubjectGoalChanged(goalTag)
                    }
                ) {
                    Text(
                        text = goalTag,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (subjectGoal == goalTag) EmeraldPrimary else TextSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PresetDurationSelector(
    presets: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DURATION PRESETS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
            Text(
                text = "$selected minutes",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presets.forEach { mins ->
                val isSelected = mins == selected
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) EmeraldPrimary else FocusSurfaceContainer,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) EmeraldPrimary else BorderEmeraldSubtle
                    ),
                    modifier = Modifier
                        .testTag("preset_$mins")
                        .clickable { onSelect(mins) }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "$mins",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) EmeraldOnPrimary else TextPrimary
                        )
                        Text(
                            text = "min",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) EmeraldOnPrimary.copy(alpha = 0.8f) else TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShieldedAppsBanner(
    count: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("shielded_apps_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FocusSurfaceContainer),
        border = BorderStroke(1.dp, BorderEmeraldSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(EmeraldPrimary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Distracting Apps Shield",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "$count apps will be locked on start",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = FocusSurfaceContainerHigh,
                border = BorderStroke(1.dp, BorderEmeraldSubtle)
            ) {
                Text(
                    text = "Configure",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MintSecondary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun ActiveSessionActionButtons(
    sessionState: SessionState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onGiveUpRequest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Pause / Resume Button
        Button(
            onClick = {
                if (sessionState == SessionState.RUNNING) onPause() else onResume()
            },
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .testTag("pause_resume_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (sessionState == SessionState.RUNNING) FocusSurfaceContainerHigh else EmeraldPrimary,
                contentColor = if (sessionState == SessionState.RUNNING) TextPrimary else EmeraldOnPrimary
            ),
            border = BorderStroke(1.dp, if (sessionState == SessionState.RUNNING) BorderEmeraldSubtle else EmeraldPrimary)
        ) {
            Icon(
                imageVector = if (sessionState == SessionState.RUNNING) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (sessionState == SessionState.RUNNING) "Pause" else "Resume",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Give up button
        OutlinedButton(
            onClick = onGiveUpRequest,
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .testTag("give_up_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
            border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "End Early",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
