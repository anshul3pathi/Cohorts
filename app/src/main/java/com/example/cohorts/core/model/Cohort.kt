package com.example.cohorts.core.model

import com.example.cohorts.utils.generateRandomString
import java.io.Serializable

/**
 * Model class for a Cohort.
 *
 * @param cohortName name of cohort
 * @param cohortDescription description of the cohort
 * @param cohortUid unique id of the cohort
 * @param numberOfMembers the number of members that are in cohort
 * @param cohortMembers list of users that are in this cohort
 * @param isCallOngoing true if call is going on else false
 * @param cohortRoomCode unique room code of this cohort
 * @param membersInMeeting list of users that are in the meeting of this cohort
 */
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
