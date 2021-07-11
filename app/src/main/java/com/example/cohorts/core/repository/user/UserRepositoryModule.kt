package com.example.cohorts.core.repository.user

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing the implementation of [UserRepo]
 */
@InstallIn(SingletonComponent::class)
@Module
abstract class UserRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepository): UserRepo

}