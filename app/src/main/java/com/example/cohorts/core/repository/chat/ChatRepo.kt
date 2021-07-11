package com.example.cohorts.core.repository.chat

import android.net.Uri
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.ChatMessage
import com.google.firebase.database.DatabaseReference

/**
 * Interface to the Chat Firebase database layer.
 */
interface ChatRepo {

    fun fetchChatReference(cohortUid: String): Result<DatabaseReference>

    suspend fun sendNewChatMessage(chatMessage: ChatMessage): Result<Any>

    suspend fun sendImageMessage(tempMessage: ChatMessage, imageUri: Uri?): Result<Any>

}