package com.example.cohorts.utils


import android.view.View
import com.example.cohorts.core.Result
import com.google.android.material.snackbar.Snackbar

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

fun snackbar(view: View, message: String) {
    Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
}

enum class Theme {
    LIGHT, DARK, SYSTEM_DEFAULT
}