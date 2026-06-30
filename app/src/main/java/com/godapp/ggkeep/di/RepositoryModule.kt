package com.godapp.ggkeep.di

import com.godapp.ggkeep.data.repository.KeepTaskRepository
import com.godapp.ggkeep.data.repository.KeepTaskRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindKeepTaskRepository(impl: KeepTaskRepositoryImpl): KeepTaskRepository
}
