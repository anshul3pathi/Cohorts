package com.example.cohorts.core.model

data class ChatMessage constructor(
    var text: String? = null,
    var name: String? = null,
    var photoUrl: String? = null,
    var imageUrl: String? = null,
    var userUid: String? = null,
    val chatOfCohort: String // cohortUid of cohort to which this chat belongs
) {
    constructor() : this(null, null, null, null, null, "")
}
