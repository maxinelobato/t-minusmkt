package com.example.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Renders an outlined Phosphor-style icon with an optional neon glow halo.
 */
@Composable
fun PhosphorIcon(
    name: String,
    tint: Color,
    modifier: Modifier = Modifier,
    glowColor: Color? = null,
    size: Dp = 24.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 1.75.dp.toPx()
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)

            // Draw neon glow underneath if enabled
            if (glowColor != null) {
                drawIntoCanvas { canvas ->
                    val nativeCanvas = canvas.nativeCanvas
                    val glowPaint = android.graphics.Paint().apply {
                        color = glowColor.copy(alpha = 0.85f).toArgb()
                        this.strokeWidth = strokeWidth * 2.8f
                        style = android.graphics.Paint.Style.STROKE
                        strokeCap = android.graphics.Paint.Cap.ROUND
                        strokeJoin = android.graphics.Paint.Join.ROUND
                        isAntiAlias = true
                        maskFilter = BlurMaskFilter(7.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                    }
                    val path = getIconPath(name, this.size.width, this.size.height)
                    nativeCanvas.drawPath(path.asAndroidPath(), glowPaint)
                }
            }

            // Crisp front icon path
            val path = getIconPath(name, this.size.width, this.size.height)
            drawPath(path = path, color = tint, style = stroke)

            // Extra decorative details per icon (e.g. document lines or rocket details)
            drawIconDetails(name, this.size.width, this.size.height, tint, stroke)
        }
    }
}

