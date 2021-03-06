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
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [MeetingRepo] for dealing with adding and removing
 * user from meetings of cohorts
 */

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

    // initialise the member variable with minimum amount of information about the
    // currently logged in user until the data from firestore is retrieved
    private var currentUser: User? = User(
        uid = auth.currentUser!!.uid,
        userName = auth.currentUser!!.displayName,
        userEmail = auth.currentUser!!.email
    )

    init {
        // listen for changes to the data of the currently logged in user
        // in firestore database
        listenToRealtimeChangesToCurrentUser()
        Timber.d("Init")
        // crash the app if the user is not logged in
        auth.currentUser!!
    }

    /**
     * Attach a realtime listener to the data of currently logged in user
     * this listener will update the currentUser member variable whenever there are changes
     * to the data of current user in firestore
     */
    private fun listenToRealtimeChangesToCurrentUser() {
        Timber.d("trying to listen to realtime changes!")
        usersCollection.document(auth.currentUser!!.uid).addSnapshotListener { value, error ->
            if (error != null) {
                Timber.e(error, "error listening to realtime changes in current user")
                return@addSnapshotListener
            }

            if (value != null && value.exists()) {
                currentUser = value.data!!.mapToUserObject()!!
                Timber.d("current user = $currentUser")
            }
        }
    }

    /**
     * Add the current user to the meeting of cohort
     *
     * @param ofCohortUid uid of the cohort in whose the meeting the user is to be added
     * @return [User] wrapped in [Result]
     */
    override suspend fun addCurrentUserToOngoingMeeting(ofCohortUid: String): Result<User> {
        return safeCall {
            // add currently logged in user to the ongoing meeting of the cohort
            cohortsCollection.document(ofCohortUid)
                .update("membersInMeeting", FieldValue.arrayUnion(auth.currentUser!!.uid))
                .await()
            Result.Success(currentUser!!)
        }
    }

    /**
     * Start a new meeting in the cohort
     *
     * @param ofCohortUid uid of the cohort in which the meeting is to be started
     * @return [Any] wrapped in [Result]
     */
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
                // if the current user was successfully added to the started meeting of cohort
                if (result.succeeded) {
                    Result.Success(Any())
                } else {
                    throw Exception("Couldn't start a new meeting!")
                }
            }
        }
    }

    /**
     * Leave the ongoing meeting of which the currently logged in user is part of
     *
     * @return [Any] wrapped in [Result]
     */
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
            // if members in meeting is 0 then the meeting has ended otherwise meeting is ongoing
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

    /**
     * Removes the user from meetings the are in, even if they are part of multiple meetings
     * because of some bug
     *
     * Call this method when the app closes
     */
    override suspend fun onDestroy() {
        Timber.d("onDestroy")
        val batch = firestore.batch()

        /* find all cohorts in whose meeting the user is in right now.
         * the user cannot be in more than one meeting at once but still we will
         * check all cohorts.
         */
        val cohortsDocuments = auth.currentUser?.uid?.let {
            cohortsCollection
                .whereArrayContains("membersInMeeting", it)
                .get()
                .await()
        }

        /* remove current user from from the meetings of all the cohorts they are in
         * and if the user was the only member in the meeting then also terminate
         * that meeting
         */
        cohortsDocuments?.documents?.forEach { document ->
            document.data?.get("membersInMeeting")?.let { membersInMeeting ->
                membersInMeeting as MutableList<*>
                // remove current user from this meeting
                membersInMeeting.remove(auth.currentUser!!.uid)
                // update membersInMeeting value in firestore
                batch.update(document.reference, "membersInMeeting", membersInMeeting)

                // if the current user was the only one in the meeting then terminate the meeting
                if (membersInMeeting.size == 0) {
                    // update callOngoing value in firestore
                    batch.update(document.reference, "callOngoing", false)
                }
            }
        }

        batch.commit().await() // commit the changes to firestore
        Timber.d("onDestroy")
    }
}