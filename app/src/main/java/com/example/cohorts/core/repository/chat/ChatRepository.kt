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
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.model.User



/**
 * Concrete implementation to load [ChatMessage]s from firebase realtime database and
 * to save images in firebase storage
 */
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

    /**
     * Fetches the [DatabaseReference] of the chats of a [Cohort]
     *
     * @param cohortUid uid of the [Cohort] whose chat reference is required
     * @return [DatabaseReference] wrapped in [Result]
     */
    override fun fetchChatReference(cohortUid: String): Result<DatabaseReference> {
        return safeCall {
            Result.Success(chatReference.child(cohortUid))
        }
    }

    /**
     * Send a new text message
     *
     * Saves the given [ChatMessage] on firebase realtime database so that other
     * [User]s in the [Cohort] can see chat messages in realtime
     *
     * @param chatMessage [ChatMessage] object containing the info of the message
     * @return [Any] wrapped in [Result]
     */
    override suspend fun sendNewChatMessage(chatMessage: ChatMessage): Result<Any> {
        return safeCall {
            chatReference.child(chatMessage.chatOfCohort).push().setValue(chatMessage).await()
            Result.Success(Any())
        }
    }

    /**
     * Send a new image message
     *
     * Saves the given [ChatMessage] on firebase realtime database so that other
     * [User]s in the [Cohort] can see that message in realtime
     *
     * Stores the given image in firebase storage and embeds a link to the saved image
     * in the given [ChatMessage]
     *
     * @param tempMessage [ChatMessage] that does not yet have the reference to the saved
     * image in firebase storage
     * @param imageUri [Uri] of the selected image to be sent
     * @return [Any] wrapped in [Result]
     */
    override suspend fun sendImageMessage(tempMessage: ChatMessage, imageUri: Uri?): Result<Any> {
        return safeCall {
            tempMessage.imageUrl = LOADING_IMAGE_URL
            // save this temporary chat message while we upload the actual image to storage
            chatReference.child(tempMessage.chatOfCohort).push().setValue(
                tempMessage,
                DatabaseReference.CompletionListener { databaseError, databaseReference ->
                    if (databaseError != null) {
                        Timber.e(
                            databaseError.toException(),
                            "Unable to write message to database"
                        )
                        return@CompletionListener
                    }

                    // build a storage reference and then upload the image file
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

    /**
     * Helper function for storing the image file in firebase storage
     *
     * Stores the given image in firebase storage and then embeds the generated link to that
     * image in [ChatMessage] which is supposed to have a link to that image
     *
     * @param storageReference [StorageReference] where the image file is supposed to be
     * stored
     * @param uri [Uri] of the image to be stored in storage
     * @param key [String] key of the temporary message that was saved and is supposed to have
     * a link to the given image in firebase storage
     * @param imageMessage [ChatMessage] temporary message that should contain the link to
     * given image
     */
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