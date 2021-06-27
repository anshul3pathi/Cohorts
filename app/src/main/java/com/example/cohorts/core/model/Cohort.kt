package com.example.cohorts.core.model

import com.example.cohorts.utils.generateRandomString
import java.io.Serializable

data class Cohort(
    var cohortName: String? = null,
    var cohortDescription: String? = null,
    var cohortUid: String = generateRandomString(20),
    var numberOfMembers: Int = 0,
    val cohortMembers: MutableList<String> = mutableListOf(), // user uids
    var isCallOngoing: Boolean = false,
    val cohortRoomCode: String = "$cohortName-${generateRandomString(15)}",
    val membersInMeeting: MutableList<String> = mutableListOf() // user uids

) : Serializable
