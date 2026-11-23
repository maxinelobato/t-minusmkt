package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandCyanLight
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonYellow

/**
 * Voice-first Milestone Creation Modal Bottom Sheet launched from the Neumorphic Mic FAB.
 * Supports one-tap SpeechRecognizer dictation plus inline manual entry & icon selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCreateTaskSheet(
    initialSpokenText: String,
    isListening: Boolean,
    isPt: Boolean,
    onTriggerSpeech: () -> Unit,
    onDismiss: () -> Unit,
    onCreateMilestone: (title: String, daysRemaining: Int, iconType: String) -> Unit
) {
    val colors = LocalNeumorphicColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(initialSpokenText) }
    var daysText by remember { mutableStateOf("10") }
    var selectedIcon by remember { mutableStateOf("megaphone") }

    // Synchronize whenever speech recognition returns a fresh transcript
    LaunchedEffect(initialSpokenText) {
        if (initialSpokenText.isNotBlank()) {
            title = initialSpokenText
        }
    }

    val icons = listOf("search", "article", "megaphone", "checkcircle", "rocket")

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val micScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.12f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_scale"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        contentColor = colors.textPrimary,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .testTag("voice_create_task_sheet"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isPt) "Nova Etapa por Voz ou Texto" else "New Milestone by Voice or Text",
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Prominent Neumorphic Voice Capture Trigger
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(micScale)
                    .neumorphicElevation(
                        cornerRadius = 36.dp,
                        backgroundColor = colors.surface,
                        lightShadowColor = colors.lightShadow,
                        darkShadowColor = colors.darkShadow,
                        offset = 5.dp,
                        blur = 10.dp
                    )
                    .clip(CircleShape)
                    .clickable { onTriggerSpeech() }
                    .testTag("sheet_voice_trigger_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = if (isPt) "Falar etapa" else "Dictate milestone",
                    tint = if (isListening) NeonPink else if (colors.isDark) NeonCyan else BrandCyanLight,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isListening) {
                    if (isPt) "Ouvindo seu comando..." else "Listening..."
                } else {
                    if (isPt) "Toque no microfone para ditar ou digite abaixo" else "Tap the microphone to dictate or type below"
                },
                fontFamily = InterFontFamily,
                fontSize = 12.5.sp,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Manual Milestone Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = {
                    Text(
                        text = if (isPt) "Descrição da etapa" else "Milestone description",
                        fontFamily = InterFontFamily,
                        fontSize = 12.5.sp
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedBorderColor = if (colors.isDark) NeonCyan else BrandCyanLight,
                    unfocusedBorderColor = colors.textMuted
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voice_sheet_title_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Days Remaining (T-Minus)
            OutlinedTextField(
                value = daysText,
                onValueChange = { input ->
                    if (input.all { it.isDigit() } && input.length <= 3) daysText = input
                },
                label = {
                    Text(
                        text = if (isPt) "Dias antes do lançamento (T-Minus)" else "Days before launch (T-Minus)",
                        fontFamily = InterFontFamily,
                        fontSize = 12.5.sp
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedBorderColor = if (colors.isDark) NeonCyan else BrandCyanLight,
                    unfocusedBorderColor = colors.textMuted
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voice_sheet_days_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Icon Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                icons.forEach { iconName ->
                    val isSelected = selectedIcon == iconName
                    val accent = when (iconName) {
                        "search" -> NeonCyan
                        "article" -> NeonPurple
                        "megaphone" -> NeonYellow
                        "checkcircle" -> NeonPink
                        else -> NeonGreen
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .then(
                                if (isSelected) {
                                    Modifier.neumorphicInset(
                                        cornerRadius = 14.dp,
                                        backgroundColor = colors.surface,
                                        darkShadowColor = colors.insetDark,
                                        lightShadowColor = colors.insetLight
                                    )
                                } else {
                                    Modifier.neumorphicElevation(
                                        cornerRadius = 14.dp,
                                        backgroundColor = colors.surface,
                                        lightShadowColor = colors.lightShadow,
                                        darkShadowColor = colors.darkShadow,
                                        offset = 3.dp,
                                        blur = 6.dp
                                    )
                                }
                            )
                            .clickable { selectedIcon = iconName },
                        contentAlignment = Alignment.Center
                    ) {
                        PhosphorIcon(
                            name = iconName,
                            tint = if (isSelected) (if (colors.isDark) accent else colors.textPrimary) else colors.textMuted,
                            glowColor = if (isSelected && colors.isDark) accent else null,
                            size = 22.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = {
                    val days = daysText.toIntOrNull()?.coerceAtLeast(1) ?: 7
                    if (title.isNotBlank()) {
                        onCreateMilestone(title.trim(), days, selectedIcon)
                    }
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (colors.isDark) NeonCyan else colors.textPrimary,
                    contentColor = if (colors.isDark) Color(0xFF09090B) else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("voice_sheet_save_btn")
            ) {
                Text(
                    text = if (isPt) "Adicionar Etapa" else "Add Milestone",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
