package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Zinc500
import com.example.ui.theme.Zinc600

/**
 * Tactile inset Neumorphic square checkbox matching the reference mockups.
 * Features a soft recessed square well with a crisp inner square outline
 * and a spring-animated checkmark on completion.
 */
@Composable
fun NeumorphicCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "neumorphic_checkbox",
    size: Dp = 32.dp,
    activeColor: Color? = null
) {
    val colors = LocalNeumorphicColors.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    val animatedProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 520f),
        label = "checkbox_anim"
    )

    val checkTint = activeColor ?: if (colors.isDark) NeonCyan else colors.textPrimary
    val innerOutlineColor = when {
        checked -> checkTint
        colors.isDark -> Zinc500
        else -> Zinc600
    }

    // Enforce 48dp minimum interactive touch target
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
        // Outer recessed neumorphic square well
        Box(
            modifier = Modifier
                .size(size)
                .neumorphicInset(
                    cornerRadius = 9.dp,
                    backgroundColor = if (colors.isDark) Color(0xFF242428) else Color(0xFFE6E6EA),
                    darkShadowColor = colors.insetDark,
                    lightShadowColor = colors.insetLight,
                    offset = 2.dp,
                    blur = 3.5.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            // Crisp inner square frame matching the reference mockups
            Box(
                modifier = Modifier
                    .size(size * 0.58f)
                    .border(
                        width = 1.6.dp,
                        color = innerOutlineColor,
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(size * 0.44f)
                        .graphicsLayer {
                            scaleX = animatedProgress
                            scaleY = animatedProgress
                            alpha = animatedProgress
                        }
                ) {
                    val w = this.size.width
                    val h = this.size.height
                    val path = Path().apply {
                        moveTo(w * 0.16f, h * 0.52f)
                        lineTo(w * 0.42f, h * 0.78f)
                        lineTo(w * 0.86f, h * 0.24f)
                    }
                    drawPath(
                        path = path,
                        color = checkTint,
                        style = Stroke(
                            width = 2.2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}
