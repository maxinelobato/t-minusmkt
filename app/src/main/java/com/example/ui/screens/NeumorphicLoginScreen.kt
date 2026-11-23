package com.example.ui.screens

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.components.GoogleLogo
import com.example.ui.components.neumorphicElevation
import com.example.ui.components.triggerInstantHaptic
import com.example.ui.theme.BrandCyanLight
import com.example.ui.theme.BrandGreenDark
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.Zinc400
import com.example.ui.theme.Zinc600
import kotlin.math.cos
import kotlin.math.sin

/**
 * Start Destination: Neumorphic Material 3 Login Screen strictly matching the
 * Light (#E4E4E7) and Dark (#27272A) reference screens:
 * - Sculpted Stopwatch-Arrow Logo (embossed soft-neumorphic in Light / glowing neon gradient in Dark)
 * - "T-Minus Mkt" wordmark + "Planejamento de Marketing" subtitle
 * - Pill-shaped raised Neumorphic "Sign in with Google" button (Google Auth exclusive)
 * - Bottom BiometricPrompt fingerprint unlock icon + "Acesso exclusivo Google" caption
 */
@Composable
fun NeumorphicLoginScreen(
    isPt: Boolean,
    isDarkMode: Boolean,
    hasExistingSession: Boolean,
    isAuthenticated: Boolean = false,
    userEmail: String? = null,
    timeDisplay: String = "14:00",
    amPmDisplay: String = "",
    dateDisplay: String = "Dom, 22 de Nov",
    activeTask: TaskEntity? = null,
    activeCampaignTitle: String? = null,
    tasks: List<TaskEntity> = emptyList(),
    onToggleTask: (id: Int, currentCompleted: Boolean) -> Unit = { _, _ -> },
    onGoogleSignInClick: () -> Unit,
    onBiometricUnlockClick: () -> Unit,
    onUnlockClick: () -> Unit,
    onToggleLanguage: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    onVoiceClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onLockClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalNeumorphicColors.current
    val context = LocalContext.current
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val infiniteTransition = rememberInfiniteTransition(label = "login_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(top = statusBarPadding, bottom = navBarPadding)
            .testTag("neumorphic_login_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 460.dp)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.weight(1.1f))

            // Top-Center Brand Block: Sculpted Stopwatch Logo + Wordmark + Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LoginStopwatchEmblem(
                    isDark = isDarkMode,
                    glowAlpha = glowPulse,
                    modifier = Modifier
                        .size(138.dp)
                        .testTag("login_brand_emblem")
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Wordmark: "T-Minus Mkt"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "T-Minus ",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp,
                        letterSpacing = (-1.0).sp,
                        color = if (isDarkMode) Color.White else colors.textPrimary
                    )
                    Text(
                        text = "Mkt",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp,
                        letterSpacing = (-1.0).sp,
                        color = if (isDarkMode) BrandGreenDark else BrandCyanLight
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isPt) "Planejamento de Marketing" else "Marketing Planning",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    letterSpacing = 0.1.sp,
                    color = if (isDarkMode) Zinc400 else Zinc600
                )
            }

            Spacer(modifier = Modifier.height(54.dp))

            // Pill-Shaped Raised Neumorphic "Sign in with Google" Button (Exclusive Google Auth)
            val googleInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .height(64.dp)
                    .neumorphicElevation(
                        cornerRadius = 32.dp,
                        backgroundColor = colors.surface,
                        lightShadowColor = colors.lightShadow,
                        darkShadowColor = colors.darkShadow,
                        offset = 6.dp,
                        blur = 14.dp
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .clickable(
                        interactionSource = googleInteraction,
                        indication = ripple(bounded = true),
                        onClick = {
                            triggerInstantHaptic(context)
                            onGoogleSignInClick()
                        }
                    )
                    .testTag("google_sign_in_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    GoogleLogo(size = 24.dp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = if (isPt) "Entrar com o Google" else "Sign in with Google",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 17.sp,
                        color = if (isDarkMode) Color.White else colors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1.3f))

            // Bottom BiometricPrompt Fingerprint Unlock & Exclusive Google Notice
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 28.dp)
            ) {
                val bioInteraction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = bioInteraction,
                            indication = ripple(bounded = false, radius = 28.dp),
                            onClick = {
                                triggerInstantHaptic(context)
                                onBiometricUnlockClick()
                            }
                        )
                        .testTag("biometric_unlock_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDarkMode) {
                        Canvas(modifier = Modifier.size(56.dp)) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.20f * glowPulse),
                                        Color.Transparent
                                    )
                                ),
                                radius = size.minDimension / 2f
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = if (isPt) "Desbloquear com biometria" else "Unlock with biometrics",
                        tint = if (isDarkMode) Color(0xFFD4D4D8) else Color(0xFF3F3F46),
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isPt) "Acesso exclusivo Google" else "Exclusive Google access",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = if (isDarkMode) Zinc400 else Zinc600
                )
            }
        }
    }
}

