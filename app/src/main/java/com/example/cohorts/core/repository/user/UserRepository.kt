package com.example.cohorts.core.repository.user

import com.example.cohorts.core.Result
import com.example.cohorts.core.model.User
import com.example.cohorts.core.model.mapToUserObject
import com.example.cohorts.core.succeeded
import com.example.cohorts.utils.safeCall
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [UserRepo]
 */
@Singleton
class UserRepository @Inject constructor(
    firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserRepo {

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    private val usersCollection = firestore.collection(USERS_COLLECTION)
    private var currentUser: User? = null
    private lateinit var userRealTimeListener: ListenerRegistration

    /**
     * Check if the current user is logged in or not
     *
     * @return true if the user is logged in else false
     */
    override fun isUserLoggedIn() = (auth.currentUser != null)

    /**
     * Attach a realtime user to the data of current user
     *
     * The realtime listener will listen for any changes to the data of current
     * user and update the currentUser member variable
     *
     * @return [Any] wrapped in [Result]
     */
    override fun initialiseCurrentUser(): Result<Any> {
        listenToRealtimeChangesToCurrentUser()
        return Result.Success(Any())
    }

    /**
     * Save the [User] data of the current user in firestore if
     * it is not already saved
     *
     * @return [Any] wrapped in [Result]
     */
    override suspend fun registerCurrentUser(): Result<Any> {
        return safeCall {
            // check if the current user already exist in users collection in firestore
            val userAlreadyRegistered = getUserByUid(auth.currentUser!!.uid)
            if (userAlreadyRegistered.succeeded) {
                // user exists in firestore, no need to save user info again
                Timber.d("User exists in user collection")
                Timber.d("current user ${auth.currentUser}")
                listenToRealtimeChangesToCurrentUser()
                Result.Success(Any())
            } else {
                // user doesn't exist in firestore, save user info
                Timber.d("User doesn't exist in firestore")
                Timber.d("current user auth = ${auth.currentUser}")
                val loggedInUser = auth.currentUser!!
                val user = User(
                    uid = loggedInUser.uid,
                    userName = loggedInUser.displayName,
                    userEmail = loggedInUser.email,
                    photoUrl = loggedInUser.photoUrl?.toString()
                )
                usersCollection.document(user.uid!!).set(user).await()
                listenToRealtimeChangesToCurrentUser()
                Result.Success(Any())
            }
        }
    }

    /**
     * Sign the user out
     *
     * Detaches the realtime listener attached to the data of current user in firestore
     * and changes the value of currentUser member variable to null
     *
     * @return [Any] wrapped in [Result]
     */
    override suspend fun signOut(): Result<Any> {
        return safeCall {
            // detach realtime listener
            userRealTimeListener.remove()
            currentUser = null
            Timber.d("Signing out --- current user ${auth.currentUser}")
            Result.Success(Any())
        }
    }

    /**
     * Get the data of the current user
     *
     * @return [User] wrapped in [Result]
     */
    override fun getCurrentUser(): Result<User> {
        return safeCall {
            Timber.d("current user is - $currentUser")

            // return the current user member variable as it contains updated data
            // of the user
            Result.Success(this.currentUser!!)
        }
    }

    /**
     * Get [User] with the given uid
     *
     * @param userUid uid of the user
     * @return [User] wrapped in [Result]
     */
    private suspend fun getUserByUid(userUid: String): Result<User> {
        return safeCall {
            val searchedUser =
                usersCollection.document(userUid).get().await().toObject(User::class.java)
            Result.Success(searchedUser!!)
        }
    }

    /**
     * This method attaches a realtime listener to the data of the
     * current user in firestore, listens for any changes in data there and updates
     * the value of currentUser members variable
     */
    private fun listenToRealtimeChangesToCurrentUser() {
        Timber.d("trying to listen to realtime changes!")
        // attaching a realtime listener to the data of current user
        // this listener will update the currentUser variable whenever there are changes
        // to the data of current user in firestore
        userRealTimeListener = usersCollection.document(auth.currentUser!!.uid)
            .addSnapshotListener { value, error ->
            if (error != null) {
                Timber.e(error, "error listening to realtime changes in current user")
                return@addSnapshotListener
            }

            if (value != null && value.exists()) {
                currentUser = value.data!!.mapToUserObject()!!
                Timber.d("current user init = $currentUser")
            }
        }
    }

}