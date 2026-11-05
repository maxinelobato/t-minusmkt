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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TaskEntity
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeonSkyBlue

@Composable
fun CalendarSyncDialog(
    currentIndex: Int,
    totalCount: Int,
    currentTask: TaskEntity?,
    isPt: Boolean,
    onAddToCalendar: (TaskEntity) -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalNeumorphicColors.current

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(colors.surface)
                .neumorphicElevation(
                    cornerRadius = 24.dp,
                    backgroundColor = colors.surface,
                    lightShadowColor = colors.lightShadow,
                    darkShadowColor = colors.darkShadow,
                    offset = 4.dp,
                    blur = 12.dp
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Header icon
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(18.dp))
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
                    PhosphorIcon(
                        name = "calendar-plus",
                        tint = if (colors.isDark) NeonSkyBlue else colors.textPrimary,
                        glowColor = if (colors.isDark) NeonSkyBlue else null,
                        size = 28.dp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isPt) "SINCRONIZAR COM GOOGLE CALENDAR" else "SYNC TO GOOGLE CALENDAR",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isPt) "Etapa ${currentIndex + 1} de $totalCount" else "Milestone ${currentIndex + 1} of $totalCount",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = if (colors.isDark) NeonSkyBlue else colors.textSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Milestone details box
                if (currentTask != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .neumorphicInset(
                                cornerRadius = 16.dp,
                                backgroundColor = colors.surface,
                                darkShadowColor = colors.insetDark,
                                lightShadowColor = colors.insetLight
                            )
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isPt) currentTask.timelinePt else currentTask.timelineEn,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = if (isPt) currentTask.titlePt else currentTask.titleEn,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isPt) "⏰ Horário padrão: 14:00 (2:00 PM)" else "⏰ Default reminder: 14:00 (2:00 PM)",
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = if (colors.isDark) NeonSkyBlue else colors.textPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Add to Calendar Primary Action
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .neumorphicElevation(
                            cornerRadius = 16.dp,
                            backgroundColor = colors.surface,
                            lightShadowColor = colors.lightShadow,
                            darkShadowColor = colors.darkShadow,
                            offset = 3.dp,
                            blur = 6.dp
                        )
                        .clickable { currentTask?.let { onAddToCalendar(it) } }
                        .padding(vertical = 12.dp)
                        .testTag("add_to_google_calendar_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPt) "Adicionar ao Google Calendar" else "Add to Google Calendar",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (colors.isDark) NeonSkyBlue else colors.textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary controls: Skip / Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onDismiss() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (isPt) "Fechar" else "Close",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSkip() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (isPt) "Pular etapa >" else "Skip milestone >",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}
