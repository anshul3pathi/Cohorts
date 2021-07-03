package com.example.cohorts.core.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.ChatMessage
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.model.User
import com.example.cohorts.core.succeeded
import com.example.cohorts.utils.safeCall
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import kotlin.IllegalArgumentException

class CohortsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase,
    private val storage: FirebaseStorage
) : CohortsRepo {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val COHORTS_COLLECTION = "cohorts"
        private const val CHAT_CHILD = "chats"
        private const val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"
    }

    private val usersCollection = firestore.collection(USERS_COLLECTION)
    private val cohortsCollection = firestore.collection(COHORTS_COLLECTION)
    private val chatReference = firebaseDatabase.reference.child(CHAT_CHILD)
    private val storageReference = storage.reference.child("chat_photos")

    override suspend fun registerCurrentUser(): Result<Any> {
        return safeCall {
            val userAlreadyRegistered = getUserByUid(auth.currentUser!!.uid)
            if (userAlreadyRegistered.succeeded) {
                Timber.d("User exists in user collection")
                Result.Success(Any())
            } else {
                Timber.e((userAlreadyRegistered as Result.Error).exception)
            }
            val currentUser = auth.currentUser!!
            val user = User(
                uid = currentUser.uid,
                userName = currentUser.displayName,
                userEmail = currentUser.email,
                photoUrl = currentUser.photoUrl?.toString()
            )
            usersCollection.document(user.uid!!).set(user).await()
            Result.Success(Any())
        }
    }

    override fun fetchCohortsQuery(): Result<Query> {
        return safeCall {
            Result.Success(
                cohortsCollection.whereArrayContains("cohortMembers", auth.currentUser!!.uid)
            )
        }
    }

    override fun fetchChatReference(cohortUid: String): Result<DatabaseReference> {
        return safeCall {
            Result.Success(chatReference.child(cohortUid))
        }
    }

    override fun fetchUsersQuery(cohortUid: String): Result<Query> {
        return safeCall {
            Result.Success(
                usersCollection.whereArrayContains("cohortsIn", cohortUid)
            )
        }
    }

    override suspend fun getUserByEmail(userEmail: String): Result<User> {
        return safeCall {
            val users = usersCollection
                .whereEqualTo("userEmail", userEmail)
                .get()
                .await()
                .toObjects(User::class.java)
            if (users.size > 1) {
                throw IllegalStateException("More than one user found with given email!")
            } else if (users.size == 0) {
                throw IllegalStateException("No user found with the given email!")
            }
            Result.Success(users[0])
        }
    }

    override suspend fun addCurrentUserToOngoingMeeting(ofCohort: Cohort): Result<User> {
        return safeCall {
            val currentUser = auth.currentUser!!
            ofCohort.membersInMeeting.add(currentUser.uid)
            saveCohort(ofCohort)
            val user = User(
                uid = currentUser.uid,
                userEmail = currentUser.email,
                userName = currentUser.displayName
            )
            Result.Success(user)
        }
    }

    override suspend fun saveCohort(cohort: Cohort): Result<Any> {
        return safeCall {
            cohortsCollection.document(cohort.cohortUid).set(cohort).await()
            Result.Success(Any())
        }
    }

    override suspend fun saveUser(user: User): Result<Any> {
        return safeCall {
            usersCollection.document(user.uid!!).set(user).await()
            Result.Success(Any())
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        return safeCall {
            val currentUser = auth.currentUser!!
            val user = User(
                uid = currentUser.uid,
                userEmail = currentUser.email,
                userName = currentUser.displayName,
                photoUrl = currentUser.photoUrl?.toString()
            )
            Result.Success(user)
        }
    }

    override suspend fun getUserByUid(userUid: String): Result<User> {
        return safeCall {
            val searchedUser =
                usersCollection.document(userUid).get().await().toObject(User::class.java)
            Result.Success(searchedUser!!)
        }
    }

    override suspend fun addNewCohort(newCohort: Cohort): Result<Any> {
        return safeCall {
            val currentUser = getUserByUid(auth.currentUser!!.uid)
            if (!currentUser.succeeded) currentUser as Result.Error

            currentUser as Result.Success
            newCohort.numberOfMembers += 1
            newCohort.cohortMembers.add(currentUser.data.uid!!)

            currentUser.data.cohortsIn.add(newCohort.cohortUid)

            val saveUserResult = saveUser(currentUser.data)
            val saveCohortResult = saveCohort(newCohort)

            if (!saveCohortResult.succeeded) return saveCohortResult
            if (!saveUserResult.succeeded) return saveUserResult

            Result.Success(Any())
        }
    }

    override suspend fun addNewMemberToCohort(cohort: Cohort, userEmail: String): Result<Any> {
        return safeCall {
            val result = getUserByEmail(userEmail)

            // user with the given name doesn't exist
            if (!result.succeeded) {
                throw IllegalArgumentException("User not found with the given email.")
            }

            val userToAdd = (result as Result.Success).data
            if (userToAdd.uid!! in cohort.cohortMembers) {
                throw IllegalArgumentException("${userToAdd.userName} is already in cohort!")
            }

            // adding the cohort to list of cohorts this user is in
            userToAdd.cohortsIn.add(cohort.cohortUid)

            // adding this user to cohorts members list
            cohort.cohortMembers.add(userToAdd.uid!!)
            cohort.numberOfMembers += 1

            // saving the updated user and cohort to firestore
            saveCohort(cohort)
            saveUser(userToAdd)

            Result.Success("${userToAdd.userName} added to cohort successfully!")
        }
    }

    override suspend fun getCohortById(cohortUid: String): Result<Cohort> {
        val cohort = cohortsCollection.document(cohortUid).get().await()
            .toObject(Cohort::class.java)
        return safeCall {
            Result.Success(cohort!!)
        }
    }

    override suspend fun startNewMeeting(ofCohort: Cohort): Result<Any> {
        return safeCall {
            if (!ofCohort.isCallOngoing) {
                ofCohort.isCallOngoing = true
                val result = addCurrentUserToOngoingMeeting(ofCohort)
                if (result.succeeded) {
                    Result.Success(Any())
                } else {
                    Result.Error(Exception("Couldn't add to meeting!"))
                }
            } else {
                throw IllegalStateException("Meeting already going on!")
            }
        }
    }

    override suspend fun leaveOngoingMeeting(): Result<Any> {
        return safeCall {
            val cohorts = cohortsCollection.whereArrayContains(
                "membersInMeeting",
                auth.currentUser!!.uid
            ).get().await().toObjects(Cohort::class.java)

            Timber.d("${cohorts[0]}")
            if (cohorts.size > 1) {
                throw IllegalStateException("More than one cohort found in whose meeting user is in!")
            }
            val meetingCohort = cohorts[0]
            meetingCohort.membersInMeeting.remove(auth.currentUser!!.uid)
            if (meetingCohort.membersInMeeting.size == 0) {
                meetingCohort.isCallOngoing = false
            }
            Timber.d("left the meeting")
            saveCohort(meetingCohort)
        }
    }

    override suspend fun deleteThisCohort(cohort: Cohort): Result<Any> {
        return safeCall {
            val batch = firestore.batch()

            // find users who are members of this cohort
            val usersInThisCohort = usersCollection
                .whereArrayContains("cohortsIn", cohort.cohortUid)
                .get()
                .await()

            // remove this cohort from the list of cohorts the users are in
            usersInThisCohort.documents.forEach { document ->
                val updatedCohortsIn = document.data!!["cohortsIn"]!! as MutableList<*>
                updatedCohortsIn.remove(cohort.cohortUid)
                batch.update(document.reference, "cohortsIn", updatedCohortsIn)
            }

            batch.commit().await()

            // delete the chats associated with the cohort
            chatReference.child(cohort.cohortUid).removeValue().await()
//            storageReference.child(cohort.cohortUid).delete().await()

            // finally delete the cohort
            cohortsCollection.document(cohort.cohortUid).delete().await()

            Result.Success(Any())
        }
    }

    override suspend fun removeThisUserFromCohort(user: User, cohort: Cohort): Result<Any> {
        return safeCall {
            // remove this cohort from the list of cohorts the user is in
            user.cohortsIn.remove(cohort.cohortUid)

            // remove this user from the list of users that are in this cohort
            cohort.cohortMembers.remove(user.uid!!)

            saveCohort(cohort)
            saveUser(user)
            Result.Success(
                "${user.userName} was successfully removed from ${cohort.cohortName}"
            )
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