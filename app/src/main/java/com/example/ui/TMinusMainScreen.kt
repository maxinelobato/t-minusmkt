package com.example.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.ui.components.CalendarSyncDialog
import com.example.ui.components.CountdownLockScreenView
import com.example.ui.components.EditTaskDialog
import com.example.ui.components.SharePlanBottomSheet
import com.example.ui.components.SwipeableTaskItem
import com.example.ui.components.VoiceCreateTaskSheet
import com.example.ui.components.neumorphicElevation
import com.example.ui.components.triggerInstantHaptic
import com.example.ui.theme.BrandCyanLight
import com.example.ui.theme.BrandGreenDark
import com.example.ui.theme.InterFontFamily
import com.example.ui.theme.LocalNeumorphicColors
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.Zinc300
import com.example.ui.theme.Zinc400
import com.example.ui.theme.Zinc500
import com.example.ui.theme.Zinc600
import com.example.ui.theme.Zinc700
import com.example.util.CountdownBitmapRenderer
import com.example.util.NotificationHelper
import com.example.util.ShareHelper
import kotlinx.coroutines.launch

private const val TAB_TASKS = 0
private const val TAB_CALENDAR = 1
private const val TAB_LOCK_SCREEN = 2
private const val TAB_SHARE = 3

/**
 * Main Post-Login Screen for T-Minus Mkt strictly matching the Light (#E4E4E7)
 * and Dark (#27272A) Soft-Neumorphic Material 3 reference screens:
 * - Minimalist Home Task List (TopBar + Neumorphic Milestone Cards + Mic FAB)
 * - Clean 4-tab Material 3 NavigationBar with pill-shaped active indicator:
 *   1) Tarefas / Tasks
 *   2) Agenda / Calendar (Google Calendar Sync)
 *   3) Lock Screen (Countdown Lock Screen preview + Wallpaper/Widget export)
 *   4) Compartilhar / Share (Image Card Bitmap / Text Summary modal)
 */
