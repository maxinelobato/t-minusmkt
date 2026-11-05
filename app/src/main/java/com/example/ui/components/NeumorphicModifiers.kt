package com.example.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Applies an extruded Neumorphic elevation to a composable using dual top-left
 * highlight and bottom-right dark drop shadows, followed by the surface fill.
 */
fun Modifier.neumorphicElevation(
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color,
    lightShadowColor: Color,
    darkShadowColor: Color,
    offset: Dp = 4.dp,
    blur: Dp = 8.dp
): Modifier = this.drawBehind {
    val cornerPx = cornerRadius.toPx()
    val offsetPx = offset.toPx()
    val blurPx = blur.toPx().coerceAtLeast(1f)

    drawIntoCanvas { canvas ->
        val nativeCanvas = canvas.nativeCanvas

        // 1. Top-left light highlight shadow
        val lightPaint = android.graphics.Paint().apply {
            color = lightShadowColor.toArgb()
            isAntiAlias = true
            maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
        }
        val lightRect = android.graphics.RectF(
            -offsetPx,
            -offsetPx,
            size.width - offsetPx,
            size.height - offsetPx
        )
        nativeCanvas.drawRoundRect(lightRect, cornerPx, cornerPx, lightPaint)

        // 2. Bottom-right dark drop shadow
        val darkPaint = android.graphics.Paint().apply {
            color = darkShadowColor.toArgb()
            isAntiAlias = true
            maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
        }
        val darkRect = android.graphics.RectF(
            offsetPx,
            offsetPx,
            size.width + offsetPx,
            size.height + offsetPx
        )
        nativeCanvas.drawRoundRect(darkRect, cornerPx, cornerPx, darkPaint)
    }

    // 3. Crisp surface body
    drawRoundRect(
        color = backgroundColor,
        topLeft = Offset.Zero,
        size = size,
        cornerRadius = CornerRadius(cornerPx, cornerPx)
    )
}

/**
 * Applies an Inset (depressed / recessed) Neumorphic appearance, perfect for tactile
 * checkboxes, pressed buttons, and recessed slots.
 */
fun Modifier.neumorphicInset(
    cornerRadius: Dp = 8.dp,
    backgroundColor: Color,
    darkShadowColor: Color,
    lightShadowColor: Color,
    offset: Dp = 2.5.dp,
    blur: Dp = 4.dp
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .drawWithContent {
        val cornerPx = cornerRadius.toPx()
        val offsetPx = offset.toPx()
        val blurPx = blur.toPx().coerceAtLeast(1f)

        // Fill background
        drawRoundRect(
            color = backgroundColor,
            cornerRadius = CornerRadius(cornerPx, cornerPx)
        )

        val path = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(Offset.Zero, size),
                    cornerRadius = CornerRadius(cornerPx, cornerPx)
                )
            )
        }

        clipPath(path) {
            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas

                // Dark inner shadow creeping from top-left
                val darkPaint = android.graphics.Paint().apply {
                    color = darkShadowColor.toArgb()
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = offsetPx * 2.2f
                    isAntiAlias = true
                    maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
                }
                val darkRect = android.graphics.RectF(
                    -offsetPx * 0.4f,
                    -offsetPx * 0.4f,
                    size.width + offsetPx,
                    size.height + offsetPx
                )
                nativeCanvas.drawRoundRect(darkRect, cornerPx, cornerPx, darkPaint)

                // Light inner highlight creeping from bottom-right
                val lightPaint = android.graphics.Paint().apply {
                    color = lightShadowColor.toArgb()
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = offsetPx * 2.0f
                    isAntiAlias = true
                    maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
                }
                val lightRect = android.graphics.RectF(
                    -offsetPx,
                    -offsetPx,
                    size.width + offsetPx * 0.5f,
                    size.height + offsetPx * 0.5f
                )
                nativeCanvas.drawRoundRect(lightRect, cornerPx, cornerPx, lightPaint)
            }
        }

        // Render foreground content inside the recessed area
        drawContent()
    }

/**
 * Applies a glowing neon border and radiant halo for Dark Mode elements (e.g. Countdown header).
 */
fun Modifier.neonGlow(
    color: Color,
    cornerRadius: Dp = 24.dp,
    glowRadius: Dp = 12.dp,
    borderWidth: Dp = 1.5.dp
): Modifier = this.drawBehind {
    val cornerPx = cornerRadius.toPx()
    val glowPx = glowRadius.toPx()

    drawIntoCanvas { canvas ->
        val nativeCanvas = canvas.nativeCanvas
        val glowPaint = android.graphics.Paint().apply {
            this.color = color.copy(alpha = 0.55f).toArgb()
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = borderWidth.toPx() * 2.8f
            isAntiAlias = true
            maskFilter = BlurMaskFilter(glowPx, BlurMaskFilter.Blur.NORMAL)
        }
        val rect = android.graphics.RectF(0f, 0f, size.width, size.height)
        nativeCanvas.drawRoundRect(rect, cornerPx, cornerPx, glowPaint)
    }

    // Crisp inner neon outline
    drawRoundRect(
        color = color.copy(alpha = 0.95f),
        topLeft = Offset.Zero,
        size = size,
        cornerRadius = CornerRadius(cornerPx, cornerPx),
        style = Stroke(width = borderWidth.toPx())
    )
}
