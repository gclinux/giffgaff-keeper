package com.godapp.ggkeep.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.ZonedDateTime

/**
 * Schedules and cancels exact alarms for task reminders.
 * Each task gets its own alarm at its [reminderHour] every day.
 * The receiver re-schedules itself after firing.
 */
object ReminderAlarmManager {

    fun scheduleReminder(context: Context, taskId: Long, reminderHour: Int) {
        val alarmMgr = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, taskId)
        }
        val pending = PendingIntent.getBroadcast(
            context.applicationContext,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Compute next occurrence of reminderHour (today if not yet passed, else tomorrow)
        val now = ZonedDateTime.now()
        var next = now.withHour(reminderHour).withMinute(0).withSecond(0).withNano(0)
        if (!next.isAfter(now)) {
            next = next.plusDays(1)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // On API 31+, setExactAndAllowWhileIdle requires SCHEDULE_EXACT_ALARM or USE_EXACT_ALARM permission
                if (alarmMgr.canScheduleExactAlarms()) {
                    alarmMgr.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        next.toInstant().toEpochMilli(),
                        pending
                    )
                } else {
                    // Fallback: inexact alarm. Better than nothing.
                    alarmMgr.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        next.toInstant().toEpochMilli(),
                        pending
                    )
                }
            } else {
                alarmMgr.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    next.toInstant().toEpochMilli(),
                    pending
                )
            }
        } catch (e: SecurityException) {
            // Permission revoked; fall back to inexact
            alarmMgr.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                next.toInstant().toEpochMilli(),
                pending
            )
        }
    }

    fun cancelReminder(context: Context, taskId: Long) {
        val alarmMgr = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, ReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context.applicationContext,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmMgr.cancel(pending)
    }

    const val EXTRA_TASK_ID = "task_id"
}
