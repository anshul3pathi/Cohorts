package com.example.cohorts.ui.cohorts

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.model.User
import com.example.cohorts.core.repository.cohorts.CohortsRepo
import com.example.cohorts.core.repository.meeting.MeetingRepo
import com.example.cohorts.core.succeeded
import com.example.cohorts.jitsi.initJitsi
import com.example.cohorts.jitsi.launchJitsi
import com.example.cohorts.utils.Event
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jitsi.meet.sdk.BroadcastReceiver
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel to the [Cohort]s list screen
 */
@HiltViewModel
class CohortsViewModel @Inject constructor(
    private val cohortsRepository: CohortsRepo,
    private val meetingRepository: MeetingRepo,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _snackbarMessage = MutableLiveData<Event<String>>()
    val snackbarMessage: LiveData<Event<String>> = _snackbarMessage

    private lateinit var currentUser: User

    init {
        viewModelScope.launch(coroutineDispatcher) {
            val userResult = cohortsRepository.getCurrentUser()
            if (userResult.succeeded) {
                currentUser = (userResult as Result.Success).data
            } else {
                val exception = (userResult as Result.Error).exception
                Timber.e(exception, "error getting current user")
                _snackbarMessage.postValue(Event("There was some error."))
            }
        }
    }

    fun fetchCohortsQuery(): Query? {
        val query = cohortsRepository.fetchCohortsQuery()
        return if (query.succeeded) {
            (query as Result.Success).data
        } else {
            val exception = (query as Result.Error).exception
            Timber.e(exception, "error getting cohorts query.")
            _snackbarMessage.postValue(Event("There was some error."))
            null
        }
    }

    /**
     * Adds the current user to the ongoing meeting of the given [Cohort]
     *
     * @param ofCohort object containing the data of the [Cohort] whose meeting the user
     * wants to join
     * @param broadcastReceiver for listening to broadcast events by Jitsi
     * @param context [Context]
     */
    fun addCurrentUserToOngoingMeeting(
        ofCohort: Cohort,
        broadcastReceiver: BroadcastReceiver,
        context: Context
    ) {
        viewModelScope.launch(coroutineDispatcher) {
            val addedUser = meetingRepository.addCurrentUserToOngoingMeeting(ofCohort.cohortUid)
            if (addedUser.succeeded) {
                addedUser as Result.Success
                initJitsi(addedUser.data, broadcastReceiver, context)
                launchJitsi(context, ofCohort.cohortRoomCode)
            } else {
                val exception = (addedUser as Result.Error).exception
                Timber.e(exception, "error adding current user to ongoing meeting")
                _snackbarMessage.postValue(
                    Event("Cannot add you to ongoing meeting. Please try later")
                )
            }
        }
    }

    /**
     * Checks if the current user if in meeting of the given [Cohort]
     *
     * @param cohort object containing the information of the [Cohort]
     * @return true if user is in meeting otherwise false
     */
    fun isCurrentUserInMeetingOfThisCohort(cohort: Cohort) =
        (currentUser.uid!! in cohort.membersInMeeting)

}