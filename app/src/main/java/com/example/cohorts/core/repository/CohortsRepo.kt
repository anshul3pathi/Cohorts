package com.example.cohorts.core.repository

import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.model.User
import com.google.firebase.firestore.Query

interface CohortsRepo {

    suspend fun registerCurrentUser(): Result<Any>
    suspend fun saveCohort(cohort: Cohort): Result<Any>
    suspend fun getCohortById(cohortUid: String): Result<Cohort>
    suspend fun getCurrentUser(): Result<User>
    fun fetchCohortsQuery(): Result<Query>
    suspend fun addCurrentUserToMeeting(cohort: Cohort): Result<Any>
    suspend fun startNewMeeting(cohort: Cohort): Result<Any>

}