package com.example.ui.completion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.random.Random

@Composable
fun CompletionScreen(
    durationMinutes: Int,
    subjectGoal: String,
    shieldedAppsCount: Int,
    interceptedAttempts: Int,
    onStartNewSession: () -> Unit,
    onTakeBreak: () -> Unit,
    onSaveNotesAndFinish: (String) -> Unit
) {
    var notesText by remember { mutableStateOf("") }

    val quotes = listOf(
        "\"Focus is a muscle: the more you protect your deep work, the stronger your mind becomes.\" - Cal Newport",
        "\"Success is the sum of small efforts, repeated day in and day out.\" - Robert Collier",
        "\"Concentrate all your thoughts upon the work in hand. The sun's rays do not burn until brought to a focus.\" - Alexander Graham Bell",
        "\"Deep focus produces extraordinary work in ordinary time.\" - Student Mindset"
    )
    val randomQuote = remember { quotes.random() }

    // Confetti particles
    val particleCount = 28
    val particleY = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        particleY.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1800, easing = LinearEasing)
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("completion_screen"),
        color = FocusBlack
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Confetti canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val progress = particleY.value
                val colors = listOf(EmeraldPrimary, MintSecondary, Color(0xFF69F0AE), Color(0xFFFFD54F))
                val random = Random(42)
                for (i in 0 until particleCount) {
                    val startX = (random.nextFloat() * size.width)
                    val speed = 0.5f + random.nextFloat() * 0.8f
                    val y = (progress * size.height * speed) % size.height
                    val color = colors[i % colors.size]
                    val particleSize = 6.dp.toPx() + (random.nextFloat() * 6.dp.toPx())
                    drawCircle(
                        color = color.copy(alpha = (1f - (y / size.height)).coerceIn(0.2f, 1f)),
                        radius = particleSize / 2f,
                        center = Offset(startX, y)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(28.dp))

                // Trophy Badge
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    EmeraldPrimary.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(68.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Session Completed!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Congratulations! You locked out distractions and completed your study target.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Summary Stats Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = FocusSurfaceContainer),
                    border = BorderStroke(1.dp, BorderEmeraldSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "ACCOMPLISHMENT RECAP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = subjectGoal.ifBlank { "Deep Work Focus Block" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatBox(
                                label = "Time Focused",
                                value = "$durationMinutes min",
                                icon = Icons.Default.Star,
                                color = EmeraldPrimary
                            )
                            StatBox(
                                label = "Apps Shielded",
                                value = "$shieldedAppsCount apps",
                                icon = Icons.Default.Shield,
                                color = MintSecondary
                            )
                            StatBox(
                                label = "Resisted Urges",
                                value = "$interceptedAttempts times",
                                icon = Icons.Default.CheckCircle,
                                color = Color(0xFF69F0AE)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Reflection Note Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = FocusSurfaceContainerHigh),
                    border = BorderStroke(1.dp, BorderEmeraldSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Session Reflection / Notes",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = notesText,
                            onValueChange = { notesText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("completion_notes_input"),
                            placeholder = { Text("e.g. Finished Chapter 4 review and solved 12 practice questions", color = TextMuted) },
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = BorderEmeraldSubtle,
                                focusedContainerColor = FocusSurface,
                                unfocusedContainerColor = FocusSurface,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Motivational Quote
                Text(
                    text = randomQuote,
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Action Buttons
                Button(
                    onClick = { onSaveNotesAndFinish(notesText) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("save_and_done_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = EmeraldOnPrimary
                    )
                ) {
                    Text(
                        text = "Save Session & Return",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onTakeBreak,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("take_break_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BorderEmeraldSubtle)
                    ) {
                        Icon(Icons.Default.Coffee, contentDescription = null, tint = MintSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("5-Min Break", color = MintSecondary, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = onStartNewSession,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("new_session_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BorderEmeraldBright.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Session", color = EmeraldPrimary, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun StatBox(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextMuted
        )
    }
}
