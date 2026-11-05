package com.example.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeonSkyBlue
import kotlin.math.roundToInt

@Composable
fun SwipeLockHandle(
    isLocked: Boolean,
    onUnlock: () -> Unit,
    onLock: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalNeumorphicColors.current
    val haptic = LocalHapticFeedback.current
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    // Pulsing animation for subtle hint
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_swipe")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("swipe_lock_handle")
            .offset { IntOffset(0, dragOffsetY.coerceAtMost(0f).roundToInt()) }
            .pointerInput(isLocked) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetY += dragAmount
                        if (dragOffsetY < -140f) {
                            // Trigger unlock with sound and haptic
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            try {
                                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
                                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
                            } catch (_: Exception) {}
                            if (isLocked) onUnlock() else onLock()
                            dragOffsetY = 0f
                        }
                    },
                    onDragEnd = {
                        dragOffsetY = 0f
                    },
                    onDragCancel = {
                        dragOffsetY = 0f
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Lock / Unlock icon with subtle pulse
        PhosphorIcon(
            name = if (isLocked) "lock" else "check",
            tint = if (colors.isDark) NeonSkyBlue.copy(alpha = pulseAlpha) else colors.textMuted.copy(alpha = pulseAlpha),
            size = 15.dp,
            glowColor = if (colors.isDark) NeonSkyBlue else null
        )

        Spacer(modifier = Modifier.height(3.dp))

        // "DESLIZE" / "SWIPE UP"
        Text(
            text = label.uppercase(),
            fontFamily = InterFontFamily,
            fontSize = 10.sp,
            letterSpacing = 1.6.sp,
            color = if (colors.isDark) colors.textSecondary.copy(alpha = pulseAlpha) else colors.textMuted,
            modifier = Modifier.alpha(pulseAlpha)
        )
    }
}
