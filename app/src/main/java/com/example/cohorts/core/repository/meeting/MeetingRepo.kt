package com.example.cohorts.core.repository.meeting

import com.example.cohorts.core.Result
import com.example.cohorts.core.model.User

/**
 * Interface for dealing with the operations if adding and removing users from
 * the meeting of a cohort
 */
interface MeetingRepo {

    suspend fun addCurrentUserToOngoingMeeting(ofCohortUid: String): Result<User>

    suspend fun startNewMeeting(ofCohortUid: String): Result<Any>

    suspend fun leaveOngoingMeeting(): Result<Any>

    suspend fun onDestroy()

}