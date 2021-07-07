package com.example.cohorts.core.repository.meeting

import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.model.User
import com.example.cohorts.core.model.mapToUserObject
import com.example.cohorts.core.succeeded
import com.example.cohorts.utils.safeCall
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeetingRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : MeetingRepo {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val COHORTS_COLLECTION = "cohorts"
    }

    private val usersCollection = firestore.collection(USERS_COLLECTION)
    private val cohortsCollection = firestore.collection(COHORTS_COLLECTION)

    private var currentUser: User? = User(
        uid = auth.currentUser!!.uid,
        userName = auth.currentUser!!.displayName,
        userEmail = auth.currentUser!!.email
    )

    init {
        listenToRealtimeChangesToCurrentUser()
        Timber.d("Init")
        auth.currentUser!!
    }

    private fun listenToRealtimeChangesToCurrentUser() {
        Timber.d("trying to listen to realtime changes!")
        // attaching a realtime listener to the data of current user
        // this listener will update the currentUser variable whenever there are changes
        // to the data of current user in firestore
        usersCollection.document(auth.currentUser!!.uid).addSnapshotListener { value, error ->
            if (error != null) {
                Timber.e(error, "error listening to realtime changes in current user")
                return@addSnapshotListener
            }

            if (value != null && value.exists()) {
                currentUser = value.data!!.mapToUserObject(value.data!!)
                Timber.d("current user = $currentUser")
            }
        }
    }

    override suspend fun addCurrentUserToOngoingMeeting(ofCohortUid: String): Result<User> {
        return safeCall {
            // add currently logged in user to the ongoing meeting of the
            cohortsCollection.document(ofCohortUid)
                .update("membersInMeeting", FieldValue.arrayUnion(auth.currentUser!!.uid))
                .await()
            Result.Success(currentUser!!)
        }
    }

    override suspend fun startNewMeeting(ofCohortUid: String): Result<Any> {
        return safeCall {
            // retrieving the cohort from firestore
            val meetingCohort = cohortsCollection.document(ofCohortUid)
                .get()
                .await()
                .toObject(Cohort::class.java)!!

            // checking if a call in this cohort already going on
            if (meetingCohort.isCallOngoing) {
                throw IllegalStateException("Meeting already going on!")
            } else {
                // when there is no call ongoing, start the call and add this user to
                // list of members in call
                cohortsCollection.document(ofCohortUid)
                    .update("callOngoing", true)
                    .await()
                val result = addCurrentUserToOngoingMeeting(ofCohortUid)
                if (result.succeeded) {
                    Result.Success(Any())
                } else {
                    throw Exception("Couldn't start a new meeting!")
                }
            }
        }
    }

    override suspend fun leaveOngoingMeeting(): Result<Any> {
        return safeCall {
            // retrieving the cohort in whose meeting the user is in
            val cohorts = cohortsCollection.whereArrayContains(
                "membersInMeeting",
                auth.currentUser!!.uid
            ).get().await().toObjects(Cohort::class.java)

            var isCallOngoing = true


            // the user is in multiple meetings, this should not happen
            if (cohorts.size > 1) {
                throw IllegalStateException(
                    "More than one cohort found in whose meeting user is in!"
                )
            }

            val meetingCohortUid = cohorts[0].cohortUid
            val meetingCohortReference = cohortsCollection.document(meetingCohortUid)

            // attaching a realtime listener to the list of members in meeting
            // if members in meeting is 0 then the call has ended otherwise call is ongoing
            meetingCohortReference.addSnapshotListener { value, error ->
                if (error != null) {
                    Timber.e(error)
                    return@addSnapshotListener
                }
                if (value != null && value.exists()) {
                    Timber.d("CurrentData - ${value.data}")
                    val membersInMeeting = value.data?.get("membersInMeeting") as List<*>
                    isCallOngoing = (membersInMeeting.isNotEmpty())
                    Timber.d("$isCallOngoing")
                } else {
                    Timber.d("Value doesn't exist!")
                }
            }

            // removing currently logged in user from the ongoing meeting
            meetingCohortReference
                .update("membersInMeeting", FieldValue.arrayRemove(auth.currentUser!!.uid))
                .await()

            // updating the call ongoing field
            meetingCohortReference
                .update("callOngoing", isCallOngoing)
                .await()

            Result.Success(Any())
        }
    }
}