package com.godapp.ggkeep.data.mapper

import com.godapp.ggkeep.data.local.KeepTaskEntity
import com.godapp.ggkeep.domain.model.KeepTask

fun KeepTaskEntity.toDomain(): KeepTask = KeepTask(
    id = id,
    name = name,
    phoneNumber = phoneNumber,
    lastConsumeDate = lastConsumeDate,
    reminderHour = reminderHour,
    remindDaysBefore = remindDaysBefore,
    smsToNumber = smsToNumber
)

fun List<KeepTaskEntity>.toDomain(): List<KeepTask> = map { it.toDomain() }
