package com.example.cohorts.core.repository.chat

import android.net.Uri
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.ChatMessage
import com.example.cohorts.utils.safeCall
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ChatRepository @Inject constructor(
    firebaseDatabase: FirebaseDatabase,
    firebaseStorage: FirebaseStorage
) : ChatRepo {

    companion object {
        private const val CHAT_CHILD = "chats"
        private const val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"
        private const val CHAT_PHOTOS = "chat_photos"
    }

    private val chatReference = firebaseDatabase.reference.child(CHAT_CHILD)
    private val storageReference = firebaseStorage.reference.child(CHAT_PHOTOS)

    override fun fetchChatReference(cohortUid: String): Result<DatabaseReference> {
        return safeCall {
            Result.Success(chatReference.child(cohortUid))
        }
    }

    override suspend fun sendNewChatMessage(chatMessage: ChatMessage): Result<Any> {
        return safeCall {
            chatReference.child(chatMessage.chatOfCohort).push().setValue(chatMessage).await()
            Result.Success(Any())
        }
    }

    override suspend fun sendImageMessage(tempMessage: ChatMessage, imageUri: Uri?): Result<Any> {
        return safeCall {
            tempMessage.imageUrl = LOADING_IMAGE_URL
            // save this temporary chat message while we upload the actual image to storage
            chatReference.child(tempMessage.chatOfCohort).push().setValue(
                tempMessage,
                DatabaseReference.CompletionListener { databaseError, databaseReference ->
                    if (databaseError != null) {
                        Timber.e("Unable to write message to database")
                        Timber.e(databaseError.toException())
                        return@CompletionListener
                    }

                    // build a storage reference and then upload the file
                    val key = databaseReference.key
                    val storageReference = storageReference
                        .child(tempMessage.chatOfCohort)
                        .child(imageUri!!.lastPathSegment!!)
                    putImageInStorage(storageReference, imageUri, key, tempMessage)
                }
            )
            Result.Success(Any())
        }
    }

    private fun putImageInStorage(
        storageReference: StorageReference,
        uri: Uri,
        key: String?, imageMessage: ChatMessage
    ) {
        storageReference.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        imageMessage.imageUrl = uri.toString()
                        chatReference.child(imageMessage.chatOfCohort)
                            .child(key!!)
                            .setValue(imageMessage)
                    }
            }
            .addOnFailureListener {
                Timber.e(it)
            }
    }

}