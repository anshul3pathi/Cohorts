package com.example.cohorts.core.repository.user

import com.example.cohorts.core.Result
import com.example.cohorts.core.model.User

interface UserRepo {

    fun isUserLoggedIn(): Boolean
    fun getCurrentUser(): Result<User>
    fun initialiseCurrentUser(): Result<Any>
    suspend fun registerCurrentUser(): Result<Any>
    suspend fun signOut(): Result<Any>

}