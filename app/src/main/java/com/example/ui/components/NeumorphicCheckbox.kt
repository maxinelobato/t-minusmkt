package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeonSkyBlue

/**
 * Tactile inset Neumorphic checkbox matching the reference design.
 * Features an 8.dp rounded square with recessed drop/highlight inner shadows
 * and an animated checked indicator.
 */
@Composable
fun NeumorphicCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "neumorphic_checkbox",
    size: Dp = 26.dp,
    activeColor: Color? = null
) {
    val colors = LocalNeumorphicColors.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    val animatedProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 500f),
        label = "checkbox_anim"
    )

    // Touch target of at least 48.dp
    Box(
        modifier = modifier
            .size(48.dp)
            .testTag(testTag)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 24.dp),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCheckedChange(!checked)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        // Inset recessed box
        Box(
            modifier = Modifier
                .size(size)
                .neumorphicInset(
                    cornerRadius = 8.dp,
                    backgroundColor = colors.surface,
                    darkShadowColor = colors.insetDark,
                    lightShadowColor = colors.insetLight,
                    offset = 2.dp,
                    blur = 3.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            // Inner checkmark / circle icon
            val checkTint = activeColor ?: if (colors.isDark) NeonSkyBlue else colors.textPrimary

            Canvas(
                modifier = Modifier
                    .size(size * 0.75f)
                    .graphicsLayer {
                        scaleX = animatedProgress
                        scaleY = animatedProgress
                        alpha = animatedProgress
                    }
            ) {
                val w = this.size.width
                val h = this.size.height
                val path = Path().apply {
                    moveTo(w * 0.22f, h * 0.52f)
                    lineTo(w * 0.42f, h * 0.72f)
                    lineTo(w * 0.78f, h * 0.32f)
                }
                drawPath(
                    path = path,
                    color = checkTint,
                    style = Stroke(
                        width = 2.4.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}
