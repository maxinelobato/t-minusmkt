package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.TaskRepository
import com.example.data.model.TaskEntity
import com.example.util.CalendarSyncHelper
import com.example.util.SecurityManager
import com.example.util.UserSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

data class TMinusUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val isPortuguese: Boolean = true,
    val isDarkMode: Boolean = false,
    val isLocked: Boolean = true,
    val timeDisplay: String = "08:30",
    val amPmDisplay: String = "",
    val dateDisplay: String = "Sábado, 07 de Nov",
    val daysUntilDeadline: Long = 33,
    val showAddDialog: Boolean = false,
    val initialDialogText: String = "",
    val feedbackToast: String? = null,
    // Google Auth & Biometric security states
    val isAuthenticated: Boolean = false,
    val hasStoredSession: Boolean = false,
    val userSession: UserSession? = null,
    // Calendar sync states
    val showCalendarDialog: Boolean = false,
    val calendarSyncIndex: Int = 0
)

class TMinusViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    private val securityManager = SecurityManager(application)

    private val _isPortuguese = MutableStateFlow(true)
    private val _isDarkMode = MutableStateFlow(false)
    private val _isLocked = MutableStateFlow(true)
    private val _timeDisplay = MutableStateFlow("08:30")
    private val _amPmDisplay = MutableStateFlow("")
    private val _dateDisplay = MutableStateFlow("")
    private val _daysUntilDeadline = MutableStateFlow(33L)
    private val _showAddDialog = MutableStateFlow(false)
    private val _initialDialogText = MutableStateFlow("")
    private val _feedbackToast = MutableStateFlow<String?>(null)

    // Security & Auth StateFlows
    private val _isAuthenticated = MutableStateFlow(false)
    private val _hasStoredSession = MutableStateFlow(false)
    private val _userSession = MutableStateFlow<UserSession?>(null)

    // Calendar Sync StateFlows
    private val _showCalendarDialog = MutableStateFlow(false)
    private val _calendarSyncIndex = MutableStateFlow(0)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TaskRepository(database.taskDao())

        // Check encrypted session
        val stored = securityManager.getSession()
        if (stored != null) {
            _hasStoredSession.value = true
            _userSession.value = stored
            // User has an active Google account session, but requires biometric check to unlock
            _isAuthenticated.value = false
        } else {
            _hasStoredSession.value = false
            _isAuthenticated.value = false
        }

        viewModelScope.launch {
            repository.checkAndSeedDefaults()
        }

        // Start live clock ticking
        viewModelScope.launch {
            while (true) {
                updateTimeAndDate()
                delay(1000L)
            }
        }
    }

    val uiState: StateFlow<TMinusUiState> = combine(
        repository.allTasks,
        _isPortuguese,
        _isDarkMode,
        _isLocked,
        _timeDisplay,
        _amPmDisplay,
        _dateDisplay,
        _daysUntilDeadline,
        _showAddDialog,
        _initialDialogText,
        _feedbackToast
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val tasks = args[0] as List<TaskEntity>
        val isPt = args[1] as Boolean
        val isDark = args[2] as Boolean
        val isLocked = args[3] as Boolean
        val timeDisplay = args[4] as String
        val amPmDisplay = args[5] as String
        val dateDisplay = args[6] as String
        val daysUntilDeadline = args[7] as Long
        val showAdd = args[8] as Boolean
        val initialText = args[9] as String
        val feedback = args[10] as String?

        TMinusUiState(
            tasks = tasks,
            isPortuguese = isPt,
            isDarkMode = isDark,
            isLocked = isLocked,
            timeDisplay = timeDisplay,
            amPmDisplay = amPmDisplay,
            dateDisplay = dateDisplay,
            daysUntilDeadline = daysUntilDeadline,
            showAddDialog = showAdd,
            initialDialogText = initialText,
            feedbackToast = feedback,
            isAuthenticated = _isAuthenticated.value,
            hasStoredSession = _hasStoredSession.value,
            userSession = _userSession.value,
            showCalendarDialog = _showCalendarDialog.value,
            calendarSyncIndex = _calendarSyncIndex.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TMinusUiState()
    )

    private fun updateTimeAndDate() {
        val now = Calendar.getInstance()
        val isPt = _isPortuguese.value

        if (isPt) {
            val timeFmt = SimpleDateFormat("HH:mm", Locale("pt", "BR"))
            _timeDisplay.value = timeFmt.format(now.time)
            _amPmDisplay.value = ""

            val dayOfWeek = SimpleDateFormat("EEEE", Locale("pt", "BR")).format(now.time)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }
            val dayAndMonth = SimpleDateFormat("dd 'de' MMM", Locale("pt", "BR")).format(now.time)
            _dateDisplay.value = "$dayOfWeek, $dayAndMonth"
        } else {
            val timeFmt = SimpleDateFormat("hh:mm", Locale.US)
            val amPmFmt = SimpleDateFormat("a", Locale.US)
            _timeDisplay.value = timeFmt.format(now.time)
            _amPmDisplay.value = amPmFmt.format(now.time).uppercase()

            val dayOfWeek = SimpleDateFormat("EEEE", Locale.US).format(now.time)
            val dayAndMonth = SimpleDateFormat("MMM dd", Locale.US).format(now.time)
            _dateDisplay.value = "$dayOfWeek, $dayAndMonth"
        }

        // Hard deadline: December 8, 2026
        val deadlineCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.DECEMBER)
            set(Calendar.DAY_OF_MONTH, 8)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }
        val diffMillis = deadlineCal.timeInMillis - now.timeInMillis
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis).coerceAtLeast(0)
        _daysUntilDeadline.value = diffDays
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
        _isPortuguese.update { !it }
        updateTimeAndDate()
    }

    fun toggleTheme() {
        _isDarkMode.update { !it }
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

    fun addNewTask(title: String, daysRemaining: Int, iconType: String) {
        viewModelScope.launch {
            val targetCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, 2026)
                set(Calendar.MONTH, Calendar.DECEMBER)
                set(Calendar.DAY_OF_MONTH, 8)
                add(Calendar.DAY_OF_YEAR, -daysRemaining)
            }
            val dateFmt = SimpleDateFormat("dd/MM", Locale.US).format(targetCal.time)
            val wordsPt = numberToWordsPt(daysRemaining)
            val wordsEn = numberToWordsEn(daysRemaining)

            val timelinePt = "$wordsPt dias antes ($dateFmt):"
            val timelineEn = "$wordsEn days before ($dateFmt):"

            repository.addTask(
                timelinePt = timelinePt,
                timelineEn = timelineEn,
                titlePt = title,
                titleEn = title,
                iconType = iconType,
                daysRemaining = daysRemaining,
                targetDateFormatted = dateFmt
            )
            _showAddDialog.value = false
            _initialDialogText.value = ""
            _feedbackToast.value = if (_isPortuguese.value) "Nova etapa adicionada!" else "New milestone added!"
        }
    }

    fun deleteTask(id: Int) {
        viewModelScope.launch {
            repository.deleteTask(id)
            _feedbackToast.value = if (_isPortuguese.value) "Etapa removida" else "Milestone deleted"
        }
    }

    fun clearFeedbackToast() {
        _feedbackToast.value = null
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
}
