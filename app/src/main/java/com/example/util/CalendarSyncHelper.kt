package com.example.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.example.data.model.TaskEntity
import java.util.Calendar

object CalendarSyncHelper {

    fun createCalendarIntent(task: TaskEntity, isPt: Boolean): Intent {
        val targetCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.DECEMBER)
            set(Calendar.DAY_OF_MONTH, 8)
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -task.daysRemaining)
        }

        val startMillis = targetCal.timeInMillis
        val endMillis = startMillis + (60 * 60 * 1000L) // 1 hour duration (14:00 - 15:00)

        val title = if (isPt) {
            "T-Minus Mkt: ${task.titlePt}"
        } else {
            "T-Minus Mkt: ${task.titleEn}"
        }

        val description = if (isPt) {
            "${task.timelinePt} ${task.titlePt}\nMeta estratégica para lançamento em 08/12/2026.\nHorário padrão de lembrete: 14:00."
        } else {
            "${task.timelineEn} ${task.titleEn}\nStrategic milestone for launch on 12/08/2026.\nDefault reminder: 14:00 (2:00 PM)."
        }

        return Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.Events.DESCRIPTION, description)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            putExtra(CalendarContract.Events.EVENT_LOCATION, "Planejamento de Marketing")
            putExtra(CalendarContract.Events.HAS_ALARM, 1)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun launchCalendarInsert(context: Context, task: TaskEntity, isPt: Boolean): Boolean {
        return try {
            val intent = createCalendarIntent(task, isPt)
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
