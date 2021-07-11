package com.example.cohorts.core.storage

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides implementation of [Storage]
 */
@InstallIn(SingletonComponent::class)
@Module
abstract class StorageModule {

    @Singleton
    @Binds
    abstract fun bindStorage(impl: SharedPreferenceStorage): Storage

}