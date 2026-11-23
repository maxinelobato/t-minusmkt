package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import kotlin.math.roundToInt
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonSkyBlue

/**
 * Translucent Frosted Glass Card for Lock Screen matching Reference Image:
 * - "T-MINUS COUNTDOWN: Planejamento de Marketing"
 * - Full checklist showing all milestones (T-29, T-14, T-7, T-3, T-1)
 * - Interactive checkboxes for instant checkoff directly on the lock screen
 * - Themed category icons on the right
 */
@Composable
fun FrostedGlassTaskCard(
    tasks: List<TaskEntity>,
    isPt: Boolean,
    isDark: Boolean,
    onToggleTask: (id: Int, currentCompleted: Boolean) -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .neumorphicElevation(
                cornerRadius = 26.dp,
                backgroundColor = neuColors.surface,
                lightShadowColor = neuColors.lightShadow,
                darkShadowColor = neuColors.darkShadow,
                offset = 5.dp,
                blur = 12.dp
            )
            .clip(RoundedCornerShape(26.dp))
            .padding(horizontal = 18.dp, vertical = 20.dp)
            .testTag("frosted_glass_task_card")
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Header: T-MINUS
            Text(
                text = "T-MINUS",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                letterSpacing = 2.sp,
                color = if (isDark) NeonCyan else Color(0xFF0F172A)
            )

            // Subtitle: CONTAGEM REGRESSIVA: / COUNTDOWN:
            Text(
                text = if (isPt) "CONTAGEM REGRESSIVA:" else "COUNTDOWN:",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.5.sp,
                color = if (isDark) Color.White.copy(alpha = 0.9f) else Color(0xFF334155)
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Project Title: "PLANEJAMENTO DE MARKETING" / "MARKETING PLANNING"
            Text(
                text = if (isPt) "PLANEJAMENTO DE MARKETING" else "MARKETING PLANNING",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                letterSpacing = 1.2.sp,
                color = if (isDark) NeonSkyBlue else Color(0xFF0F172A)
            )

            val totalTasks = tasks.size
            val completedTasks = tasks.count { it.isCompleted }
            val completionRatio = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
            val percentInt = (completionRatio * 100).roundToInt()

            Spacer(modifier = Modifier.height(14.dp))

            // Linear Progress Indicator for Launch Preparation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isPt) "Progresso do Lançamento" else "Launch Preparation Progress",
                    fontFamily = InterFontFamily,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF475569)
                )
                Text(
                    text = "$percentInt%",
                    fontFamily = InterFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0F172A)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { completionRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .testTag("lock_screen_launch_progress"),
                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                trackColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFFE2E8F0),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Full List of Milestones with Checkboxes and Themed Icons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                tasks.forEach { task ->
                    LockScreenTaskItem(
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

@Composable
private fun LockScreenTaskItem(
    task: TaskEntity,
    isPt: Boolean,
    isDark: Boolean,
    onToggle: () -> Unit
) {
    val timelineText = if (isPt) task.timelinePt else task.timelineEn
    val taskTitle = if (isPt) task.titlePt else task.titleEn

    val iconColor = if (isDark) {
        when (task.iconType.lowercase()) {
            "search", "magnifyingglass" -> NeonCyan
            "article", "list" -> NeonPurple
            "megaphone" -> NeonAmber
            "checkcircle", "check" -> NeonPink
            "rocketlaunch", "rocket" -> NeonGreen
            else -> NeonSkyBlue
        }
    } else {
        Color(0xFF334155)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .fluidNeumorphicClick { onToggle() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Square Checkbox matching reference style
        NeumorphicCheckbox(
            checked = task.isCompleted,
            onCheckedChange = { onToggle() },
            activeColor = iconColor,
            testTag = "lock_task_checkbox_${task.id}"
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Task Content
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = timelineText,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(1.dp))

            Text(
                text = taskTitle,
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.5.sp,
                color = if (task.isCompleted) {
                    if (isDark) Color.Gray else Color.LightGray
                } else {
                    if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
                }
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Category Icon
        PhosphorIcon(
            name = task.iconType,
            tint = iconColor,
            glowColor = null,
            size = 22.dp
        )
    }
}
