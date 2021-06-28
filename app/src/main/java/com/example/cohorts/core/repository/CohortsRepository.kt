package com.example.cohorts.core.repository

import android.accounts.NetworkErrorException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.model.User
import com.example.cohorts.core.succeeded
import com.example.cohorts.utils.safeCall
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class CohortsRepository @Inject constructor(
    firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : CohortsRepo {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val COHORTS_COLLECTION = "cohorts"
    }

    private val usersCollection = firestore.collection(USERS_COLLECTION)
    private val cohortsCollection = firestore.collection(COHORTS_COLLECTION)

    override suspend fun registerCurrentUser(): Result<Any> {
        return safeCall {
            val currentUser = auth.currentUser!!
            val user = User(
                uid = currentUser.uid,
                userName = currentUser.displayName,
                userEmail = currentUser.email
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

    override suspend fun addCurrentUserToMeeting(cohort: Cohort): Result<Any> {
        return safeCall {
            val currentUser = auth.currentUser!!
            cohort.membersInMeeting.add(currentUser.uid)
            saveCohort(cohort)
        }
    }

    override suspend fun saveCohort(cohort: Cohort): Result<Any> {
        return safeCall {
            cohortsCollection.document(cohort.cohortUid).set(cohort).await()
            Result.Success(Any())
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        return safeCall {
            val currentUser = auth.currentUser!!
            val user = User(
                uid = currentUser.uid,
                userEmail = currentUser.email,
                userName = currentUser.displayName
            )
            Result.Success(user)
        }
    }

    override suspend fun getCohortById(cohortUid: String): Result<Cohort> {
        val cohort = cohortsCollection.document(cohortUid).get().await()
            .toObject(Cohort::class.java)
        return safeCall {
            Result.Success(cohort!!)
        }
    }

    override suspend fun startNewMeeting(cohort: Cohort): Result<Any> {
        return safeCall {
            if (!cohort.isCallOngoing) {
                cohort.isCallOngoing = true
                val result = addCurrentUserToMeeting(cohort)
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
}