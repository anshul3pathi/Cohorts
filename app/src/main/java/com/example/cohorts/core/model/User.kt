package com.example.cohorts.core.model

import timber.log.Timber

data class User(
    var userName: String? = null,
    var userEmail: String? = null,
    var uid: String? = null,
    var photoUrl: String? = null,
    val cohortsIn: MutableList<String> = mutableListOf() // cohort unique ids
)

fun MutableMap<String?, Any?>.mapToUserObject(data: MutableMap<String?, Any?>): User? {
    return try {
        User(
            uid = data["uid"] as String,
            userName = data["userName"] as String,
            userEmail = data["userEmail"] as String,
            photoUrl = data["photoUrl"] as String,
            cohortsIn = data["cohortsIn"] as MutableList<String>
        )
    } catch (ex: Exception) {
        Timber.e(ex, "Error converting map to user object!")
        null
    }
}
