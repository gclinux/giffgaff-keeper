package com.godapp.ggkeep.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KeepTaskDao {
    @Query("SELECT * FROM keep_tasks ORDER BY lastConsumeDate ASC")
    fun observeAllTasks(): Flow<List<KeepTaskEntity>>

    @Query("SELECT * FROM keep_tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): KeepTaskEntity?

    @Query("SELECT * FROM keep_tasks")
    suspend fun getAllTasksOnce(): List<KeepTaskEntity>

    @Insert
    suspend fun insert(task: KeepTaskEntity): Long

    @Update
    suspend fun update(task: KeepTaskEntity)

    @Delete
    suspend fun delete(task: KeepTaskEntity)

    @Query("DELETE FROM keep_tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE keep_tasks SET lastConsumeDate = :timestamp, updatedAt = :now WHERE id = :id")
    suspend fun updateLastConsume(id: Long, timestamp: Long, now: Long = System.currentTimeMillis())
}
