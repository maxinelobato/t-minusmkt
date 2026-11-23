package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.util.LaunchNotificationScheduler

class LaunchAlertReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID_LAUNCH_ALERTS = "t_minus_launch_alerts_high"
        private val VIBRATION_PATTERN = longArrayOf(0, 250, 150, 250, 150, 400)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val timerId = intent.getLongExtra(LaunchNotificationScheduler.EXTRA_TIMER_ID, 0L)
        val title = intent.getStringExtra(LaunchNotificationScheduler.EXTRA_TIMER_TITLE) ?: "Campanha de Lançamento"
        val stageCode = intent.getStringExtra(LaunchNotificationScheduler.EXTRA_STAGE_CODE) ?: LaunchNotificationScheduler.STAGE_0M
        val isPt = intent.getBooleanExtra(LaunchNotificationScheduler.EXTRA_IS_PT, true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureNotificationChannel(context, notificationManager, isPt)

        when (action) {
            LaunchNotificationScheduler.ACTION_LAUNCH_ALERT -> {
                showLaunchNotification(
                    context = context,
                    notificationManager = notificationManager,
                    timerId = timerId,
                    title = title,
                    stageCode = stageCode,
                    isPt = isPt
                )
            }
            LaunchNotificationScheduler.ACTION_SNOOZE_ALERT -> {
                val notifId = getNotificationId(timerId, stageCode)
                notificationManager.cancel(notifId)

                LaunchNotificationScheduler.snoozeAlert(
                    context = context,
                    timerId = timerId,
                    title = title,
                    stageCode = stageCode,
                    isPt = isPt,
                    snoozeMinutes = 15
                )

                Toast.makeText(
                    context,
                    if (isPt) "Lembrete de '$title' adiado por 15 minutos" else "Reminder for '$title' snoozed for 15 mins",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showLaunchNotification(
        context: Context,
        notificationManager: NotificationManager,
        timerId: Long,
        title: String,
        stageCode: String,
        isPt: Boolean
    ) {
        val notifId = getNotificationId(timerId, stageCode)

        val (notificationTitle, notificationBody) = when (stageCode) {
            LaunchNotificationScheduler.STAGE_24H -> {
                val t = if (isPt) "T-24H • $title" else "T-24H • $title"
                val b = if (isPt) {
                    "Faltam 24 horas para o lançamento! Revise a lista de verificação de marketing e alinhe canais de mídia."
                } else {
                    "24 hours remaining until launch! Review marketing checklist and coordinate channel readiness."
                }
                t to b
            }
            LaunchNotificationScheduler.STAGE_1H -> {
                val t = if (isPt) "⚡ T-1H • $title" else "⚡ T-1H • $title"
                val b = if (isPt) {
                    "Contagem regressiva final: 1 hora restante para a campanha ir ao ar! Prepare os disparos."
                } else {
                    "Final countdown: 1 hour remaining until the campaign goes live! Prepare announcement blast."
                }
                t to b
            }
            else -> {
                val t = if (isPt) "🚀 T-0: LANÇAMENTO INICIADO! • $title" else "🚀 T-0: LAUNCH IS LIVE! • $title"
                val b = if (isPt) {
                    "A campanha está oficialmente no ar agora! Acompanhe métricas de conversão e engajamento em tempo real."
                } else {
                    "The launch is officially LIVE right now! Monitor conversion metrics and engagement in real time."
                }
                t to b
            }
        }

        // Tap content intent: Open MainActivity focusing on the campaign
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_UNLOCK_DIRECT", true)
            putExtra("EXTRA_TARGET_TIMER_ID", timerId)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notifId + 1000,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 1: "Open Dashboard" / "Abrir Painel"
        val dashboardPendingIntent = PendingIntent.getActivity(
            context,
            notifId + 2000,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: "Snooze 15m" / "Adiar 15m"
        val snoozeIntent = Intent(context, LaunchAlertReceiver::class.java).apply {
            action = LaunchNotificationScheduler.ACTION_SNOOZE_ALERT
            putExtra(LaunchNotificationScheduler.EXTRA_TIMER_ID, timerId)
            putExtra(LaunchNotificationScheduler.EXTRA_TIMER_TITLE, title)
            putExtra(LaunchNotificationScheduler.EXTRA_STAGE_CODE, stageCode)
            putExtra(LaunchNotificationScheduler.EXTRA_IS_PT, isPt)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            notifId + 3000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIconBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.tminus_notification_icon)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_LAUNCH_ALERTS)
            .setSmallIcon(R.drawable.ic_stat_tminus_stopwatch)
            .setLargeIcon(largeIconBitmap)
            .setContentTitle(notificationTitle)
            .setContentText(notificationBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBody))
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .setVibrate(VIBRATION_PATTERN)
            .addAction(
                0,
                if (isPt) "Abrir Painel" else "Open Dashboard",
                dashboardPendingIntent
            )

        // Only show Snooze action if before T-0
        if (stageCode != LaunchNotificationScheduler.STAGE_0M) {
            builder.addAction(
                0,
                if (isPt) "Adiar 15m" else "Snooze 15m",
                snoozePendingIntent
            )
        }

        try {
            notificationManager.notify(notifId, builder.build())
        } catch (_: SecurityException) {
            // Notifications permission not yet granted on Android 13+
        }
    }

    private fun ensureNotificationChannel(
        context: Context,
        notificationManager: NotificationManager,
        isPt: Boolean
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = if (isPt) "Alertas Críticos de Lançamento" else "Critical Launch Alerts"
            val descriptionText = if (isPt) {
                "Alertas em tempo real quando cronômetros atingem 24 horas, 1 hora e momento do lançamento"
            } else {
                "Real-time countdown alerts triggered at 24 hours, 1 hour, and zero minutes remaining"
            }

            val channel = NotificationChannel(
                CHANNEL_ID_LAUNCH_ALERTS,
                name,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = descriptionText
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                enableVibration(true)
                vibrationPattern = VIBRATION_PATTERN
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getNotificationId(timerId: Long, stageCode: String): Int {
        val stageNum = when (stageCode) {
            LaunchNotificationScheduler.STAGE_24H -> 24
            LaunchNotificationScheduler.STAGE_1H -> 1
            LaunchNotificationScheduler.STAGE_0M -> 0
            else -> 9
        }
        return ((timerId % 1000) * 100 + stageNum + 5000).toInt()
    }
}
