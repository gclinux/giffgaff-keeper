package com.godapp.ggkeep.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.godapp.ggkeep.R
import com.godapp.ggkeep.alarm.ReminderAlarmManager
import com.godapp.ggkeep.di.KeepSimEntryPoint
import com.godapp.ggkeep.widget.WidgetUpdater
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Handles the "确认消费" action from reminder notifications.
 * Updates lastConsumeDate to today, cancels the alarm, refreshes the widget,
 * and posts a confirmation notification.
 */
class ConfirmConsumeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(ReminderAlarmManager.EXTRA_TASK_ID, -1L)
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

                repository.confirmConsume(taskId)
                ReminderAlarmManager.cancelReminder(context.applicationContext, taskId)
                WidgetUpdater.updateAll(context.applicationContext)

                // Cancel the original reminder notification
                NotificationManagerCompat.from(context.applicationContext)
                    .cancel(taskId.toInt())

                // Post a small confirmation notification
                showConfirmationNotification(context.applicationContext, taskId)
            } catch (e: Exception) {
                // Swallow
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showConfirmationNotification(context: Context, taskId: Long) {
        NotificationHelper.createChannel(context)
        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("已确认消费")
            .setContentText("保号任务已重置为今天，下次提醒将在 165 天后")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context)
                .notify((taskId * 10 + 2).toInt(), builder.build())
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS not granted
        }
    }
}
