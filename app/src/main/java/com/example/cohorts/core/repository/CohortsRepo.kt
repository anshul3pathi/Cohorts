package com.example.cohorts.core.repository


import com.example.cohorts.core.Result

interface CohortsRepo {

    suspend fun registerCurrentUser(): Result<Any>

}