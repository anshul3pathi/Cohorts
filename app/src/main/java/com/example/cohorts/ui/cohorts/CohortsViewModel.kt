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
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jitsi.meet.sdk.BroadcastReceiver
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CohortsViewModel @Inject constructor(
    private val cohortsRepository: CohortsRepo,
    private val meetingRepository: MeetingRepo,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _userAddedToMeeting = MutableLiveData(false)
    val userAddedToMeeting: LiveData<Boolean> = _userAddedToMeeting

    private val _errorAddingUserToMeeting = MutableLiveData(false)
    val errorAddingUserToMeeting: LiveData<Boolean> = _errorAddingUserToMeeting

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private lateinit var currentUser: User

    init {
        viewModelScope.launch(coroutineDispatcher) {
            val userResult = cohortsRepository.getCurrentUser()
            if (userResult.succeeded) {
                currentUser = (userResult as Result.Success).data
            } else {
                _errorMessage.postValue((userResult as Result.Error).exception.message)
            }
        }
    }

    fun fetchCohortsQuery(): Query {
        val query = cohortsRepository.fetchCohortsQuery()
        query as Result.Success
        return query.data
    }

    fun addCurrentUserToOngoingMeeting(
        ofCohort: Cohort,
        broadcastReceiver: BroadcastReceiver,
        context: Context
    ) {
        viewModelScope.launch(coroutineDispatcher) {
            val addedUser = meetingRepository.addCurrentUserToOngoingMeeting(ofCohort.cohortUid)
            if (addedUser.succeeded) {
                addedUser as Result.Success
                _userAddedToMeeting.postValue(true)
                initJitsi(addedUser.data, broadcastReceiver, context)
                launchJitsi(context, ofCohort.cohortRoomCode)
            } else {
                _errorAddingUserToMeeting.postValue(true)
                val exception = (addedUser as Result.Error).exception
                _errorMessage.postValue(exception.message)
                Timber.e(exception)
            }
        }
    }

    fun isCurrentUserInMeetingOfThisCohort(cohort: Cohort) =
        (currentUser.uid!! in cohort.membersInMeeting)


    fun resetUserAddedToMeeting() {
        _userAddedToMeeting.postValue(false)
    }

    fun resetErrorAddingUserToMeeting() {
        _errorAddingUserToMeeting.postValue(false)
    }

}