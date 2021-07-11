package com.example.cohorts.core.model

/**
 * Model class for a ChatMessage
 *
 * @param text message text
 * @param name name of the user who sent the message
 * @param photoUrl url of user profile image
 * @param imageUrl url of image message
 * @param userUid uid of the user who sent the message
 * @param chatOfCohort uid of cohort to which the chat belongs
 */
data class ChatMessage constructor(
    var text: String? = null,
    var name: String? = null,
    var photoUrl: String? = null,
    var imageUrl: String? = null,
    var userUid: String? = null,
    val chatOfCohort: String // cohortUid of cohort to which this chat belongs
) {
    constructor() :
            this(null, null, null, null, null, "")
}
