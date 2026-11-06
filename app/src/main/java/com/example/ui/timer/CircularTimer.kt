package com.example.ui.timer

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SessionState
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FocusSurfaceContainer
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircularCountdownTimer(
    remainingSeconds: Int,
    totalSeconds: Int,
    sessionState: SessionState,
    subjectGoal: String,
    shieldedAppsCount: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (totalSeconds > 0) {
        (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 1f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "progress_anim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_anim"
    )

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeText = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = modifier
            .testTag("circular_timer_container"),
        contentAlignment = Alignment.Center
    ) {
        // Background subtle ambient glow
        if (sessionState == SessionState.RUNNING) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(pulseGlow)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                EmeraldPrimary.copy(alpha = 0.14f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Custom Canvas Drawing
        Canvas(
            modifier = Modifier
                .size(280.dp)
                .testTag("circular_timer_canvas")
        ) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = diameter / 2f

            // 1. Subtle Background Track
            drawArc(
                color = Color(0xFF10271B),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Subtle Minute Tick Marks around the outer rim
            val tickRadius = radius + 12.dp.toPx()
            for (i in 0 until 60 step 5) {
                val angleRad = Math.toRadians((i * 6 - 90).toDouble())
                val startX = center.x + (tickRadius - 6.dp.toPx()) * cos(angleRad).toFloat()
                val startY = center.y + (tickRadius - 6.dp.toPx()) * sin(angleRad).toFloat()
                val endX = center.x + tickRadius * cos(angleRad).toFloat()
                val endY = center.y + tickRadius * sin(angleRad).toFloat()
                val isMajor = i % 15 == 0
                drawLine(
                    color = if (isMajor) EmeraldPrimary.copy(alpha = 0.45f) else Color(0xFF163725),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isMajor) 2.5f else 1.5f
                )
            }

            // 3. Glowing Progress Arc
            val sweep = animatedProgress * 360f
            if (sweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to EmeraldPrimary,
                        0.5f to MintSecondary,
                        1.0f to EmeraldPrimary,
                        center = center
                    ),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Leading glowing indicator dot
                val leadingAngleRad = Math.toRadians((sweep - 90).toDouble())
                val dotX = center.x + radius * cos(leadingAngleRad).toFloat()
                val dotY = center.y + radius * sin(leadingAngleRad).toFloat()
                drawCircle(
                    color = MintSecondary,
                    radius = strokeWidth * 0.7f,
                    center = Offset(dotX, dotY)
                )
                drawCircle(
                    color = Color.White,
                    radius = strokeWidth * 0.35f,
                    center = Offset(dotX, dotY)
                )
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            // Status Pill
            Surface(
                color = when (sessionState) {
                    SessionState.RUNNING -> EmeraldPrimary.copy(alpha = 0.15f)
                    SessionState.PAUSED -> Color(0xFFFFA000).copy(alpha = 0.15f)
                    SessionState.COMPLETED -> MintSecondary.copy(alpha = 0.15f)
                    SessionState.IDLE -> FocusSurfaceContainer
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (sessionState) {
                            SessionState.RUNNING -> Icons.Default.Lock
                            SessionState.PAUSED -> Icons.Default.Lock
                            else -> Icons.Default.Shield
                        },
                        contentDescription = null,
                        tint = when (sessionState) {
                            SessionState.RUNNING -> EmeraldPrimary
                            SessionState.PAUSED -> Color(0xFFFFA000)
                            SessionState.COMPLETED -> MintSecondary
                            SessionState.IDLE -> TextMuted
                        },
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = when (sessionState) {
                            SessionState.RUNNING -> "FOCUS LOCKED"
                            SessionState.PAUSED -> "SESSION PAUSED"
                            SessionState.COMPLETED -> "COMPLETED"
                            SessionState.IDLE -> "STUDY SESSION"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (sessionState) {
                            SessionState.RUNNING -> EmeraldPrimary
                            SessionState.PAUSED -> Color(0xFFFFA000)
                            SessionState.COMPLETED -> MintSecondary
                            SessionState.IDLE -> TextSecondary
                        },
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Large Countdown Digits
            Text(
                text = timeText,
                fontSize = 54.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subject / Goal Tag
            Text(
                text = subjectGoal.ifBlank { "Deep Work Session" },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Shield status badge
            Surface(
                color = Color(0xFF0F261B),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "$shieldedAppsCount apps shielded",
                    fontSize = 11.sp,
                    color = EmeraldPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
