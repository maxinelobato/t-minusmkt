package com.example.ui.screens

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import com.example.data.model.TaskEntity
import com.example.ui.components.CountdownLockScreenView
import com.example.ui.components.triggerInstantHaptic
import kotlin.math.roundToInt

/**
 * Fullscreen Interactive Android Lock Screen Composable used by LockScreenActivity.
 * Renders the unified Soft-Neumorphic CountdownLockScreenView with swipe-up & fingerprint unlock.
 */
@Composable
fun LockScreenComposable(
    tasks: List<TaskEntity>,
    isPt: Boolean,
    isDark: Boolean,
    timeDisplay: String,
    amPmDisplay: String,
    dateDisplay: String,
    daysRemaining: Long = 16L,
    hoursRemaining: Long = 0L,
    minutesRemaining: Long = 0L,
    secondsRemaining: Long = 0L,
    onToggleTask: (id: Int, currentCompleted: Boolean) -> Unit,
    onUnlock: () -> Unit,
    onToggleLanguage: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val animatedOffsetY by animateFloatAsState(
        targetValue = dragOffsetY,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
        label = "swipe_up_offset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dragOffsetY < -140f) {
                            triggerInstantHaptic(context)
                            onUnlock()
                        }
                        dragOffsetY = 0f
                    },
                    onDragCancel = {
                        dragOffsetY = 0f
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        if (dragAmount < 0 || dragOffsetY < 0) {
                            dragOffsetY = (dragOffsetY + dragAmount).coerceIn(-450f, 0f)
                        }
                    }
                )
            }
            .offset { IntOffset(0, animatedOffsetY.roundToInt()) }
            .testTag("lock_screen_container")
    ) {
        CountdownLockScreenView(
            tasks = tasks,
            daysRemaining = daysRemaining,
            hoursRemaining = hoursRemaining,
            minutesRemaining = minutesRemaining,
            secondsRemaining = secondsRemaining,
            timeDisplay = timeDisplay,
            dateDisplay = dateDisplay,
            isPt = isPt,
            isDark = isDark,
            onToggleTask = onToggleTask,
            onFingerprintAction = onUnlock,
            showExportControls = false,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        )
    }
}
