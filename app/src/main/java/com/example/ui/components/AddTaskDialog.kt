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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeonSkyBlue

@Composable
fun AddTaskDialog(
    initialText: String = "",
    isPt: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (title: String, daysRemaining: Int, iconType: String) -> Unit
) {
    val colors = LocalNeumorphicColors.current
    var title by remember { mutableStateOf(initialText) }
    var daysText by remember { mutableStateOf("10") }
    var selectedIcon by remember { mutableStateOf("rocket") }

    val icons = listOf("search", "article", "megaphone", "checkcircle", "rocket")

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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isPt) "NOVA ETAPA DE MARKETING" else "NEW MARKETING MILESTONE",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = {
                        Text(
                            if (isPt) "Descrição da etapa" else "Milestone description",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedBorderColor = if (colors.isDark) NeonSkyBlue else colors.textPrimary,
                        unfocusedBorderColor = colors.textMuted
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = daysText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) daysText = it },
                    label = {
                        Text(
                            if (isPt) "Dias antes do lançamento (T-Minus)" else "Days before launch (T-Minus)",
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_days_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedBorderColor = if (colors.isDark) NeonSkyBlue else colors.textPrimary,
                        unfocusedBorderColor = colors.textMuted
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Icon selector
                Text(
                    text = if (isPt) "Selecione o Ícone:" else "Select Icon:",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    icons.forEach { iconName ->
                        val isSelected = selectedIcon == iconName
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedIcon = iconName }
                                .then(
                                    if (isSelected) {
                                        Modifier.neumorphicInset(
                                            cornerRadius = 12.dp,
                                            backgroundColor = colors.surface,
                                            darkShadowColor = colors.insetDark,
                                            lightShadowColor = colors.insetLight
                                        )
                                    } else {
                                        Modifier.neumorphicElevation(
                                            cornerRadius = 12.dp,
                                            backgroundColor = colors.surface,
                                            lightShadowColor = colors.lightShadow,
                                            darkShadowColor = colors.darkShadow,
                                            offset = 2.dp,
                                            blur = 4.dp
                                        )
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            PhosphorIcon(
                                name = iconName,
                                tint = if (isSelected) (if (colors.isDark) NeonSkyBlue else colors.textPrimary) else colors.textMuted,
                                size = 20.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onDismiss() }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = if (isPt) "Cancelar" else "Cancel",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textSecondary,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .neumorphicElevation(
                                cornerRadius = 14.dp,
                                backgroundColor = colors.surface,
                                lightShadowColor = colors.lightShadow,
                                darkShadowColor = colors.darkShadow,
                                offset = 2.dp,
                                blur = 6.dp
                            )
                            .clickable(enabled = title.isNotBlank()) {
                                val days = daysText.toIntOrNull() ?: 5
                                onConfirm(title.trim(), days, selectedIcon)
                            }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = if (isPt) "Adicionar" else "Add Milestone",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = if (colors.isDark) NeonSkyBlue else colors.textPrimary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
