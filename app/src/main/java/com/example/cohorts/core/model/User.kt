package com.example.cohorts.core.model

data class User(
    var userName: String? = null,
    var userEmail: String? = null,
    var uid: String? = null,
    var photoUrl: String? = null,
    val cohortsIn: MutableList<String> = mutableListOf() // cohort unique ids
)
