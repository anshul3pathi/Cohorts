package com.example.cohorts.core.repository.cohorts

import com.example.cohorts.core.repository.cohorts.CohortsRepo
import com.example.cohorts.core.repository.cohorts.CohortsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class CohortsRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRepository(impl: CohortsRepository): CohortsRepo

}