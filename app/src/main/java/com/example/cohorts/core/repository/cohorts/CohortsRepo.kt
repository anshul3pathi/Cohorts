package com.example.cohorts.core.repository.cohorts

import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.model.User
import com.google.firebase.firestore.Query

/**
 * Interface to Cohorts firestore database layer
 */
interface CohortsRepo {

    fun fetchCohortsQuery(): Result<Query>

    fun fetchUsersQuery(cohortUid: String): Result<Query>

    suspend fun saveCohort(cohort: Cohort): Result<Any>

    suspend fun saveUser(user: User): Result<Any>

    suspend fun addNewCohort(newCohort: Cohort): Result<Any>

    suspend fun addNewMemberToCohort(cohort: Cohort, userEmail: String): Result<Any>

    suspend fun getUserByUid(userUid: String): Result<User>

    suspend fun getCohortById(cohortUid: String): Result<Cohort>

    suspend fun getCurrentUser(): Result<User>

    suspend fun getUserByEmail(userEmail: String): Result<User>

    suspend fun deleteThisCohort(cohort: Cohort): Result<Any>

    suspend fun removeThisUserFromCohort(user: User, cohort: Cohort): Result<Any>

}