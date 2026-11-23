package com.example.ui.screens

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainActivity
import com.example.ui.TMinusViewModel
import com.example.ui.theme.TMinusMktTheme
import com.example.util.NotificationHelper

class LockScreenActivity : ComponentActivity() {

    private val viewModel: TMinusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configure activity to display over the Android Lock Screen and turn on the screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER
        )

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            TMinusMktTheme(darkTheme = uiState.isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.ui.graphics.Color.Transparent
                ) {
                    LockScreenComposable(
                        tasks = if (uiState.allTasks.isNotEmpty()) uiState.allTasks else uiState.tasks,
                        isPt = uiState.isPortuguese,
                        isDark = uiState.isDarkMode,
                        timeDisplay = uiState.timeDisplay,
                        amPmDisplay = uiState.amPmDisplay,
                        dateDisplay = uiState.dateDisplay,
                        daysRemaining = uiState.countdownDays,
                        hoursRemaining = uiState.countdownHours,
                        minutesRemaining = uiState.countdownMinutes,
                        secondsRemaining = uiState.countdownSeconds,
                        onToggleTask = { id, currentCompleted ->
                            viewModel.toggleTask(id, currentCompleted)
                            NotificationHelper.showOrUpdateCountdownNotification(
                                context = this@LockScreenActivity,
                                tasks = if (uiState.allTasks.isNotEmpty()) uiState.allTasks else uiState.tasks,
                                daysRemaining = uiState.countdownDays,
                                hoursRemaining = uiState.countdownHours,
                                minutesRemaining = uiState.countdownMinutes,
                                isPt = uiState.isPortuguese,
                                triggerFullScreen = false
                            )
                        },
                        onUnlock = {
                            unlockAndOpenApp()
                        },
                        onToggleLanguage = {
                            viewModel.toggleLanguage()
                        },
                        onToggleTheme = {
                            viewModel.toggleTheme()
                        }
                    )
                }
            }
        }
    }

    private fun unlockAndOpenApp() {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && keyguardManager != null && keyguardManager.isKeyguardLocked) {
            keyguardManager.requestDismissKeyguard(this, object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissSucceeded() {
                    openMainActivity()
                }

                override fun onDismissCancelled() {
                    // User stayed on lock screen
                }

                override fun onDismissError() {
                    openMainActivity()
                }
            })
        } else {
            openMainActivity()
        }
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_UNLOCK_DIRECT", true)
        }
        startActivity(intent)
        finish()
    }
}
