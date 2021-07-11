package com.example.cohorts.core.repository.user

import com.example.cohorts.core.Result
import com.example.cohorts.core.model.User

/**
 * Interface that acts as a layer between [User]s data on firestore
 */
interface UserRepo {

    fun isUserLoggedIn(): Boolean

    fun getCurrentUser(): Result<User>

    fun initialiseCurrentUser(): Result<Any>

    suspend fun registerCurrentUser(): Result<Any>

    suspend fun signOut(): Result<Any>

}