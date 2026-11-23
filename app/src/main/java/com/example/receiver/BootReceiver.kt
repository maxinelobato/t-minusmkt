package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.UserPreferencesRepository
import com.example.util.LaunchNotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Log.i("BootReceiver", "Device boot or package update detected ($action). Rescheduling launch alarms...")

            val pendingResult = goAsync()
            val scope = CoroutineScope(Dispatchers.IO)

            scope.launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val userPreferences = UserPreferencesRepository(context)
                    val isPt = userPreferences.isPortuguese

                    val allTimers = db.countdownTimerDao().getAllTimers().first()
                    val now = System.currentTimeMillis()

                    for (timer in allTimers) {
                        if (timer.targetTimestamp > now) {
                            LaunchNotificationScheduler.scheduleCountdownAlerts(context, timer, isPt)
                        }
                    }

                    if (userPreferences.isSystemLockScreenEnabled) {
                        com.example.service.SystemLockScreenService.syncLatestFromDatabase(
                            context = context.applicationContext,
                            triggerFullScreen = false,
                            updateWallpaper = true
                        )
                        com.example.service.SystemLockScreenService.start(context.applicationContext)
                    }
                    Log.i("BootReceiver", "Successfully rescheduled ${allTimers.size} countdown launch alarms and lock screen.")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling launch alarms on boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
