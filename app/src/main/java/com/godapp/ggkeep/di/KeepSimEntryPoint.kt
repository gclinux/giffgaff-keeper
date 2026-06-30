package com.godapp.ggkeep.di

import com.godapp.ggkeep.data.repository.KeepTaskRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * EntryPoint for accessing KeepTaskRepository from BroadcastReceivers,
 * which cannot use @AndroidEntryPoint. Use with EntryPointAccessors.fromApplication().
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface KeepSimEntryPoint {
    fun keepTaskRepository(): KeepTaskRepository
}
