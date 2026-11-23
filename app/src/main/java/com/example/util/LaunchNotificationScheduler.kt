package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.CountdownTimerEntity
import com.example.receiver.LaunchAlertReceiver

object LaunchNotificationScheduler {

    private const val TAG = "LaunchScheduler"

    const val STAGE_24H = "24h"
    const val STAGE_1H = "1h"
    const val STAGE_0M = "0m"

    const val ACTION_LAUNCH_ALERT = "com.example.ACTION_LAUNCH_ALERT"
    const val ACTION_SNOOZE_ALERT = "com.example.ACTION_SNOOZE_ALERT"

    const val EXTRA_TIMER_ID = "EXTRA_TIMER_ID"
    const val EXTRA_TIMER_TITLE = "EXTRA_TIMER_TITLE"
    const val EXTRA_STAGE_CODE = "EXTRA_STAGE_CODE"
    const val EXTRA_IS_PT = "EXTRA_IS_PT"

    // Offsets in milliseconds
    private const val OFFSET_24H = 24 * 60 * 60 * 1000L
    private const val OFFSET_1H = 1 * 60 * 60 * 1000L
    private const val OFFSET_0M = 0L

    /**
     * Schedules exact alarms for a launch countdown timer at:
     * - 24 hours remaining
     * - 1 hour remaining
     * - 0 minutes remaining (Launch time)
     */
    fun scheduleCountdownAlerts(
        context: Context,
        timer: CountdownTimerEntity,
        isPt: Boolean
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val now = System.currentTimeMillis()

        val stages = listOf(
            STAGE_24H to (timer.targetTimestamp - OFFSET_24H),
            STAGE_1H to (timer.targetTimestamp - OFFSET_1H),
            STAGE_0M to (timer.targetTimestamp - OFFSET_0M)
        )

        for ((stage, triggerAtMillis) in stages) {
            if (triggerAtMillis > now) {
                scheduleSingleAlarm(
                    context = context,
                    alarmManager = alarmManager,
                    timerId = timer.id,
                    title = timer.title,
                    stage = stage,
                    triggerAtMillis = triggerAtMillis,
                    isPt = isPt
                )
            } else {
                Log.d(TAG, "Stage $stage for timer ${timer.id} is in the past ($triggerAtMillis <= $now); skipping")
            }
        }
    }

    private fun scheduleSingleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        timerId: Long,
        title: String,
        stage: String,
        triggerAtMillis: Long,
        isPt: Boolean
    ) {
        val requestCode = getAlarmRequestCode(timerId, stage)
        val intent = Intent(context, LaunchAlertReceiver::class.java).apply {
            action = ACTION_LAUNCH_ALERT
            putExtra(EXTRA_TIMER_ID, timerId)
            putExtra(EXTRA_TIMER_TITLE, title)
            putExtra(EXTRA_STAGE_CODE, stage)
            putExtra(EXTRA_IS_PT, isPt)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Log.i(TAG, "Scheduled $stage alarm for timer '$title' (ID: $timerId) at $triggerAtMillis")
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException scheduling exact alarm; falling back", e)
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    /**
     * Cancels all scheduled alarms for the specified countdown timer across all 3 intervals.
     */
    fun cancelCountdownAlerts(context: Context, timerId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val stages = listOf(STAGE_24H, STAGE_1H, STAGE_0M)

        for (stage in stages) {
            val requestCode = getAlarmRequestCode(timerId, stage)
            val intent = Intent(context, LaunchAlertReceiver::class.java).apply {
                action = ACTION_LAUNCH_ALERT
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d(TAG, "Cancelled $stage alarm for timer ID $timerId")
            }
        }
    }

    /**
     * Schedules a snooze reminder 15 minutes in the future for a triggered alert.
     */
    fun snoozeAlert(
        context: Context,
        timerId: Long,
        title: String,
        stageCode: String,
        isPt: Boolean,
        snoozeMinutes: Int = 15
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerAtMillis = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
        val requestCode = (timerId * 100 + 99).toInt()

        val intent = Intent(context, LaunchAlertReceiver::class.java).apply {
            action = ACTION_LAUNCH_ALERT
            putExtra(EXTRA_TIMER_ID, timerId)
            putExtra(EXTRA_TIMER_TITLE, "$title (Snoozed)")
            putExtra(EXTRA_STAGE_CODE, stageCode)
            putExtra(EXTRA_IS_PT, isPt)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Log.i(TAG, "Snoozed alert for '$title' ($snoozeMinutes min) scheduled for $triggerAtMillis")
        } catch (_: Exception) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    /**
     * Immediately triggers a notification preview for interactive testing in the UI.
     */
    fun triggerInstantPreview(
        context: Context,
        timerId: Long,
        title: String,
        stageCode: String,
        isPt: Boolean
    ) {
        val intent = Intent(context, LaunchAlertReceiver::class.java).apply {
            action = ACTION_LAUNCH_ALERT
            putExtra(EXTRA_TIMER_ID, timerId)
            putExtra(EXTRA_TIMER_TITLE, title)
            putExtra(EXTRA_STAGE_CODE, stageCode)
            putExtra(EXTRA_IS_PT, isPt)
        }
        context.sendBroadcast(intent)
    }

    private fun getAlarmRequestCode(timerId: Long, stage: String): Int {
        val stageNum = when (stage) {
            STAGE_24H -> 24
            STAGE_1H -> 1
            STAGE_0M -> 0
            else -> 9
        }
        return ((timerId % 10000) * 100 + stageNum).toInt()
    }
}
