package com.godapp.ggkeep.data.repository

import android.content.Context
import com.godapp.ggkeep.data.local.KeepTaskDao
import com.godapp.ggkeep.data.local.KeepTaskEntity
import com.godapp.ggkeep.data.mapper.toDomain
import com.godapp.ggkeep.domain.model.KeepTask
import com.godapp.ggkeep.alarm.ReminderAlarmManager
import com.godapp.ggkeep.widget.WidgetUpdater
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

interface KeepTaskRepository {
    fun observeAllTasks(): Flow<List<KeepTask>>
    suspend fun getTaskById(id: Long): KeepTask?
    suspend fun getAllTasksOnce(): List<KeepTask>
    suspend fun createTask(task: KeepTask): Long
    suspend fun updateTask(task: KeepTask)
    suspend fun deleteTask(id: Long)
    suspend fun confirmConsume(id: Long, date: LocalDate = LocalDate.now(ZoneId.systemDefault())): Long
}

@Singleton
class KeepTaskRepositoryImpl @Inject constructor(
    private val dao: KeepTaskDao,
    @ApplicationContext private val context: Context
) : KeepTaskRepository {

    override fun observeAllTasks(): Flow<List<KeepTask>> =
        dao.observeAllTasks().map { entities -> entities.toDomain() }

    override suspend fun getTaskById(id: Long): KeepTask? =
        dao.getTaskById(id)?.toDomain()

    override suspend fun getAllTasksOnce(): List<KeepTask> =
        dao.getAllTasksOnce().toDomain()

    override suspend fun createTask(task: KeepTask): Long {
        val now = System.currentTimeMillis()
        val entity = KeepTaskEntity(
            id = 0,
            name = task.name,
            phoneNumber = task.phoneNumber,
            lastConsumeDate = task.lastConsumeDate,
            reminderHour = task.reminderHour,
            remindDaysBefore = task.remindDaysBefore,
            smsToNumber = task.smsToNumber,
            createdAt = now,
            updatedAt = now
        )
        val newId = dao.insert(entity)
        val saved = entity.copy(id = newId)
        ReminderAlarmManager.scheduleReminder(context, saved.id, saved.reminderHour)
        WidgetUpdater.updateAll(context)
        return newId
    }

    override suspend fun updateTask(task: KeepTask) {
        val existing = dao.getTaskById(task.id) ?: return
        val now = System.currentTimeMillis()
        val entity = KeepTaskEntity(
            id = task.id,
            name = task.name,
            phoneNumber = task.phoneNumber,
            lastConsumeDate = task.lastConsumeDate,
            reminderHour = task.reminderHour,
            remindDaysBefore = task.remindDaysBefore,
            smsToNumber = task.smsToNumber,
            createdAt = existing.createdAt,
            updatedAt = now
        )
        dao.update(entity)
        ReminderAlarmManager.scheduleReminder(context, entity.id, entity.reminderHour)
        WidgetUpdater.updateAll(context)
    }

    override suspend fun deleteTask(id: Long) {
        dao.deleteById(id)
        ReminderAlarmManager.cancelReminder(context, id)
        WidgetUpdater.updateAll(context)
    }

    override suspend fun confirmConsume(id: Long, date: LocalDate): Long {
        val timestamp = date.atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        dao.updateLastConsume(id, timestamp)
        // Re-arm the alarm so it will re-evaluate tomorrow at reminderHour.
        // After a fresh consume the task is no longer urgent (180 days remaining),
        // so the receiver will fire-and-reschedule without notifying until it re-enters the window.
        dao.getTaskById(id)?.let {
            ReminderAlarmManager.scheduleReminder(context, it.id, it.reminderHour)
        }
        WidgetUpdater.updateAll(context)
        return timestamp
    }
}
