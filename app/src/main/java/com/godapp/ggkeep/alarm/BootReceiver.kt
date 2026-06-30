package com.godapp.ggkeep.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.godapp.ggkeep.di.KeepSimEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Restores all alarms after device boot (including direct boot).
 * Registered for BOOT_COMPLETED and LOCKED_BOOT_COMPLETED.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    KeepSimEntryPoint::class.java
                )
                val repository = entryPoint.keepTaskRepository()
                val tasks = repository.getAllTasksOnce()

                tasks.forEach { task ->
                    if (!task.isExpired) {
                        ReminderAlarmManager.scheduleReminder(
                            context.applicationContext,
                            task.id,
                            task.reminderHour
                        )
                    }
                }
            } catch (e: Exception) {
                // Swallow; user can open the app to re-arm alarms
            } finally {
                pendingResult.finish()
            }
        }
    }
}
