package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.InterFontFamily
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

// Core Color Constants - Vibrant & High Contrast
private val DarkSplashBg = Color(0xFF18181B) // Deep Zinc 900
private val LightSplashBg = Color(0xFFF1F5F9) // Slate 100 - clean, crisp surface

// Brand Gradients - Dark Mode (Neon Glow)
private val DarkCyan = Color(0xFF38BDF8)
private val DarkViolet = Color(0xFFA855F7)
private val DarkMagenta = Color(0xFFEC4899)

// Brand Gradients - Light Mode (High Contrast & Sharp)
private val LightCyan = Color(0xFF0284C7)
private val LightViolet = Color(0xFF7C3AED)
private val LightTextCharcoal = Color(0xFF0F172A) // Slate 900

// Subtle Ambient Drop Shadow for Light Mode depth
private val LightShadow = Color(0x24000000)

/**
 * High-performance, Theme-Aware Motion Splash Screen for T-Minus Mkt.
 * Features:
 * - Dynamic continuous gradient arc sweep on the stopwatch dial
 * - Subtle rocket thrust motion on the marketing growth arrow
 * - Radiant multi-layer glow in Dark Mode
 * - Razor-sharp, high-contrast gradient strokes in Light Mode
 * - 12-particle revolving orbital comet spinner with fading gradient trail
 */
@Composable
fun SplashScreen(
    isDarkMode: Boolean,
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 1600ms launch screen delay
    LaunchedEffect(Unit) {
        delay(1600L)
        onSplashFinished()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash_motion")

    // Dial trace angle rotation: continuous sweep from 0 to 360 degrees
    val dialSweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dial_sweep_angle"
    )

    // Breathing pulse for the entire emblem container
    val emblemScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emblem_scale"
    )

    // Marketing growth arrow dynamic thrust motion along the diagonal (+X, -Y)
    val arrowThrustFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrow_thrust"
    )

    // Spinner rotation angle (comet trail)
    val spinnerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinner_rotation"
    )

    val bgColor = if (isDarkMode) DarkSplashBg else LightSplashBg

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Stopwatch with Marketing Growth Arrow Emblem
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .scale(emblemScale)
                    .testTag("splash_brand_emblem"),
                contentAlignment = Alignment.Center
            ) {
                AnimatedStopwatchCanvas(
                    isDark = isDarkMode,
                    dialSweepAngle = dialSweepAngle,
                    arrowThrustFraction = arrowThrustFraction,
                    modifier = Modifier.size(180.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Brand Logotype Title ("T-Minus Mkt")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "T-Minus ",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 38.sp,
                    letterSpacing = (-0.5).sp,
                    color = if (isDarkMode) Color.White else LightTextCharcoal
                )

                Text(
                    text = "Mkt",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 38.sp,
                    letterSpacing = (-0.5).sp,
                    color = if (isDarkMode) DarkCyan else LightCyan
                )
            }

            Spacer(modifier = Modifier.height(80.dp))

            // High-Quality Orbital Comet Particle Spinner
            OrbitalCometSpinner(
                isDark = isDarkMode,
                rotationAngle = spinnerRotation,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("splash_loading_spinner")
            )
        }
    }
}

/**
 * Animated Canvas drawing the Stopwatch and Growth Arrow with motion:
 * - Plunger button on top with gradient fill
 * - Circular dial with continuous gradient arc sweep and secondary subtle static ring
 * - 4 dial tick marks (12, 3, 6, 9)
 * - Dynamic marketing growth arrow with animated thrust along the diagonal
 * - Rich glow layers in Dark Mode and crisp high-contrast strokes in Light Mode
 */
