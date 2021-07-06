package com.example.cohorts.core.repository.cohorts

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.model.User
import com.example.cohorts.core.succeeded
import com.example.cohorts.utils.safeCall
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import kotlin.IllegalArgumentException

class CohortsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    firebaseDatabase: FirebaseDatabase,
) : CohortsRepo {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val COHORTS_COLLECTION = "cohorts"
        private const val CHAT_CHILD = "chats"
    }

    private val usersCollection = firestore.collection(USERS_COLLECTION)
    private val cohortsCollection = firestore.collection(COHORTS_COLLECTION)
    private val chatReference = firebaseDatabase.reference.child(CHAT_CHILD)

    override suspend fun registerCurrentUser(): Result<Any> {
        return safeCall {
            // check if the current user already exist in users collection in firestore
            val userAlreadyRegistered = getUserByUid(auth.currentUser!!.uid)
            if (userAlreadyRegistered.succeeded) {
                // user exists in firestore, no need to save user info again
                Timber.d("User exists in user collection")
                Result.Success(Any())
            } else {
                // user doesn't exist in firestore, save user info
                Timber.e((userAlreadyRegistered as Result.Error).exception)
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
    }

    override fun fetchCohortsQuery(): Result<Query> {
        return safeCall {
            Result.Success(
                cohortsCollection.whereArrayContains("cohortMembers", auth.currentUser!!.uid)
            )
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

            // adding this new cohort to the list of cohorts current user is in
            usersCollection.document(currentUser.data.uid!!)
                .update("cohortsIn", FieldValue.arrayUnion(newCohort.cohortUid))

            val saveCohortResult = saveCohort(newCohort)

            if (!saveCohortResult.succeeded) return saveCohortResult

            Result.Success(Any())
        }
    }

    override suspend fun addNewMemberToCohort(cohort: Cohort, userEmail: String): Result<Any> {
        return safeCall {
            val result = getUserByEmail(userEmail)

            // user with the given name doesn't exist
            if (!result.succeeded) {
                throw IllegalArgumentException("User with given email is not found!")
            }

            val userToAdd = (result as Result.Success).data

            // check if the user is already a member of the given cohort
            if (cohort.cohortUid in userToAdd.cohortsIn) {
                throw IllegalArgumentException("${userToAdd.userName} is already in cohort!")
            }

            // adding the cohort to list of cohorts this user is in
            usersCollection.document(userToAdd.uid!!)
                .update("cohortsIn", FieldValue.arrayUnion(cohort.cohortUid))
                .await()

            // adding this user to cohorts members list
            cohortsCollection.document(cohort.cohortUid)
                .update("cohortMembers", FieldValue.arrayUnion(userToAdd.uid!!))
                .await()
            cohortsCollection.document(cohort.cohortUid)
                .update("numberOfMembers", FieldValue.increment(1))
                .await()

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
//            user.cohortsIn.remove(cohort.cohortUid)
            usersCollection.document(user.uid!!)
                .update("cohortsIn", FieldValue.arrayRemove(cohort.cohortUid))
                .await()

            // remove this user from the list of users that are in this cohort
//            cohort.cohortMembers.remove(user.uid!!)
            cohortsCollection.document(cohort.cohortUid)
                .update("cohortMembers", FieldValue.arrayRemove(user.uid!!))

            // number of members in the cohort are on less now
            cohortsCollection.document(cohort.cohortUid)
                .update("numberOfMembers", cohort.numberOfMembers - 1)
                .await()
//            saveCohort(cohort)
//            saveUser(user)
            Result.Success(
                "${user.userName} was successfully removed from ${cohort.cohortName}"
            )
        }
    }

}