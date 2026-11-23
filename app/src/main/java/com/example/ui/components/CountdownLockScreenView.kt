package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LightLockAmber
import com.example.ui.theme.LightLockCyan
import com.example.ui.theme.LightLockGreen
import com.example.ui.theme.LightLockPink
import com.example.ui.theme.LightLockPurple
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeonBadgeCyan
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.Zinc400
import com.example.ui.theme.Zinc600
import java.util.Locale

/**
 * Primary Feature — Countdown Lock Screen strictly matching the Light (#E4E4E7)
 * and Dark (#27272A) Soft-Neumorphic aesthetic:
 * - Safe status bar padding so the digital clock & date never clip at the top
 * - Compact top-right icon controls (Export Wallpaper & Live Widget Notification) when in preview tab
 * - Prominent centered Neumorphic M3 Card displaying:
 *     "CONTAGEM REGRESSIVA" / "COUNTDOWN"
 *     "T-MINUS MKT • PLANEJAMENTO DE MARKETING" / "T-MINUS MKT • MARKETING PLANNING"
 *     Recessed glowing neon cyan countdown badge ("T-XX DIAS" / "T-XX DAYS")
 *     Live ticking remaining time ("HHh : MMm : SSs") until launch
 *     Nested Neumorphic card showing ALL tasks from the app in chronological order (PT & EN dynamically)
 * - Bottom circular raised Neumorphic fingerprint button
 */
