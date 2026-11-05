package com.example.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TaskEntity
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.CalendarSyncDialog
import com.example.ui.components.NeumorphicBottomDock
import com.example.ui.components.NeumorphicCheckbox
import com.example.ui.components.PhosphorIcon
import com.example.ui.components.SwipeLockHandle
import com.example.ui.components.TMinusBrandLogo
import com.example.ui.components.neonGlow
import com.example.ui.components.neumorphicElevation
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonSkyBlue
import com.example.ui.theme.NeonYellow
import kotlinx.coroutines.launch

/**
 * Main Countdown List Screen strictly matching Reference Images 4 and 5:
 * - Giant digital clock & localized date
 * - Glowing cyan header card ("COUNTDOWN: MARKETING PLANNING" in Dark / soft white in Light)
 * - Quick action pills ("Sincronizar Google Agenda" & "Acesso")
 * - List of individual, tactile Neumorphic Task Cards with circular concave checkboxes and category neon icons
 * - Fixed 4-button dock (PT/EN, Mic, Bell, Sun/Moon)
 * - Bottom swipe handle ("SWIPE UP" / "DESLIZE")
 */
@Composable
fun TMinusMainScreen(
    viewModel: TMinusViewModel,
    onLockApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalNeumorphicColors.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Speech Recognizer launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!spokenMatches.isNullOrEmpty()) {
                val recognized = spokenMatches[0]
                viewModel.openAddDialog(recognized)
            }
        }
    }

    // Audio permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchSpeechInput(context, uiState.isPortuguese, speechLauncher)
        } else {
            Toast.makeText(
                context,
                if (uiState.isPortuguese) "Permissão de microfone necessária para comando de voz" else "Microphone permission required for voice input",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Feedback toast display
    LaunchedEffect(uiState.feedbackToast) {
        uiState.feedbackToast?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(msg)
                viewModel.clearFeedbackToast()
            }
        }
    }

    // Add Task Dialog
    if (uiState.showAddDialog) {
        AddTaskDialog(
            initialText = uiState.initialDialogText,
            isPt = uiState.isPortuguese,
            onDismiss = { viewModel.dismissAddDialog() },
            onConfirm = { title, daysRemaining, iconType ->
                viewModel.addNewTask(title, daysRemaining, iconType)
            }
        )
    }

    // Google Calendar Sync Queue Dialog
    if (uiState.showCalendarDialog && uiState.tasks.isNotEmpty()) {
        val currentTask = uiState.tasks.getOrNull(uiState.calendarSyncIndex)
        CalendarSyncDialog(
            currentIndex = uiState.calendarSyncIndex,
            totalCount = uiState.tasks.size,
            currentTask = currentTask,
            isPt = uiState.isPortuguese,
            onAddToCalendar = { task ->
                viewModel.advanceCalendarSync(context, task)
            },
            onSkip = {
                viewModel.skipCalendarSync()
            },
            onDismiss = {
                viewModel.dismissCalendarSync()
            }
        )
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(top = statusBarPadding, bottom = navBarPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // LazyColumn for Scrollable Content: Clock + Header + Action Badges + Task Cards
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Item: TOP CLOCK & DATE (Images 4 & 5)
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = uiState.timeDisplay,
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 48.sp,
                                letterSpacing = (-1.5).sp,
                                color = colors.textPrimary
                            )

                            if (uiState.amPmDisplay.isNotBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = uiState.amPmDisplay,
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = uiState.dateDisplay,
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.5.sp,
                            color = colors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                // Item: COUNTDOWN HEADER CARD with Brand Logo
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TMinusBrandLogo(
                            height = 42.dp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .then(
                                if (colors.isDark) {
                                    Modifier
                                        .neonGlow(
                                            color = NeonCyan,
                                            cornerRadius = 22.dp,
                                            glowRadius = 14.dp,
                                            borderWidth = 1.6.dp
                                        )
                                        .background(colors.surface)
                                } else {
                                    Modifier.neumorphicElevation(
                                        cornerRadius = 22.dp,
                                        backgroundColor = colors.surface,
                                        lightShadowColor = colors.lightShadow,
                                        darkShadowColor = colors.darkShadow,
                                        offset = 4.dp,
                                        blur = 10.dp
                                    )
                                }
                            )
                            .padding(vertical = 14.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (uiState.isPortuguese) "CONTAGEM REGRESSIVA:" else "COUNTDOWN:",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.5.sp,
                                letterSpacing = 1.6.sp,
                                color = if (colors.isDark) Color(0xFFF1F5F9) else colors.textPrimary
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = if (uiState.isPortuguese) "PLANEJAMENTO DE MARKETING" else "MARKETING PLANNING",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                letterSpacing = 1.2.sp,
                                color = if (colors.isDark) NeonCyan else colors.textSecondary
                            )
                        }
                    }
                }
            }

                // Item: QUICK ACTION PILLS (Reference Image 5: "Sincronizar Google Agenda" & "Acesso")
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sincronizar Google Agenda Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .then(
                                    if (colors.isDark) {
                                        Modifier
                                            .background(Color(0xFF161C26))
                                            .border(1.dp, Color(0xFF243040), RoundedCornerShape(14.dp))
                                    } else {
                                        Modifier.neumorphicElevation(
                                            cornerRadius = 14.dp,
                                            backgroundColor = colors.surface,
                                            lightShadowColor = colors.lightShadow,
                                            darkShadowColor = colors.darkShadow,
                                            offset = 2.5.dp,
                                            blur = 5.dp
                                        )
                                    }
                                )
                                .clickable { viewModel.startCalendarSync() }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("sync_calendar_pill")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PhosphorIcon(
                                    name = "calendar",
                                    tint = if (colors.isDark) NeonSkyBlue else colors.textSecondary,
                                    size = 14.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (uiState.isPortuguese) "Sincronizar Google Agenda" else "Sync Google Calendar",
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp,
                                    color = colors.textPrimary
                                )
                            }
                        }

                        // Acesso / Bloquear Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .then(
                                    if (colors.isDark) {
                                        Modifier
                                            .background(Color(0xFF161C26))
                                            .border(1.dp, Color(0xFF243040), RoundedCornerShape(14.dp))
                                    } else {
                                        Modifier.neumorphicElevation(
                                            cornerRadius = 14.dp,
                                            backgroundColor = colors.surface,
                                            lightShadowColor = colors.lightShadow,
                                            darkShadowColor = colors.darkShadow,
                                            offset = 2.5.dp,
                                            blur = 5.dp
                                        )
                                    }
                                )
                                .clickable { onLockApp() }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("lock_access_pill")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PhosphorIcon(
                                    name = "lock",
                                    tint = if (colors.isDark) NeonSkyBlue else colors.textSecondary,
                                    size = 14.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (uiState.isPortuguese) "Acesso" else "Access",
                                    fontFamily = InterFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }
                }

                // Items: INDIVIDUAL TASK CARDS (Faithful to Reference Images 4 & 5)
                items(uiState.tasks, key = { it.id }) { task ->
                    IndividualTaskCard(
                        task = task,
                        isPt = uiState.isPortuguese,
                        isDark = colors.isDark,
                        onToggle = { viewModel.toggleTask(task.id, task.isCompleted) }
                    )
                }
            }

            // BOTTOM CONTROLS & SWIPE HANDLE
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NeumorphicBottomDock(
                    isPt = uiState.isPortuguese,
                    isDark = colors.isDark,
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onVoiceClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                        ) {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            launchSpeechInput(context, uiState.isPortuguese, speechLauncher)
                        }
                    },
                    onNotificationClick = {
                        val days = uiState.daysUntilDeadline
                        Toast.makeText(
                            context,
                            if (uiState.isPortuguese) "T-Minus: $days dias restantes para o lançamento!" else "T-Minus: $days days remaining until launch!",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onToggleTheme = { viewModel.toggleTheme() }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SwipeLockHandle(
                    isLocked = false,
                    onUnlock = {},
                    onLock = { onLockApp() },
                    label = if (uiState.isPortuguese) "DESLIZE" else "SWIPE UP"
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )
    }
}