private fun getIconPath(name: String, w: Float, h: Float): Path {
    val path = Path()
    when (name.lowercase()) {
        "magnifyingglass", "search" -> {
            // Circle with 45 degree handle
            val cx = w * 0.42f
            val cy = h * 0.42f
            val radius = w * 0.30f
            path.addOval(Rect(cx - radius, cy - radius, cx + radius, cy + radius))
            path.moveTo(cx + radius * 0.707f, cy + radius * 0.707f)
            path.lineTo(w * 0.88f, h * 0.88f)
        }
        "article", "list" -> {
            // Document with folded corner or clean rounded card
            val left = w * 0.20f
            val top = h * 0.14f
            val right = w * 0.80f
            val bottom = h * 0.86f
            val r = w * 0.10f
            path.addRoundRect(RoundRect(Rect(left, top, right, bottom), CornerRadius(r, r)))
        }
        "megaphone" -> {
            // Megaphone cone and handle
            path.moveTo(w * 0.20f, h * 0.38f)
            path.lineTo(w * 0.46f, h * 0.38f)
            path.lineTo(w * 0.78f, h * 0.20f)
            path.lineTo(w * 0.78f, h * 0.80f)
            path.lineTo(w * 0.46f, h * 0.62f)
            path.lineTo(w * 0.20f, h * 0.62f)
            path.close()
            // Handle
            path.moveTo(w * 0.30f, h * 0.62f)
            path.lineTo(w * 0.30f, h * 0.82f)
            path.lineTo(w * 0.42f, h * 0.82f)
            path.lineTo(w * 0.42f, h * 0.62f)
        }
        "checkcircle", "check" -> {
            // Circle with check inside
            val cx = w * 0.50f
            val cy = h * 0.50f
            val r = w * 0.40f
            path.addOval(Rect(cx - r, cy - r, cx + r, cy + r))
            path.moveTo(w * 0.32f, h * 0.52f)
            path.lineTo(w * 0.46f, h * 0.65f)
            path.lineTo(w * 0.70f, h * 0.38f)
        }
        "rocketlaunch", "rocket" -> {
            // Angled rocket fuselage pointing to top-right (35 deg)
            path.moveTo(w * 0.82f, h * 0.18f) // Nose tip
            // Right wing & body
            path.cubicTo(w * 0.76f, h * 0.36f, w * 0.68f, h * 0.54f, w * 0.58f, h * 0.64f)
            path.lineTo(w * 0.66f, h * 0.78f) // Right fin
            path.lineTo(w * 0.50f, h * 0.74f)
            path.lineTo(w * 0.42f, h * 0.82f) // Engine base
            path.lineTo(w * 0.36f, h * 0.66f)
            path.lineTo(w * 0.22f, h * 0.58f) // Left fin
            path.lineTo(w * 0.36f, h * 0.50f)
            // Left body back to tip
            path.cubicTo(w * 0.46f, h * 0.40f, w * 0.64f, h * 0.24f, w * 0.82f, h * 0.18f)
            path.close()
        }
        "microphone", "mic" -> {
            // Mic pill and cradle
            val left = w * 0.36f
            val top = h * 0.18f
            val right = w * 0.64f
            val bottom = h * 0.58f
            val r = (right - left) / 2
            path.addRoundRect(RoundRect(Rect(left, top, right, bottom), CornerRadius(r, r)))
            // Cradle arc
            path.moveTo(w * 0.24f, h * 0.46f)
            path.cubicTo(w * 0.24f, h * 0.74f, w * 0.76f, h * 0.74f, w * 0.76f, h * 0.46f)
            // Base stand
            path.moveTo(w * 0.50f, h * 0.74f)
            path.lineTo(w * 0.50f, h * 0.86f)
            path.moveTo(w * 0.34f, h * 0.86f)
            path.lineTo(w * 0.66f, h * 0.86f)
        }
        "bell", "notification" -> {
            // Bell body and clapper
            path.moveTo(w * 0.50f, h * 0.18f)
            path.cubicTo(w * 0.34f, h * 0.18f, w * 0.26f, h * 0.42f, w * 0.24f, h * 0.68f)
            path.lineTo(w * 0.16f, h * 0.76f)
            path.lineTo(w * 0.84f, h * 0.76f)
            path.lineTo(w * 0.76f, h * 0.68f)
            path.cubicTo(w * 0.74f, h * 0.42f, w * 0.66f, h * 0.18f, w * 0.50f, h * 0.18f)
            path.close()
            // Clapper
            path.moveTo(w * 0.42f, h * 0.82f)
            path.cubicTo(w * 0.42f, h * 0.88f, w * 0.58f, h * 0.88f, w * 0.58f, h * 0.82f)
        }
        "moon" -> {
            // Crescent moon
            path.moveTo(w * 0.74f, h * 0.20f)
            path.cubicTo(w * 0.40f, h * 0.24f, w * 0.30f, h * 0.68f, w * 0.58f, h * 0.84f)
            path.cubicTo(w * 0.80f, h * 0.80f, w * 0.86f, h * 0.58f, w * 0.82f, h * 0.46f)
            path.cubicTo(w * 0.74f, h * 0.52f, w * 0.64f, h * 0.48f, w * 0.64f, h * 0.38f)
            path.cubicTo(w * 0.64f, h * 0.28f, w * 0.70f, h * 0.22f, w * 0.74f, h * 0.20f)
            path.close()
        }
        "sun" -> {
            // Sun center circle
            val cx = w * 0.50f
            val cy = h * 0.50f
            val r = w * 0.22f
            path.addOval(Rect(cx - r, cy - r, cx + r, cy + r))
            // 8 Rays
            val rayLen = w * 0.12f
            val rayDist = w * 0.32f
            val angles = listOf(0.0, 45.0, 90.0, 135.0, 180.0, 225.0, 270.0, 315.0)
            for (deg in angles) {
                val rad = Math.toRadians(deg)
                val cos = Math.cos(rad).toFloat()
                val sin = Math.sin(rad).toFloat()
                path.moveTo(cx + cos * rayDist, cy + sin * rayDist)
                path.lineTo(cx + cos * (rayDist + rayLen), cy + sin * (rayDist + rayLen))
            }
        }
        "lock" -> {
            // Lock body
            val left = w * 0.28f
            val top = h * 0.46f
            val right = w * 0.72f
            val bottom = h * 0.84f
            path.addRoundRect(RoundRect(Rect(left, top, right, bottom), CornerRadius(w * 0.08f, w * 0.08f)))
            // Shackle
            path.moveTo(w * 0.36f, h * 0.46f)
            path.lineTo(w * 0.36f, h * 0.30f)
            path.cubicTo(w * 0.36f, h * 0.16f, w * 0.64f, h * 0.16f, w * 0.64f, h * 0.30f)
            path.lineTo(w * 0.64f, h * 0.46f)
        }
        "share" -> {
            // Share nodes & links
            val r = w * 0.10f
            path.addOval(Rect(w * 0.74f - r, h * 0.26f - r, w * 0.74f + r, h * 0.26f + r))
            path.addOval(Rect(w * 0.26f - r, h * 0.50f - r, w * 0.26f + r, h * 0.50f + r))
            path.addOval(Rect(w * 0.74f - r, h * 0.74f - r, w * 0.74f + r, h * 0.74f + r))
            path.moveTo(w * 0.34f, h * 0.46f)
            path.lineTo(w * 0.66f, h * 0.30f)
            path.moveTo(w * 0.34f, h * 0.54f)
            path.lineTo(w * 0.66f, h * 0.70f)
        }
        "calendar", "calendar-plus", "calendar_plus" -> {
            // Calendar body
            val left = w * 0.18f
            val top = h * 0.24f
            val right = w * 0.82f
            val bottom = h * 0.84f
            path.addRoundRect(RoundRect(Rect(left, top, right, bottom), CornerRadius(w * 0.08f, w * 0.08f)))
            // Top binder rings
            path.moveTo(w * 0.34f, h * 0.14f)
            path.lineTo(w * 0.34f, h * 0.26f)
            path.moveTo(w * 0.66f, h * 0.14f)
            path.lineTo(w * 0.66f, h * 0.26f)
            // Plus mark inside calendar
            path.moveTo(w * 0.50f, h * 0.50f)
            path.lineTo(w * 0.50f, h * 0.72f)
            path.moveTo(w * 0.39f, h * 0.61f)
            path.lineTo(w * 0.61f, h * 0.61f)
        }
        "google" -> {
            // Google G outline
            path.moveTo(w * 0.84f, h * 0.50f)
            path.lineTo(w * 0.50f, h * 0.50f)
            val r = w * 0.36f
            path.arcTo(
                rect = Rect(w * 0.50f - r, h * 0.50f - r, w * 0.50f + r, h * 0.50f + r),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 300f,
                forceMoveTo = false
            )
        }
        "plus", "add" -> {
            path.moveTo(w * 0.50f, h * 0.22f)
            path.lineTo(w * 0.50f, h * 0.78f)
            path.moveTo(w * 0.22f, h * 0.50f)
            path.lineTo(w * 0.78f, h * 0.50f)
        }
        "close", "x" -> {
            path.moveTo(w * 0.28f, h * 0.28f)
            path.lineTo(w * 0.72f, h * 0.72f)
            path.moveTo(w * 0.72f, h * 0.28f)
            path.lineTo(w * 0.28f, h * 0.72f)
        }
        "arrowright", "arrow-right", "arrow_right" -> {
            path.moveTo(w * 0.20f, h * 0.50f)
            path.lineTo(w * 0.80f, h * 0.50f)
            path.moveTo(w * 0.56f, h * 0.26f)
            path.lineTo(w * 0.80f, h * 0.50f)
            path.lineTo(w * 0.56f, h * 0.74f)
        }
        "shield" -> {
            path.moveTo(w * 0.20f, h * 0.22f)
            path.lineTo(w * 0.80f, h * 0.22f)
            path.cubicTo(w * 0.80f, h * 0.55f, w * 0.65f, h * 0.78f, w * 0.50f, h * 0.88f)
            path.cubicTo(w * 0.35f, h * 0.78f, w * 0.20f, h * 0.55f, w * 0.20f, h * 0.22f)
            path.close()
        }
        "faceid", "scan" -> {
            // Corner brackets
            path.moveTo(w * 0.36f, h * 0.20f)
            path.lineTo(w * 0.20f, h * 0.20f)
            path.lineTo(w * 0.20f, h * 0.36f)

            path.moveTo(w * 0.64f, h * 0.20f)
            path.lineTo(w * 0.80f, h * 0.20f)
            path.lineTo(w * 0.80f, h * 0.36f)

            path.moveTo(w * 0.20f, h * 0.64f)
            path.lineTo(w * 0.20f, h * 0.80f)
            path.lineTo(w * 0.36f, h * 0.80f)

            path.moveTo(w * 0.80f, h * 0.64f)
            path.lineTo(w * 0.80f, h * 0.80f)
            path.lineTo(w * 0.64f, h * 0.80f)
        }
        "fingerprint" -> {
            // Concentric biometric ridge loops
            val cx = w * 0.50f
            val cy = h * 0.52f
            // Outer arch
            path.arcTo(
                rect = Rect(cx - w * 0.36f, cy - h * 0.38f, cx + w * 0.36f, cy + h * 0.20f),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            // Mid arch
            path.arcTo(
                rect = Rect(cx - w * 0.24f, cy - h * 0.26f, cx + w * 0.24f, cy + h * 0.16f),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = true
            )
            // Inner whorl
            path.arcTo(
                rect = Rect(cx - w * 0.12f, cy - h * 0.14f, cx + w * 0.12f, cy + h * 0.12f),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = true
            )
        }
        else -> {
            // Fallback circle
            path.addOval(Rect(w * 0.2f, h * 0.2f, w * 0.8f, h * 0.8f))
        }
    }
    return path
}

private fun DrawScope.drawIconDetails(name: String, w: Float, h: Float, tint: Color, stroke: Stroke) {
    when (name.lowercase()) {
        "article", "list" -> {
            // Draw horizontal document text lines
            drawLine(tint, Offset(w * 0.32f, h * 0.36f), Offset(w * 0.68f, h * 0.36f), stroke.width)
            drawLine(tint, Offset(w * 0.32f, h * 0.50f), Offset(w * 0.68f, h * 0.50f), stroke.width)
            drawLine(tint, Offset(w * 0.32f, h * 0.64f), Offset(w * 0.52f, h * 0.64f), stroke.width)
        }
        "calendar", "calendar-plus", "calendar_plus" -> {
            drawLine(tint, Offset(w * 0.18f, h * 0.40f), Offset(w * 0.82f, h * 0.40f), stroke.width)
        }
        "megaphone" -> {
            // Megaphone sound waves
            drawLine(tint, Offset(w * 0.86f, h * 0.36f), Offset(w * 0.94f, h * 0.30f), stroke.width)
            drawLine(tint, Offset(w * 0.88f, h * 0.50f), Offset(w * 0.96f, h * 0.50f), stroke.width)
            drawLine(tint, Offset(w * 0.86f, h * 0.64f), Offset(w * 0.94f, h * 0.70f), stroke.width)
        }
        "rocketlaunch", "rocket" -> {
            // Rocket window porthole and thrust trails
            drawCircle(
                color = tint,
                radius = w * 0.08f,
                center = Offset(w * 0.60f, h * 0.40f),
                style = stroke
            )
            drawLine(tint, Offset(w * 0.28f, h * 0.78f), Offset(w * 0.16f, h * 0.90f), stroke.width)
            drawLine(tint, Offset(w * 0.34f, h * 0.84f), Offset(w * 0.24f, h * 0.94f), stroke.width)
            drawLine(tint, Offset(w * 0.40f, h * 0.90f), Offset(w * 0.32f, h * 0.98f), stroke.width)
        }
        "lock" -> {
            // Keyhole
            drawCircle(
                color = tint,
                radius = w * 0.04f,
                center = Offset(w * 0.50f, h * 0.62f)
            )
            drawLine(tint, Offset(w * 0.50f, h * 0.64f), Offset(w * 0.50f, h * 0.72f), stroke.width)
        }
    }
}
