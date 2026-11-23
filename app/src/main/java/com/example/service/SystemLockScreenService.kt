package com.example.service

import android.app.KeyguardManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.data.AppDatabase
import com.example.data.UserPreferencesRepository
import com.example.data.model.TaskEntity
import com.example.ui.screens.LockScreenActivity
import com.example.util.CountdownBitmapRenderer
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Play-Policy-compliant Foreground Service (foregroundServiceType="specialUse") that keeps
 * the T-Minus Mkt Lock Screen active on the Android OS:
 * - Maintains the public all-tasks countdown notification on the Android Lock Screen
 * - Listens to ACTION_SCREEN_OFF / ACTION_SCREEN_ON and surfaces LockScreenActivity
 *   automatically when the device is locked
 * - Synchronizes the Lock Screen Wallpaper (FLAG_LOCK) cleanly in the background
 */
class SystemLockScreenService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isReceiverRegistered = false

    private val screenEventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    handleDeviceLocked(context, launchFullScreen = true)
                }
                Intent.ACTION_SCREEN_ON -> {
                    val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                    if (keyguard?.isKeyguardLocked == true) {
                        handleDeviceLocked(context, launchFullScreen = true)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        registerScreenReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val prefs = UserPreferencesRepository(this)
        if (!prefs.isSystemLockScreenEnabled) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Immediately enter foreground with a public Lock Screen notification (required within 5s on Android 8+)
        startAsForeground(
            tasks = emptyList(),
            daysRemaining = computeFallbackCountdown().first,
            hoursRemaining = computeFallbackCountdown().second,
            minutesRemaining = computeFallbackCountdown().third,
            isPt = prefs.isPortuguese
        )

        // Hydrate with real Room tasks + refresh notification & FLAG_LOCK wallpaper
        serviceScope.launch {
            syncLatestFromDatabase(
                context = applicationContext,
                triggerFullScreen = false,
                updateWallpaper = intent?.getBooleanExtra(EXTRA_SYNC_WALLPAPER, false) == true
            )
        }

        return START_STICKY
    }

    override fun onDestroy() {
        unregisterScreenReceiver()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerScreenReceiver() {
        if (isReceiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        ContextCompat.registerReceiver(
            this,
            screenEventReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        isReceiverRegistered = true
    }

    private fun unregisterScreenReceiver() {
        if (!isReceiverRegistered) return
        try {
            unregisterReceiver(screenEventReceiver)
        } catch (_: IllegalArgumentException) {
            // Already unregistered
        }
        isReceiverRegistered = false
    }

    private fun startAsForeground(
        tasks: List<TaskEntity>,
        daysRemaining: Long,
        hoursRemaining: Long,
        minutesRemaining: Long,
        isPt: Boolean
    ) {
        val notification = NotificationHelper.buildCountdownNotification(
            context = this,
            tasks = tasks,
            daysRemaining = daysRemaining,
            hoursRemaining = hoursRemaining,
            minutesRemaining = minutesRemaining,
            isPt = isPt,
            triggerFullScreen = false
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceCompat.startForeground(
                    this,
                    NotificationHelper.COUNTDOWN_NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NotificationHelper.COUNTDOWN_NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to promote to foreground service: ${e.message}")
            stopSelf()
        }
    }

    private fun handleDeviceLocked(context: Context, launchFullScreen: Boolean) {
        val prefs = UserPreferencesRepository(context)
        if (!prefs.isSystemLockScreenEnabled) return

        serviceScope.launch {
            syncLatestFromDatabase(
                context = context,
                triggerFullScreen = launchFullScreen,
                updateWallpaper = false
            )
            if (launchFullScreen) {
                try {
                    val lockIntent = Intent(context, LockScreenActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    }
                    context.startActivity(lockIntent)
                } catch (e: Exception) {
                    // Android 10+ background launch restriction handled cleanly by FullScreenIntent notification
                    Log.d(TAG, "Direct activity start deferred to FullScreenIntent: ${e.message}")
                }
            }
        }
    }

    companion object {
        private const val TAG = "SystemLockScreenService"
        private const val EXTRA_SYNC_WALLPAPER = "extra_sync_wallpaper"

        fun start(context: Context, syncWallpaper: Boolean = false) {
            val prefs = UserPreferencesRepository(context)
            if (!prefs.isSystemLockScreenEnabled) return

            val intent = Intent(context, SystemLockScreenService::class.java).apply {
                putExtra(EXTRA_SYNC_WALLPAPER, syncWallpaper)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context, intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Deferred SystemLockScreenService start: ${e.message}")
            }
        }

        fun stop(context: Context) {
            try {
                context.stopService(Intent(context, SystemLockScreenService::class.java))
            } catch (_: Exception) {
            }
            NotificationHelper.cancelCountdownNotification(context)
        }

        suspend fun syncLatestFromDatabase(
            context: Context,
            triggerFullScreen: Boolean = false,
            updateWallpaper: Boolean = false
        ) {
            try {
                val db = AppDatabase.getDatabase(context)
                val prefs = UserPreferencesRepository(context)
                val tasks = db.taskDao().getAllTasks().first()
                val activeTimer = db.countdownTimerDao().getActiveTimer().first()
                val (days, hours, minutes, seconds) = computeCountdownParts(activeTimer?.targetTimestamp)

                NotificationHelper.showOrUpdateCountdownNotification(
                    context = context,
                    tasks = tasks,
                    daysRemaining = days,
                    hoursRemaining = hours,
                    minutesRemaining = minutes,
                    isPt = prefs.isPortuguese,
                    triggerFullScreen = triggerFullScreen
                )

                if (updateWallpaper && prefs.isSystemLockScreenEnabled && tasks.isNotEmpty()) {
                    CountdownBitmapRenderer.applyLockScreenWallpaperSilently(
                        context = context,
                        tasks = tasks,
                        daysRemaining = days,
                        hoursRemaining = hours,
                        minutesRemaining = minutes,
                        secondsRemaining = seconds,
                        isPt = prefs.isPortuguese,
                        isDark = prefs.isDarkMode
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error syncing system lock screen from DB: ${e.message}")
            }
        }

        private fun computeFallbackCountdown(): Triple<Long, Long, Long> {
            val parts = computeCountdownParts(null)
            return Triple(parts[0], parts[1], parts[2])
        }

        fun computeCountdownParts(targetTimestamp: Long?): LongArray {
            val target = targetTimestamp ?: Calendar.getInstance().apply {
                set(Calendar.YEAR, 2026)
                set(Calendar.MONTH, Calendar.DECEMBER)
                set(Calendar.DAY_OF_MONTH, 8)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.timeInMillis

            val diff = (target - System.currentTimeMillis()).coerceAtLeast(0L)
            val secs = (diff / 1000) % 60
            val mins = (diff / (1000 * 60)) % 60
            val hrs = (diff / (1000 * 60 * 60)) % 24
            val days = diff / (1000 * 60 * 60 * 24)
            return longArrayOf(days, hrs, mins, secs)
        }
    }
}
