package com.example.cohorts.core.model

import timber.log.Timber

/**
 * Model class of a user
 *
 * @param userName name of the user
 * @param userEmail email of the user
 * @param uid uid of the user
 * @param photoUrl url of the profile picture of the user
 * @param cohortsIn list of uids of cohort the user is in
 */
data class User(
    var userName: String? = null,
    var userEmail: String? = null,
    var uid: String? = null,
    var photoUrl: String? = null,
    val cohortsIn: MutableList<String> = mutableListOf() // cohort unique ids
)

/**
 * Extension function on Map<String?, Any?> that transforms map to [User] object
 *
 * @return Object containing the user information
 */
fun MutableMap<String?, Any?>.mapToUserObject(): User? {
    return try {
        User(
            uid = this["uid"] as String,
            userName = this["userName"] as String,
            userEmail = this["userEmail"] as String,
            photoUrl = (this["photoUrl"] as? String),
            cohortsIn = (this["cohortsIn"] as? MutableList<String>)?: mutableListOf()
        )
    } catch (ex: Exception) {
        Timber.e(ex, "Error converting map to user object!")
        null
    }
}
