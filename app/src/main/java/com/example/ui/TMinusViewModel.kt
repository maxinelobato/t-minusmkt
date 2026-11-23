package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.TaskRepository
import com.example.data.model.TaskEntity
import com.example.util.CalendarSyncHelper
import com.example.util.NotificationHelper
import com.example.util.SecurityManager
import com.example.util.UserSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.data.CountdownTimerRepository
import com.example.data.UserPreferencesRepository
import com.example.data.model.CountdownTimerEntity
import com.example.ui.components.AiChatMessage
import com.example.util.CountdownBitmapRenderer
import com.example.util.LaunchNotificationScheduler
import com.example.util.MarketingAiService
import com.example.util.SuggestedTask
import com.example.util.TextToSpeechHelper
import java.util.concurrent.TimeUnit

enum class MilestoneSortMode {
    TIMELINE,
    DUE_DATE,
    PRIORITY
}

data class TMinusUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val allTasks: List<TaskEntity> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val milestoneSortMode: MilestoneSortMode = MilestoneSortMode.TIMELINE,
    val isPortuguese: Boolean = true,
    val isDarkMode: Boolean = true,
    val isLocked: Boolean = false,
    val timeDisplay: String = "08:30",
    val amPmDisplay: String = "",
    val dateDisplay: String = "Sábado, 07 de Nov",
    val daysUntilDeadline: Long = 33,
    val showAddDialog: Boolean = false,
    val initialDialogText: String = "",
    val feedbackToast: String? = null,
    val editingTask: TaskEntity? = null,
    val lastDeletedTask: TaskEntity? = null,
    // Google Auth & Biometric security states
    val isAuthenticated: Boolean = false,
    val hasStoredSession: Boolean = false,
    val userSession: UserSession? = null,
    // Calendar sync states
    val showCalendarDialog: Boolean = false,
    val calendarSyncIndex: Int = 0,
    // Active milestone for Lock Screen notification
    val activeTask: TaskEntity? = null,
    // Multiple Marketing Launch Countdown Timers
    val countdownTimers: List<CountdownTimerEntity> = emptyList(),
    val activeCountdownTimer: CountdownTimerEntity? = null,
    val countdownDays: Long = 33L,
    val countdownHours: Long = 0L,
    val countdownMinutes: Long = 0L,
    val countdownSeconds: Long = 0L,
    val showCampaignManagerDialog: Boolean = false,
    val showAddTimerDialog: Boolean = false,
    val editingTimer: CountdownTimerEntity? = null,
    // AI Voice Assistant states
    val isVoiceAssistantOpen: Boolean = false,
    val isListening: Boolean = false,
    val isAiThinking: Boolean = false,
    val isAiSpeaking: Boolean = false,
    val isVoiceOutputEnabled: Boolean = true,
    val liveTranscript: String = "",
    val voiceChatMessages: List<AiChatMessage> = emptyList()
)

class TMinusViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    private val countdownTimerRepository: CountdownTimerRepository
    private val securityManager = SecurityManager(application)
    private val userPreferences = UserPreferencesRepository(application)

    private val _isPortuguese = MutableStateFlow(userPreferences.isPortuguese)
    private val _isDarkMode = MutableStateFlow(userPreferences.isDarkMode)
    private val _milestoneSortMode = MutableStateFlow(MilestoneSortMode.TIMELINE)
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _isLocked = MutableStateFlow(false)
    private val _timeDisplay = MutableStateFlow("08:30")
    private val _amPmDisplay = MutableStateFlow("")
    private val _dateDisplay = MutableStateFlow("")
    private val _daysUntilDeadline = MutableStateFlow(33L)
    private val _showAddDialog = MutableStateFlow(false)
    private val _initialDialogText = MutableStateFlow("")
    private val _editingTask = MutableStateFlow<TaskEntity?>(null)
    private val _lastDeletedTask = MutableStateFlow<TaskEntity?>(null)
    private val _feedbackToast = MutableStateFlow<String?>(null)

    // Multiple Marketing Launch Countdown Timers StateFlows
    private val _countdownTimers = MutableStateFlow<List<CountdownTimerEntity>>(emptyList())
    private val _activeCountdownTimer = MutableStateFlow<CountdownTimerEntity?>(null)
    private val _countdownDays = MutableStateFlow(33L)
    private val _countdownHours = MutableStateFlow(0L)
    private val _countdownMinutes = MutableStateFlow(0L)
    private val _countdownSeconds = MutableStateFlow(0L)
    private val _showCampaignManagerDialog = MutableStateFlow(false)
    private val _showAddTimerDialog = MutableStateFlow(false)
    private val _editingTimer = MutableStateFlow<CountdownTimerEntity?>(null)

    // Security & Auth StateFlows (restored immediately from SecurityManager so login persists)
    private val initialStoredSession: UserSession? = securityManager.getSession()
    private val initialIsLoggedIn: Boolean = securityManager.isLoggedIn()
    private val _isAuthenticated = MutableStateFlow(initialIsLoggedIn)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()
    private val _hasStoredSession = MutableStateFlow(initialStoredSession != null)
    private val _userSession = MutableStateFlow(initialStoredSession)

    // Calendar Sync StateFlows
    private val _showCalendarDialog = MutableStateFlow(false)
    private val _calendarSyncIndex = MutableStateFlow(0)

    // AI Voice Assistant StateFlows & Services
    private val aiService = MarketingAiService(application)
    private val ttsHelper = TextToSpeechHelper(application)
    private val _isVoiceAssistantOpen = MutableStateFlow(false)
    private val _isListening = MutableStateFlow(false)
    private val _isAiThinking = MutableStateFlow(false)
    private val _isAiSpeaking = MutableStateFlow(false)
    private val _isVoiceOutputEnabled = MutableStateFlow(true)
    private val _liveTranscript = MutableStateFlow("")
    private val _voiceChatMessages = MutableStateFlow<List<AiChatMessage>>(emptyList())

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TaskRepository(database.taskDao())
        countdownTimerRepository = CountdownTimerRepository(database.countdownTimerDao())

        viewModelScope.launch {
            repository.checkAndSeedDefaults()
        }

        viewModelScope.launch {
            countdownTimerRepository.seedInitialTimersIfEmpty()
        }

        viewModelScope.launch {
            countdownTimerRepository.allTimers.collect { timers ->
                _countdownTimers.value = timers
                val now = System.currentTimeMillis()
                for (timer in timers) {
                    if (timer.targetTimestamp > now) {
                        LaunchNotificationScheduler.scheduleCountdownAlerts(application, timer, _isPortuguese.value)
                    }
                }
            }
        }

        viewModelScope.launch {
            countdownTimerRepository.activeTimer.collect { active ->
                _activeCountdownTimer.value = active
                updateCountdownTicker()
            }
        }

        // Keep Android System Lock Screen (all-tasks public notification + FLAG_LOCK wallpaper)
        // automatically synced whenever tasks, language, or theme change
        viewModelScope.launch {
            combine(repository.allTasks, _isPortuguese, _isDarkMode) { tasks, isPt, isDark ->
                Triple(tasks, isPt, isDark)
            }.collectLatest { (tasks, isPt, isDark) ->
                if (!userPreferences.isSystemLockScreenEnabled) return@collectLatest
                if (tasks.isEmpty()) return@collectLatest
                delay(500L)
                val sorted = tasks.sortedWith(compareBy({ it.orderIndex }, { it.id }))
                NotificationHelper.showOrUpdateCountdownNotification(
                    context = application,
                    tasks = sorted,
                    daysRemaining = _countdownDays.value,
                    hoursRemaining = _countdownHours.value,
                    minutesRemaining = _countdownMinutes.value,
                    isPt = isPt,
                    triggerFullScreen = false
                )
                withContext(kotlinx.coroutines.Dispatchers.IO) {
                    CountdownBitmapRenderer.applyLockScreenWallpaperSilently(
                        context = application,
                        tasks = sorted,
                        daysRemaining = _countdownDays.value,
                        hoursRemaining = _countdownHours.value,
                        minutesRemaining = _countdownMinutes.value,
                        secondsRemaining = _countdownSeconds.value,
                        isPt = isPt,
                        isDark = isDark
                    )
                }
            }
        }

        // Start live clock and countdown ticker
        viewModelScope.launch {
            while (true) {
                updateTimeAndDate()
                updateCountdownTicker()
                delay(1000L)
            }
        }
    }

    private val authAndCalendarState = combine(
        _isAuthenticated,
        _hasStoredSession,
        _userSession,
        _showCalendarDialog,
        _calendarSyncIndex
    ) { isAuth, hasStored, session, showCal, calIdx ->
        AuthAndCalendarSnapshot(isAuth, hasStored, session, showCal, calIdx)
    }

    private data class AuthAndCalendarSnapshot(
        val isAuthenticated: Boolean,
        val hasStoredSession: Boolean,
        val userSession: UserSession?,
        val showCalendarDialog: Boolean,
        val calendarSyncIndex: Int
    )

    val uiState: StateFlow<TMinusUiState> = combine(
        repository.allTasks.onStart { emit(emptyList()) },
        _isPortuguese,
        _isDarkMode,
        _isLocked,
        _timeDisplay,
        _amPmDisplay,
        _dateDisplay,
        _countdownSeconds,
        _showAddDialog,
        _initialDialogText,
        _editingTask,
        _lastDeletedTask,
        _feedbackToast,
        _isVoiceAssistantOpen,
        _isListening,
        _isAiThinking,
        _isAiSpeaking,
        _isVoiceOutputEnabled,
        _liveTranscript,
        _voiceChatMessages,
        _milestoneSortMode,
        _selectedCategory,
        _searchQuery,
        authAndCalendarState
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val tasks = args[0] as List<TaskEntity>
        val isPt = args[1] as Boolean
        val isDark = args[2] as Boolean
        val isLocked = args[3] as Boolean
        val timeDisplay = args[4] as String
        val amPmDisplay = args[5] as String
        val dateDisplay = args[6] as String
        val currentSeconds = args[7] as Long
        val showAdd = args[8] as Boolean
        val initialText = args[9] as String
        val editing = args[10] as TaskEntity?
        val lastDeleted = args[11] as TaskEntity?
        val feedback = args[12] as String?
        val isVoiceAssistantOpen = args[13] as Boolean
        val isListening = args[14] as Boolean
        val isAiThinking = args[15] as Boolean
        val isAiSpeaking = args[16] as Boolean
        val isVoiceOutputEnabled = args[17] as Boolean
        val liveTranscript = args[18] as String
        @Suppress("UNCHECKED_CAST")
        val voiceChatMessages = args[19] as List<AiChatMessage>
        val sortMode = args[20] as MilestoneSortMode
        val selectedCategory = args[21] as String?
        val searchQuery = args[22] as String
        val authCal = args[23] as AuthAndCalendarSnapshot

        val sortedTasks = when (sortMode) {
            MilestoneSortMode.TIMELINE -> tasks.sortedWith(compareBy({ it.orderIndex }, { it.id }))
            MilestoneSortMode.DUE_DATE -> tasks.sortedWith(compareBy({ it.daysRemaining }, { it.orderIndex }))
            MilestoneSortMode.PRIORITY -> tasks.sortedWith(
                compareBy(
                    { task ->
                        when (task.priority.lowercase()) {
                            "high", "alta" -> 0
                            "medium", "média", "media" -> 1
                            else -> 2
                        }
                    },
                    { it.daysRemaining }
                )
            )
        }

        val normalizedQuery = searchQuery.trim().lowercase()
        val filteredSortedTasks = sortedTasks.filter { task ->
            val matchesCategory = selectedCategory == null ||
                task.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = normalizedQuery.isEmpty() ||
                task.titlePt.lowercase().contains(normalizedQuery) ||
                task.titleEn.lowercase().contains(normalizedQuery) ||
                task.timelinePt.lowercase().contains(normalizedQuery) ||
                task.timelineEn.lowercase().contains(normalizedQuery)
            matchesCategory && matchesSearch
        }

        val activeTask = sortedTasks.firstOrNull { !it.isCompleted } ?: sortedTasks.firstOrNull()

        TMinusUiState(
            tasks = filteredSortedTasks,
            allTasks = sortedTasks,
            selectedCategory = selectedCategory,
            searchQuery = searchQuery,
            milestoneSortMode = sortMode,
            isPortuguese = isPt,
            isDarkMode = isDark,
            isLocked = isLocked,
            timeDisplay = timeDisplay,
            amPmDisplay = amPmDisplay,
            dateDisplay = dateDisplay,
            daysUntilDeadline = _countdownDays.value,
            showAddDialog = showAdd,
            initialDialogText = initialText,
            feedbackToast = feedback,
            editingTask = editing,
            lastDeletedTask = lastDeleted,
            isAuthenticated = authCal.isAuthenticated,
            hasStoredSession = authCal.hasStoredSession,
            userSession = authCal.userSession,
            showCalendarDialog = authCal.showCalendarDialog,
            calendarSyncIndex = authCal.calendarSyncIndex,
            activeTask = activeTask,
            // Multi-timer Room state & live ticker
            countdownTimers = _countdownTimers.value,
            activeCountdownTimer = _activeCountdownTimer.value,
            countdownDays = _countdownDays.value,
            countdownHours = _countdownHours.value,
            countdownMinutes = _countdownMinutes.value,
            countdownSeconds = currentSeconds,
            showCampaignManagerDialog = _showCampaignManagerDialog.value,
            showAddTimerDialog = _showAddTimerDialog.value,
            editingTimer = _editingTimer.value,
            isVoiceAssistantOpen = isVoiceAssistantOpen,
            isListening = isListening,
            isAiThinking = isAiThinking,
            isAiSpeaking = isAiSpeaking,
            isVoiceOutputEnabled = isVoiceOutputEnabled,
            liveTranscript = liveTranscript,
            voiceChatMessages = voiceChatMessages
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = TMinusUiState(
            isPortuguese = userPreferences.isPortuguese,
            isDarkMode = userPreferences.isDarkMode,
            isAuthenticated = initialIsLoggedIn,
            hasStoredSession = initialStoredSession != null,
            userSession = initialStoredSession
        )
    )

    private fun updateTimeAndDate() {
        val now = Calendar.getInstance()
        val isPt = _isPortuguese.value

        if (isPt) {
            val ptLocale = Locale("pt", "BR")
            val timeFmt = SimpleDateFormat("HH:mm", ptLocale)
            _timeDisplay.value = timeFmt.format(now.time)
            _amPmDisplay.value = ""

            val shortDay = SimpleDateFormat("EEE", ptLocale).format(now.time)
                .trimEnd('.')
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(ptLocale) else it.toString() }
            val dayNum = SimpleDateFormat("dd", ptLocale).format(now.time)
            val shortMonth = SimpleDateFormat("MMM", ptLocale).format(now.time)
                .trimEnd('.')
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(ptLocale) else it.toString() }
            _dateDisplay.value = "$shortDay, $dayNum de $shortMonth"
        } else {
            val timeFmt = SimpleDateFormat("HH:mm", Locale.US)
            _timeDisplay.value = timeFmt.format(now.time)
            _amPmDisplay.value = ""

            val shortDay = SimpleDateFormat("EEE", Locale.US).format(now.time)
            val dayAndMonth = SimpleDateFormat("MMM dd", Locale.US).format(now.time)
            _dateDisplay.value = "$shortDay, $dayAndMonth"
        }
    }

    private fun updateCountdownTicker() {
        val target = _activeCountdownTimer.value?.targetTimestamp ?: run {
            val deadlineCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, 2026)
                set(Calendar.MONTH, Calendar.DECEMBER)
                set(Calendar.DAY_OF_MONTH, 8)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }
            deadlineCal.timeInMillis
        }
        val diff = target - System.currentTimeMillis()
        if (diff > 0) {
            val secs = (diff / 1000) % 60
            val mins = (diff / (1000 * 60)) % 60
            val hrs = (diff / (1000 * 60 * 60)) % 24
            val days = diff / (1000 * 60 * 60 * 24)

            _countdownDays.value = days
            _countdownHours.value = hrs
            _countdownMinutes.value = mins
            _countdownSeconds.value = secs
            _daysUntilDeadline.value = days
        } else {
            _countdownDays.value = 0
            _countdownHours.value = 0
            _countdownMinutes.value = 0
            _countdownSeconds.value = 0
            _daysUntilDeadline.value = 0
        }
    }

    // MULTI-TIMER ACTIONS (Room Database)
    fun openCampaignManager() {
        _showCampaignManagerDialog.value = true
    }

    fun closeCampaignManager() {
        _showCampaignManagerDialog.value = false
    }

    fun openAddTimerDialog() {
        _editingTimer.value = null
        _showAddTimerDialog.value = true
    }

    fun openEditTimerDialog(timer: CountdownTimerEntity) {
        _editingTimer.value = timer
        _showAddTimerDialog.value = true
    }

    fun closeAddTimerDialog() {
        _showAddTimerDialog.value = false
        _editingTimer.value = null
    }

    fun saveCountdownTimer(
        title: String,
        targetTimestamp: Long,
        categoryTag: String,
        notes: String,
        accentColorHex: String
    ) {
        viewModelScope.launch {
            val current = _editingTimer.value
            if (current != null) {
                countdownTimerRepository.updateTimer(
                    current.copy(
                        title = title,
                        targetTimestamp = targetTimestamp,
                        categoryTag = categoryTag,
                        notes = notes,
                        accentColorHex = accentColorHex
                    )
                )
                _feedbackToast.value = if (_isPortuguese.value) {
                    "Campanha '$title' atualizada!"
                } else {
                    "Campaign '$title' updated!"
                }
            } else {
                val newTimer = CountdownTimerEntity(
                    title = title,
                    targetTimestamp = targetTimestamp,
                    categoryTag = categoryTag,
                    notes = notes,
                    accentColorHex = accentColorHex,
                    isActive = false
                )
                countdownTimerRepository.insertTimer(newTimer)
                _feedbackToast.value = if (_isPortuguese.value) {
                    "Campanha '$title' adicionada com sucesso!"
                } else {
                    "Campaign '$title' added successfully!"
                }
            }
            closeAddTimerDialog()
            updateCountdownTicker()
        }
    }

    fun selectActiveTimer(id: Long) {
        viewModelScope.launch {
            countdownTimerRepository.selectActiveTimer(id)
            val selected = _countdownTimers.value.firstOrNull { it.id == id }
            val name = selected?.title ?: ""
            _feedbackToast.value = if (_isPortuguese.value) {
                "Campanha '$name' ativada no cronômetro!"
            } else {
                "Campaign '$name' set as active timer!"
            }
            updateCountdownTicker()
        }
    }

    fun deleteTimer(id: Long) {
        viewModelScope.launch {
            val toDelete = _countdownTimers.value.firstOrNull { it.id == id }
            countdownTimerRepository.deleteTimerById(id)
            _feedbackToast.value = if (_isPortuguese.value) {
                "Campanha '${toDelete?.title ?: ""}' excluída"
            } else {
                "Campaign '${toDelete?.title ?: ""}' deleted"
            }
            updateCountdownTicker()
        }
    }

    // AUTH & BIOMETRIC ACTIONS
    fun onGoogleSignInSuccess(
        email: String,
        displayName: String,
        photoUrl: String? = null,
        idToken: String? = null
    ) {
        val session = UserSession(
            email = email,
            displayName = displayName,
            photoUrl = photoUrl,
            idToken = idToken
        )
        securityManager.saveSession(session)
        _userSession.value = session
        _hasStoredSession.value = true
        _isAuthenticated.value = true
        _feedbackToast.value = if (_isPortuguese.value) "Login realizado com sucesso: $email" else "Signed in successfully: $email"
    }

    fun onBiometricSuccess() {
        val session = _userSession.value ?: UserSession(
            email = "authorized@tminusmkt.local",
            displayName = "T-Minus Mkt"
        )
        securityManager.saveSession(session)
        _userSession.value = session
        _hasStoredSession.value = true
        _isAuthenticated.value = true
        _feedbackToast.value = if (_isPortuguese.value) "Identidade confirmada!" else "Identity verified!"
    }

    fun lockSession() {
        if (_hasStoredSession.value) {
            _isAuthenticated.value = false
        }
    }

    fun signOut() {
        securityManager.clearSession()
        _userSession.value = null
        _hasStoredSession.value = false
        _isAuthenticated.value = false
        _feedbackToast.value = if (_isPortuguese.value) "Sessão encerrada com segurança" else "Signed out securely"
    }

    fun setMilestoneSortMode(mode: MilestoneSortMode) {
        _milestoneSortMode.value = mode
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }

    // CALENDAR SYNC ACTIONS
    fun startCalendarSync() {
        _calendarSyncIndex.value = 0
        _showCalendarDialog.value = true
    }

    fun dismissCalendarSync() {
        _showCalendarDialog.value = false
        _calendarSyncIndex.value = 0
    }

    fun advanceCalendarSync(context: Context, task: TaskEntity) {
        // Launch Google Calendar native insertion
        CalendarSyncHelper.launchCalendarInsert(context, task, _isPortuguese.value)

        // Advance to next milestone
        val currentTasks = uiState.value.tasks
        val nextIdx = _calendarSyncIndex.value + 1
        if (nextIdx < currentTasks.size) {
            _calendarSyncIndex.value = nextIdx
        } else {
            _showCalendarDialog.value = false
            _calendarSyncIndex.value = 0
            _feedbackToast.value = if (_isPortuguese.value) {
                "Sincronização com Google Calendar concluída!"
            } else {
                "Google Calendar synchronization complete!"
            }
        }
    }

    fun skipCalendarSync() {
        val currentTasks = uiState.value.tasks
        val nextIdx = _calendarSyncIndex.value + 1
        if (nextIdx < currentTasks.size) {
            _calendarSyncIndex.value = nextIdx
        } else {
            _showCalendarDialog.value = false
            _calendarSyncIndex.value = 0
        }
    }

    // TASK MANAGEMENT
    fun toggleTask(id: Int, currentCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleTask(id, currentCompleted)
        }
    }

    fun toggleLanguage() {
        val newVal = !_isPortuguese.value
        _isPortuguese.value = newVal
        userPreferences.isPortuguese = newVal
        updateTimeAndDate()
    }

    fun toggleTheme() {
        val newVal = !_isDarkMode.value
        _isDarkMode.value = newVal
        userPreferences.isDarkMode = newVal
    }

    fun openEditDialog(task: TaskEntity) {
        _editingTask.value = task
    }

    fun dismissEditDialog() {
        _editingTask.value = null
    }

    fun updateTask(
        id: Int,
        newTitle: String,
        newDaysRemaining: Int,
        newIconType: String,
        newPriority: String = "Normal",
        newCategory: String = "Marketing"
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val targetCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, 2026)
                set(Calendar.MONTH, Calendar.DECEMBER)
                set(Calendar.DAY_OF_MONTH, 8)
                add(Calendar.DAY_OF_YEAR, -newDaysRemaining)
            }
            val dateFmt = SimpleDateFormat("dd/MM", Locale.US).format(targetCal.time)
            val wordsPt = numberToWordsPt(newDaysRemaining)
            val timelinePt = "$wordsPt dias antes ($dateFmt):"
            val timelineEn = "T-$newDaysRemaining ($dateFmt):"

            val existing = uiState.value.allTasks.firstOrNull { it.id == id }
                ?: uiState.value.tasks.firstOrNull { it.id == id }
                ?: return@launch
            val updated = existing.copy(
                titlePt = newTitle,
                titleEn = newTitle,
                timelinePt = timelinePt,
                timelineEn = timelineEn,
                daysRemaining = newDaysRemaining,
                targetDateFormatted = dateFmt,
                iconType = newIconType,
                priority = newPriority,
                category = newCategory,
                orderIndex = 100 - newDaysRemaining
            )
            repository.updateTask(updated)
        }
        _editingTask.value = null
        _feedbackToast.value = if (_isPortuguese.value) "Etapa atualizada com sucesso!" else "Milestone updated successfully!"
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.deleteTask(task.id)
        }
        _lastDeletedTask.value = task
        _feedbackToast.value = if (_isPortuguese.value) "Etapa excluída: ${task.titlePt}" else "Milestone deleted: ${task.titleEn}"
    }

    fun undoDelete() {
        val toRestore = _lastDeletedTask.value ?: return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.insertTask(toRestore)
        }
        _lastDeletedTask.value = null
        _feedbackToast.value = if (_isPortuguese.value) "Etapa restaurada!" else "Milestone restored!"
    }

    fun clearFeedbackToast() {
        _feedbackToast.value = null
    }

    fun setLocked(locked: Boolean) {
        _isLocked.value = locked
    }

    fun openAddDialog(initialText: String = "") {
        _initialDialogText.value = initialText
        _showAddDialog.value = true
    }

    fun dismissAddDialog() {
        _showAddDialog.value = false
        _initialDialogText.value = ""
    }

    fun addNewTask(
        title: String,
        daysRemaining: Int,
        iconType: String,
        priority: String = "Normal",
        category: String = "Marketing"
    ) {
        viewModelScope.launch {
            val targetCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, 2026)
                set(Calendar.MONTH, Calendar.DECEMBER)
                set(Calendar.DAY_OF_MONTH, 8)
                add(Calendar.DAY_OF_YEAR, -daysRemaining)
            }
            val dateFmt = SimpleDateFormat("dd/MM", Locale.US).format(targetCal.time)
            val wordsPt = numberToWordsPt(daysRemaining)

            val timelinePt = "$wordsPt dias antes ($dateFmt):"
            val timelineEn = "T-$daysRemaining ($dateFmt):"

            repository.addTask(
                timelinePt = timelinePt,
                timelineEn = timelineEn,
                titlePt = title,
                titleEn = title,
                iconType = iconType,
                daysRemaining = daysRemaining,
                targetDateFormatted = dateFmt,
                priority = priority,
                category = category
            )
            _showAddDialog.value = false
            _initialDialogText.value = ""
            _feedbackToast.value = if (_isPortuguese.value) "Nova etapa adicionada!" else "New milestone added!"
        }
    }



    private fun numberToWordsPt(n: Int): String {
        return when (n) {
            1 -> "Um dia"
            2 -> "Dois"
            3 -> "Três"
            4 -> "Quatro"
            5 -> "Cinco"
            6 -> "Seis"
            7 -> "Sete"
            8 -> "Oito"
            9 -> "Nove"
            10 -> "Dez"
            11 -> "Onze"
            12 -> "Doze"
            13 -> "Treze"
            14 -> "Catorze"
            15 -> "Quinze"
            16 -> "Dezesseis"
            17 -> "Dezessete"
            18 -> "Dezoito"
            19 -> "Dezenove"
            20 -> "Vinte"
            21 -> "Vinte e um"
            25 -> "Vinte e cinco"
            29 -> "Vinte e nove"
            30 -> "Trinta"
            else -> "$n"
        }
    }

    private fun numberToWordsEn(n: Int): String {
        return when (n) {
            1 -> "One day"
            2 -> "Two"
            3 -> "Three"
            4 -> "Four"
            5 -> "Five"
            6 -> "Six"
            7 -> "Seven"
            8 -> "Eight"
            9 -> "Nine"
            10 -> "Ten"
            11 -> "Eleven"
            12 -> "Twelve"
            13 -> "Thirteen"
            14 -> "Fourteen"
            15 -> "Fifteen"
            16 -> "Sixteen"
            17 -> "Seventeen"
            18 -> "Eighteen"
            19 -> "Nineteen"
            20 -> "Twenty"
            21 -> "Twenty-one"
            25 -> "Twenty-five"
            29 -> "Twenty-nine"
            30 -> "Thirty"
            else -> "$n"
        }
    }

    // AI VOICE ASSISTANT METHODS
    fun openVoiceAssistant() {
        _isVoiceAssistantOpen.value = true
        if (_voiceChatMessages.value.isEmpty()) {
            val greeting = if (_isPortuguese.value) {
                "Olá! Sou seu estrategista de marketing para o lançamento. Como posso ajudar com seu cronograma T-Minus hoje?"
            } else {
                "Hello! I am your launch marketing strategist. How can I help fine-tune your T-Minus countdown today?"
            }
            _voiceChatMessages.value = listOf(
                AiChatMessage(
                    sender = "ai",
                    text = greeting
                )
            )
            if (_isVoiceOutputEnabled.value) {
                speakText(greeting)
            }
        }
    }

    fun closeVoiceAssistant() {
        _isVoiceAssistantOpen.value = false
        _isListening.value = false
        _isAiThinking.value = false
        stopSpeaking()
    }

    fun setListening(listening: Boolean) {
        _isListening.value = listening
        if (listening) {
            stopSpeaking()
        }
    }

    fun updateLiveTranscript(transcript: String) {
        _liveTranscript.value = transcript
    }

    fun toggleVoiceOutput() {
        val next = !_isVoiceOutputEnabled.value
        _isVoiceOutputEnabled.value = next
        if (!next) {
            stopSpeaking()
        }
    }

    fun stopSpeaking() {
        ttsHelper.stop()
        _isAiSpeaking.value = false
    }

    private fun speakText(text: String) {
        if (!_isVoiceOutputEnabled.value) return
        _isAiSpeaking.value = true
        ttsHelper.speak(
            text = text,
            isPortuguese = _isPortuguese.value,
            onDone = {
                _isAiSpeaking.value = false
            }
        )
    }

    fun sendVoiceMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) return

        stopSpeaking()
        _isListening.value = false
        _liveTranscript.value = ""

        val userMessage = AiChatMessage(sender = "user", text = trimmed)
        val currentList = _voiceChatMessages.value + userMessage
        _voiceChatMessages.value = currentList
        _isAiThinking.value = true

        viewModelScope.launch {
            val response = aiService.askAssistant(
                userPrompt = trimmed,
                chatHistory = currentList,
                isPortuguese = _isPortuguese.value
            )
            _isAiThinking.value = false
            val aiMessage = AiChatMessage(
                sender = "ai",
                text = response.spokenText,
                suggestedTask = response.suggestedTask
            )
            _voiceChatMessages.value = _voiceChatMessages.value + aiMessage
            speakText(response.spokenText)
        }
    }

    fun addAiSuggestedTask(task: SuggestedTask) {
        addNewTask(
            title = task.title,
            daysRemaining = task.daysRemaining,
            iconType = task.iconType
        )
        // Mark task as added in the chat message
        _voiceChatMessages.update { list ->
            list.map { msg ->
                if (msg.suggestedTask?.title == task.title && msg.suggestedTask?.daysRemaining == task.daysRemaining) {
                    msg.copy(isTaskAdded = true)
                } else {
                    msg
                }
            }
        }
        val msg = if (_isPortuguese.value) {
            "Etapa '${task.title}' adicionada a T-${task.daysRemaining}!"
        } else {
            "Milestone '${task.title}' added to T-${task.daysRemaining}!"
        }
        _feedbackToast.value = msg
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.shutdown()
    }
}

