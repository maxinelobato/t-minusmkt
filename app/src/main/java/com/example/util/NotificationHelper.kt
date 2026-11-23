package com.example.util

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.TaskEntity
import com.example.receiver.TMinusAlarmReceiver
import com.example.receiver.TaskActionReceiver
import com.example.ui.screens.LockScreenActivity
import java.util.Locale

object NotificationHelper {

    const val COUNTDOWN_NOTIFICATION_ID = 1002
    const val COUNTDOWN_CHANNEL_ID = "t_minus_lockscreen_countdown"

    fun ensureCountdownChannel(context: Context, isPt: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                COUNTDOWN_CHANNEL_ID,
                if (isPt) "Contagem Regressiva no Lock Screen" else "Lock Screen Countdown",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = if (isPt) {
                    "Exibe a contagem regressiva e todas as tarefas na tela de bloqueio do Android"
                } else {
                    "Displays the live countdown and all tasks on the Android lock screen"
                }
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setShowBadge(true)
                enableVibration(false)
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Checks whether the app is allowed to launch Full-Screen Intents over the Android Lock Screen
     * (Android 14 / API 34+ requires explicit check via NotificationManager.canUseFullScreenIntent).
     */
    fun canUseFullScreenIntent(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.canUseFullScreenIntent()
        } else {
            true
        }
    }

