package com.godapp.ggkeep.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.godapp.ggkeep.MainActivity
import com.godapp.ggkeep.R
import com.godapp.ggkeep.alarm.ReminderAlarmManager
import com.godapp.ggkeep.domain.model.KeepTask
import com.godapp.ggkeep.domain.model.TaskStatus

object NotificationHelper {
    const val CHANNEL_ID = "keep_reminder"
    const val CHANNEL_NAME = "保号提醒"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "SIM 卡保号到期提醒"
                enableVibration(true)
            }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    fun showReminderNotification(context: Context, task: KeepTask) {
        createChannel(context)

        val confirmIntent = Intent(context, com.godapp.ggkeep.notification.ConfirmConsumeReceiver::class.java).apply {
            putExtra(ReminderAlarmManager.EXTRA_TASK_ID, task.id)
        }
        val confirmPending = PendingIntent.getBroadcast(
            context.applicationContext,
            task.id.toInt(),
            confirmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(context.applicationContext, MainActivity::class.java).apply {
            putExtra("task_id", task.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
            context.applicationContext,
            task.id.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "保号提醒 · ${task.name}"
        val text = when (task.status) {
            TaskStatus.EXPIRED -> "已过期！请立即消费保号"
            TaskStatus.URGENT -> "剩余 ${task.remainingDays} 天，请尽快消费保号"
            TaskStatus.OK -> "剩余 ${task.remainingDays} 天"
        }

        val builder = NotificationCompat.Builder(context.applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openPending)
            .addAction(0, "确认消费", confirmPending)
            .setAutoCancel(true)

        // SMS action only if smsToNumber is set and task is urgent/expired
        if (!task.smsToNumber.isNullOrBlank() && task.status != TaskStatus.OK) {
            val smsIntent = Intent(context.applicationContext, MainActivity::class.java).apply {
                putExtra("task_id", task.id)
                putExtra("trigger_sms", true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val smsPending = PendingIntent.getActivity(
                context.applicationContext,
                (task.id * 10 + 1).toInt(),
                smsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "发短信", smsPending)
        }

        try {
            NotificationManagerCompat.from(context.applicationContext)
                .notify(task.id.toInt(), builder.build())
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS not granted; silently skip
        }
    }
}
