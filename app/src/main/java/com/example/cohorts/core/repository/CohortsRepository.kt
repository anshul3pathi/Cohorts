package com.example.cohorts.core.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.User
import com.example.cohorts.utils.safeCall
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
}