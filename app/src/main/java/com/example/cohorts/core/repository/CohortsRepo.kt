package com.example.cohorts.core.repository

import com.example.cohorts.core.Result
import com.example.cohorts.core.model.ChatMessage
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.model.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.Query

interface CohortsRepo {

    suspend fun registerCurrentUser(): Result<Any>
    suspend fun saveCohort(cohort: Cohort): Result<Any>
    suspend fun saveUser(user: User): Result<Any>
    suspend fun addNewCohort(newCohort: Cohort): Result<Any>
    suspend fun addNewMemberToCohort(cohort: Cohort, userEmail: String): Result<Any>
    suspend fun getUserByUid(userUid: String): Result<User>
    suspend fun getCohortById(cohortUid: String): Result<Cohort>
    suspend fun getCurrentUser(): Result<User>
    fun fetchCohortsQuery(): Result<Query>
    suspend fun addCurrentUserToOngoingMeeting(ofCohort: Cohort): Result<User>
    suspend fun startNewMeeting(ofCohort: Cohort): Result<Any>
    suspend fun leaveOngoingMeeting(): Result<Any>
    suspend fun getUserByEmail(userEmail: String): Result<User>
    suspend fun deleteThisCohort(cohort: Cohort): Result<Any>
    suspend fun removeThisUserFromCohort(user: User, cohort: Cohort): Result<Any>
    fun fetchUsersQuery(cohortUid: String): Result<Query>
    fun fetchChatReference(cohortUid: String): Result<DatabaseReference>
    suspend fun sendNewChatMessage(chatMessage: ChatMessage): Result<Any>

}