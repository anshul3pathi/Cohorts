package com.example.cohorts.ui.cohorts.viewpager

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.repository.CohortsRepo
import com.example.cohorts.core.succeeded
import com.example.cohorts.jitsi.destroyJitsi
import com.example.cohorts.jitsi.initJitsi
import com.example.cohorts.jitsi.launchJitsi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import org.jitsi.meet.sdk.BroadcastReceiver
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ViewPagerViewModel @Inject constructor(
    private val repository: CohortsRepo,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _inMeeting = MutableLiveData(false)
    val inMeeting: LiveData<Boolean> = _inMeeting

    private val _errorOccurred = MutableLiveData("")
    val errorOccurred: LiveData<String> = _errorOccurred

    private val _cohortDeleted = MutableLiveData(false)
    val cohortDeleted: LiveData<Boolean> = _cohortDeleted

    fun startNewMeeting(ofCohort: Cohort, context: Context) {
        viewModelScope.launch(coroutineDispatcher) {
            val result = repository.startNewMeeting(ofCohort.cohortUid)
            if (result.succeeded) {
                _inMeeting.postValue(true)
                launchJitsi(context, ofCohort.cohortRoomCode)
            } else {
                result as Result.Error
                _errorOccurred.postValue(result.exception.message)
            }
        }
    }

    fun initialiseJitsi(broadcastReceiver: BroadcastReceiver, context: Context) {
        viewModelScope.launch(coroutineDispatcher) {
            val currentUser = repository.getCurrentUser()
            if (currentUser.succeeded) {
                currentUser as Result.Success
                initJitsi(currentUser.data, broadcastReceiver, context)
            }
        }
    }

    fun terminateOngoingMeeting(context: Context, broadcastReceiver: BroadcastReceiver) {
        if (!_inMeeting.value!!) return

        CoroutineScope(coroutineDispatcher).launch {
            val result = repository.leaveOngoingMeeting()
            destroyJitsi(context, broadcastReceiver)
            if (result.succeeded) {
                Timber.i("left the meeting")
                _inMeeting.postValue(false)
            } else {
                result as Result.Error
                Timber.e(result.exception)
            }
        }
    }

    fun deleteThisCohort(cohort: Cohort) {
        GlobalScope.launch(coroutineDispatcher) {
            val result = repository.deleteThisCohort(cohort)
            if (result.succeeded) {
                _cohortDeleted.postValue(true)
            } else {
                val exception = (result as Result.Error).exception
                _cohortDeleted.postValue(false)
                _errorOccurred.postValue(exception.message)
            }
        }
    }

}