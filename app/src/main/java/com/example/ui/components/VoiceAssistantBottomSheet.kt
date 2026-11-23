package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonSkyBlue
import com.example.util.SuggestedTask

data class AiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "ai"
    val text: String,
    val suggestedTask: SuggestedTask? = null,
    val isTaskAdded: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAssistantBottomSheet(
    isOpen: Boolean,
    isPt: Boolean,
    isDark: Boolean,
    messages: List<AiChatMessage>,
    isListening: Boolean,
    isThinking: Boolean,
    isSpeaking: Boolean,
    isVoiceOutputEnabled: Boolean,
    liveTranscript: String,
    onDismiss: () -> Unit,
    onToggleListening: () -> Unit,
    onToggleVoiceOutput: () -> Unit,
    onSendMessage: (String) -> Unit,
    onAddSuggestedTask: (SuggestedTask) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    if (!isOpen) return

    val colors = LocalNeumorphicColors.current
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, liveTranscript) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.textMuted.copy(alpha = 0.4f))
            )
        },
        modifier = Modifier.testTag("voice_assistant_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .neumorphicInset(
                                cornerRadius = 18.dp,
                                backgroundColor = colors.surface,
                                darkShadowColor = colors.insetDark,
                                lightShadowColor = colors.insetLight,
                                offset = 2.dp,
                                blur = 4.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        PhosphorIcon(
                            name = "rocket",
                            tint = NeonCyan,
                            size = 18.dp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isPt) "ESTRATEGISTA DE MARKETING IA" else "AI MARKETING STRATEGIST",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = if (isPt) "Gemini 3.8 Flash • Especialista em Lançamentos" else "Gemini 3.8 Flash • Launch Specialist",
                            fontFamily = InterFontFamily,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Speaker TTS Mute/Unmute Toggle
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .then(
                                if (isVoiceOutputEnabled) {
                                    Modifier.neumorphicInset(
                                        cornerRadius = 20.dp,
                                        backgroundColor = colors.surface,
                                        darkShadowColor = colors.insetDark,
                                        lightShadowColor = colors.insetLight,
                                        offset = 2.dp,
                                        blur = 4.dp
                                    )
                                } else {
                                    Modifier.neumorphicElevation(
                                        cornerRadius = 20.dp,
                                        backgroundColor = colors.surface,
                                        lightShadowColor = colors.lightShadow,
                                        darkShadowColor = colors.darkShadow,
                                        offset = 2.dp,
                                        blur = 5.dp
                                    )
                                }
                            )
                            .clip(CircleShape)
                            .fluidNeumorphicClick { onToggleVoiceOutput() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isVoiceOutputEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = "Voice Audio Toggle",
                            tint = if (isVoiceOutputEnabled) NeonCyan else colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Close Button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .neumorphicElevation(
                                cornerRadius = 20.dp,
                                backgroundColor = colors.surface,
                                lightShadowColor = colors.lightShadow,
                                darkShadowColor = colors.darkShadow,
                                offset = 2.dp,
                                blur = 5.dp
                            )
                            .clip(CircleShape)
                            .fluidNeumorphicClick { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated Audio Waveform Recessed Chamber
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .neumorphicInset(
                        cornerRadius = 18.dp,
                        backgroundColor = colors.surface,
                        darkShadowColor = colors.insetDark,
                        lightShadowColor = colors.insetLight,
                        offset = 3.dp,
                        blur = 6.dp
                    )
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    NeonAudioWaveform(
                        isListening = isListening,
                        isThinking = isThinking,
                        isSpeaking = isSpeaking,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Status and Live Transcription Text
                    val statusText = when {
                        isListening -> if (liveTranscript.isNotBlank()) liveTranscript else if (isPt) "Ouvindo você..." else "Listening to you..."
                        isThinking -> if (isPt) "Analisando estratégia com Gemini..." else "Analyzing strategy with Gemini..."
                        isSpeaking -> if (isPt) "Falando resposta estratégica..." else "Speaking strategic advice..."
                        else -> if (isPt) "Toque no microfone para falar ou digite abaixo" else "Tap microphone to speak or type below"
                    }

                    Text(
                        text = statusText,
                        fontFamily = InterFontFamily,
                        fontWeight = if (isListening) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.5.sp,
                        color = when {
                            isListening -> NeonCyan
                            isThinking -> NeonAmber
                            else -> colors.textSecondary
                        },
                        modifier = Modifier.padding(bottom = 8.dp, start = 12.dp, end = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chat Message Thread
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubbleItem(
                        message = msg,
                        isPt = isPt,
                        isDark = isDark,
                        onAddSuggestedTask = onAddSuggestedTask
                    )
                }

                if (isThinking) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = NeonCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPt) "Estrategista pensando..." else "Strategist thinking...",
                                fontFamily = InterFontFamily,
                                fontSize = 12.sp,
                                color = colors.textMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Prompt Chips
            val quickChips = if (isPt) {
                listOf(
                    "Sugerir etapa de tráfego pago",
                    "Como aquecer a lista VIP?",
                    "Checklist para dia do lançamento",
                    "Ideias de anúncios em vídeo"
                )
            } else {
                listOf(
                    "Suggest paid traffic milestone",
                    "How to warm up VIP list?",
                    "Launch day checklist",
                    "Video ad creative ideas"
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp, top = 2.dp)
            ) {
                items(quickChips) { chipText ->
                    Box(
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .neumorphicElevation(
                                cornerRadius = 12.dp,
                                backgroundColor = colors.surface,
                                lightShadowColor = colors.lightShadow,
                                darkShadowColor = colors.darkShadow,
                                offset = 2.dp,
                                blur = 5.dp
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .fluidNeumorphicClick { onSendMessage(chipText) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = chipText,
                            fontFamily = InterFontFamily,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            // Bottom Input Controls: Mic Button + Recessed Text Input Field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large Pulsing Mic Button
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .neumorphicElevation(
                            cornerRadius = 25.dp,
                            backgroundColor = if (isListening) NeonPink else NeonCyan,
                            lightShadowColor = colors.lightShadow,
                            darkShadowColor = colors.darkShadow,
                            offset = 3.dp,
                            blur = 7.dp
                        )
                        .clip(CircleShape)
                        .fluidNeumorphicClick { onToggleListening() }
                        .testTag("voice_assistant_mic_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Microphone",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Recessed Text Input
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .neumorphicInset(
                            cornerRadius = 16.dp,
                            backgroundColor = colors.surface,
                            darkShadowColor = colors.insetDark,
                            lightShadowColor = colors.insetLight,
                            offset = 2.dp,
                            blur = 4.dp
                        )
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = if (isPt) "Fale ou digite sua dúvida..." else "Speak or type question...",
                                fontFamily = InterFontFamily,
                                fontSize = 12.5.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank()) {
                                    onSendMessage(inputText.trim())
                                    inputText = ""
                                }
                            }
                        ),
                        trailingIcon = {
                            if (inputText.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(NeonCyan)
                                        .fluidNeumorphicClick {
                                            onSendMessage(inputText.trim())
                                            inputText = ""
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voice_assistant_input")
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: AiChatMessage,
    isPt: Boolean,
    isDark: Boolean,
    onAddSuggestedTask: (SuggestedTask) -> Unit
) {
    val isUser = message.sender == "user"
    val colors = LocalNeumorphicColors.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 2.dp)
                .neumorphicElevation(
                    cornerRadius = 16.dp,
                    backgroundColor = if (isUser) {
                        if (isDark) colors.surface else Color(0xFF0F172A)
                    } else {
                        colors.surface
                    },
                    lightShadowColor = colors.lightShadow,
                    darkShadowColor = colors.darkShadow,
                    offset = 3.dp,
                    blur = 7.dp
                )
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                fontFamily = InterFontFamily,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = if (isUser) {
                    Color.White
                } else {
                    colors.textPrimary
                }
            )
        }

        // If the AI response contains a suggested marketing task:
        message.suggestedTask?.let { task ->
            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(vertical = 2.dp)
                    .neumorphicElevation(
                        cornerRadius = 16.dp,
                        backgroundColor = colors.surface,
                        lightShadowColor = colors.lightShadow,
                        darkShadowColor = colors.darkShadow,
                        offset = 3.dp,
                        blur = 8.dp
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PhosphorIcon(
                            name = task.iconType,
                            tint = NeonGreen,
                            size = 18.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPt) "ETAPA SUGERIDA (T-${task.daysRemaining})" else "SUGGESTED MILESTONE (T-${task.daysRemaining})",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = NeonGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = task.title,
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.5.sp,
                        color = colors.textPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (message.isTaskAdded) Color(0xFF16A34A) else NeonGreen)
                            .fluidNeumorphicClick(enabled = !message.isTaskAdded) {
                                onAddSuggestedTask(task)
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (message.isTaskAdded) {
                                if (isPt) "✓ Adicionada ao Cronograma" else "✓ Added to Timeline"
                            } else {
                                if (isPt) "+ Adicionar ao Cronograma" else "+ Add to Timeline"
                            },
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