    /**
     * Opens the native Android Settings page for allowing Full-Screen Lock Screen Intents on Android 14+.
     */
    fun openFullScreenIntentSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                // Fallback to app details settings
                val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallback)
            }
        }
    }

    /**
     * Builds the public Android Lock Screen notification listing ALL tasks in chronological order
     * with InboxStyle, T-XX countdown header, and interactive lock screen actions.
     */
    fun buildCountdownNotification(
        context: Context,
        tasks: List<TaskEntity>,
        daysRemaining: Long,
        hoursRemaining: Long = 0L,
        minutesRemaining: Long = 0L,
        isPt: Boolean,
        triggerFullScreen: Boolean = false
    ): Notification {
        ensureCountdownChannel(context, isPt)

        val activeTask = tasks.firstOrNull { !it.isCompleted } ?: tasks.firstOrNull()
        val completedCount = tasks.count { it.isCompleted }
        val totalCount = tasks.size

        // Tap on notification or Full Screen action: opens LockScreenActivity over lock screen
        val lockScreenIntent = Intent(context, LockScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        }
        val lockScreenPendingIntent = PendingIntent.getActivity(
            context,
            104,
            lockScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Ver Detalhes -> opens MainActivity directly unlocked
        val viewDetailsIntent = Intent(context, TaskActionReceiver::class.java).apply {
            action = TaskActionReceiver.ACTION_VIEW_DETAILS
            putExtra(TaskActionReceiver.EXTRA_IS_PT, isPt)
        }
        val viewDetailsPendingIntent = PendingIntent.getBroadcast(
            context,
            102,
            viewDetailsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val daysLabel = if (isPt) "T-$daysRemaining DIAS" else "T-$daysRemaining DAYS"
        val timeRemain = String.format(
            Locale.US,
            "%02dh %02dm",
            hoursRemaining.coerceAtLeast(0L),
            minutesRemaining.coerceAtLeast(0L)
        )

        val titleText = "T-MINUS MKT • $daysLabel ($timeRemain)"
        val summaryText = if (totalCount > 0) {
            if (isPt) {
                "$completedCount/$totalCount tarefas concluídas • Planejamento de Marketing"
            } else {
                "$completedCount/$totalCount tasks completed • Marketing Planning"
            }
        } else {
            if (isPt) "Planejamento de Marketing" else "Marketing Planning"
        }

        val contentText = if (activeTask != null) {
            val tl = (if (isPt) activeTask.timelinePt else activeTask.timelineEn).trimEnd(':').trim()
            val name = if (isPt) activeTask.titlePt else activeTask.titleEn
            "$tl — $name"
        } else {
            if (isPt) "Todas as etapas concluídas!" else "All milestones completed!"
        }

        // InboxStyle displays ALL tasks cleanly right on the Android Lock Screen
        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle(titleText)
            .setSummaryText(summaryText)

        if (tasks.isNotEmpty()) {
            tasks.forEach { item ->
                val check = if (item.isCompleted) "✓" else "☐"
                val tl = (if (isPt) item.timelinePt else item.timelineEn).trimEnd(':').trim()
                val name = if (isPt) item.titlePt else item.titleEn
                inboxStyle.addLine("$check $tl — $name")
            }
        } else {
            inboxStyle.addLine(contentText)
        }

        val largeIconBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.tminus_notification_icon)
        val builder = NotificationCompat.Builder(context, COUNTDOWN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_tminus_stopwatch)
            .setLargeIcon(largeIconBitmap)
            .setContentTitle(titleText)
            .setContentText(contentText)
            .setSubText(daysLabel)
            .setStyle(inboxStyle)
            .setContentIntent(lockScreenPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setOnlyAlertOnce(!triggerFullScreen)
            .setSilent(true)
            .setAutoCancel(false)
            .addAction(
                0,
                if (isPt) "Tela Cheia" else "Full Screen",
                lockScreenPendingIntent
            )
            .addAction(
                0,
                if (isPt) "Ver Detalhes" else "View Details",
                viewDetailsPendingIntent
            )

        if (triggerFullScreen) {
            builder.setFullScreenIntent(lockScreenPendingIntent, true)
        }

        // Action: Concluir Etapa (for next pending task)
        val pendingTask = tasks.firstOrNull { !it.isCompleted }
        if (pendingTask != null) {
            val completeIntent = Intent(context, TaskActionReceiver::class.java).apply {
                action = TaskActionReceiver.ACTION_COMPLETE_TASK
                putExtra(TaskActionReceiver.EXTRA_TASK_ID, pendingTask.id)
                putExtra(TaskActionReceiver.EXTRA_IS_PT, isPt)
            }
            val completePendingIntent = PendingIntent.getBroadcast(
                context,
                103 + pendingTask.id,
                completeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            builder.addAction(
                0,
                if (isPt) "Concluir Etapa" else "Complete Step",
                completePendingIntent
            )
        }

        return builder.build()
    }

    /**
     * Shows or updates the ongoing public Lock Screen countdown notification with ALL tasks.
     */
    fun showOrUpdateCountdownNotification(
        context: Context,
        tasks: List<TaskEntity>,
        daysRemaining: Long,
        hoursRemaining: Long = 0L,
        minutesRemaining: Long = 0L,
        isPt: Boolean,
        triggerFullScreen: Boolean = false
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = buildCountdownNotification(
            context = context,
            tasks = tasks,
            daysRemaining = daysRemaining,
            hoursRemaining = hoursRemaining,
            minutesRemaining = minutesRemaining,
            isPt = isPt,
            triggerFullScreen = triggerFullScreen
        )
        try {
            notificationManager.notify(COUNTDOWN_NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Android 13+ POST_NOTIFICATIONS permission not yet granted
        }
    }

    /**
     * Backward-compatible overload accepting a single active TaskEntity.
     */
    fun showOrUpdateCountdownNotification(
        context: Context,
        task: TaskEntity?,
        isPt: Boolean
    ) {
        val list = if (task != null) listOf(task) else emptyList()
        val days = task?.daysRemaining?.toLong() ?: 0L
        showOrUpdateCountdownNotification(
            context = context,
            tasks = list,
            daysRemaining = days,
            isPt = isPt,
            triggerFullScreen = false
        )
    }

    fun cancelCountdownNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(COUNTDOWN_NOTIFICATION_ID)
    }

    fun scheduleMilestoneAlert(
        context: Context,
        title: String,
        message: String,
        delaySeconds: Long = 10L
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TMinusAlarmReceiver::class.java).apply {
            putExtra("EXTRA_TITLE", title)
            putExtra("EXTRA_MESSAGE", message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            2001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + (delaySeconds * 1000L)
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
        } catch (_: Exception) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }
}
