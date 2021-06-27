package com.example.cohorts.core.model

data class User(
    var userName: String? = null,
    var userEmail: String? = null,
    var uid: String? = null,
    val cohortsIn: MutableMap<String, String> = HashMap() // cohort name to cohort unique id
)
