package com.godapp.ggkeep.di

import android.content.Context
import androidx.room.Room
import com.godapp.ggkeep.data.local.AppDatabase
import com.godapp.ggkeep.data.local.KeepTaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideKeepTaskDao(database: AppDatabase): KeepTaskDao {
        return database.keepTaskDao()
    }
}
