package com.godapp.ggkeep.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [KeepTaskEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun keepTaskDao(): KeepTaskDao

    companion object {
        const val DATABASE_NAME = "ggkeep.db"
    }
}
