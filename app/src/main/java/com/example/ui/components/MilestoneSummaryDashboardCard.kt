package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.theme.InterFontFamily
import kotlin.math.roundToInt

/**
 * Material 3 Summary Dashboard Card displaying:
 * 1. Status Ring Donut Chart (Completed vs Pending launch tasks)
 * 2. Priority Distribution Bar Chart (High, Medium, Normal)
 * With an expandable/collapsible toggle to preserve screen space.
 */
@Composable
fun MilestoneSummaryDashboardCard(
    tasks: List<TaskEntity>,
    isPt: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(true) }

    val totalTasks = tasks.size
    val completedCount = tasks.count { it.isCompleted }
    val pendingCount = totalTasks - completedCount
    val completionRatio = if (totalTasks > 0) completedCount.toFloat() / totalTasks else 0f
    val percentInt = (completionRatio * 100).roundToInt()

    val highCount = tasks.count {
        val p = it.priority.lowercase()
        p == "high" || p == "alta"
    }
    val medCount = tasks.count {
        val p = it.priority.lowercase()
        p == "medium" || p == "média" || p == "media"
    }
    val normalCount = tasks.count {
        val p = it.priority.lowercase()
        p != "high" && p != "alta" && p != "medium" && p != "média" && p != "media"
    }

    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "dashboard_arrow_rotation"
    )

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("milestone_summary_dashboard_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row (Clickable to Expand/Collapse)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isExpanded = !isExpanded }
                    .testTag("dashboard_toggle_button"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (isPt) "PAINEL DE MARCOS" else "MILESTONES DASHBOARD",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            letterSpacing = 0.8.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isPt) {
                                "$completedCount de $totalTasks concluídos • $percentInt%"
                            } else {
                                "$completedCount of $totalTasks completed • $percentInt%"
                            },
                            fontFamily = InterFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Task Count Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isPt) "$totalTasks marcos" else "$totalTasks tasks",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(arrowRotation)
                    )
                }
            }

            // Expandable Body: Status Ring Chart & Priority Distribution Bars
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Status Ring Donut Chart
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dashboard_status_ring_chart"),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isPt) "STATUS" else "STATUS",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                letterSpacing = 0.8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val animatedSweep by animateFloatAsState(
                                targetValue = completionRatio * 360f,
                                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                                label = "ring_sweep"
                            )

                            val primaryColor = MaterialTheme.colorScheme.primary
                            val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest

                            Box(
                                modifier = Modifier.size(76.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(76.dp)) {
                                    val strokeWidth = 9.dp.toPx()
                                    val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)

                                    // Background Track
                                    drawCircle(
                                        color = trackColor,
                                        style = stroke
                                    )

                                    // Completed Arc
                                    if (animatedSweep > 0f) {
                                        drawArc(
                                            color = primaryColor,
                                            startAngle = -90f,
                                            sweepAngle = animatedSweep,
                                            useCenter = false,
                                            style = stroke
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$percentInt%",
                                        fontFamily = InterFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Ring Chart Legend
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$completedCount",
                                        fontFamily = InterFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.outlineVariant)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$pendingCount",
                                        fontFamily = InterFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Divider line
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(95.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // 2. Priority Distribution Bar Chart
                        Column(
                            modifier = Modifier
                                .weight(1.35f)
                                .testTag("dashboard_priority_bar_chart"),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isPt) "DISTRIBUIÇÃO POR PRIORIDADE" else "PRIORITY BREAKDOWN",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                letterSpacing = 0.8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val safeTotal = if (totalTasks > 0) totalTasks.toFloat() else 1f

                            // High Priority Row
                            PriorityBarRow(
                                label = if (isPt) "Alta" else "High",
                                count = highCount,
                                ratio = highCount / safeTotal,
                                color = MaterialTheme.colorScheme.error,
                                badgeBg = MaterialTheme.colorScheme.errorContainer
                            )

                            // Medium Priority Row
                            PriorityBarRow(
                                label = if (isPt) "Média" else "Med",
                                count = medCount,
                                ratio = medCount / safeTotal,
                                color = Color(0xFFF59E0B),
                                badgeBg = Color(0xFFFEF3C7)
                            )

                            // Normal Priority Row
                            PriorityBarRow(
                                label = if (isPt) "Normal" else "Norm",
                                count = normalCount,
                                ratio = normalCount / safeTotal,
                                color = MaterialTheme.colorScheme.primary,
                                badgeBg = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriorityBarRow(
    label: String,
    count: Int,
    ratio: Float,
    color: Color,
    badgeBg: Color
) {
    val animatedProgress by animateFloatAsState(
        targetValue = ratio.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "priority_bar_$label"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Label badge
        Box(
            modifier = Modifier
                .width(42.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(badgeBg)
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label.uppercase(),
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 8.5.sp,
                color = color
            )
        }

        // Animated Bar
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            strokeCap = StrokeCap.Round
        )

        // Count number
        Text(
            text = "$count",
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(16.dp),
            textAlign = TextAlign.End
        )
    }
}
