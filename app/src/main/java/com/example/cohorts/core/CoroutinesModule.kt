package com.example.cohorts.core

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Hilt module that provides the default [CoroutineDispatcher]
 */
@InstallIn(SingletonComponent::class)
@Module
object CoroutinesModule {

    @Provides
    fun provideCoroutineDispatcher() = Dispatchers.IO

}