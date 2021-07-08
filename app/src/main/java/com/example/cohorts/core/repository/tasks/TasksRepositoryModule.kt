package com.example.cohorts.core.repository.tasks

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class TasksRepositoryModule {

    @Singleton
    @Binds
    abstract fun bindTasksRepository(impl: TasksRepository): TasksRepo

}