package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.Zinc600

/**
 * Minimalist Soft-Neumorphic Milestone Card strictly matching the Light & Dark
 * Home Task List reference screens:
 * - Raised dual-shadow Zinc card (#EBEBEE Light / #2C2C30 Dark)
 * - Inset square NeumorphicCheckbox on the left
 * - Clean timeline subtitle + bold milestone title in the center
 * - Right-side thematic icon (clean charcoal stroke in Light Mode; luminous neon glow in Dark Mode)
 * - Swipe right to delete (with Undo) and swipe left to edit
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTaskItem(
    task: TaskEntity,
    isPt: Boolean,
    isDark: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalNeumorphicColors.current
    val interactionSource = remember { MutableInteractionSource() }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    triggerInstantHaptic(context)
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    triggerInstantHaptic(context)
                    onEdit()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.fillMaxWidth(),
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val backgroundColor by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFFEF4444)
                    SwipeToDismissBoxValue.EndToStart -> NeonCyan
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                },
                label = "swipe_bg_color"
            )

            val iconScale by animateFloatAsState(
                targetValue = if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) 1.2f else 0.85f,
                label = "swipe_icon_scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(backgroundColor)
                    .padding(horizontal = 24.dp),
                contentAlignment = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                    else -> Alignment.Center
                }
            ) {
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.scale(iconScale)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = if (isPt) "Excluir" else "Delete",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPt) "Excluir" else "Delete",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                    SwipeToDismissBoxValue.EndToStart -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.scale(iconScale)
                        ) {
                            Text(
                                text = if (isPt) "Editar" else "Edit",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF09090B)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = if (isPt) "Editar" else "Edit",
                                tint = Color(0xFF09090B),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    ) {
        val rawTimeline = if (isPt) task.timelinePt else task.timelineEn
        val timelineText = rawTimeline.trimEnd(':').trim()
        val titleText = if (isPt) task.titlePt else task.titleEn

        val neonAccent = when (task.iconType.lowercase()) {
            "search", "magnifyingglass" -> NeonCyan
            "article", "list" -> NeonPurple
            "megaphone" -> NeonYellow
            "checkcircle", "check" -> NeonPink
            "rocketlaunch", "rocket" -> NeonGreen
            else -> NeonCyan
        }

        val iconTint = if (isDark) neonAccent else Zinc600
        val iconGlow = if (isDark) neonAccent else null

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .neumorphicElevation(
                    cornerRadius = 22.dp,
                    backgroundColor = colors.surface,
                    lightShadowColor = colors.lightShadow,
                    darkShadowColor = colors.darkShadow,
                    offset = if (isDark) 5.dp else 5.dp,
                    blur = if (isDark) 10.dp else 10.dp
                )
                .clip(RoundedCornerShape(22.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true),
                    onClick = {
                        triggerInstantHaptic(context)
                        onToggle()
                    }
                )
                .padding(horizontal = 14.dp, vertical = 16.dp)
                .testTag("task_item_${task.id}")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left: Inset Square Neumorphic Checkbox
                NeumorphicCheckbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggle() },
                    testTag = "task_checkbox_${task.id}",
                    size = 32.dp,
                    activeColor = if (isDark) neonAccent else colors.textPrimary
                )

                // Center: Timeline Subtitle + Bold Milestone Title
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = timelineText,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.5.sp,
                        lineHeight = 18.sp,
                        color = if (task.isCompleted) {
                            colors.textSecondary.copy(alpha = 0.55f)
                        } else {
                            colors.textSecondary
                        }
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = titleText,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.5.sp,
                        lineHeight = 21.sp,
                        color = if (task.isCompleted) {
                            colors.textPrimary.copy(alpha = 0.45f)
                        } else {
                            colors.textPrimary
                        },
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                }

                // Right: Thematic Icon (clean outline in Light / luminous neon glow in Dark)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .padding(end = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PhosphorIcon(
                        name = task.iconType,
                        tint = if (task.isCompleted) iconTint.copy(alpha = 0.45f) else iconTint,
                        glowColor = if (task.isCompleted) null else iconGlow,
                        size = 30.dp
                    )
                }
            }
        }
    }
}
