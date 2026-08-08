package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun LanternLightSurround(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lanternGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GoldPrimary.copy(alpha = 0.18f * glowAlpha),
                        CardSurfaceVariant,
                        CardSurface
                    ),
                    radius = 900f
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(GoldBright, GoldPrimary.copy(alpha = 0.5f), CardBorder)
                ),
                shape = RoundedCornerShape(28.dp)
            )
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val centerTop = Offset(w / 2f, 0f)

            // Hanging String
            drawLine(
                color = GoldBright.copy(alpha = 0.7f),
                start = centerTop,
                end = Offset(w / 2f, 28f),
                strokeWidth = 2.5f
            )

            // Cap / Socket
            drawRect(
                color = GoldPrimary,
                topLeft = Offset(w / 2f - 10f, 28f),
                size = androidx.compose.ui.geometry.Size(20f, 10f)
            )

            // Lightbulb Body / Lantern Glass
            drawCircle(
                color = GoldBright.copy(alpha = glowAlpha),
                radius = 16f,
                center = Offset(w / 2f, 46f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = 8f,
                center = Offset(w / 2f, 46f)
            )

            // Radiating Rays around Lightbulb
            val rayLength = 24f
            val angles = listOf(-45, -15, 15, 45, 90, 135, 165)
            for (angle in angles) {
                val rad = Math.toRadians(angle.toDouble())
                val startX = (w / 2f + 18f * Math.cos(rad)).toFloat()
                val startY = (46f + 18f * Math.sin(rad)).toFloat()
                val endX = (w / 2f + (18f + rayLength) * Math.cos(rad)).toFloat()
                val endY = (46f + (18f + rayLength) * Math.sin(rad)).toFloat()

                drawLine(
                    color = GoldBright.copy(alpha = 0.6f * glowAlpha),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.5f
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}

@Composable
fun PrayerTimerDisplay(
    hours: Int,
    minutes: Int,
    seconds: Int,
    progress: Float = 0f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "timerPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Digital Timer Boxes
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            TimerBlock(value = hours, label = "ساعة")
            TimerSeparator()
            TimerBlock(value = minutes, label = "دقيقة")
            TimerSeparator()
            TimerBlock(value = seconds, label = "ثانية")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Timer Progress Line
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(6.dp)
                .clip(CircleShape)
                .background(CardSurfaceVariant)
                .border(0.5.dp, CardBorder, CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0.05f, 1f))
                    .clip(CircleShape)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(GoldPrimary, GoldBright)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(GoldBright.copy(alpha = pulseAlpha))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "جاري الحساب المباشر لموعد الأذان",
                fontSize = 11.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TimerBlock(value: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(width = 62.dp, height = 54.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(CardSurface)
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format("%02d", value),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GoldBright
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TimerSeparator() {
    Text(
        text = ":",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = GoldPrimary,
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
    )
}

@Composable
fun ShieldIcon(
    tint: Color = GoldPrimary,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.5f, h * 0.05f)
            cubicTo(w * 0.75f, h * 0.05f, w * 0.95f, h * 0.15f, w * 0.95f, h * 0.35f)
            cubicTo(w * 0.95f, h * 0.7f, w * 0.65f, h * 0.92f, w * 0.5f, h * 0.98f)
            cubicTo(w * 0.35f, h * 0.92f, w * 0.05f, h * 0.7f, w * 0.05f, h * 0.35f)
            cubicTo(w * 0.05f, h * 0.15f, w * 0.25f, h * 0.05f, w * 0.5f, h * 0.05f)
            close()
        }
        drawPath(
            path = path,
            color = tint,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = w * 0.09f)
        )
    }
}
