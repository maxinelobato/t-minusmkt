package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LaunchCategory
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeonSkyBlue

@Composable
fun AddTaskDialog(
    initialText: String = "",
    isPt: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (title: String, daysRemaining: Int, iconType: String, priority: String, category: String) -> Unit
) {
    val colors = LocalNeumorphicColors.current
    var title by remember { mutableStateOf(initialText) }
    var daysText by remember { mutableStateOf("10") }
    var selectedIcon by remember { mutableStateOf("rocket") }
    var selectedPriority by remember { mutableStateOf("Normal") }
    var selectedCategory by remember { mutableStateOf(LaunchCategory.MARKETING.id) }

    val icons = listOf("search", "article", "megaphone", "checkcircle", "rocket")
    val priorities = listOf("Normal", "Medium", "High")
    val categories = LaunchCategory.entries

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
                .padding(22.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (isPt) "NOVA ETAPA DE LANÇAMENTO" else "NEW LAUNCH MILESTONE",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicInset(
                            cornerRadius = 14.dp,
                            backgroundColor = colors.surface,
                            darkShadowColor = colors.insetDark,
                            lightShadowColor = colors.insetLight,
                            offset = 2.dp,
                            blur = 4.dp
                        )
                ) {
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
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .neumorphicInset(
                            cornerRadius = 14.dp,
                            backgroundColor = colors.surface,
                            darkShadowColor = colors.insetDark,
                            lightShadowColor = colors.insetLight,
                            offset = 2.dp,
                            blur = 4.dp
                        )
                ) {
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
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selector
                Text(
                    text = if (isPt) "Categoria da Etapa:" else "Milestone Category:",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory.equals(cat.id, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .then(
                                    if (isSelected) {
                                        Modifier.neumorphicInset(
                                            cornerRadius = 10.dp,
                                            backgroundColor = cat.primaryColor.copy(alpha = 0.18f),
                                            darkShadowColor = colors.insetDark,
                                            lightShadowColor = colors.insetLight,
                                            offset = 2.dp,
                                            blur = 4.dp
                                        )
                                    } else {
                                        Modifier.neumorphicElevation(
                                            cornerRadius = 10.dp,
                                            backgroundColor = colors.surface,
                                            lightShadowColor = colors.lightShadow,
                                            darkShadowColor = colors.darkShadow,
                                            offset = 2.dp,
                                            blur = 4.dp
                                        )
                                    }
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedCategory = cat.id }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat.getDisplayName(isPt),
                                fontFamily = InterFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.5.sp,
                                color = if (isSelected) cat.primaryColor else colors.textMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Priority Selector
                Text(
                    text = if (isPt) "Prioridade:" else "Priority:",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    priorities.forEach { p ->
                        val isSelected = selectedPriority.equals(p, ignoreCase = true)
                        val pColor = when (p.lowercase()) {
                            "high" -> Color(0xFFEF4444)
                            "medium" -> Color(0xFFF59E0B)
                            else -> if (colors.isDark) NeonSkyBlue else Color(0xFF0284C7)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .then(
                                    if (isSelected) {
                                        Modifier.neumorphicInset(
                                            cornerRadius = 10.dp,
                                            backgroundColor = pColor.copy(alpha = 0.18f),
                                            darkShadowColor = colors.insetDark,
                                            lightShadowColor = colors.insetLight,
                                            offset = 2.dp,
                                            blur = 4.dp
                                        )
                                    } else {
                                        Modifier.neumorphicElevation(
                                            cornerRadius = 10.dp,
                                            backgroundColor = colors.surface,
                                            lightShadowColor = colors.lightShadow,
                                            darkShadowColor = colors.darkShadow,
                                            offset = 2.dp,
                                            blur = 4.dp
                                        )
                                    }
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedPriority = p }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (p.lowercase()) {
                                    "high" -> if (isPt) "Alta" else "High"
                                    "medium" -> if (isPt) "Média" else "Medium"
                                    else -> if (isPt) "Normal" else "Normal"
                                },
                                fontFamily = InterFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.5.sp,
                                color = if (isSelected) pColor else colors.textMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Icon selector
                Text(
                    text = if (isPt) "Ícone:" else "Icon:",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    icons.forEach { iconName ->
                        val isSelected = selectedIcon == iconName
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedIcon = iconName }
                                .then(
                                    if (isSelected) {
                                        Modifier.neumorphicInset(
                                            cornerRadius = 10.dp,
                                            backgroundColor = colors.surface,
                                            darkShadowColor = colors.insetDark,
                                            lightShadowColor = colors.insetLight
                                        )
                                    } else {
                                        Modifier.neumorphicElevation(
                                            cornerRadius = 10.dp,
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
                                size = 18.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onDismiss() }
                            .padding(horizontal = 16.dp, vertical = 9.dp)
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
                            .clip(RoundedCornerShape(12.dp))
                            .neumorphicElevation(
                                cornerRadius = 12.dp,
                                backgroundColor = colors.surface,
                                lightShadowColor = colors.lightShadow,
                                darkShadowColor = colors.darkShadow,
                                offset = 2.dp,
                                blur = 6.dp
                            )
                            .clickable(enabled = title.isNotBlank()) {
                                val days = daysText.toIntOrNull() ?: 5
                                onConfirm(title.trim(), days, selectedIcon, selectedPriority, selectedCategory)
                            }
                            .padding(horizontal = 18.dp, vertical = 9.dp)
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