@Composable
fun TMinusMainScreen(
    viewModel: TMinusViewModel,
    onLockApp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = LocalNeumorphicColors.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(TAB_TASKS) }
    var showVoiceCreateSheet by remember { mutableStateOf(false) }
    var spokenDraftText by remember { mutableStateOf("") }
    var showShareModal by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Handle system back press when previewing Lock Screen tab -> return to Home Tasks
    BackHandler(enabled = selectedTab != TAB_TASKS) {
        selectedTab = TAB_TASKS
    }

    // Speech Recognizer Activity Result Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!matches.isNullOrEmpty()) {
                spokenDraftText = matches[0]
            }
        }
        viewModel.setListening(false)
    }

    // Runtime RECORD_AUDIO Permission Launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setListening(true)
            launchSpeechInput(context, uiState.isPortuguese, speechLauncher)
        } else {
            viewModel.setListening(false)
            Toast.makeText(
                context,
                if (uiState.isPortuguese) {
                    "Permissão de microfone negada — você pode digitar sua etapa abaixo"
                } else {
                    "Microphone permission denied — you can type your milestone below"
                },
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val startVoiceCapture: () -> Unit = {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.setListening(true)
            launchSpeechInput(context, uiState.isPortuguese, speechLauncher)
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Feedback Toast / Undo Snackbar
    LaunchedEffect(uiState.feedbackToast) {
        uiState.feedbackToast?.let { msg ->
            scope.launch {
                if (uiState.lastDeletedTask != null) {
                    val result = snackbarHostState.showSnackbar(
                        message = msg,
                        actionLabel = if (uiState.isPortuguese) "DESFAZER" else "UNDO",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDelete()
                    }
                } else {
                    snackbarHostState.showSnackbar(msg)
                }
                viewModel.clearFeedbackToast()
            }
        }
    }

    // Voice + Manual Milestone Creation Sheet (launched by Mic FAB)
    if (showVoiceCreateSheet) {
        VoiceCreateTaskSheet(
            initialSpokenText = spokenDraftText,
            isListening = uiState.isListening,
            isPt = uiState.isPortuguese,
            onTriggerSpeech = startVoiceCapture,
            onDismiss = {
                showVoiceCreateSheet = false
                spokenDraftText = ""
                viewModel.setListening(false)
            },
            onCreateMilestone = { title, daysRemaining, iconType ->
                viewModel.addNewTask(title, daysRemaining, iconType)
                showVoiceCreateSheet = false
                spokenDraftText = ""
            }
        )
    }

    // Edit Milestone Dialog (triggered by swipe left on a task card)
    uiState.editingTask?.let { taskToEdit ->
        EditTaskDialog(
            task = taskToEdit,
            isPt = uiState.isPortuguese,
            onDismiss = { viewModel.dismissEditDialog() },
            onConfirm = { id, title, days, icon, priority, category ->
                viewModel.updateTask(id, title, days, icon, priority, category)
            }
        )
    }

    // Google Calendar Sync Queue Dialog (Tab 2: Agenda / Calendar)
    val allTasks = if (uiState.allTasks.isNotEmpty()) uiState.allTasks else uiState.tasks
    if (uiState.showCalendarDialog && allTasks.isNotEmpty()) {
        val currentTask = allTasks.getOrNull(uiState.calendarSyncIndex)
        CalendarSyncDialog(
            currentIndex = uiState.calendarSyncIndex,
            totalCount = allTasks.size,
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

    // Share Plan Modal Bottom Sheet (Tab 4: Compartilhar / Share)
    if (showShareModal) {
        SharePlanBottomSheet(
            isPt = uiState.isPortuguese,
            onDismiss = { showShareModal = false },
            onShareImageCard = {
                val bitmap = CountdownBitmapRenderer.renderShareCardBitmap(
                    tasks = allTasks,
                    daysRemaining = uiState.daysUntilDeadline,
                    hoursRemaining = uiState.countdownHours,
                    minutesRemaining = uiState.countdownMinutes,
                    secondsRemaining = uiState.countdownSeconds,
                    isPt = uiState.isPortuguese,
                    isDark = colors.isDark
                )
                ShareHelper.shareBitmap(
                    context = context,
                    bitmap = bitmap,
                    title = if (uiState.isPortuguese) "T-Minus Mkt • Planejamento de Marketing" else "T-Minus Mkt • Marketing Plan"
                )
            },
            onShareTextSummary = {
                val summary = CountdownBitmapRenderer.buildShareTextSummary(
                    tasks = allTasks,
                    daysRemaining = uiState.daysUntilDeadline,
                    hoursRemaining = uiState.countdownHours,
                    minutesRemaining = uiState.countdownMinutes,
                    secondsRemaining = uiState.countdownSeconds,
                    isPt = uiState.isPortuguese
                )
                ShareHelper.shareText(
                    context = context,
                    subject = if (uiState.isPortuguese) "T-Minus Mkt • Planejamento de Marketing" else "T-Minus Mkt • Marketing Plan",
                    body = summary
                )
            }
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (selectedTab == TAB_TASKS) {
                NeumorphicHomeTopBar(
                    isPt = uiState.isPortuguese,
                    isDark = colors.isDark,
                    onToggleTheme = {
                        triggerInstantHaptic(context)
                        viewModel.toggleTheme()
                    },
                    onToggleLanguage = {
                        triggerInstantHaptic(context)
                        viewModel.toggleLanguage()
                    }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            if (selectedTab == TAB_TASKS) {
                val fabInteraction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp, bottom = 6.dp)
                        .size(62.dp)
                        .neumorphicElevation(
                            cornerRadius = 31.dp,
                            backgroundColor = if (colors.isDark) Color(0xFF34343B) else Color(0xFFEBEBEF),
                            lightShadowColor = colors.lightShadow,
                            darkShadowColor = colors.darkShadow,
                            offset = 5.dp,
                            blur = 12.dp
                        )
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = fabInteraction,
                            indication = ripple(bounded = true),
                            onClick = {
                                triggerInstantHaptic(context)
                                spokenDraftText = ""
                                showVoiceCreateSheet = true
                                startVoiceCapture()
                            }
                        )
                        .testTag("voice_fab"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = if (uiState.isPortuguese) "Adicionar etapa por voz" else "Add milestone by voice",
                        tint = if (colors.isDark) Color(0xFFE4E4E7) else Color(0xFF3F3F46),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
            ) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = if (colors.isDark) Zinc700.copy(alpha = 0.55f) else Zinc300.copy(alpha = 0.85f)
                )
                NavigationBar(
                    containerColor = colors.background,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier.testTag("main_bottom_navigation_bar")
                ) {
                    val activePillColor = if (colors.isDark) Color(0xFF3C3C44) else Color(0xFFD6D6DE)
                    val activeIconColor = if (colors.isDark) Color.White else Color(0xFF18181B)
                    val inactiveColor = if (colors.isDark) Zinc400 else Zinc600

                    // Tab 1: Tarefas / Tasks
                    NavigationBarItem(
                        selected = selectedTab == TAB_TASKS,
                        onClick = {
                            triggerInstantHaptic(context)
                            selectedTab = TAB_TASKS
                        },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == TAB_TASKS) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                contentDescription = if (uiState.isPortuguese) "Tarefas" else "Tasks",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = if (uiState.isPortuguese) "Tarefas" else "Tasks",
                                fontFamily = InterFontFamily,
                                fontWeight = if (selectedTab == TAB_TASKS) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 11.5.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeIconColor,
                            selectedTextColor = activeIconColor,
                            unselectedIconColor = inactiveColor,
                            unselectedTextColor = inactiveColor,
                            indicatorColor = activePillColor
                        ),
                        modifier = Modifier.testTag("nav_tab_tasks")
                    )

                    // Tab 2: Agenda / Calendar (Google Calendar Sync)
                    NavigationBarItem(
                        selected = uiState.showCalendarDialog,
                        onClick = {
                            triggerInstantHaptic(context)
                            viewModel.startCalendarSync()
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = if (uiState.isPortuguese) "Agenda" else "Calendar",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = if (uiState.isPortuguese) "Agenda" else "Calendar",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.5.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeIconColor,
                            selectedTextColor = activeIconColor,
                            unselectedIconColor = inactiveColor,
                            unselectedTextColor = inactiveColor,
                            indicatorColor = activePillColor
                        ),
                        modifier = Modifier.testTag("nav_tab_calendar")
                    )

                    // Tab 3: Lock Screen (Countdown Lock Screen preview & wallpaper/widget export)
                    NavigationBarItem(
                        selected = selectedTab == TAB_LOCK_SCREEN,
                        onClick = {
                            triggerInstantHaptic(context)
                            selectedTab = TAB_LOCK_SCREEN
                        },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == TAB_LOCK_SCREEN) Icons.Filled.Lock else Icons.Outlined.Lock,
                                contentDescription = "Lock Screen",
                                modifier = Modifier.size(23.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Lock Screen",
                                fontFamily = InterFontFamily,
                                fontWeight = if (selectedTab == TAB_LOCK_SCREEN) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 11.5.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeIconColor,
                            selectedTextColor = activeIconColor,
                            unselectedIconColor = inactiveColor,
                            unselectedTextColor = inactiveColor,
                            indicatorColor = activePillColor
                        ),
                        modifier = Modifier.testTag("nav_tab_lock_screen")
                    )

                    // Tab 4: Compartilhar / Share (Bitmap/Text share sheet)
                    NavigationBarItem(
                        selected = showShareModal,
                        onClick = {
                            triggerInstantHaptic(context)
                            showShareModal = true
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = if (uiState.isPortuguese) "Compartilhar" else "Share",
                                modifier = Modifier.size(23.dp)
                            )
                        },
                        label = {
                            Text(
                                text = if (uiState.isPortuguese) "Compartilhar" else "Share",
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.5.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = activeIconColor,
                            selectedTextColor = activeIconColor,
                            unselectedIconColor = inactiveColor,
                            unselectedTextColor = inactiveColor,
                            indicatorColor = activePillColor
                        ),
                        modifier = Modifier.testTag("nav_tab_share")
                    )
                }
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = selectedTab,
            animationSpec = tween(240),
            label = "main_tab_crossfade",
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = if (selectedTab == TAB_LOCK_SCREEN) 0.dp else innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
        ) { currentTab ->
            when (currentTab) {
                TAB_LOCK_SCREEN -> {
                    CountdownLockScreenView(
                        tasks = allTasks,
                        daysRemaining = uiState.countdownDays,
                        hoursRemaining = uiState.countdownHours,
                        minutesRemaining = uiState.countdownMinutes,
                        secondsRemaining = uiState.countdownSeconds,
                        timeDisplay = uiState.timeDisplay,
                        dateDisplay = uiState.dateDisplay,
                        isPt = uiState.isPortuguese,
                        isDark = colors.isDark,
                        onToggleTask = { id, completed ->
                            viewModel.toggleTask(id, completed)
                        },
                        onFingerprintAction = {
                            selectedTab = TAB_TASKS
                        },
                        showExportControls = true,
                        onExportWallpaper = {
                            val applied = CountdownBitmapRenderer.applyLockScreenWallpaperOrShare(
                                context = context,
                                tasks = allTasks,
                                daysRemaining = uiState.countdownDays,
                                hoursRemaining = uiState.countdownHours,
                                minutesRemaining = uiState.countdownMinutes,
                                secondsRemaining = uiState.countdownSeconds,
                                isPt = uiState.isPortuguese,
                                isDark = colors.isDark
                            )
                            if (applied) {
                                Toast.makeText(
                                    context,
                                    if (uiState.isPortuguese) "Papel de parede da tela de bloqueio atualizado!" else "Lock screen wallpaper updated!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onPinWidgetNotification = {
                            com.example.data.UserPreferencesRepository(context).isSystemLockScreenEnabled = true
                            com.example.service.SystemLockScreenService.start(
                                context = context,
                                syncWallpaper = true
                            )
                            NotificationHelper.showOrUpdateCountdownNotification(
                                context = context,
                                tasks = allTasks,
                                daysRemaining = uiState.countdownDays,
                                hoursRemaining = uiState.countdownHours,
                                minutesRemaining = uiState.countdownMinutes,
                                isPt = uiState.isPortuguese,
                                triggerFullScreen = false
                            )
                            if (!NotificationHelper.canUseFullScreenIntent(context)) {
                                Toast.makeText(
                                    context,
                                    if (uiState.isPortuguese) {
                                        "Permita 'Notificações em tela cheia' para exibir o Lock Screen ao bloquear!"
                                    } else {
                                        "Allow 'Full-screen notifications' to display Lock Screen when locked!"
                                    },
                                    Toast.LENGTH_LONG
                                ).show()
                                NotificationHelper.openFullScreenIntentSettings(context)
                            } else {
                                Toast.makeText(
                                    context,
                                    if (uiState.isPortuguese) {
                                        "Lock Screen do Android ativado e sincronizado!"
                                    } else {
                                        "Android System Lock Screen active & synced!"
                                    },
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    // Strict Minimalist Soft-Neumorphic Task List Home Screen
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 8.dp,
                            bottom = 104.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("home_task_list")
                    ) {
                        items(allTasks, key = { it.id }) { task ->
                            SwipeableTaskItem(
                                task = task,
                                isPt = uiState.isPortuguese,
                                isDark = colors.isDark,
                                onToggle = { viewModel.toggleTask(task.id, task.isCompleted) },
                                onDelete = { viewModel.deleteTask(task) },
                                onEdit = { viewModel.openEditDialog(task) },
                                modifier = Modifier.widthIn(max = 540.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom Soft-Neumorphic TopAppBar matching the Light & Dark Task List reference screens:
 * - Left: Bold two-line title "Planejamento de\nMarketing" / "Marketing\nPlanning"
 * - Right: Compact Sun/Moon Theme Toggle pill + circular "PT"/"EN" Language Toggle button
 */
@Composable
private fun NeumorphicHomeTopBar(
    isPt: Boolean,
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit
) {
    val colors = LocalNeumorphicColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .statusBarsPadding()
            .padding(horizontal = 22.dp, vertical = 14.dp)
            .testTag("home_top_app_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 540.dp)
                .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
                    .testTag("top_bar_title")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "T-Minus ",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        lineHeight = 30.sp,
                        letterSpacing = (-0.6).sp,
                        color = if (isDark) Color.White else colors.textPrimary
                    )
                    Text(
                        text = "Mkt",
                        fontFamily = InterFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        lineHeight = 30.sp,
                        letterSpacing = (-0.6).sp,
                        color = if (isDark) BrandGreenDark else BrandCyanLight
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isPt) "Planejamento de Marketing" else "Marketing Planning",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = colors.textSecondary,
                    maxLines = 1
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Theme Toggle Pill (Sun / Moon icons side by side matching reference)
                val themeInteraction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .clickable(
                            interactionSource = themeInteraction,
                            indication = ripple(bounded = false, radius = 26.dp),
                            onClick = onToggleTheme
                        )
                        .testTag("theme_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .then(
                                if (isDark) {
                                    Modifier.neumorphicElevation(
                                        cornerRadius = 18.dp,
                                        backgroundColor = Color(0xFF232327),
                                        lightShadowColor = colors.lightShadow,
                                        darkShadowColor = colors.darkShadow,
                                        offset = 2.dp,
                                        blur = 5.dp
                                    )
                                } else {
                                    Modifier.background(Color.Transparent, RoundedCornerShape(18.dp))
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LightMode,
                            contentDescription = if (isPt) "Modo claro" else "Light mode",
                            tint = if (isDark) NeonYellow else Color(0xFF3F3F46),
                            modifier = Modifier.size(19.dp)
                        )
                        Icon(
                            imageVector = Icons.Outlined.DarkMode,
                            contentDescription = if (isPt) "Modo escuro" else "Dark mode",
                            tint = if (isDark) Color(0xFFE4E4E7) else Zinc500,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Compact Circular Language Toggle Chip ("PT" / "EN")
                val langInteraction = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(
                            interactionSource = langInteraction,
                            indication = ripple(bounded = false, radius = 24.dp),
                            onClick = onToggleLanguage
                        )
                        .testTag("language_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF3B3B42) else Color(0xFFD6D6DE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPt) "PT" else "EN",
                            fontFamily = InterFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isDark) Color.White else Color(0xFF27272A)
                        )
                    }
                }
            }
        }
    }
}

private fun launchSpeechInput(
    context: Context,
    isPt: Boolean,
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>
) {
    try {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isPt) "pt-BR" else "en-US")
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                if (isPt) "Fale sua nova etapa de marketing..." else "Speak your new marketing milestone..."
            )
        }
        launcher.launch(intent)
    } catch (_: Exception) {
        Toast.makeText(
            context,
            if (isPt) "Reconhecimento de voz indisponível — digite manualmente" else "Speech recognition unavailable — type manually",
            Toast.LENGTH_SHORT
        ).show()
    }
}