/**
 * Sculpted T-Minus Mkt Stopwatch-Arrow Logo:
 * - Light Mode: Soft embossed white/zinc neumorphic relief with dual shadows
 * - Dark Mode: Luminous cyan-to-magenta neon glow outline with internal tick marks
 */
@Composable
private fun LoginStopwatchEmblem(
    isDark: Boolean,
    glowAlpha: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val center = Offset(w * 0.50f, h * 0.55f)
        val radius = w * 0.34f
        val strokeW = w * 0.055f

        val plungerY = h * 0.13f
        val plungerHalf = w * 0.11f
        val stemBottom = center.y - radius

        // Side crown button at top-left (~-135 deg)
        val sideCrownAngle = Math.toRadians(-135.0)
        val sideStart = Offset(
            center.x + (radius * cos(sideCrownAngle)).toFloat(),
            center.y + (radius * sin(sideCrownAngle)).toFloat()
        )
        val sideEnd = Offset(
            center.x + ((radius + w * 0.06f) * cos(sideCrownAngle)).toFloat(),
            center.y + ((radius + w * 0.06f) * sin(sideCrownAngle)).toFloat()
        )

        if (isDark) {
            val neonBrush = Brush.linearGradient(
                colors = listOf(NeonCyan, Color(0xFF818CF8), NeonMagenta),
                start = Offset(w * 0.1f, h * 0.85f),
                end = Offset(w * 0.9f, h * 0.15f)
            )
            // Soft outer neon bloom via native BlurMaskFilter
            drawIntoCanvas { canvas ->
                val native = canvas.nativeCanvas
                val glowCyan = android.graphics.Paint().apply {
                    color = NeonCyan.copy(alpha = 0.45f * glowAlpha).toArgb()
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = strokeW * 2.4f
                    isAntiAlias = true
                    maskFilter = BlurMaskFilter(14.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                }
                native.drawCircle(center.x - w * 0.04f, center.y + h * 0.02f, radius, glowCyan)

                val glowPink = android.graphics.Paint().apply {
                    color = NeonMagenta.copy(alpha = 0.45f * glowAlpha).toArgb()
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = strokeW * 2.4f
                    isAntiAlias = true
                    maskFilter = BlurMaskFilter(14.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                }
                native.drawCircle(center.x + w * 0.04f, center.y - h * 0.02f, radius, glowPink)
            }

            drawStopwatchOutline(
                center = center,
                radius = radius,
                stroke = strokeW * 0.72f,
                brush = neonBrush,
                plungerY = plungerY,
                plungerHalf = plungerHalf,
                stemBottom = stemBottom,
                sideStart = sideStart,
                sideEnd = sideEnd,
                drawDialTicks = true,
                drawTraversalArrow = true
            )
        } else {
            // LIGHT MODE: Soft embossed neumorphic stopwatch matching reference
            val shadowOffset = Offset(w * 0.028f, h * 0.028f)
            val highlightOffset = Offset(-w * 0.024f, -h * 0.024f)

            // Bottom-right soft shadow
            drawStopwatchOutline(
                center = center + shadowOffset,
                radius = radius,
                stroke = strokeW * 1.15f,
                brush = Brush.linearGradient(listOf(Color(0xFFBEBEC8), Color(0xFFBEBEC8))),
                plungerY = plungerY + shadowOffset.y,
                plungerHalf = plungerHalf,
                stemBottom = stemBottom + shadowOffset.y,
                sideStart = sideStart + shadowOffset,
                sideEnd = sideEnd + shadowOffset,
                drawDialTicks = false,
                drawTraversalArrow = false
            )

            // Top-left bright highlight
            drawStopwatchOutline(
                center = center + highlightOffset,
                radius = radius,
                stroke = strokeW * 1.10f,
                brush = Brush.linearGradient(listOf(Color.White, Color.White)),
                plungerY = plungerY + highlightOffset.y,
                plungerHalf = plungerHalf,
                stemBottom = stemBottom + highlightOffset.y,
                sideStart = sideStart + highlightOffset,
                sideEnd = sideEnd + highlightOffset,
                drawDialTicks = false,
                drawTraversalArrow = false
            )

            // Main sculpted surface body
            drawStopwatchOutline(
                center = center,
                radius = radius,
                stroke = strokeW,
                brush = Brush.linearGradient(listOf(Color(0xFFF0F0F3), Color(0xFFE6E6EA))),
                plungerY = plungerY,
                plungerHalf = plungerHalf,
                stemBottom = stemBottom,
                sideStart = sideStart,
                sideEnd = sideEnd,
                drawDialTicks = false,
                drawTraversalArrow = false
            )
        }
    }
}

private fun DrawScope.drawStopwatchOutline(
    center: Offset,
    radius: Float,
    stroke: Float,
    brush: Brush,
    plungerY: Float,
    plungerHalf: Float,
    stemBottom: Float,
    sideStart: Offset,
    sideEnd: Offset,
    drawDialTicks: Boolean,
    drawTraversalArrow: Boolean
) {
    val style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Top plunger bar + stem
    drawLine(brush, Offset(center.x - plungerHalf, plungerY), Offset(center.x + plungerHalf, plungerY), stroke, StrokeCap.Round)
    drawLine(brush, Offset(center.x, plungerY), Offset(center.x, stemBottom), stroke, StrokeCap.Round)

    // Side crown button
    drawLine(brush, sideStart, sideEnd, stroke * 0.95f, StrokeCap.Round)

    // Main circular body
    drawCircle(brush = brush, radius = radius, center = center, style = style)

    if (drawDialTicks) {
        val tickCount = 8
        for (i in 0 until tickCount) {
            val angle = Math.toRadians(i * 45.0)
            val innerR = radius * 0.72f
            val outerR = radius * 0.84f
            drawLine(
                brush = brush,
                start = Offset(center.x + (innerR * cos(angle)).toFloat(), center.y + (innerR * sin(angle)).toFloat()),
                end = Offset(center.x + (outerR * cos(angle)).toFloat(), center.y + (outerR * sin(angle)).toFloat()),
                strokeWidth = stroke * 0.55f,
                cap = StrokeCap.Round
            )
        }
    }

    if (drawTraversalArrow) {
        // Dark Mode diagonal growth arrow traversing the dial up-right
        val tail = Offset(center.x - radius * 0.62f, center.y + radius * 0.52f)
        val tip = Offset(center.x + radius * 1.02f, center.y - radius * 0.92f)
        val head = radius * 0.42f
        drawLine(brush, tail, tip, stroke * 1.1f, StrokeCap.Round)
        drawLine(brush, Offset(tip.x - head, tip.y + head * 0.14f), tip, stroke * 1.05f, StrokeCap.Round)
        drawLine(brush, tip, Offset(tip.x - head * 0.14f, tip.y + head), stroke * 1.05f, StrokeCap.Round)
    } else {
        // Light Mode sculpted inner hand arrow from center pointing northeast
        drawCircle(brush = brush, radius = stroke * 0.72f, center = center)
        val tip = Offset(center.x + radius * 0.45f, center.y - radius * 0.45f)
        val head = radius * 0.24f
        drawLine(brush, center, tip, stroke * 0.95f, StrokeCap.Round)
        drawLine(brush, Offset(tip.x - head, tip.y), tip, stroke * 0.95f, StrokeCap.Round)
        drawLine(brush, tip, Offset(tip.x, tip.y + head), stroke * 0.95f, StrokeCap.Round)
    }
}
