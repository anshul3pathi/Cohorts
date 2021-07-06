package com.example.cohorts.fakes

import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.model.User
import com.example.cohorts.core.repository.cohorts.CohortsRepo
import com.example.cohorts.utils.generateRandomString
import com.example.cohorts.utils.safeCall
import com.google.firebase.firestore.Query

class FakeCohortsRepository : CohortsRepo {

    private val cohortsCollection = HashMap<String, Cohort>()
    private val usersCollection = HashMap<String, User>()
    private var currentUser: User? = null

    fun addCohort(cohort: Cohort) {
        cohortsCollection[cohort.cohortUid] = cohort
    }

    fun removeCohort(cohort: Cohort) {
        cohortsCollection.remove(cohort.cohortUid)
    }

    override suspend fun registerCurrentUser(): Result<Any> {
        return safeCall {
            val user = User(
                uid = generateRandomString(),
                userEmail = generateRandomString(5) + "@domain.com",
                userName = generateRandomString(8)
            )
            currentUser = user
            Result.Success(Any())
        }
    }

    override suspend fun saveCohort(cohort: Cohort): Result<Any> {
        return safeCall {
            cohortsCollection[cohort.cohortUid] = cohort
            Result.Success(Any())
        }
    }

    override suspend fun getCohortById(cohortUid: String): Result<Cohort> {
        return safeCall {
            val cohort = cohortsCollection[cohortUid]
            Result.Success(cohort!!)
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        return safeCall {
            Result.Success(currentUser!!)
        }
    }

    override fun fetchCohortsQuery(): Result<Query> {
        TODO()
    }

    override suspend fun addCurrentUserToOngoingMeeting(ofCohort: Cohort): Result<User> {
        TODO("Not yet implemented")
    }

    override suspend fun startNewMeeting(ofCohort: Cohort): Result<Any> {
        TODO("Not yet implemented")
    }

    override suspend fun leaveOngoingMeeting(): Result<Any> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserByEmail(userEmail: String): Result<User> {
        return Result.Success(currentUser!!)
    }
}