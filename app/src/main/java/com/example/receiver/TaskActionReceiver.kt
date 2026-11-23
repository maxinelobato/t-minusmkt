package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.MainActivity
import com.example.data.AppDatabase
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TaskActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_COMPLETE_TASK = "com.example.ACTION_COMPLETE_TASK"
        const val ACTION_VIEW_DETAILS = "com.example.ACTION_VIEW_DETAILS"
        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
        const val EXTRA_IS_PT = "EXTRA_IS_PT"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val isPt = intent.getBooleanExtra(EXTRA_IS_PT, true)

        when (intent.action) {
            ACTION_COMPLETE_TASK -> {
                val taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
                if (taskId != -1) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = AppDatabase.getDatabase(context)
                            val dao = db.taskDao()
                            dao.setTaskCompleted(taskId, true)

                            // Refresh public all-tasks notification and Android Lock Screen wallpaper
                            com.example.service.SystemLockScreenService.syncLatestFromDatabase(
                                context = context.applicationContext,
                                triggerFullScreen = false,
                                updateWallpaper = true
                            )

                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(
                                    context,
                                    if (isPt) "Etapa concluída! Lock Screen do Android atualizado." else "Milestone completed! Android Lock Screen updated.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }

            ACTION_VIEW_DETAILS -> {
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("EXTRA_UNLOCK_DIRECT", true)
                }
                context.startActivity(launchIntent)
            }
        }
    }
}
