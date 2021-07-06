package com.example.cohorts.core.repository.chat

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@InstallIn(ViewModelComponent::class)
@Module
abstract class ChatRepositoryModule {

    @Binds
    abstract fun bindChatRepository(impl: ChatRepository): ChatRepo

}