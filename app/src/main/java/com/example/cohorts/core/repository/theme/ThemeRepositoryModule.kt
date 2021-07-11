package com.example.cohorts.core.repository.theme

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module for providing the implementation of [ThemeRepo]
 */
@InstallIn(SingletonComponent::class)
@Module
abstract class ThemeRepositoryModule {

    @Singleton
    @Binds
    abstract fun bindThemeRepository(impl: ThemeRepository): ThemeRepo

}