@Composable
private fun AnimatedStopwatchCanvas(
    isDark: Boolean,
    dialSweepAngle: Float,
    arrowThrustFraction: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val strokeWidth = w * 0.054f
        val center = Offset(w * 0.50f, h * 0.54f)
        val dialRadius = w * 0.355f

        // Plunger coordinates
        val plungerY = h * 0.12f
        val plungerHalf = w * 0.12f
        val stemTop = plungerY
        val stemBottom = center.y - dialRadius

        // Marketing Arrow dynamic thrust offset:
        // Slides slightly towards top-right and expands tip
        val maxThrust = w * 0.035f
        val thrustX = maxThrust * arrowThrustFraction
        val thrustY = -maxThrust * arrowThrustFraction

        val arrowTail = Offset(
            center.x - dialRadius * 0.56f + thrustX * 0.4f,
            center.y + dialRadius * 0.56f + thrustY * 0.4f
        )
        val arrowTip = Offset(
            center.x + dialRadius * 0.88f + thrustX,
            center.y - dialRadius * 0.88f + thrustY
        )
        val headSize = w * 0.20f + (w * 0.015f * arrowThrustFraction)

        // Palette setup based on theme
        val cyanColor = if (isDark) DarkCyan else LightCyan
        val violetColor = if (isDark) DarkViolet else LightViolet
        val dialGradient = Brush.linearGradient(
            colors = listOf(cyanColor, violetColor),
            start = Offset(0f, h),
            end = Offset(w, 0f)
        )
        val dialTrailGradient = Brush.sweepGradient(
            colors = listOf(
                cyanColor.copy(alpha = 0.25f),
                cyanColor,
                violetColor,
                cyanColor.copy(alpha = 0.25f)
            ),
            center = center
        )

        if (isDark) {
            // LAYER 1: Deep ambient diffuse bloom
            drawStopwatchParts(
                center = center,
                radius = dialRadius,
                stroke = strokeWidth * 2.4f,
                brush = Brush.linearGradient(
                    listOf(cyanColor.copy(alpha = 0.18f), violetColor.copy(alpha = 0.18f)),
                    start = Offset(0f, h),
                    end = Offset(w, 0f)
                ),
                plungerHalf = plungerHalf,
                plungerY = plungerY,
                stemTop = stemTop,
                stemBottom = stemBottom,
                arrowTail = arrowTail,
                arrowTip = arrowTip,
                headSize = headSize,
                drawTicks = false,
                sweepAngle = 360f,
                startAngle = 0f
            )

            // LAYER 2: Vibrant core glow
            drawStopwatchParts(
                center = center,
                radius = dialRadius,
                stroke = strokeWidth * 1.5f,
                brush = Brush.linearGradient(
                    listOf(cyanColor.copy(alpha = 0.42f), violetColor.copy(alpha = 0.42f)),
                    start = Offset(0f, h),
                    end = Offset(w, 0f)
                ),
                plungerHalf = plungerHalf,
                plungerY = plungerY,
                stemTop = stemTop,
                stemBottom = stemBottom,
                arrowTail = arrowTail,
                arrowTip = arrowTip,
                headSize = headSize,
                drawTicks = false,
                sweepAngle = 360f,
                startAngle = 0f
            )

            // LAYER 3: Main crisp structure with continuous sweep
            drawStopwatchParts(
                center = center,
                radius = dialRadius,
                stroke = strokeWidth,
                brush = dialGradient,
                plungerHalf = plungerHalf,
                plungerY = plungerY,
                stemTop = stemTop,
                stemBottom = stemBottom,
                arrowTail = arrowTail,
                arrowTip = arrowTip,
                headSize = headSize,
                drawTicks = true,
                cyanTick = cyanColor,
                violetTick = violetColor,
                sweepAngle = 360f,
                startAngle = 0f
            )

            // LAYER 4: Super-bright dynamic head highlight on the dial ring
            drawArc(
                brush = dialTrailGradient,
                startAngle = dialSweepAngle,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(center.x - dialRadius, center.y - dialRadius),
                size = androidx.compose.ui.geometry.Size(dialRadius * 2, dialRadius * 2),
                style = Stroke(width = strokeWidth * 1.15f, cap = StrokeCap.Round)
            )
        } else {
            // LIGHT MODE: High-Contrast, Vibrant & Crisp
            // 1. Soft subtle ambient shadow beneath for depth
            val shadowOffset = Offset(0f, w * 0.022f)
            drawStopwatchParts(
                center = center + shadowOffset,
                radius = dialRadius,
                stroke = strokeWidth * 1.25f,
                brush = Brush.linearGradient(listOf(LightShadow, LightShadow)),
                plungerHalf = plungerHalf,
                plungerY = plungerY + shadowOffset.y,
                stemTop = stemTop + shadowOffset.y,
                stemBottom = stemBottom + shadowOffset.y,
                arrowTail = arrowTail + shadowOffset,
                arrowTip = arrowTip + shadowOffset,
                headSize = headSize,
                drawTicks = false,
                sweepAngle = 360f,
                startAngle = 0f
            )

            // 2. Underlying subtle track ring
            drawCircle(
                color = Color(0xFFCBD5E1), // Slate 300
                radius = dialRadius,
                center = center,
                style = Stroke(width = strokeWidth * 0.85f, cap = StrokeCap.Round)
            )

            // 3. Main crisp vibrant gradient structure
            drawStopwatchParts(
                center = center,
                radius = dialRadius,
                stroke = strokeWidth,
                brush = dialGradient,
                plungerHalf = plungerHalf,
                plungerY = plungerY,
                stemTop = stemTop,
                stemBottom = stemBottom,
                arrowTail = arrowTail,
                arrowTip = arrowTip,
                headSize = headSize,
                drawTicks = true,
                cyanTick = cyanColor,
                violetTick = violetColor,
                sweepAngle = 360f,
                startAngle = 0f
            )

            // 4. Vibrant rotating highlight arc indicating active motion
            drawArc(
                brush = dialGradient,
                startAngle = dialSweepAngle,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(center.x - dialRadius, center.y - dialRadius),
                size = androidx.compose.ui.geometry.Size(dialRadius * 2, dialRadius * 2),
                style = Stroke(width = strokeWidth * 1.35f, cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Composite drawing method for the plunger, outer dial ring, tick marks, and growth arrow.
 */
private fun DrawScope.drawStopwatchParts(
    center: Offset,
    radius: Float,
    stroke: Float,
    brush: Brush,
    plungerHalf: Float,
    plungerY: Float,
    stemTop: Float,
    stemBottom: Float,
    arrowTail: Offset,
    arrowTip: Offset,
    headSize: Float,
    drawTicks: Boolean,
    cyanTick: Color = DarkCyan,
    violetTick: Color = DarkViolet,
    sweepAngle: Float = 360f,
    startAngle: Float = 0f
) {
    val strokeStyle = Stroke(
        width = stroke,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )

    // 1. Plunger Horizontal Bar & Vertical Stem
    drawLine(
        brush = brush,
        start = Offset(center.x - plungerHalf, plungerY),
        end = Offset(center.x + plungerHalf, plungerY),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
    drawLine(
        brush = brush,
        start = Offset(center.x, stemTop),
        end = Offset(center.x, stemBottom),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )

    // 2. Main Dial Ring Arc
    if (sweepAngle >= 360f) {
        drawCircle(
            brush = brush,
            radius = radius,
            center = center,
            style = strokeStyle
        )
    } else {
        drawArc(
            brush = brush,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = strokeStyle
        )
    }

    // 3. Dial Ticks at 12, 9, 6, 3 o'clock
    if (drawTicks) {
        val tickLen = radius * 0.17f
        val tickStroke = stroke * 0.55f

        // 12 o'clock (top)
        drawLine(
            color = cyanTick,
            start = Offset(center.x, center.y - radius + 4f),
            end = Offset(center.x, center.y - radius + 4f + tickLen),
            strokeWidth = tickStroke,
            cap = StrokeCap.Round
        )
        // 9 o'clock (left)
        drawLine(
            color = cyanTick,
            start = Offset(center.x - radius + 4f, center.y),
            end = Offset(center.x - radius + 4f + tickLen, center.y),
            strokeWidth = tickStroke,
            cap = StrokeCap.Round
        )
        // 6 o'clock (bottom)
        drawLine(
            color = violetTick,
            start = Offset(center.x, center.y + radius - 4f),
            end = Offset(center.x, center.y + radius - 4f - tickLen),
            strokeWidth = tickStroke,
            cap = StrokeCap.Round
        )
        // 3 o'clock (right)
        drawLine(
            color = violetTick,
            start = Offset(center.x + radius - 4f, center.y),
            end = Offset(center.x + radius - 4f - tickLen, center.y),
            strokeWidth = tickStroke,
            cap = StrokeCap.Round
        )
    }

    // 4. Marketing Growth Arrow
    // Diagonal shaft
    drawLine(
        brush = brush,
        start = arrowTail,
        end = arrowTip,
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )

    // Arrowhead (angled barbs pointing back from the tip)
    drawLine(
        brush = brush,
        start = Offset(arrowTip.x - headSize, arrowTip.y),
        end = arrowTip,
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
    drawLine(
        brush = brush,
        start = arrowTip,
        end = Offset(arrowTip.x, arrowTip.y + headSize),
        strokeWidth = stroke,
        cap = StrokeCap.Round
    )
}

/**
 * 12-particle Orbital Comet Spinner:
 * Revolves seamlessly at 60fps with a smooth trailing gradient fade,
 * rendering crisp, high-definition motion in both Dark and Light modes.
 */
@Composable
private fun OrbitalCometSpinner(
    isDark: Boolean,
    rotationAngle: Float,
    modifier: Modifier = Modifier
) {
    val cyanColor = if (isDark) DarkCyan else LightCyan
    val violetColor = if (isDark) DarkViolet else LightViolet

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val particleCount = 12

        for (i in 0 until particleCount) {
            // Fraction from tail (0.0) to head (1.0)
            val fraction = (i + 1f) / particleCount.toFloat()
            val angleDeg = (i * (360f / particleCount) + rotationAngle) % 360f
            val angleRad = Math.toRadians(angleDeg.toDouble())

            val dotX = center.x + (radius * 0.78f * cos(angleRad)).toFloat()
            val dotY = center.y + (radius * 0.78f * sin(angleRad)).toFloat()

            // Smooth cubic ease for trail opacity
            val alpha = (fraction * fraction).coerceIn(0.12f, 1f)
            val dotRadius = (radius * 0.17f) * (0.50f + 0.65f * fraction)

            // Gradient transition from cyan to violet along the trail
            val dotColor = if (i % 2 == 0) cyanColor else violetColor

            drawCircle(
                color = dotColor.copy(alpha = alpha),
                radius = dotRadius,
                center = Offset(dotX, dotY)
            )
        }
    }
}