@Composable
fun CountdownLockScreenView(
    tasks: List<TaskEntity>,
    daysRemaining: Long,
    hoursRemaining: Long = 0L,
    minutesRemaining: Long = 0L,
    secondsRemaining: Long = 0L,
    timeDisplay: String,
    dateDisplay: String,
    isPt: Boolean,
    isDark: Boolean,
    onToggleTask: (id: Int, currentCompleted: Boolean) -> Unit,
    onFingerprintAction: () -> Unit,
    showExportControls: Boolean = true,
    onExportWallpaper: () -> Unit = {},
    onPinWidgetNotification: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalNeumorphicColors.current
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "lock_countdown_glow")
    val cyanGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cyan_glow"
    )

    val liveTimeFormatted = remember(hoursRemaining, minutesRemaining, secondsRemaining) {
        String.format(
            Locale.US,
            "%02dh : %02dm : %02ds",
            hoursRemaining.coerceAtLeast(0L),
            minutesRemaining.coerceAtLeast(0L),
            secondsRemaining.coerceAtLeast(0L)
        )
    }

    val completedCount = remember(tasks) { tasks.count { it.isCompleted } }
    val totalCount = tasks.size

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .testTag("countdown_lock_screen_view")
    ) {
        // Optional compact top-right Export Wallpaper & Live Widget Notification buttons
        if (showExportControls) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeumorphicMiniActionIcon(
                    onClick = {
                        triggerInstantHaptic(context)
                        onExportWallpaper()
                    },
                    testTag = "lock_export_wallpaper_btn",
                    contentDescription = if (isPt) "Exportar papel de parede" else "Export wallpaper"
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Wallpaper,
                        contentDescription = if (isPt) "Exportar papel de parede" else "Export wallpaper",
                        tint = if (isDark) NeonCyan else colors.textPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }

                NeumorphicMiniActionIcon(
                    onClick = {
                        triggerInstantHaptic(context)
                        onPinWidgetNotification()
                    },
                    testTag = "lock_pin_widget_btn",
                    contentDescription = if (isPt) "Atualizar widget na tela de bloqueio" else "Pin lock screen widget"
                ) {
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = if (isPt) "Atualizar widget na tela de bloqueio" else "Pin lock screen widget",
                        tint = if (isDark) NeonGreen else colors.textPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 460.dp)
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP CLEAN AREA FOR ANDROID SYSTEM CLOCK & DATE
            Spacer(modifier = Modifier.height(if (showExportControls) 28.dp else 18.dp))

            Text(
                text = timeDisplay,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 52.sp,
                lineHeight = 56.sp,
                letterSpacing = (-1.8).sp,
                color = if (isDark) Color(0xFFE4E4E7) else Color(0xFF27272A),
                modifier = Modifier.testTag("lock_clock_display")
            )

            Text(
                text = dateDisplay,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = if (isDark) Color(0xFFD4D4D8) else Color(0xFF27272A),
                modifier = Modifier.testTag("lock_date_display")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // CENTERED PROMINENT NEUMORPHIC M3 COUNTDOWN CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neumorphicElevation(
                        cornerRadius = 26.dp,
                        backgroundColor = colors.surface,
                        lightShadowColor = colors.lightShadow,
                        darkShadowColor = colors.darkShadow,
                        offset = 6.dp,
                        blur = 14.dp
                    )
                    .padding(horizontal = 12.dp, vertical = 16.dp)
                    .testTag("lock_countdown_card")
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header: CONTAGEM REGRESSIVA / COUNTDOWN
                    Text(
                        text = if (isPt) "CONTAGEM REGRESSIVA" else "COUNTDOWN",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        letterSpacing = 0.3.sp,
                        color = if (isDark) Color.White else Color(0xFF18181B),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Subtitle: T-MINUS MKT • PLANEJAMENTO DE MARKETING / MARKETING PLANNING
                    Text(
                        text = if (isPt) "T-MINUS MKT • PLANEJAMENTO DE MARKETING" else "T-MINUS MKT • MARKETING PLANNING",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        color = if (isDark) Zinc400 else Zinc600,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Recessed Glowing Neon Cyan Countdown Pill ("T-XX DIAS" / "T-XX DAYS")
                    val countdownText = if (isPt) "T-$daysRemaining DIAS" else "T-$daysRemaining DAYS"
                    Box(
                        modifier = Modifier
                            .neumorphicInset(
                                cornerRadius = 15.dp,
                                backgroundColor = if (isDark) Color(0xFF222226) else Color(0xFFE5E5EA),
                                darkShadowColor = colors.insetDark,
                                lightShadowColor = colors.insetLight,
                                offset = 3.dp,
                                blur = 6.dp
                            )
                            .padding(horizontal = 24.dp, vertical = 7.dp)
                            .testTag("lock_countdown_badge"),
                        contentAlignment = Alignment.Center
                    ) {
                        GlowingCyanCountdownText(
                            text = countdownText,
                            glowAlpha = cyanGlowAlpha
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Live Remaining Time Ticker ("HHh : MMm : SSs")
                    Text(
                        text = liveTimeFormatted,
                        style = TextStyle(
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 1.1.sp,
                            fontFeatureSettings = "tnum",
                            color = if (isDark) NeonBadgeCyan else Color(0xFF0891B2)
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("lock_live_countdown_timer")
                    )

                    Text(
                        text = if (isPt) "restantes até o lançamento" else "remaining until launch",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = if (isDark) Zinc400 else Zinc600,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Nested Inner Elevated Neumorphic Card displaying ALL Milestones from the App
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .neumorphicElevation(
                                cornerRadius = 20.dp,
                                backgroundColor = colors.surface,
                                lightShadowColor = colors.lightShadow,
                                darkShadowColor = colors.darkShadow,
                                offset = 4.dp,
                                blur = 10.dp
                            )
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                            .testTag("lock_all_milestones_card")
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Compact Synced Task Count Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isPt) "TODAS AS TAREFAS" else "ALL TASKS",
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.8.sp,
                                    color = if (isDark) Zinc400 else Zinc600
                                )
                                Text(
                                    text = if (isPt) "$completedCount/$totalCount concluídas" else "$completedCount/$totalCount completed",
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.5.sp,
                                    color = if (isDark) NeonBadgeCyan else Color(0xFF0891B2),
                                    modifier = Modifier.testTag("lock_task_count_label")
                                )
                            }

                            // Render EVERY task present in the app (no truncation)
                            tasks.forEach { task ->
                                LockScreenMilestoneRow(
                                    task = task,
                                    isPt = isPt,
                                    isDark = isDark,
                                    onToggle = { onToggleTask(task.id, task.isCompleted) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Bottom Circular Raised Neumorphic Fingerprint Button
            val fpInteraction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .neumorphicElevation(
                        cornerRadius = 27.dp,
                        backgroundColor = colors.surface,
                        lightShadowColor = colors.lightShadow,
                        darkShadowColor = colors.darkShadow,
                        offset = 5.dp,
                        blur = 10.dp
                    )
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = fpInteraction,
                        indication = ripple(bounded = true),
                        onClick = {
                            triggerInstantHaptic(context)
                            onFingerprintAction()
                        }
                    )
                    .testTag("lock_fingerprint_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = if (isPt) "Desbloquear com digital" else "Unlock with fingerprint",
                    tint = if (isDark) Color(0xFFD4D4D8) else Zinc600,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun GlowingCyanCountdownText(
    text: String,
    glowAlpha: Float
) {
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawIntoCanvas { canvas ->
                val glowPaint = android.graphics.Paint().apply {
                    color = NeonBadgeCyan.copy(alpha = 0.72f * glowAlpha).toArgb()
                    textSize = 24.sp.toPx()
                    typeface = android.graphics.Typeface.create(
                        android.graphics.Typeface.SANS_SERIF,
                        android.graphics.Typeface.BOLD
                    )
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                    maskFilter = BlurMaskFilter(12.dp.toPx(), BlurMaskFilter.Blur.NORMAL)
                }
                val baseline = size.height / 2f - (glowPaint.descent() + glowPaint.ascent()) / 2f
                canvas.nativeCanvas.drawText(text, size.width / 2f, baseline, glowPaint)
            }
        }
        Text(
            text = text,
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            letterSpacing = 0.5.sp,
            color = NeonBadgeCyan
        )
    }
}

@Composable
private fun LockScreenMilestoneRow(
    task: TaskEntity,
    isPt: Boolean,
    isDark: Boolean,
    onToggle: () -> Unit
) {
    val colors = LocalNeumorphicColors.current
    val timelineText = (if (isPt) task.timelinePt else task.timelineEn).trimEnd(':').trim()
    val titleText = if (isPt) task.titlePt else task.titleEn

    val iconColor = if (isDark) {
        when (task.iconType.lowercase()) {
            "search", "magnifyingglass" -> NeonCyan
            "article", "list" -> NeonPurple
            "megaphone" -> NeonYellow
            "checkcircle", "check" -> NeonPink
            "rocketlaunch", "rocket" -> NeonGreen
            else -> NeonCyan
        }
    } else {
        when (task.iconType.lowercase()) {
            "search", "magnifyingglass" -> LightLockCyan
            "article", "list" -> LightLockPurple
            "megaphone" -> LightLockAmber
            "checkcircle", "check" -> LightLockPink
            "rocketlaunch", "rocket" -> LightLockGreen
            else -> LightLockCyan
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle() }
            .padding(vertical = 2.dp, horizontal = 2.dp)
            .testTag("lock_milestone_row_${task.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NeumorphicCheckbox(
            checked = task.isCompleted,
            onCheckedChange = { onToggle() },
            testTag = "lock_milestone_checkbox_${task.id}",
            size = 24.dp,
            activeColor = iconColor
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = timelineText,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = if (task.isCompleted) colors.textPrimary.copy(alpha = 0.45f) else colors.textPrimary,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = titleText,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = if (task.isCompleted) colors.textSecondary.copy(alpha = 0.45f) else colors.textSecondary,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .padding(end = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            PhosphorIcon(
                name = task.iconType,
                tint = if (task.isCompleted) iconColor.copy(alpha = 0.45f) else iconColor,
                glowColor = if (isDark && !task.isCompleted) iconColor else null,
                size = 22.dp
            )
        }
    }
}

@Composable
private fun NeumorphicMiniActionIcon(
    onClick: () -> Unit,
    testTag: String,
    contentDescription: String,
    icon: @Composable () -> Unit
) {
    val colors = LocalNeumorphicColors.current
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = 24.dp),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .neumorphicElevation(
                    cornerRadius = 18.dp,
                    backgroundColor = colors.surface,
                    lightShadowColor = colors.lightShadow,
                    darkShadowColor = colors.darkShadow,
                    offset = 3.dp,
                    blur = 6.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    }
}
