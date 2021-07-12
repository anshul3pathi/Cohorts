package com.example.cohorts.core.repository.meeting

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Singleton

/**
 * Hilt Module for providing the implementation of [MeetingRepo]
 */
@InstallIn(ActivityRetainedComponent::class)
@Module
abstract class MeetingRepositoryModule {

    @Binds
    @ActivityRetainedScoped
    abstract fun bindMeetingRepository(impl: MeetingRepository): MeetingRepo

}