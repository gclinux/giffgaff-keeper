package com.godapp.ggkeep.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "keep_tasks")
data class KeepTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val lastConsumeDate: Long,        // epoch millis (local start of day)
    val reminderHour: Int,            // 0-23
    val remindDaysBefore: Int,        // 7 or 15
    val smsToNumber: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
