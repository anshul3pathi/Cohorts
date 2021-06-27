package com.example.cohorts.utils


import com.example.cohorts.core.Result

fun generateRandomString(length: Int = 10): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map{ allowedChars.random() }
        .joinToString("")
}

inline fun <T> safeCall(action: () -> Result<T>): Result<T> {
    return try {
        action()
    } catch (e: Exception) {
        Result.Error(e)
    }
}

enum class NetworkRequest {
    SUCCESS, FAILURE, LOADING
}