/**
 * Individual Neumorphic Task Card matching Images 4 & 5:
 * - Left: Sunken circular concave well checkbox
 * - Center: Worded timeline title in bold + task description
 * - Right: Glowing category icon with color-coded theme
 */
@Composable
private fun IndividualTaskCard(
    task: TaskEntity,
    isPt: Boolean,
    isDark: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalNeumorphicColors.current
    val timelineText = if (isPt) task.timelinePt else task.timelineEn
    val titleText = if (isPt) task.titlePt else task.titleEn

    // Distinct Category Neon Colors (Cyan, Purple, Amber, Pink, Green)
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
        colors.textPrimary
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .then(
                if (isDark) {
                    Modifier
                        .background(colors.surface)
                        .border(1.dp, colors.lightShadow.copy(alpha = 0.22f), RoundedCornerShape(22.dp))
                } else {
                    Modifier.neumorphicElevation(
                        cornerRadius = 22.dp,
                        backgroundColor = colors.surface,
                        lightShadowColor = colors.lightShadow,
                        darkShadowColor = colors.darkShadow,
                        offset = 4.dp,
                        blur = 9.dp
                    )
                }
            )
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("task_item_${task.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tactile Circular Concave Well Checkbox
            NeumorphicCheckbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                activeColor = if (isDark) iconColor else null,
                testTag = "task_checkbox_${task.id}"
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Milestone and Task description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timelineText,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = titleText,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    color = if (task.isCompleted) colors.textMuted else colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Themed Category Icon
            PhosphorIcon(
                name = task.iconType,
                tint = iconColor,
                glowColor = if (isDark) iconColor else null,
                size = 24.dp
            )
        }
    }
}

private fun launchSpeechInput(
    context: Context,
    isPortuguese: Boolean,
    launcher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>
) {
    try {
        val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                if (isPortuguese) "pt-BR" else "en-US"
            )
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                if (isPortuguese) "Fale a nova etapa de marketing..." else "Speak your marketing milestone..."
            )
        }
        launcher.launch(intent)
    } catch (_: Exception) {
        Toast.makeText(
            context,
            if (isPortuguese) "Reconhecimento de voz indisponível neste dispositivo" else "Speech recognition not available on this device",
            Toast.LENGTH_SHORT
        ).show()
    }
}
