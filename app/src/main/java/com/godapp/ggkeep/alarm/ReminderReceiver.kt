package com.godapp.ggkeep.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.godapp.ggkeep.di.KeepSimEntryPoint
import com.godapp.ggkeep.notification.NotificationHelper
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Fired by AlarmManager at the task's reminder hour.
 * Shows a notification if the task is urgent (and not expired silently),
 * then re-arms the alarm for tomorrow.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return

        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    KeepSimEntryPoint::class.java
                )
                val repository = entryPoint.keepTaskRepository()
                val task = repository.getTaskById(taskId)

                if (task != null && !task.isExpired && task.isUrgent) {
                    NotificationHelper.showReminderNotification(context.applicationContext, task)
                }

                // Re-arm for tomorrow regardless (the task may become urgent later)
                if (task != null) {
                    ReminderAlarmManager.scheduleReminder(
                        context.applicationContext,
                        task.id,
                        task.reminderHour
                    )
                }
            } catch (e: Exception) {
                // Swallow; alarms will be restored on next app open or boot
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_TASK_ID = ReminderAlarmManager.EXTRA_TASK_ID
    }
}
