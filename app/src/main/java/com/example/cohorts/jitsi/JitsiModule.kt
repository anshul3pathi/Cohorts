package com.example.cohorts.jitsi

import android.content.Context
import com.example.cohorts.core.repository.CohortsRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext

@InstallIn(ActivityComponent::class)
@Module
object JitsiModule {

    @Provides
    fun provideJitsi(@ActivityContext context: Context, repo: CohortsRepo): Jitsi {
        return Jitsi(context, repo)
    }

}