package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Authentic 4-color Google "G" logo for exclusive Google Sign-In.
 */
@Composable
fun GoogleLogo(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeW = 3.6.dp.toPx()
            val stroke = Stroke(width = strokeW, cap = StrokeCap.Square)
            val r = (this.size.width - strokeW) / 2f
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f
            val rect = Rect(cx - r, cy - r, cx + r, cy + r)

            // Red segment (top)
            val redPath = Path().apply {
                arcTo(rect, startAngleDegrees = 200f, sweepAngleDegrees = 115f, forceMoveTo = false)
            }
            drawPath(redPath, Color(0xFFEA4335), style = stroke)

            // Yellow segment (bottom-left)
            val yellowPath = Path().apply {
                arcTo(rect, startAngleDegrees = 120f, sweepAngleDegrees = 80f, forceMoveTo = false)
            }
            drawPath(yellowPath, Color(0xFFFBBC05), style = stroke)

            // Green segment (bottom-right)
            val greenPath = Path().apply {
                arcTo(rect, startAngleDegrees = 45f, sweepAngleDegrees = 75f, forceMoveTo = false)
            }
            drawPath(greenPath, Color(0xFF34A853), style = stroke)

            // Blue segment and horizontal inward bar (right)
            val bluePath = Path().apply {
                arcTo(rect, startAngleDegrees = 315f, sweepAngleDegrees = 50f, forceMoveTo = false)
                moveTo(cx + r, cy)
                lineTo(cx, cy)
            }
            drawPath(bluePath, Color(0xFF4285F4), style = stroke)
        }
    }
}
